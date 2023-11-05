package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;

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
import net.citizensnpcs.util.NMS;

@TraitName("piglintrait")
public class PiglinTrait extends Trait {
    @Persist
    private boolean dancing;

    public PiglinTrait() {
        super("piglintrait");
    }

    public boolean isDancing() {
        return dancing;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Piglin) {
            NMS.setPiglinDancing(npc.getEntity(), dancing);
        }
    }

    public void setDancing(boolean dancing) {
        this.dancing = dancing;
    }

    @Command(
            aliases = { "npc" },
            usage = "piglin (--dancing [true|false])",
            desc = "Sets piglin modifiers.",
            modifiers = { "piglin" },
            min = 1,
            max = 1,
            permission = "citizens.npc.piglin")
    @Requirements(selected = true, ownership = true, types = { EntityType.PIGLIN })
    public static void piglin(CommandContext args, CommandSender sender, NPC npc, @Flag("dancing") Boolean dancing)
            throws CommandException {
        PiglinTrait trait = npc.getOrAddTrait(PiglinTrait.class);
        boolean hasArg = false;
        if (dancing != null) {
            trait.setDancing(dancing);
            Messaging.sendTr(sender, dancing ? Messages.PIGLIN_DANCING_SET : Messages.PIGLIN_DANCING_UNSET,
                    npc.getName());
            hasArg = true;
        }
        if (!hasArg)
            throw new CommandUsageException();
    }

}
