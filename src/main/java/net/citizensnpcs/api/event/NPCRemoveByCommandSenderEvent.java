package net.citizensnpcs.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCRemoveByCommandSenderEvent extends NPCRemoveEvent {
    private final CommandSender source;

    public NPCRemoveByCommandSenderEvent(NPC npc, CommandSender source) {
        super(npc);
        this.source = source;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public CommandSender getSource() {
        return source;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
