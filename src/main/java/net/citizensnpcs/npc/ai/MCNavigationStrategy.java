package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;
import java.util.Map;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.Navigation;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

public class MCNavigationStrategy implements PathStrategy {
    private final EntityLiving entity;
    private final Navigation navigation;

    MCNavigationStrategy(CitizensNPC npc, Location dest) {
        entity = npc.getHandle();
        if (npc.getBukkitEntity() instanceof Player) {
            entity.onGround = true;
            // not sure of a better way around this - if onGround is false, then
            // navigation won't execute, and calling entity.move doesn't
            // entirely fix the problem.
        }
        navigation = npc.getHandle().al();
        navigation.a(dest.getX(), dest.getY(), dest.getZ(), getSpeed(npc.getHandle()));

    }

    MCNavigationStrategy(EntityLiving entity, EntityLiving target) {
        this.entity = entity;
        if (entity instanceof EntityHumanNPC) {
            entity.onGround = true; // see above
        }
        navigation = entity.al();
        navigation.a(target, getSpeed(entity));
    }

    private float getSpeed(EntityLiving from) {
        Float cached = MOVEMENT_SPEEDS.get(from.getBukkitEntity().getType());
        if (cached != null)
            return cached;
        if (SPEED_FIELD == null) {
            MOVEMENT_SPEEDS.put(from.getBukkitEntity().getType(), DEFAULT_SPEED);
            return DEFAULT_SPEED;
        }
        try {
            float speed = SPEED_FIELD.getFloat(from);
            MOVEMENT_SPEEDS.put(from.getBukkitEntity().getType(), speed);
            return speed;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return DEFAULT_SPEED;
        }
    }

    @Override
    public boolean update() {
        if (entity instanceof EntityHumanNPC) {
            navigation.d();
            ((EntityHumanNPC) entity).moveOnCurrentHeading();
        }
        return navigation.e();
    }

    private static final float DEFAULT_SPEED = 0.3F;
    private static final Map<EntityType, Float> MOVEMENT_SPEEDS = Maps.newEnumMap(EntityType.class);
    private static Field SPEED_FIELD;
    static {
        MOVEMENT_SPEEDS.put(EntityType.IRON_GOLEM, 0.15F);
        MOVEMENT_SPEEDS.put(EntityType.CHICKEN, 0.25F);
        MOVEMENT_SPEEDS.put(EntityType.COW, 0.2F);
        MOVEMENT_SPEEDS.put(EntityType.SHEEP, 0.25F);
        MOVEMENT_SPEEDS.put(EntityType.VILLAGER, 0.3F);
        MOVEMENT_SPEEDS.put(EntityType.SNOWMAN, 0.25F);
        try {
            SPEED_FIELD = EntityLiving.class.getDeclaredField("bb");
            SPEED_FIELD.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
