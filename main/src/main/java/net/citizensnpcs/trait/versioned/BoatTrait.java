package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;

import net.citizensnpcs.api.command.Arg.CompletionsProvider.OptionalKeyedCompletions;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.util.Messages;

@TraitName("boattrait")
public class BoatTrait extends Trait {
    @Persist
    private Boat.Type type;

    public BoatTrait() {
        super("boattrait");
    }

    @Command(
            aliases = { "npc" },
            usage = "boat --type [type]",
            desc = "",
            modifiers = { "boat" },
            min = 1,
            max = 1,
            permission = "citizens.npc.boat")
    public void boat(CommandContext args, CommandSender sender, NPC npc,
            @Flag(value = "type", completionsProvider = OptionalBoatTypeCompletions.class) String stype)
            throws CommandException {
        if (stype == null)
            throw new CommandUsageException();
        Boat.Type type = Boat.Type.valueOf(stype);
        npc.getOrAddTrait(BoatTrait.class).setType(type);
        Messaging.sendTr(sender, Messages.BOAT_TYPE_SET, type);
    }

    public Boat.Type getType() {
        return type;
    }

    @Override
    public void onSpawn() {
        int[] version = SpigotUtil.getVersion();
        if (version[1] >= 21)
            return; // technically this wasn't changed until 1.21.2 but 1.21 / 1.21.1 are no longer supported
        if (npc.getEntity() instanceof Boat) {
            if (type != null) {
                ((Boat) npc.getEntity()).setBoatType(type);
            }
        }
    }

    public void setType(Boat.Type type) {
        this.type = type;
        onSpawn();
    }

    public static class OptionalBoatTypeCompletions extends OptionalKeyedCompletions {
        public OptionalBoatTypeCompletions() {
            super("org.bukkit.entity.Boat.Type");
        }
    }
}