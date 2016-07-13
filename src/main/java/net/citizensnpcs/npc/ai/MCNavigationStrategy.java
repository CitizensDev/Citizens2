package net.citizensnpcs.npc.ai;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_10_R1.EntityHorse;
import net.minecraft.server.v1_10_R1.NavigationAbstract;

public class MCNavigationStrategy extends AbstractPathStrategy {
    private final Entity handle;
    private float lastSpeed;
    private final NavigationAbstract navigation;
    private final NavigatorParameters parameters;
    private final Location target;

    MCNavigationStrategy(final NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.target = dest;
        this.parameters = params;
        this.lastSpeed = parameters.speed();
        handle = npc.getEntity();
        net.minecraft.server.v1_10_R1.Entity raw = NMS.getHandle(handle);
        raw.onGround = true;
        // not sure of a better way around this - if onGround is false, then
        // navigation won't execute, and calling entity.move doesn't
        // entirely fix the problem.
        navigation = NMS.getNavigation(npc.getEntity());
        float oldWidth = raw.width;
        if (raw instanceof EntityHorse) {
            raw.width = Math.min(0.99f, oldWidth);
        }
        navigation.a(dest.getX(), dest.getY(), dest.getZ(), parameters.speed());
        raw.width = oldWidth; // minecraft requires that an entity fit onto both blocks if width >= 1f, but we'd
                              // prefer to make it just fit on 1 so hack around it a bit.
        if (NMS.isNavigationFinished(navigation)) {
            setCancelReason(CancelReason.STUCK);
        }
    }

    private double distanceSquared() {
        return handle.getLocation(HANDLE_LOCATION).distanceSquared(target);
    }

    @Override
    public Location getTargetAsLocation() {
        return target;
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.LOCATION;
    }

    @Override
    public void stop() {
        NMS.stopNavigation(navigation);
    }

    @Override
    public String toString() {
        return "MCNavigationStrategy [target=" + target + "]";
    }

    @Override
    public boolean update() {
        if (getCancelReason() != null)
            return true;
        if (parameters.speed() != lastSpeed) {
            Messaging.debug("Repathfinding " + ((NPCHolder) handle).getNPC().getId() + " due to speed change");
            navigation.a(target.getX(), target.getY(), target.getZ(), parameters.speed());
            lastSpeed = parameters.speed();
        }
        navigation.a(parameters.speed());
        parameters.run();
        if (distanceSquared() < parameters.distanceMargin()) {
            stop();
            return true;
        }
        return NMS.isNavigationFinished(navigation);
    }

    private static final Location HANDLE_LOCATION = new Location(null, 0, 0, 0);
}
