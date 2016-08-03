package net.citizensnpcs.trait;

import java.util.UUID;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;

@TraitName("mounttrait")
public class MountTrait extends Trait {
    @Persist("mountedon")
    private UUID mountedOn;

    public MountTrait() {
        super("mounttrait");
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        if (mountedOn != null) {
            NPC other = CitizensAPI.getNPCRegistry().getByUniqueId(mountedOn);
            if (other != null && other.isSpawned()) {
                NMS.mount(other.getEntity(), npc.getEntity());
            }
        }

        if (NMS.getVehicle(npc.getEntity()) instanceof NPCHolder) {
            mountedOn = ((NPCHolder) NMS.getVehicle(npc.getEntity())).getNPC().getUniqueId();
        }
    }
}
