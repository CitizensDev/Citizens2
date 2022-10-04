package net.citizensnpcs.trait;

import org.bukkit.entity.EntityType;

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
    private NPC holder;
    @Persist
    private boolean sitting;

    public SitTrait() {
        super("sittrait");
    }

    public boolean isSitting() {
        return sitting;
    }

    @Override
    public void onDespawn() {
        if (holder != null) {
            holder.destroy();
            holder = null;
        }
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !sitting) {
            return;
        }

        if (holder == null) {
            NPCRegistry registry = CitizensAPI.getNamedNPCRegistry("PlayerAnimationImpl");
            if (registry == null) {
                registry = CitizensAPI.createNamedNPCRegistry("PlayerAnimationImpl", new MemoryNPCDataStore());
            }
            holder = registry.createNPC(EntityType.ARMOR_STAND, "");
            holder.getOrAddTrait(ArmorStandTrait.class).setAsHelperEntity(npc);
            holder.spawn(npc.getStoredLocation());
        }

        if (holder.getEntity() != null && !NMS.getPassengers(holder.getEntity()).contains(npc.getEntity())) {
            NMS.mount(holder.getEntity(), npc.getEntity());
        }
    }

    public void setSitting(boolean val) {
        this.sitting = val;
        if (!sitting) {
            onDespawn();
        }
    }
}
