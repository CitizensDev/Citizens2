package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.TargetType;

import org.bukkit.Location;

public interface PathStrategy {
    Location getTargetAsLocation();

    TargetType getTargetType();

    void setSpeed(float speed);

    boolean update();
}