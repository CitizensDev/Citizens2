package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SulfurCube;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;

@TraitName("sulfurcubetrait")
public class SulfurCubeTrait extends Trait {

    public SulfurCubeTrait() {
        super("sulfurcubetrait");
    }

    @Override
    public void run() {
        if (npc.getCosmeticEntity() instanceof SulfurCube) {
            SulfurCube cube = (SulfurCube) npc.getCosmeticEntity();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "sulfurcube",
            desc = "",
            modifiers = { "sulfurcube" },
            min = 1,
            max = 1,
            permission = "citizens.npc.sulfurcube")
    @Requirements(selected = true, ownership = true, cosmeticTypes = EntityType.SULFUR_CUBE)
    public static void sulfurcube(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        SulfurCubeTrait trait = npc.getOrAddTrait(SulfurCubeTrait.class);
        String output = "";
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}