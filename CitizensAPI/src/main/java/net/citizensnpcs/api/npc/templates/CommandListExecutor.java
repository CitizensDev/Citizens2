package net.citizensnpcs.api.npc.templates;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Placeholders;

public class CommandListExecutor implements Consumer<NPC> {
    private final List<String> commands;

    public CommandListExecutor(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void accept(NPC npc) {
        for (String command : commands) {
            String cmd = command;
            if (command.startsWith("say")) {
                cmd = "npc speak " + command.replaceFirst("say", "").trim() + " --target <p>";
            }
            if ((cmd.startsWith("npc ") || cmd.startsWith("waypoints ") || cmd.startsWith("wp "))
                    && !cmd.contains("--id ")) {
                cmd += " --id <id>";
            }
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Placeholders.replace(cmd, null, npc));
        }
    }
}
