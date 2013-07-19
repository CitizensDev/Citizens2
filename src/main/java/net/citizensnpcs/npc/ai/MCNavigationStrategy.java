package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.Navigation;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;

public class MCNavigationStrategy extends AbstractPathStrategy {
    private final Navigation navigation;
    private final NavigatorParameters parameters;
    private final Location target;

    MCNavigationStrategy(final NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.target = dest;
        this.parameters = params;
        EntityLiving handle = ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
        handle.onGround = true;
        // not sure of a better way around this - if onGround is false, then
        // navigation won't execute, and calling entity.move doesn't
        // entirely fix the problem.
        navigation = handle.getNavigation();
        navigation.a(parameters.avoidWater());
        navigation.a(dest.getX(), dest.getY(), dest.getZ(), parameters.speed());
        if (navigation.f())
            setCancelReason(CancelReason.STUCK);
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
        if (getCancelReason() != null)
            return true;
        navigation.a(parameters.avoidWater());
        navigation.a(parameters.speed());
        return navigation.f();
    }
}
