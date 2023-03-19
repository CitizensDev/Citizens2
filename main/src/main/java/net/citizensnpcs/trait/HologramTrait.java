package net.citizensnpcs.trait;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
    @Persist
    private HologramDirection direction = HologramDirection.BOTTOM_UP;
    private double lastEntityHeight = 0;
    private boolean lastNameplateVisible;
    @Persist
    private double lineHeight = -1;
    private final List<HologramLine> lines = Lists.newArrayList();
    private NPC nameNPC;
    private final NPCRegistry registry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());
    private int t;
    private boolean useTextDisplay = false;

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
        return NMS.getHeight(npc.getEntity());
    }

    private double getHeight(int lineNumber) {
        double base = (lastNameplateVisible ? 0 : -getLineHeight());
        if (useTextDisplay) {
            base += 0.15;
        }
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
        return nameNPC != null && nameNPC.isSpawned() ? nameNPC.getEntity() : null;
    }

    private double getRotationDistance(Location loc) {
        return Math.abs(loc.getYaw() - npc.getStoredLocation().getYaw())
                + Math.abs(loc.getPitch() - npc.getStoredLocation().getPitch());
    }

    @Override
    public void load(DataKey root) {
        clear();
        for (DataKey key : root.getRelative("lines").getIntegerSubKeys()) {
            lines.add(new HologramLine(key.getString(""), true));
        }
    }

    @Override
    public void onDespawn() {
        if (nameNPC != null) {
            nameNPC.destroy();
            nameNPC = null;
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
            nameNPC = createHologram(Placeholders.replace(npc.getRawName(), null, npc), 0);
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
            currentLoc = npc.getStoredLocation();
        }

        boolean nameplateVisible = Boolean
                .parseBoolean(npc.data().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString());
        if (npc.requiresNameHologram()) {
            if (nameNPC != null && !nameplateVisible) {
                nameNPC.destroy();
                nameNPC = null;
            } else if (nameNPC == null && nameplateVisible) {
                nameNPC = createHologram(Placeholders.replace(npc.getRawName(), null, npc), 0);
            }
        }

        boolean updatePosition = currentLoc.getWorld() != npc.getStoredLocation().getWorld()
                || currentLoc.distance(npc.getStoredLocation()) >= 0.001 || lastNameplateVisible != nameplateVisible
                || Math.abs(lastEntityHeight - getEntityHeight()) >= 0.05
                || (useTextDisplay && getRotationDistance(currentLoc) >= 0.001);
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

        if (nameNPC != null && nameNPC.isSpawned()) {
            if (updatePosition) {
                nameNPC.teleport(currentLoc.clone().add(0, getEntityHeight(), 0), TeleportCause.PLUGIN);
            }
            if (updateName) {
                nameNPC.setName(Placeholders.replace(npc.getRawName(), null, npc));
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
            hologramNPC.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, npc.getRawName().length() > 0);
        }
    }

    @Override
    public void save(DataKey root) {
        root.removeKey("lines");
        int i = 0;
        for (HologramLine line : lines) {
            if (!line.persist)
                continue;
            root.setString("lines." + i, line.text);
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

    public void setUseTextDisplay(boolean useTextDisplay) {
        this.useTextDisplay = useTextDisplay;
    }

    public enum HologramDirection {
        BOTTOM_UP,
        TOP_DOWN;
    }

    private class HologramLine {
        NPC hologram;
        double mb, mt;
        boolean persist;
        String text;
        int ticks;

        public HologramLine(String text, boolean persist) {
            this(text, persist, -1);
        }

        public HologramLine(String text, boolean persist, int ticks) {
            this.text = text == null ? "" : text;
            this.persist = persist;
            this.ticks = ticks;
            if (ITEM_MATCHER.matcher(text).matches()) {
                mb = 0.21;
                mt = 0.07;
            }
        }

        public void removeNPC() {
            if (hologram == null)
                return;

            hologram.destroy();
            hologram = null;
        }

        public void setText(String text) {
            this.text = text;

            if (hologram != null) {
                hologram.setName(Placeholders.replace(text, null, npc));
            }
        }

        public void spawnNPC(double height) {
            this.hologram = createHologram(Placeholders.replace(text, null, npc), height);
        }
    }

    private static final Pattern ITEM_MATCHER = Pattern.compile("<item:(.*?)([:].*?)?>");
}
