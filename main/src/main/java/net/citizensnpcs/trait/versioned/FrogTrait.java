package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;

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

@TraitName("frogtrait")
public class FrogTrait extends Trait {
    @Persist
    private Variant variant;

    public FrogTrait() {
        super("frogtrait");
    }

    public Variant getVariant() {
        return variant;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Frog && variant != null) {
            Frog frog = (Frog) npc.getEntity();
            frog.setVariant(variant);
        }
    }

    public void setVariant(Frog.Variant variant) {
        this.variant = variant;
    }

    @Command(
            aliases = { "npc" },
            usage = "frog (--variant variant)",
            desc = "",
            modifiers = { "frog" },
            min = 1,
            max = 1,
            permission = "citizens.npc.frog")
    @Requirements(selected = true, ownership = true, types = EntityType.FROG)
    public static void frog(CommandContext args, CommandSender sender, NPC npc, @Flag("variant") Frog.Variant variant)
            throws CommandException {
        FrogTrait trait = npc.getOrAddTrait(FrogTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (variant == null)
                throw new CommandException(Messages.INVALID_FROG_VARIANT, Util.listValuesPretty(Frog.Variant.values()));
            trait.setVariant(variant);
            output += Messaging.tr(Messages.FROG_VARIANT_SET, Util.prettyEnum(variant));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}
