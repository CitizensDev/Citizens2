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
    private UUID mountedOn;
    private boolean triggered = false;
    @Persist("mountedon")
    private String uuid;

    public MountTrait() {
        super("mounttrait");
    }

    public void checkMounted(Entity mounted) {
        if (mountedOn == null || (mounted != null && mounted.getUniqueId().equals(mountedOn)))
            return;
        NPC other = CitizensAPI.getNPCRegistry().getByUniqueId(mountedOn);
        if (other != null && other.isSpawned()) {
            NMS.mount(other.getEntity(), npc.getEntity());
            triggered = true;
        }
    }

    public UUID getMountedOn() {
        return mountedOn;
    }

    @Override
    public void onDespawn() {
        if (NMS.getVehicle(npc.getEntity()) != null) {
            npc.getEntity().leaveVehicle();
        }
    }

    @Override
    public void onSpawn() {
        checkMounted(null);
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        if (!triggered && uuid != null) {
            try {
                mountedOn = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                mountedOn = null;
            }
            checkMounted(null);
        }
        Entity vehicle = NMS.getVehicle(npc.getEntity());
        if (vehicle == null && !triggered) {
            mountedOn = null;
        } else if (vehicle instanceof NPCHolder) {
            setMountedOn(((NPCHolder) vehicle).getNPC().getUniqueId());
        }
        checkMounted(vehicle);
    }

    public void setMountedOn(UUID uuid) {
        this.mountedOn = uuid;
        this.uuid = uuid.toString();
    }

    public void unmount() {
        if (mountedOn == null)
            return;
        if (NMS.getVehicle(npc.getEntity()) != null) {
            npc.getEntity().leaveVehicle();
        }
        uuid = null;
        mountedOn = null;
    }
}