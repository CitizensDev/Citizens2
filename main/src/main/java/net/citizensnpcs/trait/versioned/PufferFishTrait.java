package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
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

@TraitName("pufferfishtrait")
public class PufferFishTrait extends Trait {
    @Persist
    private int puffState = 0;

    public PufferFishTrait() {
        super("pufferfishtrait");
    }

    public int getPuffState() {
        return puffState;
    }

    public void setPuffState(int state) {
        puffState = state;
    }

    @Command(
            aliases = { "npc" },
            usage = "pufferfish (--state state)",
            desc = "",
            modifiers = { "pufferfish" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pufferfish")
    @Requirements(selected = true, ownership = true, types = EntityType.PUFFERFISH)
    public static void pufferfish(CommandContext args, CommandSender sender, NPC npc, @Flag("state") Integer state)
            throws CommandException {
        PufferFishTrait trait = npc.getOrAddTrait(PufferFishTrait.class);
        String output = "";
        if (state != null) {
            state = Math.min(Math.max(state, 0), 3);
            trait.setPuffState(state);
            output += Messaging.tr(Messages.PUFFERFISH_STATE_SET, state);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}
