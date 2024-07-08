package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("hometrait")
public class HomeTrait extends Trait {
    @Persist
    private int delay = -1;
    @Persist
    private double distance = -1;
    @Persist
    private Location location;
    @Persist
    private ReturnStrategy strategy = ReturnStrategy.TELEPORT;
    private int t;

    public HomeTrait() {
        super("hometrait");
    }

    public int getDelayTicks() {
        return delay;
    }

    public double getDistanceBlocks() {
        return distance;
    }

    public Location getHomeLocation() {
        return location.clone();
    }

    public ReturnStrategy getReturnStrategy() {
        return strategy;
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || location == null || npc.getNavigator().isNavigating()
                || npc.getStoredLocation().distance(location) < 0.1) {
            t = 0;
            return;
        }
        t++;
        if (t > delay || delay == -1) {
            if (distance == -1 || npc.getStoredLocation().distance(location) >= distance) {
                if (strategy == ReturnStrategy.TELEPORT) {
                    npc.teleport(location, TeleportCause.PLUGIN);
                } else if (strategy == ReturnStrategy.PATHFIND) {
                    npc.getNavigator().setTarget(location);
                    npc.getNavigator().getLocalParameters().distanceMargin(0.9).pathDistanceMargin(0)
                            .destinationTeleportMargin(1);
                }
            }
        }
    }

    public void setDelayTicks(int delay) {
        this.delay = delay;
    }

    public void setDistanceBlocks(double distance) {
        this.distance = distance;
    }

    public void setHomeLocation(Location location) {
        this.location = location.clone();
    }

    public void setReturnStrategy(ReturnStrategy strategy) {
        this.strategy = strategy;
    }

    public static enum ReturnStrategy {
        PATHFIND,
        TELEPORT
    }
}