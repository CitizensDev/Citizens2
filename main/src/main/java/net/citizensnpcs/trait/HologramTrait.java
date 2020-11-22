package net.citizensnpcs.trait;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

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
    private final List<NPC> hologramNPCs = Lists.newArrayList();
    @Persist
    private double lineHeight = -1;
    @Persist
    private final List<String> lines = Lists.newArrayList();
    private NPC nameNPC;
    private final NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

    public HologramTrait() {
        super("hologramtrait");
    }

    public void addLine(String text) {
        lines.add(text);
        unload();
        load();
    }

    public void clear() {
        unload();
        lines.clear();
    }

    private NPC createHologram(String line, double heightOffset) {
        NPC hologramNPC = registry.createNPC(EntityType.ARMOR_STAND, line);
        hologramNPC.addTrait(new ClickRedirectTrait(npc));
        ArmorStandTrait trait = hologramNPC.getOrAddTrait(ArmorStandTrait.class);
        trait.setVisible(false);
        trait.setSmall(true);
        trait.setMarker(true);
        trait.setGravity(false);
        trait.setHasArms(false);
        trait.setHasBaseplate(false);
        hologramNPC.spawn(currentLoc.clone().add(0,
                getEntityHeight()
                        + (direction == HologramDirection.BOTTOM_UP ? heightOffset : getMaxHeight() - heightOffset),
                0));
        return hologramNPC;
    }

    public HologramDirection getDirection() {
        return direction;
    }

    private double getEntityHeight() {
        return NMS.getHeight(npc.getEntity());
    }

    private double getHeight(int lineNumber) {
        return (lineHeight == -1 ? Setting.DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT.asDouble() : lineHeight) * (lineNumber + 1);
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public List<String> getLines() {
        return lines;
    }

    private double getMaxHeight() {
        return (lineHeight == -1 ? Setting.DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT.asDouble() : lineHeight)
                * (lines.size() + (npc.requiresNameHologram() ? 0 : 1));
    }

    private void load() {
        currentLoc = npc.getStoredLocation();
        int i = 0;
        if (npc.requiresNameHologram()
                && Boolean.parseBoolean(npc.data().<Object> get(NPC.NAMEPLATE_VISIBLE_METADATA, true).toString())) {
            nameNPC = createHologram(npc.getFullName(), 0);
        }
        for (String line : lines) {
            hologramNPCs.add(createHologram(Placeholders.replace(line, null, npc), getHeight(i)));
            i++;
        }
    }

    @Override
    public void onDespawn() {
        unload();
    }

    @Override
    public void onRemove() {
        unload();
    }

    @Override
    public void onSpawn() {
        load();
    }

    public void removeLine(int idx) {
        lines.remove(idx);
        unload();
        load();
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) {
            unload();
            return;
        }
        if (npc.requiresNameHologram()) {
            boolean visible = Boolean
                    .parseBoolean(npc.data().<Object> get(NPC.NAMEPLATE_VISIBLE_METADATA, true).toString());
            if (nameNPC != null && !visible) {
                nameNPC.destroy();
                nameNPC = null;
            } else if (nameNPC == null && visible) {
                nameNPC = createHologram(npc.getFullName(), 0);
            }
        }
        boolean update = currentLoc.getWorld() != npc.getStoredLocation().getWorld()
                || currentLoc.distanceSquared(npc.getStoredLocation()) >= 0.01;
        if (update) {
            currentLoc = npc.getStoredLocation();
        }
        if (nameNPC != null && nameNPC.isSpawned()) {
            if (update) {
                nameNPC.teleport(currentLoc.clone().add(0, getEntityHeight(), 0), TeleportCause.PLUGIN);
            }
            nameNPC.setName(npc.getFullName());
        }
        for (int i = 0; i < hologramNPCs.size(); i++) {
            NPC hologramNPC = hologramNPCs.get(i);
            if (!hologramNPC.isSpawned())
                continue;
            if (update) {
                hologramNPC.teleport(currentLoc.clone().add(0, getEntityHeight() + getHeight(i), 0),
                        TeleportCause.PLUGIN);
            }
            String text = lines.get(i);
            if (text != null && !ChatColor.stripColor(Colorizer.parseColors(text)).isEmpty()) {
                hologramNPC.setName(Placeholders.replace(text, null, npc));
                hologramNPC.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, true);
            } else {
                hologramNPC.setName("");
                hologramNPC.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, false);
            }
        }
    }

    public void setDirection(HologramDirection direction) {
        this.direction = direction;
        unload();
        load();
    }

    public void setLine(int idx, String text) {
        if (idx == lines.size()) {
            lines.add(idx, text);
        } else {
            lines.set(idx, text);
        }
    }

    public void setLineHeight(double height) {
        lineHeight = height;
        unload();
        load();
    }

    private void unload() {
        if (nameNPC != null) {
            nameNPC.destroy();
            nameNPC = null;
        }
        for (NPC npc : hologramNPCs) {
            npc.destroy();
        }
        hologramNPCs.clear();
    }

    public enum HologramDirection {
        BOTTOM_UP,
        TOP_DOWN;
    }
}
