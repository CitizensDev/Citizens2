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
    private boolean slime;

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
        if (!(npc.getEntity() instanceof Slime)) {
            slime = false;
            return;
        }
        ((Slime) npc.getEntity()).setSize(size);
        slime = true;
    }

    /**
     * @see Slime#setSize(int)
     */
    public void setSize(int size) {
        this.size = size;
        if (slime) {
            ((Slime) npc.getEntity()).setSize(size);
        }
    }
}
