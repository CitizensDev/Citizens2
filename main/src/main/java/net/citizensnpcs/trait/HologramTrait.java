package net.citizensnpcs.trait;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
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
 * Persists a hologram attached to the NPC.
 */
@TraitName("hologramtrait")
public class HologramTrait extends Trait {
    private Location currentLoc;
    private BiFunction<String, Player, String> customHologramSupplier;
    @Persist
    private HologramDirection direction = HologramDirection.BOTTOM_UP;
    @Persist(reify = true)
    private HologramFilter filter;
    private double lastEntityHeight = 0;
    private boolean lastNameplateVisible;
    @Persist
    private double lineHeight = -1;
    private final List<HologramLine> lines = Lists.newArrayList();
    private HologramLine nameLine;
    private final NPCRegistry registry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());
    private int t;
    private boolean useTextDisplay;

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

    private NPC createHologram(String line, double heightOffset) {
        NPC hologramNPC = null;
        if (useTextDisplay) {
            hologramNPC = registry.createNPC(EntityType.TEXT_DISPLAY, line);
            hologramNPC.addTrait(new ClickRedirectTrait(npc));
        } else {
            hologramNPC = registry.createNPC(EntityType.ARMOR_STAND, line);
            hologramNPC.getOrAddTrait(ArmorStandTrait.class).setAsHelperEntityWithName(npc);
        }

        if (Setting.PACKET_HOLOGRAMS.asBoolean()) {
            hologramNPC.addTrait(PacketNPC.class);
        }

        hologramNPC.spawn(currentLoc.clone().add(0,
                getEntityHeight()
                        + (direction == HologramDirection.BOTTOM_UP ? heightOffset : getMaxHeight() - heightOffset),
                0));

        if (useTextDisplay) {
            ((TextDisplay) hologramNPC.getEntity()).setBillboard(Billboard.CENTER);
            ((TextDisplay) hologramNPC.getEntity()).setInterpolationDelay(0);
        }

        Matcher itemMatcher = ITEM_MATCHER.matcher(line);
        if (itemMatcher.matches()) {
            Material item = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(itemMatcher.group(1), false)
                    : Material.matchMaterial(itemMatcher.group(1));
            final NPC itemNPC = registry.createNPCUsingItem(EntityType.DROPPED_ITEM, "", new ItemStack(item, 1));
            itemNPC.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
            if (itemMatcher.group(2) != null) {
                itemNPC.getOrAddTrait(ScoreboardTrait.class)
                        .setColor(Util.matchEnum(ChatColor.values(), itemMatcher.group(2).substring(1)));
            }
            itemNPC.getOrAddTrait(MountTrait.class).setMountedOn(hologramNPC.getUniqueId());
            itemNPC.spawn(currentLoc);
            itemNPC.addRunnable(() -> {
                if (!itemNPC.isSpawned() || !itemNPC.getEntity().isInsideVehicle()) {
                    itemNPC.destroy();
                }
            });
        }

        lastEntityHeight = getEntityHeight();
        return hologramNPC;
    }

    /**
     * @return The direction that hologram lines are displayed in
     */
    public HologramDirection getDirection() {
        return direction;
    }

    private double getEntityHeight() {
        return NMS.getHeight(npc.getEntity()) + (useTextDisplay ? 0.27 : 0);
    }

    private double getHeight(int lineNumber) {
        double base = (lastNameplateVisible ? 0 : -getLineHeight());
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
        return Lists.transform(lines, (l) -> l.text);
    }

    private double getMaxHeight() {
        return (lastNameplateVisible ? getLineHeight() : 0) + getHeight(lines.size() - 1);
    }

    /**
     * Note: this is implementation-specific and may be removed at a later date.
     */
    public Entity getNameEntity() {
        return nameLine != null && nameLine.hologram.isSpawned() ? nameLine.hologram.getEntity() : null;
    }

    @Override
    public void load(DataKey root) {
        clear();
        for (DataKey key : root.getRelative("lines").getIntegerSubKeys()) {
            HologramLine line = new HologramLine(key.keyExists("text") ? key.getString("text") : key.getString(""), true);
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

        boolean updatePosition = currentLoc.getWorld() != npc.getStoredLocation().getWorld()
                || currentLoc.distance(npc.getStoredLocation()) >= 0.001 || lastNameplateVisible != nameplateVisible
                || Math.abs(lastEntityHeight - getEntityHeight()) >= 0.05;
        boolean updateName = false;
        if (t++ >= Setting.HOLOGRAM_UPDATE_RATE.asTicks() + Util.getFastRandom().nextInt(3) /* add some jitter */) {
            t = 0;
            updateName = true;
        }
        lastNameplateVisible = nameplateVisible;

        if (updatePosition) {
            currentLoc = npc.getStoredLocation();
            lastEntityHeight = getEntityHeight();
        }

        if (nameLine != null && nameLine.hologram.isSpawned()) {
            if (updatePosition) {
                nameLine.hologram.teleport(currentLoc.clone().add(0, getEntityHeight(), 0), TeleportCause.PLUGIN);
            }
            if (updateName) {
                nameLine.setText(npc.getRawName());
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            HologramLine line = lines.get(i);
            NPC hologramNPC = line.hologram;
            if (hologramNPC == null || !hologramNPC.isSpawned())
                continue;

            if (updatePosition) {
                Location tp = currentLoc.clone().add(0, lastEntityHeight
                        + (direction == HologramDirection.BOTTOM_UP ? getHeight(i) : getMaxHeight() - getHeight(i)), 0);
                hologramNPC.teleport(tp, TeleportCause.PLUGIN);
            }

            if (line.ticks > 0 && --line.ticks == 0) {
                line.removeNPC();
                lines.remove(i--);
                continue;
            }

            String text = line.text;
            if (ITEM_MATCHER.matcher(text).matches()) {
                hologramNPC.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
                continue;
            }

            if (!updateName)
                continue;

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
     * @see #getDirection()
     * @param direction
     *            The new direction
     */
    public void setDirection(HologramDirection direction) {
        this.direction = direction;

        reloadLineHolograms();
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
        }
        else if (type.equalsIgnoreCase("bottom")) {
            lines.get(idx).mb = margin;
        }
        reloadLineHolograms();
    }

    /**
     * Implementation-specific method: {@see NPC.Metadata#HOLOGRAM_LINE_SUPPLIER}
     */
    public void setPerPlayerTextSupplier(BiFunction<String, Player, String> nameSupplier) {
        this.customHologramSupplier = nameSupplier;
    }

    public void setUseTextDisplay(boolean use) {
        this.useTextDisplay = use;
        reloadLineHolograms();
    }

    public enum HologramDirection {
        BOTTOM_UP,
        TOP_DOWN;
    }

    public static class HologramFilter {
        private HologramFilter() {
        }

        public static class Builder {
            public HologramFilter build() {
                return new HologramFilter();
            }
        }
    }

    private class HologramLine implements Function<Player, String> {
        NPC hologram;
        double mb, mt;
        boolean persist;
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
            hologram = null;
        }

        public void setText(String text) {
            this.text = text == null ? "" : text;

            if (hologram != null) {
                String name = Placeholders.replace(text, null, npc);
                hologram.setName(name);
                hologram.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, ChatColor.stripColor(name).length() > 0);
                if (Placeholders.containsPlayerPlaceholder(text)) {
                    hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER, this);
                } else {
                    hologram.data().remove(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER);
                }
            }
        }

        public void spawnNPC(double height) {
            String name = Placeholders.replace(text, null, npc);
            this.hologram = createHologram(name, height);
            if (customHologramSupplier != null) {
                hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER,
                        (Function<Player, String>) p -> customHologramSupplier.apply(text, p));
            } else if (Placeholders.containsPlayerPlaceholder(text)) {
                hologram.data().set(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER, this);
            }
        }
    }

    private static final Pattern ITEM_MATCHER = Pattern.compile("<item:(.*?)([:].*?)?>");
    private static boolean SUPPORTS_TEXT_DISPLAY = false;
    static {
        try {
            EntityType.valueOf("TEXT_DISPLAY");
            SUPPORTS_TEXT_DISPLAY = true;
        } catch (IllegalArgumentException iae) {
        }
    }
}
