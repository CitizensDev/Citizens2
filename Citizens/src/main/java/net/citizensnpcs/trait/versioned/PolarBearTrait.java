package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PolarBear;

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

@TraitName("polarbeartrait")
public class PolarBearTrait extends Trait {
    @Persist
    private boolean rearing;

    public PolarBearTrait() {
        super("polarbeartrait");
    }

    public boolean isRearing() {
        return rearing;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof PolarBear) {
            NMS.setPolarBearRearing(npc.getEntity(), rearing);
        }
    }

    public void setRearing(boolean rearing) {
        this.rearing = rearing;
    }

    @Command(
            aliases = { "npc" },
            usage = "polarbear (-r)",
            desc = "",
            modifiers = { "polarbear" },
            min = 1,
            max = 1,
            flags = "r",
            permission = "citizens.npc.polarbear")
    @Requirements(selected = true, ownership = true, types = { EntityType.POLAR_BEAR })
    public static void polarbear(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        PolarBearTrait trait = npc.getOrAddTrait(PolarBearTrait.class);
        String output = "";
        if (args.hasFlag('r')) {
            trait.setRearing(!trait.isRearing());
            output += Messaging.tr(
                    trait.isRearing() ? Messages.POLAR_BEAR_REARING : Messages.POLAR_BEAR_STOPPED_REARING,
                    npc.getName());
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        } else
            throw new CommandUsageException();
    }
}
