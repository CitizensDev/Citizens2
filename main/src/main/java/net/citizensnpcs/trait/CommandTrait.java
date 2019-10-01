package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

@TraitName("commandtrait")
public class CommandTrait extends Trait {
    @Persist
    @DelegatePersistence(NPCCommandPersister.class)
    private final Map<String, NPCCommand> commands = Maps.newHashMap();

    public CommandTrait() {
        super("commandtrait");
    }

    public int addCommand(String command, Hand hand) {
        int id = getNewId();
        commands.put(String.valueOf(id), new NPCCommand(String.valueOf(id), command, hand));
        return id;
    }

    /**
     * Send a brief description of the current state of the trait to the supplied {@link CommandSender}.
     */
    public void describe(CommandSender sender) {
        List<NPCCommand> left = Lists.newArrayList();
        List<NPCCommand> right = Lists.newArrayList();
        for (NPCCommand command : commands.values()) {
            if (command.hand == Hand.LEFT) {
                left.add(command);
            } else {
                right.add(command);
            }
        }
        String output = "";
        if (left.size() > 0) {
            output += Messaging.tr(Messages.COMMAND_LEFT_HAND_HEADER);
            for (NPCCommand command : left) {
                output += "<br>    - [" + command.id + "]: " + command.command;
            }
        }
        if (right.size() > 0) {
            output += Messaging.tr(Messages.COMMAND_RIGHT_HAND_HEADER);
            for (NPCCommand command : right) {
                output += "<br>    - [" + command.id + "]: " + command.command;
            }
        }
        if (output.isEmpty()) {
            output = Messaging.tr(Messages.COMMAND_NO_COMMANDS_ADDED);
        }
        Messaging.send(sender, output);
    }

    public void dispatch(Player player, Hand hand) {
        for (NPCCommand command : commands.values()) {
            if (command.hand != hand)
                continue;
            command.run(npc, player);
        }
    }

    private int getNewId() {
        int i = 0;
        while (commands.containsKey(String.valueOf(i))) {
            i++;
        }
        return i;
    }

    public boolean hasCommandId(int id) {
        return commands.containsKey(String.valueOf(id));
    }

    public void removeCommandById(int id) {
        commands.remove(String.valueOf(id));
    }

    public static enum Hand {
        LEFT,
        RIGHT;
    }

    private static class NPCCommand {
        String command;
        Hand hand;
        String id;

        public NPCCommand(String id, String command, Hand hand) {
            this.id = id;
            this.command = command;
            this.hand = hand;
        }

        public void run(NPC npc, Player clicker) {
            String interpolatedCommand = command.replace("<npc>", npc.getName()).replace("<p>", clicker.getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), interpolatedCommand);
        }
    }

    private static class NPCCommandPersister implements Persister<NPCCommand> {
        public NPCCommandPersister() {
        }

        @Override
        public NPCCommand create(DataKey root) {
            return new NPCCommand(root.name(), root.getString("command"), Hand.valueOf(root.getString("hand")));
        }

        @Override
        public void save(NPCCommand instance, DataKey root) {
            root.setString("command", instance.command);
            root.setString("hand", instance.hand.name());
        }
    }
}