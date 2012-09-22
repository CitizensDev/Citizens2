package net.citizensnpcs.api.npc;

import org.bukkit.command.CommandSender;

public interface NPCSelector {
    /**
     * Fetches the current {@link NPC} selected by the command sender, if any.
     * Returns null if no NPC is currently selected.
     * 
     * @param sender
     *            The sender to use when searching
     * @return The currently selected NPC
     */
    NPC getSelected(CommandSender sender);

    /**
     * Selects the given {@link NPC}.
     * 
     * @param sender
     *            The selector
     * @param npc
     *            The NPC to select
     */
    void select(CommandSender sender, NPC npc);
}