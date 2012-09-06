package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Navigation;

import org.bukkit.Location;

public class MCNavigationStrategy implements PathStrategy {
    private CancelReason cancelReason;
    private final Navigation navigation;
    private final NavigatorParameters parameters;
    private final Location target;

    MCNavigationStrategy(final CitizensNPC npc, Location dest, NavigatorParameters params) {
        this.target = dest;
        this.parameters = params;
        if (npc.getHandle() instanceof EntityPlayer) {
            npc.getHandle().onGround = true;
            // not sure of a better way around this - if onGround is false, then
            // navigation won't execute, and calling entity.move doesn't
            // entirely fix the problem.
        }
        navigation = npc.getHandle().getNavigation();
        navigation.a(parameters.avoidWater());
        navigation.a(dest.getX(), dest.getY(), dest.getZ(), parameters.speed());
        if (navigation.f())
            cancelReason = CancelReason.STUCK;
    }

    @Override
    public CancelReason getCancelReason() {
        return cancelReason;
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
        navigation.g();
    }

    @Override
    public boolean update() {
        if (cancelReason != null)
            return true;
        navigation.a(parameters.avoidWater());
        navigation.a(parameters.speed());
        return navigation.f();
    }
}
