package net.citizensnpcs.trait;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractCubeMob;
import org.bukkit.entity.Slime;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

/**
 * Persists Slime size.
 *
 * @see Slime#setSize(int)
 */
@TraitName("slimesize")
public class SlimeSize extends Trait {
    @Persist
    private int size = 3;

    public SlimeSize() {
        super("slimesize");
    }

    public void describe(CommandSender sender) {
        Messaging.sendTr(sender, Messages.SIZE_DESCRIPTION, npc.getName(), size);
    }

    public int getSize() {
        return size;
    }

    @Override
    public void onSpawn() {
        if (CUBE_MOB_EXISTS && npc.getCosmeticEntity() instanceof AbstractCubeMob) {
            ((AbstractCubeMob) npc.getCosmeticEntity()).setSize(size);
        } else if (npc.getCosmeticEntity() instanceof Slime) {
            ((Slime) npc.getCosmeticEntity()).setSize(size);
        }
    }

    /**
     * @see Slime#setSize(int)
     */
    public void setSize(int size) {
        this.size = size;
    }

    private static boolean CUBE_MOB_EXISTS = true;

    static {
        try {
            Class.forName("org.bukkit.entity.AbstractCubeMob");
        } catch (ClassNotFoundException e) {
            CUBE_MOB_EXISTS = false;
        }
    }
}
