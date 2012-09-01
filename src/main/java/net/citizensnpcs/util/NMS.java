package net.citizensnpcs.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.Navigation;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.entity.EntityType;

import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class NMS {
    private NMS() {
        // util class
    }

    private static final float DEFAULT_SPEED = 0.4F;
    private static Map<Class<? extends Entity>, Integer> ENTITY_CLASS_TO_INT;
    private static Map<Integer, Class<? extends Entity>> ENTITY_INT_TO_CLASS;
    private static Field GOAL_FIELD;
    private static final Map<EntityType, Float> MOVEMENT_SPEEDS = Maps.newEnumMap(EntityType.class);
    private static Field PATHFINDING_RANGE;

    private static Field SPEED_FIELD;

    private static Field THREAD_STOPPER;

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        if (NMS.GOAL_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                List<?> list = (List<?>) NMS.GOAL_FIELD.get(selector);
                list.clear();
            } catch (Exception e) {
            }
        }
    }

    private static Field getField(Class<?> clazz, String field) {
        Field f = null;
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
        } catch (Exception e) {
        }
        return f;
    }

    public static float getSpeedFor(EntityLiving from) {
        EntityType entityType = from.getBukkitEntity().getType();
        Float cached = MOVEMENT_SPEEDS.get(entityType);
        if (cached != null)
            return cached;
        if (SPEED_FIELD == null) {
            MOVEMENT_SPEEDS.put(entityType, DEFAULT_SPEED);
            return DEFAULT_SPEED;
        }
        try {
            float speed = SPEED_FIELD.getFloat(from);
            MOVEMENT_SPEEDS.put(entityType, speed);
            return speed;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return DEFAULT_SPEED;
        }
    }

    public static void registerEntityClass(Class<? extends Entity> clazz) {
        if (ENTITY_CLASS_TO_INT.containsKey(clazz))
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            if (!ENTITY_CLASS_TO_INT.containsKey(search))
                continue;
            int code = ENTITY_CLASS_TO_INT.get(search);
            ENTITY_INT_TO_CLASS.put(code, clazz);
            ENTITY_CLASS_TO_INT.put(clazz, code);
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass");
    }

    public static void stopNetworkThreads(NetworkManager manager) {
        if (THREAD_STOPPER == null)
            return;
        try {
            THREAD_STOPPER.set(manager, false);
        } catch (Exception e) {
        }
    }

    public static void updateAI(EntityLiving entity) {
        entity.getNavigation().e();
        entity.getControllerMove().c();
        entity.getControllerLook().a();
        entity.getControllerJump().b();
    }

    public static void updatePathfindingRange(CitizensNPC npc, float pathfindingRange) {
        if (PATHFINDING_RANGE == null)
            return;
        Navigation navigation = npc.getHandle().getNavigation();
        try {
            PATHFINDING_RANGE.set(navigation, pathfindingRange);
        } catch (Exception ex) {
            Messaging.logF("Could not update pathfinding range: %s.", ex.getMessage());
        }
    }

    static {
        // true field above false and three synchronised lists
        THREAD_STOPPER = getField(NetworkManager.class, "m");

        // constants taken from source code
        MOVEMENT_SPEEDS.put(EntityType.CHICKEN, 0.25F);
        MOVEMENT_SPEEDS.put(EntityType.COW, 0.2F);
        MOVEMENT_SPEEDS.put(EntityType.CREEPER, 0.3F);
        MOVEMENT_SPEEDS.put(EntityType.IRON_GOLEM, 0.15F);
        MOVEMENT_SPEEDS.put(EntityType.MUSHROOM_COW, 0.2F);
        MOVEMENT_SPEEDS.put(EntityType.OCELOT, 0.23F);
        MOVEMENT_SPEEDS.put(EntityType.SHEEP, 0.25F);
        MOVEMENT_SPEEDS.put(EntityType.SNOWMAN, 0.25F);
        MOVEMENT_SPEEDS.put(EntityType.PIG, 0.27F);
        MOVEMENT_SPEEDS.put(EntityType.PLAYER, 1F);
        MOVEMENT_SPEEDS.put(EntityType.VILLAGER, 0.3F);

        SPEED_FIELD = getField(EntityLiving.class, "bw");
        PATHFINDING_RANGE = getField(Navigation.class, "e");
        GOAL_FIELD = getField(PathfinderGoalSelector.class, "a");

        try {
            Field field = getField(EntityTypes.class, "d");
            field.setAccessible(true);
            ENTITY_INT_TO_CLASS = (Map<Integer, Class<? extends Entity>>) field.get(null);
            field = getField(EntityTypes.class, "e");
            field.setAccessible(true);
            ENTITY_CLASS_TO_INT = (Map<Class<? extends Entity>, Integer>) field.get(null);
        } catch (Exception e) {
        }
    }
}
