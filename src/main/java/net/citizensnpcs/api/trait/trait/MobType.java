package net.citizensnpcs.api.trait.trait;

import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents an NPC's mob type.
 */
@TraitName("type")
public class MobType extends Trait {
    private EntityType type = EntityType.PLAYER;

    public MobType() {
        super("type");
    }

    /**
     * Gets the type of mob that an NPC is.
     *
     * @return The mob type
     */
    public EntityType getType() {
        return type;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void load(DataKey key) {
        try {
            if (key.getString("").equals("PIG_ZOMBIE")) {
                type = EntityType.ZOMBIFIED_PIGLIN;
            } else {
                type = EntityType.valueOf(key.getString(""));
            }
        } catch (IllegalArgumentException ex) {
            type = EntityType.fromName(key.getString(""));
        }
        if (type == null) {
            type = EntityType.PLAYER;
        }
    }

    @Override
    public void onSpawn() {
        type = npc.getEntity().getType();
    }

    @Override
    public void save(DataKey key) {
        key.setString("", type.name());
    }

    /**
     * Sets the type of mob that an NPC is.
     *
     * @param type
     *            Mob type to set the NPC as
     */
    public void setType(EntityType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MobType{" + type + "}";
    }
}