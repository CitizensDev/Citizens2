package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.util.Messages;

@TraitName("commandtrait")
public class CommandTrait extends Trait {
    @Persist
    @DelegatePersistence(NPCCommandPersister.class)
    private final Map<String, NPCCommand> commands = Maps.newHashMap();
    @Persist
    @DelegatePersistence(PlayerNPCCommandPersister.class)
    private final Map<String, PlayerNPCCommand> cooldowns = Maps.newHashMap();

    public CommandTrait() {
        super("commandtrait");
    }

    public int addCommand(String command, Hand hand, boolean player, boolean op, int cooldown, List<String> perms,
            int n) {
        int id = getNewId();
        commands.put(String.valueOf(id),
                new NPCCommand(String.valueOf(id), command, hand, player, op, cooldown, perms, n));
        return id;
    }

    /**
     * Send a brief description of the current state of the trait to the supplied {@link CommandSender}.
     */
    public void describe(CommandSender sender) {
        List<NPCCommand> left = Lists.newArrayList();
        List<NPCCommand> right = Lists.newArrayList();
        for (NPCCommand command : commands.values()) {
            if (command.hand == Hand.LEFT || command.hand == Hand.BOTH) {
                left.add(command);
            }
            if (command.hand == Hand.RIGHT || command.hand == Hand.BOTH) {
                right.add(command);
            }
        }
        String output = "";
        if (left.size() > 0) {
            output += Messaging.tr(Messages.COMMAND_LEFT_HAND_HEADER);
            for (NPCCommand command : left) {
                output += describe(command);
            }
        }
        if (right.size() > 0) {
            output += Messaging.tr(Messages.COMMAND_RIGHT_HAND_HEADER);
            for (NPCCommand command : right) {
                output += describe(command);
            }
        }
        if (output.isEmpty()) {
            output = Messaging.tr(Messages.COMMAND_NO_COMMANDS_ADDED);
        }
        Messaging.send(sender, output);
    }

    private String describe(NPCCommand command) {
        String output = "<br>    - [" + command.id + "]: " + command.command + " [" + command.cooldown + "s]";
        if (command.op) {
            output += " -o";
        }
        if (command.player) {
            output += " -p";
        }
        return output;
    }

    public void dispatch(final Player player, final Hand hand) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (NPCCommand command : commands.values()) {
                    if (command.hand != hand && command.hand != Hand.BOTH)
                        continue;
                    PlayerNPCCommand info = cooldowns.get(player.getUniqueId().toString());
                    if (info != null && !info.canUse(player, command)) {
                        continue;
                    }
                    command.run(npc, player);
                    if (command.cooldown > 0 && info == null) {
                        cooldowns.put(player.getUniqueId().toString(), new PlayerNPCCommand(command));
                    }
                }
            }
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), task);
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
        BOTH,
        LEFT,
        RIGHT;
    }

    private static class NPCCommand {
        String bungeeServer;
        String command;
        int cooldown;
        Hand hand;
        String id;
        int n;
        boolean op;
        List<String> perms;
        boolean player;

        public NPCCommand(String id, String command, Hand hand, boolean player, boolean op, int cooldown,
                List<String> perms, int n) {
            this.id = id;
            this.command = command;
            this.hand = hand;
            this.player = player;
            this.op = op;
            this.cooldown = cooldown;
            this.perms = perms;
            this.n = n;
            List<String> split = Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(command);
            this.bungeeServer = split.size() == 2 && split.get(0).equalsIgnoreCase("server") ? split.get(1) : null;
        }

        public void run(NPC npc, Player clicker) {
            String interpolatedCommand = Placeholders.replace(command, clicker, npc);
            if (player) {
                boolean wasOp = clicker.isOp();
                if (op) {
                    clicker.setOp(true);
                }
                if (bungeeServer != null) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(bungeeServer);

                    clicker.sendPluginMessage(CitizensAPI.getPlugin(), "BungeeCord", out.toByteArray());
                }
                try {
                    clicker.chat("/" + interpolatedCommand);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (op) {
                    clicker.setOp(wasOp);
                }
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), interpolatedCommand);
            }
        }
    }

    private static class NPCCommandPersister implements Persister<NPCCommand> {
        public NPCCommandPersister() {
        }

        @Override
        public NPCCommand create(DataKey root) {
            List<String> perms = Lists.newArrayList();
            for (DataKey key : root.getRelative("permissions").getIntegerSubKeys()) {
                perms.add(key.getString(""));
            }
            return new NPCCommand(root.name(), root.getString("command"), Hand.valueOf(root.getString("hand")),
                    Boolean.valueOf(root.getString("player")), Boolean.valueOf(root.getString("op")),
                    root.getInt("cooldown"), perms, root.getInt("n"));
        }

        @Override
        public void save(NPCCommand instance, DataKey root) {
            root.setString("command", instance.command);
            root.setString("hand", instance.hand.name());
            root.setBoolean("player", instance.player);
            root.setBoolean("op", instance.op);
            root.setInt("cooldown", instance.cooldown);
            root.setInt("n", instance.n);
            for (int i = 0; i < instance.perms.size(); i++) {
                root.setString("permissions." + i, instance.perms.get(i));
            }
        }
    }

    private static class PlayerNPCCommand {
        @Persist
        Map<String, Long> lastUsed = Maps.newHashMap();
        @Persist
        Map<String, Integer> nUsed = Maps.newHashMap();

        public PlayerNPCCommand() {
        }

        public PlayerNPCCommand(NPCCommand command) {
            lastUsed.put(command.command, ((Number) (System.currentTimeMillis() / 1000)).longValue());
        }

        public boolean canUse(Player player, NPCCommand command) {
            for (String perm : command.perms) {
                if (!player.hasPermission(perm)) {
                    return false;
                }
            }
            long currentTimeSec = System.currentTimeMillis() / 1000;
            if (lastUsed.containsKey(command.command)) {
                if (currentTimeSec < ((Number) (lastUsed.get(command.command)
                        + ((Number) command.cooldown).longValue())).longValue()) {
                    return false;
                }
                lastUsed.remove(command.command);
            }
            int previouslyUsed = nUsed.getOrDefault(command.command, 0);
            if (command.n > 0 && command.n <= previouslyUsed) {
                return false;
            }
            if (command.cooldown > 0) {
                lastUsed.put(command.command, currentTimeSec);
            }
            if (command.n > 0) {
                nUsed.put(command.command, previouslyUsed + 1);
            }
            return true;
        }
    }

    private static class PlayerNPCCommandPersister implements Persister<PlayerNPCCommand> {
        public PlayerNPCCommandPersister() {
        }

        @Override
        public PlayerNPCCommand create(DataKey root) {
            return PersistenceLoader.load(PlayerNPCCommand.class, root);
        }

        @Override
        public void save(PlayerNPCCommand instance, DataKey root) {
            PersistenceLoader.save(instance, root);
        }
    }
}