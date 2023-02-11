package net.citizensnpcs.trait;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("mirrortrait")
public class MirrorTrait extends Trait {
    @Persist
    private boolean enabled;
    @Persist
    private boolean mirrorName;

    public MirrorTrait() {
        super("mirrortrait");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMirroring(Player player) {
        return enabled;
    }

    public boolean mirrorName() {
        return mirrorName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation(), SpawnReason.RESPAWN);
        }
    }

    public void setMirrorName(boolean mirror) {
        mirrorName = mirror;
    }
}