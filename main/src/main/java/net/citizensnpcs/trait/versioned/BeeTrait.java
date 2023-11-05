package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;

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

@TraitName("beetrait")
public class BeeTrait extends Trait {
    @Persist
    private int anger;
    @Persist
    private boolean nectar = false;
    @Persist
    private boolean stung = false;

    public BeeTrait() {
        super("beetrait");
    }

    public boolean hasNectar() {
        return nectar;
    }

    public boolean hasStung() {
        return stung;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Bee) {
            Bee bee = (Bee) npc.getEntity();
            bee.setHasStung(stung);
            bee.setAnger(anger);
            bee.setHasNectar(nectar);
        }
    }

    public void setAnger(int anger) {
        this.anger = anger;
    }

    public void setNectar(boolean nectar) {
        this.nectar = nectar;
    }

    public void setStung(boolean stung) {
        this.stung = stung;
    }

    @Command(
            aliases = { "npc" },
            usage = "bee (-s/-n) --anger anger",
            desc = "Sets bee modifiers",
            modifiers = { "bee" },
            min = 1,
            max = 1,
            flags = "sn",
            permission = "citizens.npc.bee")
    @Requirements(selected = true, ownership = true, types = EntityType.BEE)
    public static void bee(CommandContext args, CommandSender sender, NPC npc, @Flag("anger") Integer anger)
            throws CommandException {
        BeeTrait trait = npc.getOrAddTrait(BeeTrait.class);
        String output = "";
        if (anger != null) {
            if (anger < 0)
                throw new CommandException(Messages.INVALID_BEE_ANGER);
            trait.setAnger(anger);
            output += ' ' + Messaging.tr(Messages.BEE_ANGER_SET, args.getFlag("anger"));
        }
        if (args.hasFlag('s')) {
            trait.setStung(!trait.hasStung());
            output += ' ' + (trait.hasStung() ? Messaging.tr(Messages.BEE_STUNG, npc.getName())
                    : Messaging.tr(Messages.BEE_NOT_STUNG, npc.getName()));
        }
        if (args.hasFlag('n')) {
            trait.setNectar(!trait.hasNectar());
            output += ' ' + (trait.hasNectar() ? Messaging.tr(Messages.BEE_HAS_NECTAR, npc.getName())
                    : Messaging.tr(Messages.BEE_NO_NECTAR, npc.getName()));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
