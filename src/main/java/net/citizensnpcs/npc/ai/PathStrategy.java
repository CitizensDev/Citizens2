package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.TargetType;

import org.bukkit.Location;

public interface PathStrategy {
    boolean update();

    Location getTargetAsLocation();

    TargetType getTargetType();
}