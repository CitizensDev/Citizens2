package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.util.Vector;

public interface FlockBehavior {
    Vector getVector(NPC npc, Collection<NPC> nearby);
}
