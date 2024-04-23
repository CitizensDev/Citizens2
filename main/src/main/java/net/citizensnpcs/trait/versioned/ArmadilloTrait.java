package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Armadillo;
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
import net.citizensnpcs.util.NMS;

@TraitName("armadillotrait")
public class ArmadilloTrait extends Trait {
    @Persist
    private ArmadilloState state = ArmadilloState.IDLE;

    public ArmadilloTrait() {
        super("armadillotrait");
    }

    public ArmadilloState getState() {
        return state;
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof Armadillo))
            return;
        NMS.setArmadilloState(npc.getEntity(), state);
    }

    public void setState(ArmadilloState state) {
        this.state = state;
    }

    public enum ArmadilloState {
        IDLE,
        ROLLING_OUT,
        ROLLING_UP,
        SCARED;
    }

    @Command(
            aliases = { "npc" },
            usage = "armadillo --state [state]",
            desc = "",
            modifiers = { "armadillo" },
            min = 1,
            max = 1,
            flags = "",
            permission = "citizens.npc.armadillo")
    @Requirements(selected = true, ownership = true, types = EntityType.ARMADILLO)
    public static void allay(CommandContext args, CommandSender sender, NPC npc, @Flag("state") ArmadilloState state)
            throws CommandException {
        ArmadilloTrait trait = npc.getOrAddTrait(ArmadilloTrait.class);
        String output = "";
        if (state != null) {
            trait.setState(state);
            output += Messaging.tr(Messages.ARMADILLO_STATE_SET, state);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
