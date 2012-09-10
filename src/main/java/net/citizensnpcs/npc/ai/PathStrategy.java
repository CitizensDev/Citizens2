package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;

import org.bukkit.Location;

public interface PathStrategy {
    void clearCancelReason();

    CancelReason getCancelReason();

    Location getTargetAsLocation();

    TargetType getTargetType();

    void stop();

    boolean update();
}