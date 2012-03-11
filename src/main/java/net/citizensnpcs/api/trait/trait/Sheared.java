package net.citizensnpcs.api.trait.trait;

import org.bukkit.entity.Sheep;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the sheared status of a sheep NPC.
 */
public class Sheared extends Trait {
    private boolean sheared;
    private final NPC npc;

    public Sheared(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        sheared = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", sheared);
    }

    @Override
    public void onNPCSpawn() {
        // TODO Get rid of instanceof's, add
        // TraitFactory.withTypes(EntityType...) or similar
        if (npc.getBukkitEntity() instanceof Sheep)
            ((Sheep) npc.getBukkitEntity()).setSheared(sheared);
    }

    /**
     * Gets if a sheep NPC is sheared.
     * 
     * @return Whether a sheep NPC is sheared
     */
    public boolean isSheared() {
        return sheared;
    }

    /**
     * Sets whether a sheep NPC is sheared.
     * 
     * @param sheared
     *            Whether a sheep NPC is sheared
     */
    public void setSheared(boolean sheared) {
        this.sheared = sheared;
        if (npc.getBukkitEntity() instanceof Sheep)
            ((Sheep) npc.getBukkitEntity()).setSheared(sheared);
    }

    @Override
    public String toString() {
        return "Sheared{" + sheared + "}";
    }
}