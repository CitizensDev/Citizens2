package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.npc.NPC;

public interface StuckAction {
    /**
     * Called when the {@link Navigator} reports that it is stuck.
     * 
     * @param npc
     *            The stuck {@link NPC}
     * @param navigator
     *            The navigator
     * @return Whether to continue navigation
     */
    boolean run(NPC npc, Navigator navigator);
}
