package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.Navigation;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MCNavigationStrategy implements PathStrategy {
    private final Navigation navigation;
    private EntityHumanNPC entity = null;

    MCNavigationStrategy(CitizensNPC npc, Location dest) {
        if (npc.getBukkitEntity() instanceof Player) {
            entity = (EntityHumanNPC) npc.getHandle();
            entity.onGround = true;
            // not sure of a better way around this - if onGround is false, then
            // navigation won't execute, and calling entity.move doesn't
            // entirely fix the problem.
        }
        navigation = npc.getHandle().ak();
        navigation.a(dest.getX(), dest.getY(), dest.getZ(), getSpeed(npc.getHandle()));

    }

    MCNavigationStrategy(EntityLiving entity, EntityLiving target) {
        if (entity instanceof EntityHumanNPC) {
            this.entity = (EntityHumanNPC) entity;
            entity.onGround = true; // see above
        }
        navigation = entity.ak();
        navigation.a(target, getSpeed(entity));
    }

    private float getSpeed(EntityLiving from) {
        try {
            Field field = EntityLiving.class.getDeclaredField("bb");
            field.setAccessible(true);
            return field.getFloat(from);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.7F;
        }
    }

    @Override
    public boolean update() {
        if (entity != null) {
            navigation.d();
            entity.getControllerMove().c();
            entity.getControllerLook().a();
            entity.getControllerJump().b();
            entity.moveOnCurrentHeading();
        }
        return navigation.e();
    }
}
