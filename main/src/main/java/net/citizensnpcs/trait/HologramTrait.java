package net.citizensnpcs.trait;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

/**
 * Manages a set of <em>holograms</em> attached to the NPC. Holograms are lines of text or items that follow the NPC at
 * some offset (typically vertically offset).
 */
// TODO: cleanup: make HologramRenderer static, possibly make it singleton-friendly?
@TraitName("hologramtrait")
public class HologramTrait extends Trait {
    private Location currentLoc;
    private double lastEntityBbHeight = 0;
    private boolean lastNameplateVisible;
    @Persist
    private double lineHeight = -1;
    private final List<HologramLine> lines = Lists.newArrayList();
    private HologramLine nameLine;
    private final NPCRegistry registry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());
    private int t;
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
        lines.add(new HologramLine(text, true, -1, createDefaultHologramRenderer()));
        reloadLineHolograms();
    }

    public void addLine(String text, HologramRenderer hr) {
        lines.add(new HologramLine(text, hr));
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
        lines.add(new HologramLine(text, false, ticks, createDefaultHologramRenderer()));
        reloadLineHolograms();
    }

    public void addTemporaryLine(String text, int ticks, HologramRenderer hr) {
        lines.add(new HologramLine(text, false, ticks, hr));
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

    private HologramRenderer createDefaultHologramRenderer() {
        String hologramSetting = Setting.DEFAULT_HOLOGRAM_RENDERER.asString();
        if (!SUPPORTS_DISPLAY || hologramSetting.equalsIgnoreCase("armorstand"))
            return new ArmorstandRenderer();
        return hologramSetting.equalsIgnoreCase("interaction") ? new InteractionVehicleRenderer()
                : new TextDisplayRenderer();
    }

    private HologramRenderer createNameRenderer() {
        if (SpigotUtil.getVersion()[1] >= 20) {
            return new TextDisplayVehicleRenderer();
        } else if (SpigotUtil.getVersion()[1] == 19) {
            return new InteractionVehicleRenderer();
        }
        return new ArmorstandVehicleRenderer();
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

    @Deprecated
    public Collection<Entity> getHologramEntities() {
        return lines.stream().flatMap(l -> l.renderer.getEntities().stream()).collect(Collectors.toList());
    }

    public Collection<HologramRenderer> getHologramRenderers() {
        return lines.stream().map(l -> l.renderer).collect(Collectors.toList());
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

    @Deprecated
    public Entity getNameEntity() {
        return nameLine == null || nameLine.renderer.getEntities().size() == 0 ? null
                : nameLine.renderer.getEntities().iterator().next();
    }

    public HologramRenderer getNameRenderer() {
        return nameLine == null ? null : nameLine.renderer;
    }

    public int getViewRange() {
        return viewRange;
    }

    @Override
    public void load(DataKey root) {
        clear();
        for (DataKey key : root.getRelative("lines").getIntegerSubKeys()) {
            HologramLine line = new HologramLine(key.keyExists("text") ? key.getString("text") : key.getString(""),
                    true, -1, createDefaultHologramRenderer());
            line.mt = key.keyExists("margin.top") ? key.getDouble("margin.top") : 0.0;
            line.mb = key.keyExists("margin.bottom") ? key.getDouble("margin.bottom") : 0.0;
            lines.add(line);
        }
    }

    @Override
    public void onDespawn() {
        reloadLineHolograms();
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
    }

    private void reloadLineHolograms() {
        for (HologramLine line : lines) {
            line.removeNPC();
        }
        if (nameLine != null) {
            nameLine.removeNPC();
            nameLine = null;
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
        boolean nameplateVisible = Boolean
                .parseBoolean(npc.data().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString());
        if (npc.requiresNameHologram()) {
            if (nameLine != null && !nameplateVisible) {
                nameLine.removeNPC();
                nameLine = null;
            } else if (nameLine == null && nameplateVisible) {
                nameLine = new HologramLine(npc.getRawName(), createNameRenderer());
            }
        }
        Location npcLoc = npc.getStoredLocation();
        Vector3d offset = new Vector3d();
        boolean updatePosition = Setting.HOLOGRAM_ALWAYS_UPDATE_POSITION.asBoolean() || currentLoc == null
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
        if (nameLine != null) {
            if (updatePosition || nameLine.renderer.getEntities().size() == 0) {
                nameLine.render(offset);
            }
            if (updateName) {
                nameLine.setText(npc.getRawName());
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            HologramLine line = lines.get(i);

            if (line.ticks > 0 && --line.ticks == 0) {
                lines.remove(i--).removeNPC();
                continue;
            }
            if (updatePosition || line.renderer.getEntities().size() == 0) {
                offset.y = getHeight(i);
                line.render(offset);
            }
            if (updateName) {
                line.setText(line.text);
            }
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
        reloadLineHolograms();
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
     *            The line index
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

    public void setViewRange(int range) {
        this.viewRange = range;
        reloadLineHolograms();
    }

    public class ArmorstandRenderer extends SingleEntityHologramRenderer {
        @Override
        protected NPC createNPC(Entity base, String name, Vector3d offset) {
            NPC npc = registry.createNPC(EntityType.ARMOR_STAND, name);
            npc.getOrAddTrait(ArmorStandTrait.class).setAsHelperEntityWithName(npc);
            return npc;
        }

        @Override
        protected void render0(NPC npc, Vector3d offset) {
            hologram.getEntity().teleport(
                    npc.getStoredLocation().clone().add(offset.x, offset.y + getEntityBbHeight(), offset.z),
                    TeleportCause.PLUGIN);
        }
    }

    public class ArmorstandVehicleRenderer extends ArmorstandRenderer {
        @Override
        protected NPC createNPC(Entity base, String name, Vector3d offset) {
            NPC npc = registry.createNPC(EntityType.ARMOR_STAND, name);
            npc.getOrAddTrait(ArmorStandTrait.class).setAsHelperEntityWithName(npc);
            return npc;
        }

        @Override
        public void render0(NPC base, Vector3d offset) {
            if (hologram.getEntity().getVehicle() == null) {
                NMS.mount(base.getEntity(), hologram.getEntity());
            }
        }
    }

    private class HologramLine {
        double mb, mt;
        boolean persist;
        HologramRenderer renderer;
        String text;
        int ticks;

        public HologramLine(String text, boolean persist, int ticks, HologramRenderer hr) {
            if (ITEM_MATCHER.matcher(text).find()) {
                mb = 0.21;
                mt = 0.07;
                hr = new ItemRenderer();
            }
            this.persist = persist;
            this.ticks = ticks;
            this.renderer = hr;
            setText(text);
        }

        public HologramLine(String text, HologramRenderer renderer) {
            this(text, false, -1, renderer);
        }

        public void removeNPC() {
            renderer.destroy();
        }

        public void render(Vector3d vector3d) {
            renderer.render(npc, vector3d);
        }

        public void setText(String text) {
            this.text = text == null ? "" : text;
            if (ITEM_MATCHER.matcher(text).find() && !(renderer instanceof ItemRenderer)) {
                renderer.destroy();
                mb = 0.21;
                mt = 0.07;
                renderer = new ItemRenderer();
            }
            renderer.updateText(npc, text);
        }
    }

    public static interface HologramRenderer {
        void destroy();

        Collection<Entity> getEntities();

        String getPerPlayerText(NPC npc, Player viewer);

        default boolean isSneaking(NPC npc, Player player) {
            return NMS.isSneaking(player);
        }

        default void onSeenByPlayer(Player player) {
        }

        void render(NPC npc, Vector3d offset);

        void updateText(NPC npc, String text);
    }

    public class InteractionVehicleRenderer extends SingleEntityHologramRenderer {
        private Vector3d lastOffset;

        @Override
        protected NPC createNPC(Entity base, String name, Vector3d offset) {
            lastOffset = new Vector3d(offset);
            return registry.createNPC(EntityType.INTERACTION, name);
        }

        @Override
        public void onSeenByPlayer(Player player) {
            if (lastOffset == null)
                return;
            NMS.linkTextInteraction(player, hologram.getEntity(), npc.getEntity(), lastOffset.y);
        }

        @Override
        public void render0(NPC npc, Vector3d offset) {
            lastOffset = new Vector3d(offset);
            if (hologram.getEntity().getVehicle() == null) {
                NMS.mount(npc.getEntity(), hologram.getEntity());
            }
        }
    }

    public class ItemDisplayRenderer extends SingleEntityHologramRenderer {
        @Override
        protected NPC createNPC(Entity base, String name, Vector3d offset) {
            Matcher itemMatcher = ITEM_MATCHER.matcher(name);
            itemMatcher.find();
            Material item = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(itemMatcher.group(1), false)
                    : Material.matchMaterial(itemMatcher.group(1));
            ItemStack itemStack = new ItemStack(item, 1);
            NPC npc = registry.createNPCUsingItem(EntityType.ITEM_DISPLAY, "", itemStack);
            npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
            if (itemMatcher.group(2) != null) {
                if (itemMatcher.group(2).charAt(1) == '{') {
                    Bukkit.getUnsafe().modifyItemStack(itemStack, itemMatcher.group(2).substring(1));
                    npc.setItemProvider(() -> itemStack);
                } else {
                    npc.getOrAddTrait(ScoreboardTrait.class)
                            .setColor(Util.matchEnum(ChatColor.values(), itemMatcher.group(2).substring(1)));
                }
            }
            return npc;
        }

        @Override
        public void render0(NPC base, Vector3d offset) {
            ItemDisplay disp = (ItemDisplay) hologram.getEntity();
            Transformation tf = disp.getTransformation();
            tf.getTranslation().y = (float) offset.y + 0.1f;
            disp.setTransformation(tf);
            if (hologram.getEntity().getVehicle() == null) {
                NMS.mount(base.getEntity(), hologram.getEntity());
            }
        }

        @Override
        public void updateText(NPC npc, String text) {
            this.text = Placeholders.replace(text, null, npc);
        }
    }

    public class ItemRenderer extends SingleEntityHologramRenderer {
        private NPC itemNPC;

        @Override
        protected NPC createNPC(Entity base, String name, Vector3d offset) {
            NPC mount = registry.createNPC(EntityType.ARMOR_STAND, "");
            mount.getOrAddTrait(ArmorStandTrait.class).setAsPointEntity();
            Matcher itemMatcher = ITEM_MATCHER.matcher(name);
            itemMatcher.find();
            Material item = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(itemMatcher.group(1), false)
                    : Material.matchMaterial(itemMatcher.group(1));
            ItemStack itemStack = new ItemStack(item, 1);
            itemNPC = registry.createNPCUsingItem(Util.getFallbackEntityType("ITEM", "DROPPED_ITEM"), "", itemStack);
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
            itemNPC.spawn(base.getLocation());
            itemNPC.getOrAddTrait(MountTrait.class).setMountedOn(mount.getUniqueId());
            return mount;
        }

        @Override
        public void destroy() {
            super.destroy();
            if (itemNPC == null)
                return;
            itemNPC.destroy();
            itemNPC = null;
        }

        @Override
        protected void render0(NPC npc, Vector3d offset) {
            hologram.getEntity().teleport(
                    npc.getStoredLocation().clone().add(offset.x, offset.y + getEntityBbHeight(), offset.z),
                    TeleportCause.PLUGIN);
        }

        @Override
        public void updateText(NPC npc, String text) {
            this.text = Placeholders.replace(text, null, npc);
        }
    }

    public abstract class SingleEntityHologramRenderer implements HologramRenderer {
        protected NPC hologram;
        protected String text;

        protected abstract NPC createNPC(Entity base, String text, Vector3d offset);

        @Override
        public void destroy() {
            if (hologram != null) {
                hologram.destroy();
                hologram = null;
            }
        }

        @Override
        public Collection<Entity> getEntities() {
            return hologram != null && hologram.getEntity() != null ? ImmutableList.of(hologram.getEntity())
                    : Collections.emptyList();
        }

        @Override
        public String getPerPlayerText(NPC npc, Player viewer) {
            return Placeholders.replace(text, viewer, npc);
        }

        @Override
        public void render(NPC npc, Vector3d offset) {
            if (hologram == null) {
                spawnHologram(npc, offset);
            }
            render0(npc, offset);
        }

        protected abstract void render0(NPC npc, Vector3d offset);

        protected void spawnHologram(NPC npc, Vector3d offset) {
            hologram = createNPC(npc.getEntity(), text, offset);
            if (!hologram.hasTrait(ClickRedirectTrait.class)) {
                hologram.addTrait(new ClickRedirectTrait(npc));
            }
            hologram.data().set(NPC.Metadata.HOLOGRAM_RENDERER, this);
            if (Setting.PACKET_HOLOGRAMS.asBoolean()) {
                hologram.addTrait(PacketNPC.class);
            }
            if (viewRange != -1) {
                hologram.data().set(NPC.Metadata.TRACKING_RANGE, viewRange);
            } else if (npc.data().has(NPC.Metadata.TRACKING_RANGE)) {
                hologram.data().set(NPC.Metadata.TRACKING_RANGE, npc.data().get(NPC.Metadata.TRACKING_RANGE));
            }
            hologram.spawn(npc.getEntity().getLocation().add(offset.x, offset.y, offset.z));
        }

        @Override
        public void updateText(NPC npc, String raw) {
            this.text = Placeholders.replace(raw, null, npc);
            if (hologram == null)
                return;
            hologram.setName(text);
            if (!Placeholders.containsPlaceholders(raw)) {
                hologram.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, Messaging.stripColor(raw).length() > 0);
            }
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

        private static Set<String> LINE_ARGS = ImmutableSet.of("set", "remove", "margintop", "marginbottom");
    }

    public class TextDisplayRenderer extends SingleEntityHologramRenderer {
        private Color color;

        @Override
        protected NPC createNPC(Entity base, String name, Vector3d offset) {
            NPC hologram = registry.createNPC(EntityType.TEXT_DISPLAY, "");
            hologram.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
            hologram.data().set(NPC.Metadata.TEXT_DISPLAY_COMPONENT, Messaging.minecraftComponentFromRawMessage(name));
            return hologram;
        }

        @Override
        public void render0(NPC base, Vector3d offset) {
            TextDisplay disp = (TextDisplay) hologram.getEntity();
            disp.setInterpolationDelay(0);
            disp.setInterpolationDuration(0);
            disp.setBillboard(Billboard.CENTER);
            if (color != null) {
                disp.setBackgroundColor(color);
            }
            hologram.getEntity().teleport(
                    npc.getStoredLocation().clone().add(offset.x, offset.y + getEntityBbHeight(), offset.z),
                    TeleportCause.PLUGIN);
        }

        public void setBackgroundColor(Color color) {
            this.color = color;
        }

        @Override
        public void updateText(NPC npc, String raw) {
            this.text = Placeholders.replace(raw, null, npc);
            if (hologram == null)
                return;
            hologram.data().set(NPC.Metadata.TEXT_DISPLAY_COMPONENT, Messaging.minecraftComponentFromRawMessage(text));
        }
    }

    public class TextDisplayVehicleRenderer extends SingleEntityHologramRenderer {
        private Color color;

        @Override
        protected NPC createNPC(Entity base, String name, Vector3d offset) {
            NPC hologram = registry.createNPC(EntityType.TEXT_DISPLAY, "");
            hologram.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
            hologram.data().set(NPC.Metadata.TEXT_DISPLAY_COMPONENT, Messaging.minecraftComponentFromRawMessage(name));
            return hologram;
        }

        @Override
        public void render0(NPC base, Vector3d offset) {
            TextDisplay disp = (TextDisplay) hologram.getEntity();
            disp.setInterpolationDelay(0);
            disp.setInterpolationDuration(0);
            disp.setBillboard(Billboard.CENTER);
            Transformation tf = disp.getTransformation();
            tf.getTranslation().y = (float) offset.y + 0.7f;
            disp.setTransformation(tf);
            if (color != null) {
                disp.setBackgroundColor(color);
            }
            if (hologram.getEntity().getVehicle() == null) {
                NMS.mount(base.getEntity(), hologram.getEntity());
            }
        }

        public void setBackgroundColor(Color color) {
            this.color = color;
        }

        @Override
        public void updateText(NPC npc, String raw) {
            this.text = Placeholders.replace(raw, null, npc);
            if (hologram == null)
                return;
            hologram.data().set(NPC.Metadata.TEXT_DISPLAY_COMPONENT, Messaging.minecraftComponentFromRawMessage(text));
        }
    }

    private static final Pattern ITEM_MATCHER = Pattern.compile("<item:([a-zA-Z0-9_ ]*?)([:].*?)?>");
    private static boolean SUPPORTS_DISPLAY = false;
    static {
        try {
            SUPPORTS_DISPLAY = Class.forName("org.bukkit.entity.Display") != null;
        } catch (Throwable e) {
        }
    }
}
