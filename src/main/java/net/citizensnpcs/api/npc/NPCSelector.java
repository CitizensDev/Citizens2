package net.citizensnpcs.api.npc;

import org.bukkit.command.CommandSender;

public interface NPCSelector {
    NPC getSelected(CommandSender sender);
}
