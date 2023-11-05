package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;

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

@TraitName("parrottrait")
public class ParrotTrait extends Trait {
    @Persist
    private Variant variant = Variant.BLUE;

    public ParrotTrait() {
        super("parrottrait");
    }

    public Variant getVariant() {
        return variant;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Parrot) {
            Parrot parrot = (Parrot) npc.getEntity();
            parrot.setVariant(variant);
        }
    }

    public void setVariant(Parrot.Variant variant) {
        this.variant = variant;
    }

    @Command(
            aliases = { "npc" },
            usage = "parrot (--variant variant)",
            desc = "Sets parrot modifiers",
            modifiers = { "parrot" },
            min = 1,
            max = 1,
            permission = "citizens.npc.parrot")
    @Requirements(selected = true, ownership = true, types = EntityType.PARROT)
    public static void parrot(CommandContext args, CommandSender sender, NPC npc, @Flag("variant") Variant variant)
            throws CommandException {
        ParrotTrait trait = npc.getOrAddTrait(ParrotTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (variant == null)
                throw new CommandException(Messages.INVALID_PARROT_VARIANT, Util.listValuesPretty(Variant.values()));
            trait.setVariant(variant);
            output += Messaging.tr(Messages.PARROT_VARIANT_SET, Util.prettyEnum(variant));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}
