package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Allay;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
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

@TraitName("allaytrait")
public class AllayTrait extends Trait {
    @Persist
    private boolean dancing = false;

    public AllayTrait() {
        super("allaytrait");
    }

    public boolean isDancing() {
        return dancing;
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof Allay))
            return;
        NMS.setAllayDancing(npc.getEntity(), dancing);
    }

    public void setDancing(boolean dance) {
        dancing = dance;
    }

    @Command(
            aliases = { "npc" },
            usage = "allay (-d(ancing))",
            desc = "",
            modifiers = { "allay" },
            min = 1,
            max = 1,
            flags = "d",
            permission = "citizens.npc.allay")
    @Requirements(selected = true, ownership = true, types = EntityType.ALLAY)
    public static void allay(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        AllayTrait trait = npc.getOrAddTrait(AllayTrait.class);
        String output = "";
        if (args.hasFlag('d')) {
            trait.setDancing(!trait.isDancing());
            output += ' ' + (trait.isDancing() ? Messaging.tr(Messages.ALLAY_DANCING_SET, npc.getName())
                    : Messaging.tr(Messages.ALLAY_DANCING_UNSET, npc.getName()));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
