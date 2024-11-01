package net.citizensnpcs.trait;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("tracktargetedby")
public class TrackTargetedByTrait extends Trait {
    private Set<UUID> targetedBy;

    public TrackTargetedByTrait() {
        super("tracktargetedby");
    }

    public void add(UUID uuid) {
        if (targetedBy == null) {
            targetedBy = new HashSet<>();
        }
        targetedBy.add(uuid);
    }

    public void clearTargets() {
        if (targetedBy == null)
            return;
        targetedBy = null;
        if (!SUPPORTS_GET_ENTITY)
            return;
        for (UUID uuid : targetedBy) {
            final Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof Mob) {
                if (entity.isValid()) {
                    ((Mob) entity).setTarget(null);
                }
            }
        }
    }

    @Override
    public void onDespawn() {
        clearTargets();
    }

    public void remove(UUID uuid) {
        if (targetedBy != null) {
            targetedBy.remove(uuid);
        }
    }

    private static boolean SUPPORTS_GET_ENTITY = true;
    static {
        try {
            Bukkit.class.getMethod("getEntity", UUID.class);
        } catch (NoSuchMethodException e) {
            SUPPORTS_GET_ENTITY = false;
        }
    }
}
