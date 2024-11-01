package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.SimpleMetadataStore;
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
    private Set<UUID> beTargetedBy;
    @Persist
    private Boolean state;
    private boolean shouldSave;

    public TargetableTrait() {
        super("targetable");
    }

    @Override
    public void onAttach() {
        applyFromLegacyMetadata();
    }

    @Override
    public void onDespawn() {
        clearTargets();
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

    @Override
    public void run() {
        final boolean applied = applyFromLegacyMetadata();
        if (applied) {
            // fixme how to decide if we should persist the state? as we don't know its persistence state
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
                clearTargets();
            }
        } else {
            if (!shouldSave && persistent) {
                this.shouldSave = true; // user want to persist it so here we go
            }
        }
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

    private boolean applyFromLegacyMetadata() {
        final boolean hasLegacyMeta = npc.data().has(NPC.Metadata.TARGETABLE);
        if (hasLegacyMeta) {
            state = npc.data().get(NPC.Metadata.TARGETABLE);
            shouldSave = true; // because it is loaded from persistent metadata
            // then prevent the data from being saved through metadata code, we'll handle this
            npc.data().remove(NPC.Metadata.TARGETABLE); // fixme is this right?
            return true;
        }
        return false;
    }

}
