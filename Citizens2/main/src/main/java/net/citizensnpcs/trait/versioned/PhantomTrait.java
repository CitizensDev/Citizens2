package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

@TraitName("phantomtrait")
public class PhantomTrait extends Trait {
    @Persist
    private int size = 1;

    public PhantomTrait() {
        super("phantomtrait");
    }

    public int getSize() {
        return size;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Phantom) {
            Phantom phantom = (Phantom) npc.getEntity();
            phantom.setSize(size);
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Command(
            aliases = { "npc" },
            usage = "phantom (--size size)",
            desc = "",
            modifiers = { "phantom" },
            min = 1,
            max = 1,
            permission = "citizens.npc.phantom")
    @Requirements(selected = true, ownership = true, types = EntityType.PHANTOM)
    public static void phantom(CommandContext args, CommandSender sender, NPC npc, @Flag("size") Integer size)
            throws CommandException {
        PhantomTrait trait = npc.getOrAddTrait(PhantomTrait.class);
        String output = "";
        if (size != null) {
            if (size <= 0)
                throw new CommandUsageException();
            trait.setSize(size);
            output += Messaging.tr(Messages.PHANTOM_STATE_SET, size);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        } else
            throw new CommandUsageException();
    }
}
