package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

@TraitName("pigtrait")
public class PigTrait extends Trait {
    @Persist
    private Pig.Variant variant;

    public PigTrait() {
        super("pigtrait");
    }

    public Pig.Variant getVariant() {
        return variant;
    }

    @Override
    public void run() {
        if (variant != null && npc.getEntity() instanceof Pig) {
            Pig pig = (Pig) npc.getEntity();
            pig.setVariant(variant);
        }
    }

    public void setVariant(Pig.Variant variant) {
        this.variant = variant;
    }

    @Command(
            aliases = { "npc" },
            usage = "pig (--variant variant)",
            desc = "",
            modifiers = { "pig" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pig")
    @Requirements(selected = true, ownership = true, types = EntityType.PIG)
    public static void pig(CommandContext args, CommandSender sender, NPC npc, @Flag("variant") Pig.Variant variant)
            throws CommandException {
        PigTrait trait = npc.getOrAddTrait(PigTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (variant == null)
                throw new CommandException(Messages.INVALID_PIG_VARIANT, Util.listValuesPretty(Pig.Variant.class));
            trait.setVariant(variant);
            output += Messaging.tr(Messages.PIG_VARIANT_SET, variant.getKey().getKey());
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}