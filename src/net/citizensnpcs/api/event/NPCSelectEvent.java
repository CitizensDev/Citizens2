package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC is selected by a player
 */
public class NPCSelectEvent extends NPCEvent {
    private final Player player;

    public NPCSelectEvent(NPC npc, Player player) {
        super(npc);
        this.player = player;
    }

    /**
     * Gets the player that selected an NPC
     * 
     * @return Player that selected an NPC
     */
    public Player getPlayer() {
        return player;
    }
}