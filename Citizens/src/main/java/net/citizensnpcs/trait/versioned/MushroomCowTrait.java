package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;

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
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

@TraitName("mushroomcowtrait")
public class MushroomCowTrait extends Trait {
    @Persist("variant")
    private Variant variant;

    public MushroomCowTrait() {
        super("mushroomcowtrait");
    }

    public Variant getVariant() {
        return variant;
    }

    @Override
    public void onSpawn() {
        setVariant(variant);
    }

    @Override
    public void run() {
        if (variant != null && npc.getEntity() instanceof MushroomCow) {
            ((MushroomCow) npc.getEntity()).setVariant(variant);
        }
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    @Command(
            aliases = { "npc" },
            usage = "mushroomcow (--variant [variant])",
            desc = "",
            modifiers = { "mushroomcow", "mooshroom" },
            min = 1,
            max = 1,
            permission = "citizens.npc.mushroomcow")
    @Requirements(selected = true, ownership = true)
    public static void mushroomcow(CommandContext args, CommandSender sender, NPC npc,
            @Flag("variant") MushroomCow.Variant variant) throws CommandException {
        if (!npc.getOrAddTrait(MobType.class).getType().name().equals("MOOSHROOM")
                && !npc.getOrAddTrait(MobType.class).getType().name().equals("MUSHROOM_COW"))
            throw new CommandUsageException();
        MushroomCowTrait trait = npc.getOrAddTrait(MushroomCowTrait.class);
        boolean hasArg = false;
        if (args.hasValueFlag("variant")) {
            if (variant == null) {
                Messaging.sendErrorTr(sender, Messages.INVALID_MUSHROOM_COW_VARIANT,
                        Util.listValuesPretty(MushroomCow.Variant.values()));
                return;
            }
            trait.setVariant(variant);
            Messaging.sendTr(sender, Messages.MUSHROOM_COW_VARIANT_SET, npc.getName(), variant);
            hasArg = true;
        }
        if (!hasArg)
            throw new CommandUsageException();
    }
}
