package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;

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

@TraitName("chickentrait")
public class ChickenTrait extends Trait {
    @Persist
    private Chicken.Variant variant;

    public ChickenTrait() {
        super("chickentrait");
    }

    public Chicken.Variant getVariant() {
        return variant;
    }

    @Override
    public void run() {
        if (variant != null && npc.getEntity() instanceof Chicken) {
            Chicken chicken = (Chicken) npc.getEntity();
            chicken.setVariant(variant);
        }
    }

    public void setVariant(Chicken.Variant variant) {
        this.variant = variant;
    }

    @Command(
            aliases = { "npc" },
            usage = "chicken (--variant variant)",
            desc = "",
            modifiers = { "chicken" },
            min = 1,
            max = 1,
            permission = "citizens.npc.chicken")
    @Requirements(selected = true, ownership = true, types = EntityType.CHICKEN)
    public static void chicken(CommandContext args, CommandSender sender, NPC npc,
            @Flag("variant") Chicken.Variant variant) throws CommandException {
        ChickenTrait trait = npc.getOrAddTrait(ChickenTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (variant == null)
                throw new CommandException(Messages.INVALID_PIG_VARIANT, Util.listValuesPretty(Chicken.Variant.class));
            trait.setVariant(variant);
            output += Messaging.tr(Messages.PIG_VARIANT_SET, variant.getKey().getKey());
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}