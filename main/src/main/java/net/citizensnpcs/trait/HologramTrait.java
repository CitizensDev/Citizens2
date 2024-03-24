package net.citizensnpcs.trait;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3d;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Arg.CompletionsProvider;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

/**
 * Manages a set of <em>holograms</em> attached to the NPC. Holograms are lines of text or items that follow the NPC at
 * some offset (typically vertically offset).
 */
// TODO: refactor this class
@TraitName("hologramtrait")
public class HologramTrait extends Trait {
    private Location currentLoc;
    private BiFunction<String, Player, String> customHologramSupplier;
    private double lastEntityBbHeight = 0;
    private boolean lastNameplateVisible;
    @Persist
    private double lineHeight = -1;
    private final List<HologramLine> lines = Lists.newArrayList();
    private HologramLine nameLine;
    private final NPCRegistry registry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());
    private int t;
    private boolean useDisplayEntities = Setting.DISPLAY_ENTITY_HOLOGRAMS.asBoolean();
    @Persist
    private int viewRange = -1;

    public HologramTrait() {
        super("hologramtrait");
    }

    /**
     * Adds a new hologram line which will displayed over an NPC's head.
     *
     * @param text
     *            The new line to add
     */
    public void addLine(String text) {
        lines.add(new HologramLine(text, true));
        reloadLineHolograms();
    }

    /**
     * Adds a new hologram line which will displayed over an NPC's head. It will not persist to disk and will last for
     * the specified amount of ticks.
     *
     * @param text
     *            The new line to add
     * @param ticks
     *            The number of ticks to last for
     */
    public void addTemporaryLine(String text, int ticks) {
        lines.add(new HologramLine(text, false, ticks));
        reloadLineHolograms();
    }

    /**
     * Clears all hologram lines
     */
    public void clear() {
        for (HologramLine line : lines) {
            line.removeNPC();
        }
        lines.clear();
    }

    @SuppressWarnings("deprecation")
    private NPC createHologram(String line, double heightOffset) {
        NPC hologramNPC = null;
        if (useDisplayEntities) {
            hologramNPC = registry.createNPC(EntityType.INTERACTION, line);
            hologramNPC.addTrait(new ClickRedirectTrait(npc));
        } else {
            hologramNPC = registry.createNPC(EntityType.ARMOR_STAND, line);
            hologramNPC.getOrAddTrait(ArmorStandTrait.class).setAsHelperEntityWithName(npc);
        }
        hologramNPC.data().set(NPC.Metadata.HOLOGRAM_FOR, npc.getUniqueId().toString());
        if (Setting.PACKET_HOLOGRAMS.asBoolean()) {
            hologramNPC.addTrait(PacketNPC.class);
        }
        if (viewRange != -1) {
            hologramNPC.data().set(NPC.Metadata.TRACKING_RANGE, viewRange);
        } else if (npc.data().has(NPC.Metadata.TRACKING_RANGE)) {
            hologramNPC.data().set(NPC.Metadata.TRACKING_RANGE, npc.data().get(NPC.Metadata.TRACKING_RANGE));
        }
        hologramNPC.spawn(currentLoc.clone().add(0, getEntityBbHeight() + heightOffset, 0));

        Matcher itemMatcher = ITEM_MATCHER.matcher(line);
        if (itemMatcher.matches()) {
            Material item = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(itemMatcher.group(1), false)
                    : Material.matchMaterial(itemMatcher.group(1));
            ItemStack itemStack = new ItemStack(item, 1);
            NPC itemNPC = registry.createNPCUsingItem(EntityType.DROPPED_ITEM, "", itemStack);
            itemNPC.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
            if (itemMatcher.group(2) != null) {
                if (itemMatcher.group(2).charAt(1) == '{') {
                    Bukkit.getUnsafe().modifyItemStack(itemStack, itemMatcher.group(2).substring(1));
                    itemNPC.setItemProvider(() -> itemStack);
                } else {
                    itemNPC.getOrAddTrait(ScoreboardTrait.class)
                            .setColor(Util.matchEnum(ChatColor.values(), itemMatcher.group(2).substring(1)));
                }
            }
            itemNPC.getOrAddTrait(MountTrait.class).setMountedOn(hologramNPC.getUniqueId());
            itemNPC.spawn(currentLoc);
            NPC hn = hologramNPC;
            itemNPC.addRunnable(() -> {
                if (!itemNPC.isSpawned() || !hn.isSpawned()) {
                    itemNPC.destroy();
                }
            });
        }
        lastEntityBbHeight = getEntityBbHeight();
        return hologramNPC;
    }

    private double getEntityBbHeight() {
        return NMS.getBoundingBoxHeight(npc.getEntity());
    }

    private double getHeight(int lineNumber) {
        double base = lastNameplateVisible ? 0 : -getLineHeight();
        for (int i = 0; i <= lineNumber; i++) {
            HologramLine line = lines.get(i);
            base += line.mb + getLineHeight();
            if (i != lineNumber) {
                base += line.mt;
            }
        }
        return base;
    }

    /**
     * Note: this is implementation-specific and may be removed at a later date.
     */
    public Collection<Entity> getHologramEntities() {
        return lines.stream().filter(l -> l.hologram != null && l.hologram.getEntity() != null)
                .map(l -> l.hologram.getEntity()).collect(Collectors.toList());
    }

    /**
     * @return The line height between each hologram line, in blocks
     */
    public double getLineHeight() {
        return lineHeight == -1 ? Setting.DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT.asDouble() : lineHeight;
    }

    /**
     * @return the hologram lines, in bottom-up order
     */
    public List<String> getLines() {
        return Lists.transform(lines, l -> l.text);
    }

    /**
     * Note: this is implementation-specific and may be removed at a later date.
     */
    public Entity getNameEntity() {
        return nameLine != null && nameLine.hologram.isSpawned() ? nameLine.hologram.getEntity() : null;
    }

    public double getViewRange() {
        return viewRange;
    }

    public boolean isHologramSneaking(NPC hologram, Player player) {
        if (nameLine != null && hologram == nameLine.hologram && npc.getEntity() instanceof Player
                && ((Player) npc.getEntity()).isSneaking())
            return true;
        return false;
    }

    @Override
    public void load(DataKey root) {
        clear();
        for (DataKey key : root.getRelative("lines").getIntegerSubKeys()) {
            HologramLine line = new HologramLine(key.keyExists("text") ? key.getString("text") : key.getString(""),
                    true);
            line.mt = key.keyExists("margin.top") ? key.getDouble("margin.top") : 0.0;
            line.mb = key.keyExists("margin.bottom") ? key.getDouble("margin.bottom") : 0.0;
            lines.add(line);
        }
    }

    @Override
    public void onDespawn() {
        if (nameLine != null) {
            nameLine.removeNPC();
            nameLine = null;
        }
        for (HologramLine line : lines) {
            line.removeNPC();
        }
    }

    public void onHologramSeenByPlayer(NPC hologram, Player player) {
        if (useDisplayEntities && npc.isSpawned()) {
            double height = -1;
            if (nameLine != null && hologram.equals(nameLine.hologram)) {
                height = 0;
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    if (hologram.equals(lines.get(i).hologram)) {
                        height = getHeight(i);
                        break;
                    }
                }
            }
            if (height == -1)
                return;

            NMS.linkTextInteraction(player, hologram.getEntity(), npc.getEntity(), height);
        }
    }

    @Override
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void onSpawn() {
        if (!npc.isSpawned())
            return;

        lastNameplateVisible = Boolean
                .parseBoolean(npc.data().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString());
        currentLoc = npc.getStoredLocation();
        if (npc.requiresNameHologram() && lastNameplateVisible) {
            nameLine = new HologramLine(npc.getRawName(), false);
            nameLine.spawnNPC(0);
        }
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).spawnNPC(getHeight(i));
        }
    }

    private void reloadLineHolograms() {
        for (HologramLine line : lines) {
            line.removeNPC();
        }
        if (!npc.isSpawned())
            return;
        if (npc.requiresNameHologram() && lastNameplateVisible) {
            if (nameLine != null) {
                nameLine.removeNPC();
            }
            nameLine = new HologramLine(npc.getRawName(), false);
            nameLine.spawnNPC(0);
        }
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).spawnNPC(getHeight(i));
        }
    }

    /**
     * Removes the line at the specified index
     *
     * @param idx
     */
    public void removeLine(int idx) {
        if (idx < 0 || idx >= lines.size())
            return;

        lines.remove(idx).removeNPC();

        reloadLineHolograms();
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) {
            onDespawn();
            return;
        }
        if (currentLoc == null) {
            currentLoc = npc.getStoredLocation().clone();
        }
        boolean nameplateVisible = Boolean
                .parseBoolean(npc.data().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString());
        if (npc.requiresNameHologram()) {
            if (nameLine != null && !nameplateVisible) {
                nameLine.removeNPC();
                nameLine = null;
            } else if (nameLine == null && nameplateVisible) {
                nameLine = new HologramLine(npc.getRawName(), false);
                nameLine.spawnNPC(0);
            }
        }
        Location npcLoc = npc.getStoredLocation();
        boolean updatePosition = Setting.HOLOGRAM_ALWAYS_UPDATE_POSITION.asBoolean()
                || currentLoc.getWorld() != npcLoc.getWorld() || currentLoc.distance(npcLoc) >= 0.001
                || lastNameplateVisible != nameplateVisible
                || Math.abs(lastEntityBbHeight - getEntityBbHeight()) >= 0.05;
        boolean updateName = false;

        if (t++ >= Setting.HOLOGRAM_UPDATE_RATE.asTicks() + Util.getFastRandom().nextInt(3) /* add some jitter */) {
            t = 0;
            updateName = true;
        }
        lastNameplateVisible = nameplateVisible;

        if (updatePosition) {
            currentLoc = npcLoc.clone();
            lastEntityBbHeight = getEntityBbHeight();
        }
        if (nameLine != null && nameLine.hologram.isSpawned()) {
            if (updatePosition && !useDisplayEntities) {
                nameLine.hologram.teleport(npcLoc.clone().add(0, getEntityBbHeight(), 0), TeleportCause.PLUGIN);
            }
            if (updateName) {
                nameLine.setText(npc.getRawName());
            }
            if (useDisplayEntities && nameLine.hologram.getEntity().getVehicle() == null) {
                npc.getEntity().addPassenger(nameLine.hologram.getEntity());
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            HologramLine line = lines.get(i);
            NPC hologramNPC = line.hologram;

            if (hologramNPC == null || !hologramNPC.isSpawned())
                continue;

            if (line.ticks > 0 && --line.ticks == 0) {
                lines.remove(i--).removeNPC();
                ;
                continue;
            }
            if (updatePosition && !useDisplayEntities) {
                Location tp = npcLoc.clone().add(0, lastEntityBbHeight + getHeight(i), 0);
                hologramNPC.teleport(tp, TeleportCause.PLUGIN);
            }
            if (useDisplayEntities && hologramNPC.getEntity().getVehicle() == null) {
                npc.getEntity().addPassenger(hologramNPC.getEntity());
            }
            String text = line.text;
            if (ITEM_MATCHER.matcher(text).matches()) {
                hologramNPC.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
                continue;
            }
            if (!updateName) {
                continue;
            }
            line.setText(text);
        }
    }

    @Override
    public void save(DataKey root) {
        root.removeKey("lines");
        int i = 0;
        for (HologramLine line : lines) {
            if (!line.persist)
                continue;

            root.setString("lines." + i + ".text", line.text);
            root.setDouble("lines." + i + ".margin.top", line.mt);
            root.setDouble("lines." + i + ".margin.bottom", line.mb);
            i++;
        }
    }

    /**
     * Sets the hologram line at a specific index
     *
     * @param idx
     *            The index
     * @param text
     *            The new line
     */
    public void setLine(int idx, String text) {
        if (idx == lines.size()) {
            addLine(text);
            return;
        }
        HologramLine line = lines.get(idx);
        line.setText(text);
        if (line.hologram == null) {
            reloadLineHolograms();
        }
    }

    /**
     * Sets the line height
     *
     * @see #getLineHeight()
     * @param height
     *            The line height in blocks
     */
    public void setLineHeight(double height) {
        lineHeight = height;
        reloadLineHolograms();
    }

    /**
     * Sets the margin of a line at a specific index
     *
     * @param idx
     *            The index
     * @param type
     *            The margin type, top or bottom
     * @param margin
     *            The margin
     */
    public void setMargin(int idx, String type, double margin) {
        if (type.equalsIgnoreCase("top")) {
            lines.get(idx).mt = margin;
        } else if (type.equalsIgnoreCase("bottom")) {
            lines.get(idx).mb = margin;
        }
        reloadLineHolograms();
    }

    /**
     * Implementation-specific method: {@see NPC.Metadata#HOLOGRAM_LINE_SUPPLIER}
     */
    public void setPerPlayerTextSupplier(BiFunction<String, Player, String> nameSupplier) {
        customHologramSupplier = nameSupplier;
    }

    public void setUseDisplayEntities(boolean use) {
        useDisplayEntities = use;
        reloadLineHolograms();
    }

    public void setViewRange(int range) {
        this.viewRange = range;
        reloadLineHolograms();
    }

    public abstract class AbstractRenderer implements HologramRenderer {
        protected NPC npc;

        @Override
        public void destroy() {
            if (npc != null) {
                npc.destroy();
                npc = null;
            }
        }

        @Override
        public void render(Entity base, Vector3d offset) {
            if (npc == null)
                return;
            npc.getEntity().teleport(base.getLocation().clone().add(offset.x, offset.y, offset.z),
                    TeleportCause.PLUGIN);
        }

        protected abstract NPC spawnNPC(Entity base, String text, Vector3d offset);

        @Override
        public void updateText(Entity base, String text) {
            if (npc == null)
                return;
            NMS.setCustomName(base, text, text);
        }
    }

    public class ArmorstandRenderer extends AbstractRenderer {
        @Override
        protected NPC spawnNPC(Entity base, String name, Vector3d offset) {
            NPC hologramNPC = registry.createNPC(EntityType.ARMOR_STAND, name);
            hologramNPC.getOrAddTrait(ArmorStandTrait.class).setAsHelperEntityWithName(npc);
            return hologramNPC;
        }
    }

    private class HologramLine implements Function<Player, String> {
        NPC hologram;
        double mb, mt;
        boolean persist;
        HologramRenderer renderer;
        String text;
        int ticks;

        public HologramLine(String text, boolean persist) {
            this(text, persist, -1);
        }

        public HologramLine(String text, boolean persist, int ticks) {
            setText(text);
            this.persist = persist;
            this.ticks = ticks;
            if (ITEM_MATCHER.matcher(text).matches()) {
                mb = 0.21;
                mt = 0.07;
            }
        }

        @Override
        public String apply(Player viewer) {
            return Placeholders.replace(text, viewer, npc);
        }

        public void removeNPC() {
            if (hologram == null)
                return;

            hologram.destroy();
            renderer = null;
            hologram = null;
        }

        public void setText(String text) {
            this.text = text == null ? "" : text;

            if (hologram != null) {
                // renderer.updateText(hologram, text);
                String name = Placeholders.replace(text, null, npc);
                hologram.setName(name);
                if (Placeholders.containsPlaceholders(text)) {
                    hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER, this);
                } else {
                    hologram.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, ChatColor.stripColor(name).length() > 0);
                    hologram.data().remove(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER);
                }
            }
        }

        private void spawn(NPC npc, String text, Vector3d offset) {
            // hologram = renderer.spawn(npc, text, offset);
            if (!hologram.hasTrait(ClickRedirectTrait.class)) {
                hologram.addTrait(new ClickRedirectTrait(npc));
            }
            hologram.data().set(NPC.Metadata.HOLOGRAM_FOR, npc.getUniqueId().toString());
            if (Setting.PACKET_HOLOGRAMS.asBoolean()) {
                hologram.addTrait(PacketNPC.class);
            }
            if (viewRange != -1) {
                hologram.data().set(NPC.Metadata.TRACKING_RANGE, viewRange);
            } else if (npc.data().has(NPC.Metadata.TRACKING_RANGE)) {
                hologram.data().set(NPC.Metadata.TRACKING_RANGE, npc.data().get(NPC.Metadata.TRACKING_RANGE));
            }
            hologram.spawn(npc.getEntity().getLocation().add(offset.x, offset.y, offset.z));
            if (customHologramSupplier != null) {
                hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER,
                        (Function<Player, String>) p -> customHologramSupplier.apply(text, p));
            } else if (Placeholders.containsPlaceholders(text)) {
                hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER, this);
            }
        }

        public void spawnNPC(double height) {
            String name = Placeholders.replace(text, null, npc);
            hologram = createHologram(name, height);
            if (customHologramSupplier != null) {
                hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER,
                        (Function<Player, String>) p -> customHologramSupplier.apply(text, p));
            } else if (Placeholders.containsPlaceholders(text)) {
                hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER, this);
            }
        }
    }

    public static interface HologramRenderer {
        void destroy();

        void render(Entity base, Vector3d offset);

        void updateText(Entity base, String text);
    }

    public class InteractionRenderer extends AbstractRenderer {
        public void render(Entity base, String name, Vector3d offset) {
            if (this.npc.getEntity().getVehicle() == null) {
                HologramTrait.this.npc.getEntity().addPassenger(this.npc.getEntity());
            }
        }

        @Override
        protected NPC spawnNPC(Entity base, String name, Vector3d offset) {
            return registry.createNPC(EntityType.INTERACTION, name);
        }
    }

    public class ItemRenderer extends AbstractRenderer {
        @Override
        protected NPC spawnNPC(Entity base, String name, Vector3d offset) {
            Matcher itemMatcher = ITEM_MATCHER.matcher(name);
            Material item = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(itemMatcher.group(1), false)
                    : Material.matchMaterial(itemMatcher.group(1));
            ItemStack itemStack = new ItemStack(item, 1);
            NPC itemNPC = registry.createNPCUsingItem(EntityType.DROPPED_ITEM, "", itemStack);
            itemNPC.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
            if (itemMatcher.group(2) != null) {
                if (itemMatcher.group(2).charAt(1) == '{') {
                    Bukkit.getUnsafe().modifyItemStack(itemStack, itemMatcher.group(2).substring(1));
                    itemNPC.setItemProvider(() -> itemStack);
                } else {
                    itemNPC.getOrAddTrait(ScoreboardTrait.class)
                            .setColor(Util.matchEnum(ChatColor.values(), itemMatcher.group(2).substring(1)));
                }
            }
            return itemNPC;
        }
    }

    public static class TabCompletions implements CompletionsProvider {
        @Override
        public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc) {
            if (args.length() > 1 && npc != null && LINE_ARGS.contains(args.getString(1).toLowerCase())) {
                HologramTrait ht = npc.getOrAddTrait(HologramTrait.class);
                return IntStream.range(0, ht.getLines().size()).mapToObj(Integer::toString)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        private static List<String> LINE_ARGS = ImmutableList.of("set", "remove", "margintop", "marginbottom");
    }

    public class TextDisplayRenderer extends AbstractRenderer {
        public void render(Entity base, String name, Vector3d offset) {
            if (this.npc.getEntity().getVehicle() == null) {
                HologramTrait.this.npc.getEntity().addPassenger(this.npc.getEntity());
            }
        }

        @Override
        protected NPC spawnNPC(Entity base, String name, Vector3d offset) {
            return registry.createNPC(EntityType.TEXT_DISPLAY, name);
        }
    }

    private static Pattern ITEM_MATCHER = Pattern.compile("<item:(.*?)([:].*?)?>");
}
