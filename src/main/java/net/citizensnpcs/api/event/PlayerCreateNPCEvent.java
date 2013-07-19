package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerCreateNPCEvent extends CommandSenderCreateNPCEvent implements Cancellable {
    public PlayerCreateNPCEvent(Player player, NPC npc) {
        super(player, npc);
    }

    /**
     * @return The {@link Player} creating the NPC.
     */
    @Override
    public Player getCreator() {
        return (Player) super.getCreator();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
