package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@TraitName("targetable")
public class TargetableTrait extends Trait {
    private Set<UUID> targeters;
    @Persist
    private Boolean state;
    private boolean shouldSave;

    public TargetableTrait() {
        super("targetable");
    }

    @Override
    public void onDespawn() {
        clearTargeters();
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        final Object rawState = key.getRaw("state");
        if (rawState instanceof Boolean) {
            state = (Boolean) rawState;
            shouldSave = true;
        }
    }

    @Override
    public void save(DataKey key) {
        if (shouldSave) {
            if (state != null) {
                key.setBoolean("state", state);
            }
        }
    }

    public boolean isTargetable() {
        if (state == null) {
            return !npc.isProtected();
        }
        return state;
    }

    public void setTargetable(boolean state, boolean persistent) {
        if (this.state != state) {
            this.state = state;
            this.shouldSave = persistent;
            if (!state) {
                clearTargeters();
            }
        } else {
            if (!shouldSave && persistent) {
                this.shouldSave = true; // user want to persist it so here we go
            }
        }
    }

    // Only for internal use
    public void addTargeter(UUID uuid) {
        if (targeters == null) {
            targeters = new HashSet<>();
        }
        targeters.add(uuid);
    }

    // Only for internal use
    public void removeTargeter(UUID uuid) {
        if (targeters != null) {
            targeters.remove(uuid);
        }
    }

    public void clearTargeters() {
        if (targeters == null) {
            return;
        }
        if (SUPPORTS_GET_ENTITY) {
            for (UUID entityUUID : targeters) {
                final Entity entity = Bukkit.getEntity(entityUUID);
                if (entity instanceof Mob) {
                    if (entity.isValid()) {
                        ((Mob) entity).setTarget(null);
                    }
                }
            }
        }
        targeters = null;
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
