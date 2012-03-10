package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.Navigation;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NavigationStrategy implements PathStrategy {
    private final Navigation navigation;
    private EntityHumanNPC entity = null;

    NavigationStrategy(CitizensNPC npc, Location dest) {
        navigation = npc.getHandle().ak();
        navigation.a(dest.getX(), dest.getY(), dest.getZ(), getSpeed(npc.getHandle()));
        if (npc.getBukkitEntity() instanceof Player)
            entity = (EntityHumanNPC) npc.getHandle();
    }

    NavigationStrategy(EntityLiving entity, EntityLiving target) {
        if (entity instanceof EntityHumanNPC)
            this.entity = (EntityHumanNPC) entity;
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
