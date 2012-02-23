package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents an event thrown by an NPC
 */
public class NPCEvent extends CitizensEvent {
    private final NPC npc;

    protected NPCEvent(NPC npc) {
        super();
        this.npc = npc;
    }

    /**
     * Get the npc involved in the event
     * 
     * @return the npc involved in the event
     */
    public NPC getNPC() {
        return npc;
    }
}