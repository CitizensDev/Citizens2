package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Slime;

public class SlimeSize extends Trait {
    @Persist
    private int size = 3;
    private boolean slime;

    protected SlimeSize(String name) {
        super("slimesize");
    }

    public void describe(CommandSender sender) {
        Messaging.sendTr(sender, Messages.SIZE_DESCRIPTION, npc.getName(), size);
    }

    @Override
    public void onSpawn() {
        if (!(npc.getBukkitEntity() instanceof Slime)) {
            slime = false;
            return;
        }
        ((Slime) npc.getBukkitEntity()).setSize(size);
        slime = true;
    }

    public void setSize(int size) {
        this.size = size;
        if (slime)
            ((Slime) npc.getBukkitEntity()).setSize(size);
    }
}
