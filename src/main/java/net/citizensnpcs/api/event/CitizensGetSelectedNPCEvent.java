package net.citizensnpcs.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class CitizensGetSelectedNPCEvent extends CitizensEvent {
    private NPC selected;
    private final CommandSender sender;

    public CitizensGetSelectedNPCEvent(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public NPC getSelected() {
        return selected;
    }

    public CommandSender getSender() {
        return sender;
    }

    public void setSelected(NPC npc) {
        this.selected = npc;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
