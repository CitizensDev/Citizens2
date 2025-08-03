package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sniffer;

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

@TraitName("sniffertrait")
public class SnifferTrait extends Trait {
    @Persist
    private SnifferState state = null;

    public SnifferTrait() {
        super("sniffertrait");
    }

    public SnifferState getState() {
        return state;
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof Sniffer))
            return;
        NMS.setSnifferState(npc.getEntity(), state);
    }

    public void setState(SnifferState state) {
        this.state = state;
    }

    public enum SnifferState {
        DIGGING,
        FEELING_HAPPY,
        IDLING,
        RISING,
        SCENTING,
        SEARCHING,
        SNIFFING
    }

    @Command(
            aliases = { "npc" },
            usage = "sniffer (--state [state])",
            desc = "",
            modifiers = { "sniffer" },
            min = 1,
            max = 1,
            permission = "citizens.npc.sniffer")
    @Requirements(selected = true, ownership = true, types = EntityType.SNIFFER)
    public static void sniffer(CommandContext args, CommandSender sender, NPC npc, @Flag("state") SnifferState state)
            throws CommandException {
        SnifferTrait trait = npc.getOrAddTrait(SnifferTrait.class);
        String output = "";
        if (state != null) {
            trait.setState(state);
            output += ' ' + Messaging.tr(Messages.SNIFFER_STATE_SET, npc.getName(), state);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
