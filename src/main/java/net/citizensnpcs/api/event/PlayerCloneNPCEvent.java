package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class PlayerCloneNPCEvent extends PlayerCreateNPCEvent implements Cancellable {
    private final NPC npc;

    public PlayerCloneNPCEvent(Player player, NPC npc, NPC copy) {
        super(player, copy);
        this.npc = npc;
    }

    /**
     * @return The {@link Player} creating the NPC.
     */
    @Override
    public Player getCreator() {
        return super.getCreator();
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
