package net.citizensnpcs.trait;

import org.bukkit.command.CommandSender;
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
        if (npc.getCosmeticEntity() instanceof Slime) {
            ((Slime) npc.getCosmeticEntity()).setSize(size);
        }
    }

    /**
     * @see Slime#setSize(int)
     */
    public void setSize(int size) {
        this.size = size;
    }
}
