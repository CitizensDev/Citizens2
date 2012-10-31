package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.EntityType;

/**
 * Represents an NPC's mob type.
 */
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
    public void load(DataKey key) {
        type = EntityType.fromName(key.getString(""));
        if (type == null)
            type = EntityType.PLAYER;
    }

    @Override
    public void onSpawn() {
        type = npc.getBukkitEntity().getType();
    }

    @Override
    public void save(DataKey key) {
        key.setString("", type.getName());
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