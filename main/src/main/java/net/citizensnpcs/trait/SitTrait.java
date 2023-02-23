package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("sittrait")
public class SitTrait extends Trait {
    private NPC chair;
    @Persist
    private Location sittingAt;

    public SitTrait() {
        super("sittrait");
    }

    public boolean isSitting() {
        return sittingAt != null;
    }

    @Override
    public void onDespawn() {
        if (chair != null) {
            if (chair.getEntity() != null) {
                chair.getEntity().eject();
            }
            chair.destroy();
            chair = null;
        }
    }

    @Override
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !isSitting())
            return;

        if (chair == null) {
            NPCRegistry registry = CitizensAPI.getNamedNPCRegistry("SitRegistry");
            if (registry == null) {
                registry = CitizensAPI.createNamedNPCRegistry("SitRegistry", new MemoryNPCDataStore());
            }
            chair = registry.createNPC(EntityType.ARMOR_STAND, "");
            chair.getOrAddTrait(ArmorStandTrait.class).setAsHelperEntity(npc);
            if (!chair.spawn(sittingAt.clone())) {
                chair = null;
                return;
            }
        }

        if (chair.isSpawned() && !NMS.getPassengers(chair.getEntity()).contains(npc.getEntity())) {
            NMS.mount(chair.getEntity(), npc.getEntity());
        }

        if (chair.getStoredLocation() != null && chair.getStoredLocation().distance(sittingAt) > 0.05) {
            chair.teleport(sittingAt.clone(), TeleportCause.PLUGIN);
        }
    }

    public void setSitting(Location at) {
        this.sittingAt = at != null ? at.clone() : null;
        if (!isSitting()) {
            onDespawn();
        }
    }
}
