package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.Navigation;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MCNavigationStrategy implements PathStrategy {
    private final Navigation navigation;
    private final NavigatorParameters parameters;
    private final Location target;

    MCNavigationStrategy(final CitizensNPC npc, Location dest, NavigatorParameters params) {
        this(npc.getHandle(), dest, params);
        navigation.a(dest.getX(), dest.getY(), dest.getZ(), parameters.speed());
    }

    private MCNavigationStrategy(EntityLiving entity, Location target, NavigatorParameters params) {
        this.target = target;
        this.parameters = params;
        if (entity.getBukkitEntity() instanceof Player) {
            entity.onGround = true;
            // not sure of a better way around this - if onGround is false, then
            // navigation won't execute, and calling entity.move doesn't
            // entirely fix the problem.
        }
        navigation = entity.getNavigation();
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
    public boolean update() {
        navigation.a(parameters.avoidWater());
        navigation.a(parameters.speed());
        return navigation.f();
    }
}
