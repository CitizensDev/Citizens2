package net.citizensnpcs.trait;

import java.util.UUID;

import org.bukkit.entity.Entity;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;

/**
 * Persists the NPC's mounted on entity, if any. Will attempt to respawn on mount.
 */
@TraitName("mounttrait")
public class MountTrait extends Trait {
    private UUID currentMount;
    @Persist("mountedon")
    private UUID uuid;

    public MountTrait() {
        super("mounttrait");
    }

    public void checkMounted() {
        if (uuid == null || uuid.equals(currentMount))
            return;
        NPC other = CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(uuid);
        if (other != null && other.isSpawned()) {
            NMS.mount(other.getEntity(), npc.getEntity());
            currentMount = uuid;
        }
    }

    public UUID getMountedOn() {
        return currentMount;
    }

    @Override
    public void onDespawn() {
        if (currentMount != null) {
            npc.getEntity().leaveVehicle();
            currentMount = null;
        }
    }

    @Override
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        Entity vehicle = NMS.getVehicle(npc.getEntity());
        if (vehicle == null && currentMount != null) {
            currentMount = null;
        } else if (vehicle instanceof NPCHolder) {
            setMountedOn(((NPCHolder) vehicle).getNPC().getUniqueId());
        }
        checkMounted();
    }

    public void setMountedOn(UUID uuid) {
        this.uuid = uuid;
        if (npc.isSpawned()) {
            checkMounted();
        }
    }

    public void unmount() {
        if (currentMount == null)
            return;
        onDespawn();
        uuid = null;
    }
}