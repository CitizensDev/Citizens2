package net.citizensnpcs.trait;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.util.NMS;

/**
 * Persists a hologram attached to the NPC.
 */
@TraitName("hologramtrait")
public class HologramTrait extends Trait {
    private Location currentLoc;
    @Persist
    private HologramDirection direction = HologramDirection.BOTTOM_UP;
    private boolean lastNameplateVisible;
    @Persist
    private double lineHeight = -1;
    private final List<NPC> lineHolograms = Lists.newArrayList();
    @Persist
    private final List<String> lines = Lists.newArrayList();
    private NPC nameNPC;
    private final NPCRegistry registry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());

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
        lines.add(text);
        onDespawn();
        onSpawn();
    }

    /**
     * Clears all hologram lines
     */
    public void clear() {
        onDespawn();
        lines.clear();
    }

    private NPC createHologram(String line, double heightOffset) {
        NPC hologramNPC = registry.createNPC(EntityType.ARMOR_STAND, line);
        hologramNPC.addTrait(new ClickRedirectTrait(npc));
        hologramNPC.getOrAddTrait(ArmorStandTrait.class).setAsPointEntity();
        npc.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, true);
        hologramNPC.spawn(currentLoc.clone().add(0,
                getEntityHeight()
                        + (direction == HologramDirection.BOTTOM_UP ? heightOffset : getMaxHeight() - heightOffset),
                0));
        Matcher itemMatcher = ITEM_MATCHER.matcher(line);
        if (itemMatcher.matches()) {
            Material item = Material.matchMaterial(itemMatcher.group(1), false);
            NPC itemNPC = registry.createNPCUsingItem(EntityType.DROPPED_ITEM, "", new ItemStack(item, 1));
            itemNPC.spawn(currentLoc);
            ((ArmorStand) hologramNPC.getEntity()).addPassenger(itemNPC.getEntity());
            itemNPC.addRunnable(new Runnable() {
                @Override
                public void run() {
                    if (!itemNPC.isSpawned() || !itemNPC.getEntity().isInsideVehicle()) {
                        itemNPC.destroy();
                    }
                }
            });
        }
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
        return (lineHeight == -1 ? Setting.DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT.asDouble() : lineHeight)
                * (lastNameplateVisible ? lineNumber + 1 : lineNumber);
    }

    /**
     * Note: this is implementation-specific and may be removed at a later date.
     */
    public Collection<ArmorStand> getHologramEntities() {
        return Collections2.transform(lineHolograms, (n) -> (ArmorStand) n.getEntity());
    }

    /**
     * @return The line height between each hologram line, in blocks
     */
    public double getLineHeight() {
        return lineHeight;
    }

    /**
     * @return the hologram lines, in bottom-up order
     */
    public List<String> getLines() {
        return lines;
    }

    private double getMaxHeight() {
        return (lineHeight == -1 ? Setting.DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT.asDouble() : lineHeight)
                * (lines.size() + (npc.requiresNameHologram() ? 0 : 1));
    }

    /**
     * Note: this is implementation-specific and may be removed at a later date.
     */
    public ArmorStand getNameEntity() {
        return nameNPC != null && nameNPC.isSpawned() ? ((ArmorStand) nameNPC.getEntity()) : null;
    }

    @Override
    public void onDespawn() {
        if (nameNPC != null) {
            nameNPC.destroy();
            nameNPC = null;
        }
        for (NPC npc : lineHolograms) {
            npc.destroy();
        }
        lineHolograms.clear();
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
            nameNPC = createHologram(npc.getFullName(), 0);
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            lineHolograms.add(createHologram(Placeholders.replace(line, null, npc), getHeight(i)));
        }
    }

    /**
     * Removes the line at the specified index
     *
     * @param idx
     */
    public void removeLine(int idx) {
        lines.remove(idx);
        onDespawn();
        onSpawn();
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
                nameNPC = createHologram(npc.getFullName(), 0);
            }
        }
        boolean update = currentLoc.getWorld() != npc.getStoredLocation().getWorld()
                || currentLoc.distance(npc.getStoredLocation()) >= 0.001 || lastNameplateVisible != nameplateVisible;
        lastNameplateVisible = nameplateVisible;

        if (update) {
            currentLoc = npc.getStoredLocation();
        }
        if (nameNPC != null && nameNPC.isSpawned()) {
            if (update) {
                nameNPC.teleport(currentLoc.clone().add(0, getEntityHeight(), 0), TeleportCause.PLUGIN);
            }
            nameNPC.setName(npc.getFullName());
        }
        for (int i = 0; i < lineHolograms.size(); i++) {
            NPC hologramNPC = lineHolograms.get(i);
            if (!hologramNPC.isSpawned())
                continue;
            if (update) {
                hologramNPC.teleport(currentLoc.clone().add(0, getEntityHeight() + getHeight(i), 0),
                        TeleportCause.PLUGIN);
            }
            if (i >= lines.size()) {
                Messaging.severe("More hologram NPCs than lines for ID", npc.getId(), "lines", lines);
                break;
            }
            String text = lines.get(i);
            if (ITEM_MATCHER.matcher(text).matches()) {
                text = null;
            }
            if (text != null && !ChatColor.stripColor(Colorizer.parseColors(text)).isEmpty()) {
                hologramNPC.setName(Placeholders.replace(text, null, npc));
                hologramNPC.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, true);
            } else {
                hologramNPC.setName("");
                hologramNPC.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, "hover");
            }
        }
    }

    /**
     * @see #getDirection()
     * @param direction
     *            The new direction
     */
    public void setDirection(HologramDirection direction) {
        this.direction = direction;
        onDespawn();
        onSpawn();
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
            lines.add(text);
        } else {
            lines.set(idx, text);
            if (idx < lineHolograms.size()) {
                lineHolograms.get(idx).setName(Placeholders.replace(text, null, npc));
                return;
            }
        }
        onDespawn();
        onSpawn();
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
        onDespawn();
        onSpawn();
    }

    public enum HologramDirection {
        BOTTOM_UP,
        TOP_DOWN;
    }

    private static final Pattern ITEM_MATCHER = Pattern.compile("<item:(.*?)>");
}
