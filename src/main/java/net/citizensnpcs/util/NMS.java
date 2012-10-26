package net.citizensnpcs.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.ControllerLook;
import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMonster;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.Navigation;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class NMS {
    private NMS() {
        // util class
    }

    private static Field DAMAGE_FIELD;

    private static final float DEFAULT_SPEED = 0.4F;
    private static Map<Class<? extends Entity>, Integer> ENTITY_CLASS_TO_INT;
    private static final Map<Class<? extends Entity>, Constructor<? extends Entity>> ENTITY_CONSTRUCTOR_CACHE = new WeakHashMap<Class<? extends Entity>, Constructor<? extends Entity>>();
    private static Map<Integer, Class<? extends Entity>> ENTITY_INT_TO_CLASS;
    private static Field GOAL_FIELD;
    private static Field LAND_SPEED_MODIFIER_FIELD;
    private static final Map<EntityType, Float> MOVEMENT_SPEEDS = Maps.newEnumMap(EntityType.class);
    private static Field NAVIGATION_WORLD_FIELD;
    private static Field PATHFINDING_RANGE;
    private static Field SPEED_FIELD;
    private static Field THREAD_STOPPER;

    public static void addOrRemoveFromPlayerList(LivingEntity bukkitEntity, boolean remove) {
        EntityLiving handle = ((CraftLivingEntity) bukkitEntity).getHandle();
        if (handle.world == null)
            return;
        if (remove) {
            handle.world.players.remove(handle);
        } else {
            handle.world.players.add(handle);
        }
    }

    public static void attack(EntityLiving handle, EntityLiving target) {
        int damage = getDamage(handle);

        if (handle.hasEffect(MobEffectList.INCREASE_DAMAGE)) {
            damage += 3 << handle.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier();
        }

        if (handle.hasEffect(MobEffectList.WEAKNESS)) {
            damage -= 2 << handle.getEffect(MobEffectList.WEAKNESS).getAmplifier();
        }

        target.damageEntity(DamageSource.mobAttack(handle), damage);
    }

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        if (GOAL_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                List<?> list = (List<?>) NMS.GOAL_FIELD.get(selector);
                list.clear();
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getMessage());
            }
        }
    }

    private static Constructor<? extends Entity> getCustomEntityConstructor(Class<? extends Entity> clazz,
            EntityType type) throws SecurityException, NoSuchMethodException {
        Constructor<? extends Entity> constructor = ENTITY_CONSTRUCTOR_CACHE.get(clazz);
        if (constructor == null) {
            constructor = clazz.getConstructor(World.class);
            constructor.setAccessible(true);
            ENTITY_CLASS_TO_INT.put(clazz, (int) type.getTypeId());
            ENTITY_CONSTRUCTOR_CACHE.put(clazz, constructor);
        }
        return constructor;
    }

    private static int getDamage(EntityLiving handle) {
        if (DAMAGE_FIELD == null)
            return 2;
        try {
            return DAMAGE_FIELD.getInt(handle);
        } catch (Exception e) {
        }
        return 2;
    }

    private static Field getField(Class<?> clazz, String field) {
        Field f = null;
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, e.getMessage());
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

    public static void look(ControllerLook controllerLook, EntityLiving handle, EntityLiving target) {
        controllerLook.a(target, 10.0F, handle.bf());
    }

    public static void look(EntityLiving handle, float yaw, float pitch) {
        handle.yaw = handle.as = yaw;
        handle.pitch = pitch;
    }

    public static boolean rayTrace(LivingEntity entity, LivingEntity entity2) {
        EntityLiving from = ((CraftLivingEntity) entity).getHandle();
        EntityLiving to = ((CraftLivingEntity) entity2).getHandle();
        return from.l(to);
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

    public static void setLandSpeedModifier(EntityLiving handle, float speed) {
        if (LAND_SPEED_MODIFIER_FIELD == null)
            return;
        try {
            LAND_SPEED_MODIFIER_FIELD.setFloat(handle, speed);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_UPDATING_SPEED, e.getMessage());
        }
    }

    public static org.bukkit.entity.Entity spawnCustomEntity(org.bukkit.World world, Location at,
            Class<? extends Entity> clazz, EntityType type) {
        World handle = ((CraftWorld) world).getHandle();
        Entity entity = null;
        try {
            Constructor<? extends Entity> constructor = getCustomEntityConstructor(clazz, type);
            entity = constructor.newInstance(handle);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_SPAWNING_CUSTOM_ENTITY, e.getMessage());
            return null;
        }
        handle.addEntity(entity);
        entity.setLocation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        return entity.getBukkitEntity();
    }

    public static void stopNetworkThreads(NetworkManager manager) {
        if (THREAD_STOPPER == null)
            return;
        try {
            THREAD_STOPPER.set(manager, false);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_STOPPING_NETWORK_THREADS, e.getMessage());
        }
    }

    public static void trySwim(EntityLiving handle) {
        if ((handle.H() || handle.J()) && Math.random() < 0.8F)
            handle.motY += 0.04;
    }

    public static void updateAI(EntityLiving entity) {
        entity.getNavigation().e();
        entity.getControllerMove().c();
        entity.getControllerLook().a();
        entity.getControllerJump().b();
    }

    public static void updateNavigationWorld(org.bukkit.entity.Entity entity, org.bukkit.World world) {
        if (NAVIGATION_WORLD_FIELD == null || !(entity instanceof LivingEntity))
            return;
        EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
        World worldHandle = ((CraftWorld) world).getHandle();
        try {
            NAVIGATION_WORLD_FIELD.set(handle.getNavigation(), worldHandle);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_UPDATING_NAVIGATION_WORLD, e.getMessage());
        }
    }

    public static void updatePathfindingRange(CitizensNPC npc, float pathfindingRange) {
        if (PATHFINDING_RANGE == null)
            return;
        Navigation navigation = npc.getHandle().getNavigation();
        try {
            PATHFINDING_RANGE.set(navigation, pathfindingRange);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_UPDATING_PATHFINDING_RANGE, e.getMessage());
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

        LAND_SPEED_MODIFIER_FIELD = getField(EntityLiving.class, "bB");
        SPEED_FIELD = getField(EntityLiving.class, "bw");
        NAVIGATION_WORLD_FIELD = getField(Navigation.class, "b");
        PATHFINDING_RANGE = getField(Navigation.class, "e");
        GOAL_FIELD = getField(PathfinderGoalSelector.class, "a");
        DAMAGE_FIELD = getField(EntityMonster.class, "damage");

        try {
            Field field = getField(EntityTypes.class, "d");
            ENTITY_INT_TO_CLASS = (Map<Integer, Class<? extends Entity>>) field.get(null);
            field = getField(EntityTypes.class, "e");
            ENTITY_CLASS_TO_INT = (Map<Class<? extends Entity>, Integer>) field.get(null);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_ID_MAPPING, e.getMessage());
        }
    }
}
