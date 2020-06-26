package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class NPCTraitCommandDetachEvent extends NPCEvent {
    private final CommandSender sender;
    private final Class<? extends Trait> traitClass;

    public NPCTraitCommandDetachEvent(NPC npc, Class<? extends Trait> traitClass, CommandSender sender) {
        super(npc);
        this.traitClass = traitClass;
        this.sender = sender;
    }

    public CommandSender getCommandSender() {
        return sender;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Class<? extends Trait> getTraitClass() {
        return traitClass;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
