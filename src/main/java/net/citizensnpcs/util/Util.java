package net.citizensnpcs.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.Packet;
import net.minecraft.server.PathfinderGoalSelector;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Util {
    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    private static Field GOAL_FIELD;

    private static final Map<Class<?>, Class<?>> primitiveClassMap = Maps.newHashMap();

    public static void callCollisionEvent(NPC npc, net.minecraft.server.Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0)
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity.getBukkitEntity()));
    }

    public static NPCPushEvent callPushEvent(NPC npc, Vector vector) {
        NPCPushEvent event = new NPCPushEvent(npc, vector);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        if (GOAL_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                List<?> list = (List<?>) GOAL_FIELD.get(selector);
                list.clear();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Given a set of instantiation parameters, attempts to find a matching
     * constructor with the greatest number of matching class parameters and
     * invokes it.
     * 
     * @param clazz
     * @param params
     * @return null if no instance could be created with the given parameters
     */
    public static <T> T createInstance(Class<? extends T> clazz, Object... params) {
        Validate.notNull(params);
        Validate.noNullElements(params);
        try {
            if (params.length == 0) {
                return clazz.newInstance();
            }
            return createInstance0(clazz, params);
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> T createInstance0(Class<? extends T> clazz, Object[] params)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        @SuppressWarnings("unchecked")
        Constructor<? extends T>[] constructors = (Constructor<? extends T>[]) clazz.getConstructors();
        Arrays.sort(constructors, new Comparator<Constructor<?>>() {
            @Override
            public int compare(Constructor<?> o1, Constructor<?> o2) {
                return o2.getParameterTypes().length - o1.getParameterTypes().length;
            }
        });
        constructorLoop: for (Constructor<? extends T> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (Sets.newHashSet(types).size() != types.length)
                continue;
            // we can't resolve the order of the constructor parameters
            if (types.length == 0)
                return clazz.newInstance();
            Object[] rebuild = new Object[types.length];
            for (Object param : params) {
                for (int i = 0; i < types.length; ++i) {
                    if (param.getClass() == types[i] || primitiveClassMap.get(param.getClass()) == types[i]
                            || searchInterfaces(param.getClass(), types[i])) {
                        rebuild[i] = param;
                    }
                }
            }
            for (Object constructorParam : rebuild) {
                if (constructorParam == null)
                    continue constructorLoop;
            }

            return constructor.newInstance(rebuild);
        }
        return null;
    }

    public static boolean isSettingFulfilled(Player player, Setting setting) {
        String parts = setting.asString();
        if (parts.contains("*"))
            return true;
        for (String part : Splitter.on(',').split(parts)) {
            if (Material.matchMaterial(part) == player.getItemInHand().getType()) {
                return true;
            }
        }
        return false;
    }

    public static EntityType matchEntityType(String toMatch) {
        EntityType type = EntityType.fromName(toMatch);
        if (type != null)
            return type;
        for (EntityType check : EntityType.values()) {
            if (check.name().matches(toMatch) || check.name().replace('_', '-').equals(toMatch)) {
                type = check;
                break;
            }
        }
        return type;
    }

    private static boolean searchInterfaces(Class<?> class1, Class<?> class2) {
        for (Class<?> test : class1.getInterfaces())
            if (test == class2)
                return true;
        return false;
    }

    public static void sendPacketNearby(Location location, Packet packet, double radius) {
        radius *= radius;
        final World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld()) {
                continue;
            }
            if (location.distanceSquared(ply.getLocation()) > radius) {
                continue;
            }
            ((CraftPlayer) ply).getHandle().netServerHandler.sendPacket(packet);
        }
    }

    public static void sendToOnline(Packet... packets) {
        Validate.notNull(packets, "packets cannot be null");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline())
                continue;
            for (Packet packet : packets) {
                ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(packet);
            }
        }
    }

    static {
        primitiveClassMap.put(Boolean.class, boolean.class);
        primitiveClassMap.put(Byte.class, byte.class);
        primitiveClassMap.put(Short.class, short.class);
        primitiveClassMap.put(Character.class, char.class);
        primitiveClassMap.put(Integer.class, int.class);
        primitiveClassMap.put(Long.class, long.class);
        primitiveClassMap.put(Float.class, float.class);
        primitiveClassMap.put(Double.class, double.class);
        primitiveClassMap.put(boolean.class, Boolean.class);
        primitiveClassMap.put(byte.class, Byte.class);
        primitiveClassMap.put(short.class, Short.class);
        primitiveClassMap.put(char.class, Character.class);
        primitiveClassMap.put(int.class, Integer.class);
        primitiveClassMap.put(long.class, Long.class);
        primitiveClassMap.put(float.class, Float.class);
        primitiveClassMap.put(double.class, Double.class);
        try {
            GOAL_FIELD = PathfinderGoalSelector.class.getDeclaredField("a");
        } catch (Exception e) {
            GOAL_FIELD = null;
        }
    }
}
