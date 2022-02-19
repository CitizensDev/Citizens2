package net.citizensnpcs.api.npc;

import org.bukkit.command.CommandSender;

/**
 * Manages the 'selected {@link NPC}' for the server. {@link NPC}s can be selected using selection-item specified in the
 * config or via commands.
 */
public interface NPCSelector {
    NPC getSelected(CommandSender sender);

    void select(CommandSender sender, NPC npc);
}
