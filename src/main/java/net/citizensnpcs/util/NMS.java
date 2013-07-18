package net.citizensnpcs.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.ControllerJump;
import net.minecraft.server.v1_6_R2.DamageSource;
import net.minecraft.server.v1_6_R2.EnchantmentManager;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityInsentient;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.EntityTypes;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.MathHelper;
import net.minecraft.server.v1_6_R2.MobEffectList;
import net.minecraft.server.v1_6_R2.Navigation;
import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_6_R2.World;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLoadOrder;

@SuppressWarnings("unchecked")
public class NMS {
    private NMS() {
        // util class
    }

    public static void addOrRemoveFromPlayerList(LivingEntity bukkitEntity, boolean remove) {
        if (bukkitEntity == null)
            return;
        EntityLiving handle = getHandle(bukkitEntity);
        if (handle.world == null)
            return;
        if (remove) {
            handle.world.players.remove(handle);
        } else if (!handle.world.players.contains(handle)) {
            handle.world.players.add(handle);
        }
    }

    public static void attack(EntityLiving handle, Entity target) {
        AttributeInstance attribute = handle.getAttributeInstance(GenericAttributes.e);
        float damage = (float) (attribute == null ? 1D : attribute.getValue());

        if (handle.hasEffect(MobEffectList.INCREASE_DAMAGE)) {
            damage += 3 << handle.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier();
        }

        if (handle.hasEffect(MobEffectList.WEAKNESS)) {
            damage -= 2 << handle.getEffect(MobEffectList.WEAKNESS).getAmplifier();
        }

        int knockbackLevel = 0;

        if (target instanceof EntityLiving) {
            damage += EnchantmentManager.a(handle, (EntityLiving) target);
            knockbackLevel += EnchantmentManager.getKnockbackEnchantmentLevel(handle, (EntityLiving) target);
        }

        boolean success = target.damageEntity(DamageSource.mobAttack(handle), damage);

        if (!success)
            return;
        if (knockbackLevel > 0) {
            target.g(-MathHelper.sin((float) (handle.yaw * Math.PI / 180.0F)) * knockbackLevel * 0.5F, 0.1D,
                    MathHelper.cos((float) (handle.yaw * Math.PI / 180.0F)) * knockbackLevel * 0.5F);
            handle.motX *= 0.6D;
            handle.motZ *= 0.6D;
        }

        int fireAspectLevel = EnchantmentManager.getFireAspectEnchantmentLevel(handle);

        if (fireAspectLevel > 0)
            target.setOnFire(fireAspectLevel * 4);
    }

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        if (GOAL_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                List<?> list = (List<?>) NMS.GOAL_FIELD.get(selector);
                list.clear();
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            }
        }
    }

    private static Constructor<?> getCustomEntityConstructor(Class<?> clazz, EntityType type) throws SecurityException,
            NoSuchMethodException {
        Constructor<?> constructor = ENTITY_CONSTRUCTOR_CACHE.get(clazz);
        if (constructor == null) {
            constructor = clazz.getConstructor(World.class);
            constructor.setAccessible(true);
            ENTITY_CLASS_TO_INT.put(clazz, (int) type.getTypeId());
            ENTITY_CONSTRUCTOR_CACHE.put(clazz, constructor);
        }
        return constructor;
    }

    public static Field getField(Class<?> clazz, String field) {
        Field f = null;
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, e.getLocalizedMessage());
        }
        return f;
    }

    public static EntityLiving getHandle(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle();
    }

    public static float getHeadYaw(EntityLiving handle) {
        return handle.aP;
    }

    public static Navigation getNavigation(EntityLiving handle) {
        return handle instanceof EntityInsentient ? ((EntityInsentient) handle).getNavigation()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getNavigation() : null;
    }

    public static float getSpeedFor(NPC npc) {
        if (!npc.isSpawned())
            return DEFAULT_SPEED;
        // this is correct, but too slow. TODO: investigate
        // return (float)
        // NMS.getHandle(npc.getBukkitEntity()).getAttributeInstance(GenericAttributes.d).getValue();
        return DEFAULT_SPEED;
    }

    public static boolean inWater(LivingEntity entity) {
        EntityLiving mcEntity = getHandle(entity);
        return mcEntity.G() || mcEntity.I();
    }

    public static boolean isNavigationFinished(Navigation navigation) {
        return navigation.g();
    }

    public static void loadPlugins() {
        ((CraftServer) Bukkit.getServer()).enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    public static void look(EntityLiving handle, Entity target) {
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerLook().a(target, 10.0F, ((EntityInsentient) handle).bp());
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setTargetLook(target, 10F, 40);
        }
    }

    public static void look(LivingEntity bukkitEntity, float yaw, float pitch) {
        EntityLiving handle = getHandle(bukkitEntity);
        handle.yaw = yaw;
        setHeadYaw(handle, yaw);
        handle.pitch = pitch;
    }

    public static float modifiedSpeed(float baseSpeed, NPC npc) {
        return npc == null ? baseSpeed : baseSpeed * npc.getNavigator().getLocalParameters().speedModifier();
    }

    public static void registerEntityClass(Class<?> clazz) {
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

    public static void removeFromServerPlayerList(Player player) {
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(handle);
    }

    public static void sendPacket(Player player, Packet packet) {
        if (packet == null)
            return;
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendPacketNearby(Location location, Packet packet) {
        NMS.sendPacketsNearby(location, Arrays.asList(packet), 64);
    }

    public static void sendPacketsNearby(Location location, Collection<Packet> packets) {
        NMS.sendPacketsNearby(location, packets, 64);
    }

    public static void sendPacketsNearby(Location location, Collection<Packet> packets, double radius) {
        radius *= radius;
        final org.bukkit.World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld()) {
                continue;
            }
            if (location.distanceSquared(ply.getLocation(PACKET_CACHE_LOCATION)) > radius) {
                continue;
            }
            for (Packet packet : packets) {
                sendPacket(ply, packet);
            }
        }
    }

    public static void sendPacketsNearby(Location location, Packet... packets) {
        NMS.sendPacketsNearby(location, Arrays.asList(packets), 64);
    }

    public static void sendToOnline(Packet... packets) {
        Validate.notNull(packets, "packets cannot be null");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline())
                continue;
            for (Packet packet : packets) {
                sendPacket(player, packet);
            }
        }
    }

    public static void setDestination(LivingEntity bukkitEntity, double x, double y, double z, float speed) {
        EntityLiving handle = ((CraftLivingEntity) bukkitEntity).getHandle();
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerMove().a(x, y, z, speed);
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setMoveDestination(x, y, z, speed);
        }
    }

    public static void setHeadYaw(EntityLiving handle, float yaw) {
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }
        handle.aP = yaw;
        if (!(handle instanceof EntityHuman))
            handle.aN = yaw;
        handle.aQ = yaw;
    }

    public static void setShouldJump(LivingEntity entity) {
        EntityLiving handle = getHandle(entity);
        if (handle instanceof EntityInsentient) {
            ControllerJump controller = ((EntityInsentient) handle).getControllerJump();
            controller.a();
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setShouldJump();
        }
    }

    public static boolean shouldJump(net.minecraft.server.v1_6_R2.Entity entity) {
        if (JUMP_FIELD == null || !(entity instanceof EntityLiving))
            return false;
        try {
            return JUMP_FIELD.getBoolean(entity);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static org.bukkit.entity.Entity spawnCustomEntity(org.bukkit.World world, Location at,
            Class<? extends Entity> clazz, EntityType type) {
        World handle = ((CraftWorld) world).getHandle();
        Entity entity = null;
        try {
            Constructor<?> constructor = getCustomEntityConstructor(clazz, type);
            entity = (Entity) constructor.newInstance(handle);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_SPAWNING_CUSTOM_ENTITY, e.getMessage());
            return null;
        }
        entity.setLocation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        handle.addEntity(entity);
        entity.setLocation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        return entity.getBukkitEntity();
    }

    public static void stopNavigation(Navigation navigation) {
        navigation.h();
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

    public static void trySwim(LivingEntity handle) {
        trySwim(handle, 0.04F);
    }

    public static void trySwim(LivingEntity entity, float power) {
        Entity handle = getHandle(entity);
        if (RANDOM.nextFloat() < 0.8F && inWater(entity)) {
            handle.motY += power;
        }
    }

    public static void updateAI(EntityLiving entity) {
        if (entity instanceof EntityInsentient) {
            EntityInsentient handle = (EntityInsentient) entity;
            handle.getEntitySenses().a();
            NMS.updateNavigation(handle.getNavigation());
            handle.getControllerMove().c();
            handle.getControllerLook().a();
            handle.getControllerJump().b();
        } else if (entity instanceof EntityHumanNPC) {
            ((EntityHumanNPC) entity).updateAI();
        }
    }

    public static void updateNavigation(Navigation navigation) {
        navigation.f();
    }

    public static void updateNavigationWorld(LivingEntity entity, org.bukkit.World world) {
        if (NAVIGATION_WORLD_FIELD == null)
            return;
        EntityLiving en = ((CraftLivingEntity) entity).getHandle();
        if (!(en instanceof EntityInsentient))
            return;
        EntityInsentient handle = (EntityInsentient) en;
        World worldHandle = ((CraftWorld) world).getHandle();
        try {
            NAVIGATION_WORLD_FIELD.set(handle.getNavigation(), worldHandle);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_UPDATING_NAVIGATION_WORLD, e.getMessage());
        }
    }

    public static void updatePathfindingRange(NPC npc, float pathfindingRange) {
        if (!npc.isSpawned())
            return;
        EntityLiving en = ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
        if (!(en instanceof EntityInsentient)) {
            if (en instanceof EntityHumanNPC) {
                ((EntityHumanNPC) en).updatePathfindingRange(pathfindingRange);
            }
            return;
        }
        if (PATHFINDING_RANGE == null)
            return;
        EntityInsentient handle = (EntityInsentient) en;
        Navigation navigation = handle.getNavigation();
        try {
            AttributeInstance inst = (AttributeInstance) PATHFINDING_RANGE.get(navigation);
            inst.setValue(pathfindingRange);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static final float DEFAULT_SPEED = 1F;

    private static Map<Class<?>, Integer> ENTITY_CLASS_TO_INT;
    private static final Map<Class<?>, Constructor<?>> ENTITY_CONSTRUCTOR_CACHE = new WeakHashMap<Class<?>, Constructor<?>>();
    private static Map<Integer, Class<?>> ENTITY_INT_TO_CLASS;
    private static Field GOAL_FIELD = getField(PathfinderGoalSelector.class, "a");
    private static final Field JUMP_FIELD = getField(EntityLiving.class, "bd");
    private static Field NAVIGATION_WORLD_FIELD = getField(Navigation.class, "b");
    private static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static Field PATHFINDING_RANGE = getField(Navigation.class, "e");
    private static final Random RANDOM = Util.getFastRandom();
    private static Field THREAD_STOPPER = getField(NetworkManager.class, "n");
    // true field above false and three synchronised lists

    static {
        try {
            Field field = getField(EntityTypes.class, "d");
            ENTITY_INT_TO_CLASS = (Map<Integer, Class<?>>) field.get(null);
            field = getField(EntityTypes.class, "e");
            ENTITY_CLASS_TO_INT = (Map<Class<?>, Integer>) field.get(null);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_ID_MAPPING, e.getMessage());
        }
    }
}
