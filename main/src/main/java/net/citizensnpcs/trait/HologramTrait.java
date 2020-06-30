package net.citizensnpcs.trait;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Placeholders;

/**
 * Persists a hologram attached to the NPC.
 */
@TraitName("hologramtrait")
public class HologramTrait extends Trait {
    private final NPCRegistry registry = CitizensAPI
            .createAnonymousNPCRegistry(new MemoryNPCDataStore());
    private NPC hologramNPC;
    @Persist
    private String text;

    public HologramTrait() {
        super("hologramtrait");
    }

    @Override
    public void onDespawn() {
        if (hologramNPC != null) {
            hologramNPC.destroy();
        }
    }

    @Override
    public void onSpawn() {
        hologramNPC = registry.createNPC(EntityType.ARMOR_STAND, "");
        ArmorStandTrait trait = hologramNPC.getTrait(ArmorStandTrait.class);
        trait.setVisible(false);
        trait.setSmall(true);
        hologramNPC.spawn(npc.getStoredLocation());
        hologramNPC.getEntity().setInvulnerable(true);
        hologramNPC.getEntity().setGravity(false);
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        ArmorStand hologram = (ArmorStand) hologramNPC.getEntity();
        if (hologram == null)
            return;
        if (hologram.getVehicle() == null || hologram.getVehicle() != npc.getEntity()) {
            if (hologram.getVehicle() != npc.getEntity()) {
                hologram.leaveVehicle();
            }
            npc.getEntity().addPassenger(hologram);
        }
        if (text != null && !text.isEmpty()) {
            hologramNPC.setName(Placeholders.replace(text, null, npc));
        } else {
        }
    }

    public void setText(String text) {
        this.text = text;
    }

}
