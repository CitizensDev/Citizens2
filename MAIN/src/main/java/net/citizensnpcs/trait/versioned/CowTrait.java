package net.citizensnpcs.trait.versioned;

import java.lang.invoke.MethodHandle;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Cow;
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
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

@TraitName("cowtrait")
public class CowTrait extends Trait {
    @Persist
    private Cow.Variant variant;

    public CowTrait() {
        super("cowtrait");
    }

    public Cow.Variant getVariant() {
        return variant;
    }

    @Override
    public void run() {
        if (variant != null && npc.getEntity() instanceof Cow) {
            Cow cow = (Cow) npc.getEntity();
            try {
                COW_VARIANT_SETTER.invoke(cow, variant);
            } catch (Throwable e) {
                Messaging.severe("Failed to set cow variant");
                e.printStackTrace();
            }
        }
    }

    public void setVariant(Cow.Variant variant) {
        this.variant = variant;
    }

    @Command(
            aliases = { "npc" },
            usage = "cow (--variant variant)",
            desc = "",
            modifiers = { "cow" },
            min = 1,
            max = 1,
            permission = "citizens.npc.cow")
    @Requirements(selected = true, ownership = true, types = EntityType.COW)
    public static void cow(CommandContext args, CommandSender sender, NPC npc, @Flag("variant") Cow.Variant variant)
            throws CommandException {
        CowTrait trait = npc.getOrAddTrait(CowTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (variant == null)
                throw new CommandException(Messages.INVALID_PIG_VARIANT, Util.listValuesPretty(Cow.Variant.class));
            trait.setVariant(variant);
            output += Messaging.tr(Messages.PIG_VARIANT_SET, variant.getKey().getKey());
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }

    private static MethodHandle COW_VARIANT_SETTER;
    static {
        try {
            COW_VARIANT_SETTER = NMS.getMethodHandle(Class.forName("org.bukkit.entity.Cow"), "setVariant", true,
                    Cow.Variant.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}