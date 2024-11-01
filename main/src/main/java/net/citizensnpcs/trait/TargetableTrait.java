package net.citizensnpcs.trait;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@TraitName("targetable")
public class TargetableTrait extends Trait {
    private Set<UUID> beTargetedBy;

    public TargetableTrait() {
        super("targetable");
    }

    @Override
    public void onDespawn() {
        clearTargets();
    }

    // Only for internal use
    public void add(UUID uuid) {
        if (beTargetedBy == null) {
            beTargetedBy = new HashSet<>();
        }
        beTargetedBy.add(uuid);
    }

    // Only for internal use
    public void remove(UUID uuid) {
        if (beTargetedBy != null) {
            beTargetedBy.remove(uuid);
        }
    }

    public void clearTargets() {
        if (beTargetedBy != null) {
            for (UUID entityUUID : beTargetedBy) {
                final Entity entity = Bukkit.getEntity(entityUUID);
                if (entity instanceof Mob) {
                    if (entity.isValid()) {
                        ((Mob) entity).setTarget(null);
                    }
                }
            }
            beTargetedBy = null;
        }
    }
}
