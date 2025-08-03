package net.citizensnpcs.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class CommandSenderCloneNPCEvent extends CommandSenderCreateNPCEvent {
    private final NPC npc;

    public CommandSenderCloneNPCEvent(CommandSender sender, NPC npc, NPC copy) {
        super(sender, copy);
        this.npc = npc;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public NPC getOriginal() {
        return npc;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
