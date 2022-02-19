package net.citizensnpcs.commands.history;

import org.bukkit.command.CommandSender;

import net.citizensnpcs.api.npc.NPCSelector;

public interface CommandHistoryItem {
    void undo(CommandSender sender, NPCSelector selector);
}
