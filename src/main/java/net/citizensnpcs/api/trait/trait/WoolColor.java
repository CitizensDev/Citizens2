package net.citizensnpcs.api.trait.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the wool color of a sheep.
 */
public class WoolColor extends Trait {
    private DyeColor color;
    private final NPC npc;

    public WoolColor(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            color = DyeColor.valueOf(key.getString(""));
        } catch (Exception ex) {
            color = DyeColor.WHITE;
        }
    }

    @Override
    public void onNPCSpawn() {
        if (npc.getBukkitEntity() instanceof Sheep)
            ((Sheep) npc.getBukkitEntity()).setColor(color);
    }

    @Override
    public void save(DataKey key) {
        key.setString("", color.name());
    }

    public DyeColor getColor() {
        return color;
    }
}