package net.citizensnpcs.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC is selected by a player.
 */
public class NPCSelectEvent extends NPCEvent {
    private final CommandSender sender;

    public NPCSelectEvent(NPC npc, CommandSender sender) {
        super(npc);
        this.sender = sender;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the selector of the NPC.
     *
     * @return CommandSender that selected an NPC
     */
    public CommandSender getSelector() {
        return sender;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}