package net.citizensnpcs.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.astar.pathfinder.SwimmingExaminer;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.NPCKnockbackEvent;
import net.citizensnpcs.api.jnbt.CompoundTag;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.EntityDim;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.MirrorTrait;
import net.citizensnpcs.trait.PacketNPC;
import net.citizensnpcs.trait.versioned.CamelTrait.CamelPose;
import net.citizensnpcs.trait.versioned.SnifferTrait.SnifferState;
import net.citizensnpcs.util.EntityPacketTracker.PacketAggregator;

public class NMS {
    private NMS() {
        // util class
    }

    public enum MinecraftNavigationType {
        GROUND,
        WALL_CLIMB;
    }

    public static void activate(Entity entity) {
        BRIDGE.activate(entity);
    }

    public static boolean addEntityToWorld(org.bukkit.entity.Entity entity, SpawnReason custom) {
        return BRIDGE.addEntityToWorld(entity, custom);
    }

    public static void addOrRemoveFromPlayerList(org.bukkit.entity.Entity entity, boolean remove) {
        BRIDGE.addOrRemoveFromPlayerList(entity, remove);
    }

    public static void attack(LivingEntity attacker, LivingEntity bukkitTarget) {
        BRIDGE.attack(attacker, bukkitTarget);
    }

    public static float[][] calculateDragonPositions(float yrot, double[][] latency) {
        float[][] positions = new float[8][];
        float f7 = (float) (latency[1][1] - latency[2][1]) * 10.0F * 0.017453292F;
        float f8 = (float) Math.cos(f7);
        float f9 = (float) Math.sin(f7);
        float f6 = yrot * 0.017453292F;
        float f11 = (float) Math.sin(f6);
        float f12 = (float) Math.cos(f6);
        positions[2] = new float[] { f11 * 0.5F, 0.0F, -f12 * 0.5F };
        positions[6] = new float[] { f12 * 4.5F, 2F, f11 * 4.5F };
        positions[7] = new float[] { f12 * -4.5F, 2f, f11 * -4.5F };
        float f15 = (float) (latency[1][1] - latency[0][1]);
        positions[0] = new float[] { f11 * 6.5F * f8, f15 + f9 * 6.5F, -f12 * 6.5F * f8 };
        positions[1] = new float[] { f11 * 5.5F * f8, f15 + f9 * 5.5F, -f12 * 5.5F * f8 };
        for (int k = 3; k < 6; ++k) {
            float f16 = f6 + Util.clamp((float) (latency[k][0] - latency[1][0])) * 0.017453292F;
            float f3 = (float) Math.sin(f16);
            float f4 = (float) Math.cos(f16);
            float f17 = (k - 2) * 2.0F;
            positions[k] = new float[] { -(f11 * 1.5F + f3 * f17) * f8,
                    (float) (latency[k][1] - latency[1][1] - (f17 + 1.5F) * f9 + 1.5), (f12 * 1.5F + f4 * f17) * f8 };
        }
        return positions;
    }

    public static void callKnockbackEvent(NPC npc, float strength, double dx, double dz,
            Consumer<NPCKnockbackEvent> cb) {
        if (npc.getEntity() == null)
            return;
        if (SUPPORT_KNOCKBACK_RESISTANCE && npc.getEntity() instanceof LivingEntity) {
            try {
                AttributeInstance attribute = ((LivingEntity) npc.getEntity())
                        .getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                if (attribute != null) {
                    strength *= 1 - attribute.getValue();
                }
            } catch (Throwable t) {
                SUPPORT_KNOCKBACK_RESISTANCE = false;
            }
        }
        Vector vector = npc.getEntity().getVelocity();
        Vector impulse = new Vector(dx, 0, dz).normalize().multiply(strength);
        Vector delta = new Vector(vector.getX() / 2 - impulse.getX() - vector.getX(),
                -vector.getY()
                        + (npc.getEntity().isOnGround() ? Math.min(0.4, vector.getY() / 2 + strength) : vector.getY()),
                vector.getZ() / 2 - impulse.getZ() - vector.getZ());
        NPCKnockbackEvent event = new NPCKnockbackEvent(npc, strength, delta, null);
        Bukkit.getPluginManager().callEvent(event);
        if (!PAPER_KNOCKBACK_EVENT_EXISTS) {
            event.getKnockbackVector().multiply(new Vector(-1, 0, -1));
        }
        if (!event.isCancelled()) {
            cb.accept(event);
        }
    }

    public static void cancelMoveDestination(Entity entity) {
        BRIDGE.cancelMoveDestination(entity);
    }

    public static Iterable<Object> createBundlePacket(List<Object> packets) {
        return BRIDGE.createBundlePacket(packets);
    }

    public static EntityPacketTracker createPacketTracker(Entity entity) {
        return createPacketTracker(entity, new PacketAggregator());
    }

    /*
     * Yggdrasil's default implementation of this method silently fails instead of throwing
     * an Exception like it should.
     */

    public static EntityPacketTracker createPacketTracker(Entity entity, PacketAggregator agg) {
        return BRIDGE.createPacketTracker(entity, agg);
    }

    public static GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Throwable {
        return BRIDGE.fillProfileProperties(profile, requireSecure);
    }

    public static void findProfilesByNames(String[] names, ProfileLookupCallback cb) {
        if (FIND_PROFILES_BY_NAMES == null) {
            try {
                Class<?> agentClass = Class.forName("com.mojang.authlib.Agent");
                Object minecraftAgent = agentClass.getField("MINECRAFT").get(null);
                MethodHandle mh = getMethodHandle(BRIDGE.getGameProfileRepository().getClass(), "findProfilesByNames",
                        false, String[].class, agentClass, ProfileLookupCallback.class);
                FIND_PROFILES_BY_NAMES = MethodHandles.insertArguments(mh, 2, minecraftAgent);
            } catch (Exception e) {
                FIND_PROFILES_BY_NAMES = getMethodHandle(BRIDGE.getGameProfileRepository().getClass(),
                        "findProfilesByNames", false, String[].class, ProfileLookupCallback.class);
            }
        }
        try {
            FIND_PROFILES_BY_NAMES.invoke(BRIDGE.getGameProfileRepository(), names, cb);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static BlockBreaker getBlockBreaker(Entity entity, Block targetBlock, BlockBreakerConfiguration config) {
        return BRIDGE.getBlockBreaker(entity, targetBlock, config);
    }

    public static Object getBossBar(Entity entity) {
        return BRIDGE.getBossBar(entity);
    }

    public static BoundingBox getBoundingBox(org.bukkit.entity.Entity handle) {
        return BRIDGE.getBoundingBox(handle);
    }

    public static double getBoundingBoxHeight(Entity entity) {
        return BRIDGE.getBoundingBoxHeight(entity);
    }

    public static BoundingBox getCollisionBox(Block block) {
        if (block.getType() == Material.AIR)
            return BoundingBox.EMPTY;

        return BRIDGE.getCollisionBox(block).add(block.getX(), block.getY(), block.getZ());
    }

    public static Location getDestination(Entity entity) {
        return BRIDGE.getDestination(entity);
    }

    public static int getFallDistance(NPC npc, int def) {
        return npc == null ? def
                : npc.data().get(NPC.Metadata.PATHFINDER_FALL_DISTANCE,
                        Setting.PATHFINDER_FALL_DISTANCE.asInt() != -1 ? Setting.PATHFINDER_FALL_DISTANCE.asInt()
                                : def);
    }

    public static Field getField(Class<?> clazz, String field) {
        return getField(clazz, field, true);
    }

    public static Field getField(Class<?> clazz, String field, boolean log) {
        if (clazz == null)
            return null;
        Field f = null;
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private static List<Field> getFieldsMatchingType(Class<?> clazz, Class<?> type, boolean allowStatic) {
        List<Field> found = Lists.newArrayList();
        for (Field field : clazz.getDeclaredFields()) {
            if (allowStatic ^ Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType() == type) {
                found.add(field);
                field.setAccessible(true);
            }
        }
        return found;
    }

    public static List<MethodHandle> getFieldsOfType(Class<?> clazz, Class<?> type) {
        List<Field> found = getFieldsMatchingType(clazz, type, false);
        if (found.isEmpty())
            return null;
        return found.stream().map(f -> {
            try {
                return LOOKUP.unreflectGetter(f);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }).filter(f -> f != null).collect(Collectors.toList());
    }

    public static MethodHandle getFinalSetter(Class<?> clazz, String field) {
        return getFinalSetter(clazz, field, true);
    }

    public static MethodHandle getFinalSetter(Class<?> clazz, String field, boolean log) {
        return getFinalSetter(NMS.getField(clazz, field, log), log);
    }

    public static MethodHandle getFinalSetter(Field field, boolean log) {
        if (field == null)
            return null;
        if (MODIFIERS_FIELD == null) {
            if (UNSAFE == null) {
                try {
                    UNSAFE = NMS.getField(Class.forName("sun.misc.Unsafe"), "theUnsafe").get(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (log) {
                        Messaging.logTr(Messages.ERROR_GETTING_FIELD, field.getName(), e.getLocalizedMessage());
                    }
                    return null;
                }
                UNSAFE_STATIC_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "staticFieldOffset", true, Field.class)
                        .bindTo(UNSAFE);
                UNSAFE_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "objectFieldOffset", true, Field.class)
                        .bindTo(UNSAFE);
                UNSAFE_PUT_OBJECT = getMethodHandle(UNSAFE.getClass(), "putObject", true, Object.class, long.class,
                        Object.class).bindTo(UNSAFE);
                UNSAFE_PUT_INT = getMethodHandle(UNSAFE.getClass(), "putInt", true, Object.class, long.class, int.class)
                        .bindTo(UNSAFE);
                UNSAFE_PUT_FLOAT = getMethodHandle(UNSAFE.getClass(), "putFloat", true, Object.class, long.class,
                        float.class).bindTo(UNSAFE);
                UNSAFE_PUT_DOUBLE = getMethodHandle(UNSAFE.getClass(), "putDouble", true, Object.class, long.class,
                        double.class).bindTo(UNSAFE);
                UNSAFE_PUT_BOOLEAN = getMethodHandle(UNSAFE.getClass(), "putBoolean", true, Object.class, long.class,
                        boolean.class).bindTo(UNSAFE);
                UNSAFE_PUT_LONG = getMethodHandle(UNSAFE.getClass(), "putLong", true, Object.class, long.class,
                        long.class).bindTo(UNSAFE);
            }
            try {
                boolean isStatic = Modifier.isStatic(field.getModifiers());
                long offset = (long) (isStatic ? UNSAFE_STATIC_FIELD_OFFSET.invoke(field)
                        : UNSAFE_FIELD_OFFSET.invoke(field));
                MethodHandle mh = field.getType() == int.class ? UNSAFE_PUT_INT
                        : field.getType() == boolean.class ? UNSAFE_PUT_BOOLEAN
                                : field.getType() == double.class ? UNSAFE_PUT_DOUBLE
                                        : field.getType() == float.class ? UNSAFE_PUT_FLOAT
                                                : field.getType() == long.class ? UNSAFE_PUT_LONG : UNSAFE_PUT_OBJECT;
                return isStatic ? MethodHandles.insertArguments(mh, 0, field.getDeclaringClass(), offset)
                        : MethodHandles.insertArguments(mh, 1, offset);
            } catch (Throwable t) {
                t.printStackTrace();
                if (log) {
                    Messaging.logTr(Messages.ERROR_GETTING_FIELD, field.getName(), t.getLocalizedMessage());
                }
                return null;
            }
        }
        try {
            MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, field.getName(), e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        try {
            return LOOKUP.unreflectSetter(field);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, field.getName(), e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static MethodHandle getFirstFinalSetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return getFinalSetter(found.get(0), true);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, type, e.getLocalizedMessage());
        }
        return null;
    }

    public static MethodHandle getFirstGetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return LOOKUP.unreflectGetter(found.get(0));
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, type, e.getLocalizedMessage());
        }
        return null;
    }

    public static MethodHandle getFirstMethodHandle(Class<?> clazz, boolean log, Class<?>... params) {
        return getFirstMethodHandleWithReturnType(clazz, log, null, params);
    }

    public static MethodHandle getFirstMethodHandleWithReturnType(Class<?> clazz, boolean log, Class<?> returnType,
            Class<?>... params) {
        if (clazz == null)
            return null;
        try {
            Method first = null;
            for (Method method : clazz.getDeclaredMethods()) {
                if (returnType != null && !returnType.equals(method.getReturnType())) {
                    continue;
                }
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == params.length) {
                    first = method;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (paramTypes[i] != params[i]) {
                            first = null;
                            break;
                        }
                    }
                    if (first != null) {
                        break;
                    }
                }
            }
            if (first == null)
                return null;
            first.setAccessible(true);
            return LOOKUP.unreflect(first);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_METHOD, e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static MethodHandle getFirstSetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, false);
            if (found.isEmpty())
                return null;
            return LOOKUP.unreflectSetter(found.get(0));
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, type, e.getLocalizedMessage());
        }
        return null;
    }

    public static MethodHandle getFirstStaticGetter(Class<?> clazz, Class<?> type) {
        try {
            List<Field> found = getFieldsMatchingType(clazz, type, true);
            if (found.isEmpty())
                return null;
            return LOOKUP.unreflectGetter(found.get(0));
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, type, e.getLocalizedMessage());
        }
        return null;
    }

    public static MethodHandle getGetter(Class<?> clazz, String name) {
        return getGetter(clazz, name, true);
    }

    public static MethodHandle getGetter(Class<?> clazz, String name, boolean log) {
        try {
            return LOOKUP.unreflectGetter(getField(clazz, name, log));
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, name, e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static float getHeadYaw(org.bukkit.entity.Entity entity) {
        return BRIDGE.getHeadYaw(entity);
    }

    public static float getHorizontalMovement(org.bukkit.entity.Entity bukkitEntity) {
        return BRIDGE.getHorizontalMovement(bukkitEntity);
    }

    public static float getJumpPower(NPC npc, float original) {
        if (npc == null)
            return original;
        if (npc.data().has(NPC.Metadata.JUMP_POWER_SUPPLIER))
            return npc.data().<Function<NPC, Float>> get(NPC.Metadata.JUMP_POWER_SUPPLIER).apply(npc);

        return original;
    }

    public static Method getMethod(Class<?> clazz, String method, boolean log, Class<?>... params) {
        if (clazz == null)
            return null;
        Method f = null;
        try {
            f = clazz.getDeclaredMethod(method, params);
            f.setAccessible(true);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_METHOD, method, e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
        }
        return f;
    }

    public static MethodHandle getMethodHandle(Class<?> clazz, String method, boolean log, Class<?>... params) {
        if (clazz == null)
            return null;
        try {
            return LOOKUP.unreflect(getMethod(clazz, method, log, params));
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_METHOD, method, e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static CompoundTag getNBT(ItemStack item) {
        return BRIDGE.getNBT(item);
    }

    private static Collection<Player> getNearbyPlayers(Entity from) {
        return getNearbyPlayers(from, from.getLocation(), 64);
    }

    private static Collection<Player> getNearbyPlayers(Entity from, Location location, double radius) {
        List<Player> players = Lists.newArrayList();
        for (Player player : CitizensAPI.getLocationLookup().getNearbyPlayers(location, radius)) {
            if (location.getWorld() != player.getWorld() || from != null && Util.canSee(player, from)
                    || location.distance(player.getLocation()) > radius)
                continue;

            players.add(player);
        }
        return players;
    }

    public static NPC getNPC(Entity entity) {
        return BRIDGE.getNPC(entity);
    }

    public static EntityPacketTracker getPacketTracker(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof NPCHolder) {
            NPC npc = ((NPCHolder) entity).getNPC();
            if (npc.hasTrait(PacketNPC.class))
                return npc.getOrAddTrait(PacketNPC.class).getPacketTracker();
        }
        if (!entity.isValid())
            return null;
        return BRIDGE.getPacketTracker(entity);
    }

    public static List<org.bukkit.entity.Entity> getPassengers(org.bukkit.entity.Entity entity) {
        return BRIDGE.getPassengers(entity);
    }

    public static GameProfile getProfile(Player player) {
        return BRIDGE.getProfile(player);
    }

    public static GameProfile getProfile(SkullMeta meta) {
        return BRIDGE.getProfile(meta);
    }

    public static MethodHandle getSetter(Class<?> clazz, String name) {
        return getSetter(clazz, name, true);
    }

    public static MethodHandle getSetter(Class<?> clazz, String name, boolean log) {
        try {
            return LOOKUP.unreflectSetter(getField(clazz, name, log));
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, name, e.getLocalizedMessage());
                if (Messaging.isDebugging()) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String getSoundPath(Sound flag) throws CommandException {
        return BRIDGE.getSoundPath(flag);
    }

    public static Entity getSource(BlockCommandSender sender) {
        return BRIDGE.getSource(sender);
    }

    public static float getSpeedFor(NPC npc) {
        return BRIDGE.getSpeedFor(npc);
    }

    public static float getStepHeight(org.bukkit.entity.Entity entity) {
        return BRIDGE.getStepHeight(entity);
    }

    public static MCNavigator getTargetNavigator(Entity entity, Iterable<Vector> dest, NavigatorParameters params) {
        return BRIDGE.getTargetNavigator(entity, dest, params);
    }

    public static MCNavigator getTargetNavigator(Entity entity, Location dest, NavigatorParameters params) {
        return BRIDGE.getTargetNavigator(entity, dest, params);
    }

    public static TargetNavigator getTargetNavigator(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity target,
            NavigatorParameters parameters) {
        return BRIDGE.getTargetNavigator(entity, target, parameters);
    }

    public static org.bukkit.entity.Entity getVehicle(org.bukkit.entity.Entity entity) {
        return BRIDGE.getVehicle(entity);
    }

    public static float getVerticalMovement(org.bukkit.entity.Entity bukkitEntity) {
        return BRIDGE.getVerticalMovement(bukkitEntity);
    }

    public static Collection<Player> getViewingPlayers(org.bukkit.entity.Entity entity) {
        return BRIDGE.getViewingPlayers(entity);
    }

    public static double getWidth(Entity entity) {
        return BRIDGE.getWidth(entity);
    }

    public static float getYaw(Entity entity) {
        return BRIDGE.getYaw(entity);
    }

    public static void giveReflectiveAccess(Class<?> from, Class<?> to) {
        try {
            if (GET_MODULE == null) {
                Class<?> module = Class.forName("java.lang.Module");
                GET_MODULE = Class.class.getMethod("getModule");
                ADD_OPENS = module.getMethod("addOpens", String.class, module);
            }
            ADD_OPENS.invoke(GET_MODULE.invoke(from), from.getPackage().getName(), GET_MODULE.invoke(to));
        } catch (Exception e) {
        }
    }

    public static boolean isLeashed(NPC npc, Supplier<Boolean> isLeashed, Runnable unleash) {
        if (npc == null)
            return isLeashed.get();
        boolean protectedDefault = npc.isProtected();
        if (!protectedDefault || !npc.data().get(NPC.Metadata.LEASH_PROTECTED, protectedDefault))
            return isLeashed.get();
        if (isLeashed.get()) {
            unleash.run();
        }
        return false;
    }

    public static boolean isOnGround(org.bukkit.entity.Entity entity) {
        return BRIDGE.isOnGround(entity);
    }

    public static boolean isSolid(Block in) {
        return BRIDGE.isSolid(in);
    }

    public static boolean isValid(Entity entity) {
        return BRIDGE.isValid(entity);
    }

    public static void linkTextInteraction(Player player, Entity interaction, Entity mount, double height) {
        BRIDGE.linkTextInteraction(player, interaction, mount, height);
    }

    public static void load(CommandManager commands) {
        BRIDGE.load(commands);
    }

    public static void loadBridge(String rev) throws Exception {
        Class<?> entity = null;
        try {
            entity = Class.forName("net.minecraft.server." + rev + ".Entity");
        } catch (ClassNotFoundException ex) {
            entity = Class.forName("net.minecraft.world.entity.Entity");
        }
        giveReflectiveAccess(entity, NMS.class);
        BRIDGE = (NMSBridge) Class.forName("net.citizensnpcs.nms." + rev + ".util.NMSImpl").getConstructor()
                .newInstance();
    }

    public static void look(Entity entity, float yaw, float pitch) {
        BRIDGE.look(entity, yaw, pitch);
    }

    public static void look(org.bukkit.entity.Entity entity, Location to, boolean headOnly, boolean immediate) {
        BRIDGE.look(entity, to, headOnly, immediate);
    }

    public static void look(org.bukkit.entity.Entity bhandle, org.bukkit.entity.Entity btarget) {
        BRIDGE.look(bhandle, btarget);
    }

    public static void mount(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity passenger) {
        BRIDGE.mount(entity, passenger);
    }

    public static void onPlayerInfoAdd(Player player, Object source, Function<UUID, MirrorTrait> mirrorTraits) {
        BRIDGE.onPlayerInfoAdd(player, source, mirrorTraits);
    }

    public static InventoryView openAnvilInventory(Player player, Inventory inventory, String title) {
        return BRIDGE.openAnvilInventory(player, inventory, title);
    }

    public static void openHorseScreen(Tameable horse, Player equipper) {
        BRIDGE.openHorseInventory(horse, equipper);
    }

    public static void playAnimation(PlayerAnimation animation, Player player, Iterable<Player> to) {
        BRIDGE.playAnimation(animation, player, to);
    }

    public static Runnable playerTicker(Player entity) {
        Runnable tick = BRIDGE.playerTicker(entity);
        return () -> {
            if (entity.isValid()) {
                tick.run();
            }
        };
    }

    public static void registerEntityClass(Class<?> clazz) {
        BRIDGE.registerEntityClass(clazz);
    }

    public static void remove(Entity entity) {
        BRIDGE.remove(entity);
    }

    public static void removeFromServerPlayerList(Player player) {
        BRIDGE.removeFromServerPlayerList(player);
    }

    public static void removeFromWorld(org.bukkit.entity.Entity entity) {
        BRIDGE.removeFromWorld(entity);
    }

    public static void removeHookIfNecessary(FishHook entity) {
        BRIDGE.removeHookIfNecessary(entity);
    }

    public static void replaceTracker(Entity entity) {
        BRIDGE.replaceTrackerEntry(entity);
    }

    public static void sendPositionUpdate(Entity from, Collection<Player> to, boolean position) {
        sendPositionUpdate(from, to, position, NMS.getYaw(from), from.getLocation().getPitch(), NMS.getHeadYaw(from));
    }

    public static void sendPositionUpdate(Entity from, Collection<Player> to, boolean position, Float bodyYaw,
            Float pitch, Float headYaw) {
        BRIDGE.sendPositionUpdate(from, to, position, bodyYaw, pitch, headYaw);
    }

    public static void sendPositionUpdateNearby(Entity from, boolean position) {
        sendPositionUpdate(from, getNearbyPlayers(from), position, NMS.getYaw(from), from.getLocation().getPitch(),
                NMS.getHeadYaw(from));
    }

    public static void sendPositionUpdateNearby(Entity from, boolean position, Float bodyYaw, Float pitch,
            Float headYaw) {
        sendPositionUpdate(from, getNearbyPlayers(from), position, bodyYaw, pitch, headYaw);
    }

    public static boolean sendTabListAdd(Player recipient, Player listPlayer) {
        return BRIDGE.sendTabListAdd(recipient, listPlayer);
    }

    public static void sendTabListRemove(Player recipient, Collection<? extends SkinnableEntity> skinnableNPCs) {
        BRIDGE.sendTabListRemove(recipient, skinnableNPCs);
    }

    public static void sendTabListRemove(Player recipient, Player listPlayer) {
        BRIDGE.sendTabListRemove(recipient, listPlayer);
    }

    public static void sendTeamPacket(Player recipient, Team team, int mode) {
        BRIDGE.sendTeamPacket(recipient, team, mode);
    }

    public static void setAggressive(Entity entity, boolean aggro) {
        BRIDGE.setAggressive(entity, aggro);
    }

    public static void setAllayDancing(Entity entity, boolean dancing) {
        BRIDGE.setAllayDancing(entity, dancing);
    }

    public static void setBodyYaw(Entity entity, float yaw) {
        BRIDGE.setBodyYaw(entity, yaw);
    }

    public static void setBoundingBox(Entity entity, BoundingBox box) {
        BRIDGE.setBoundingBox(entity, box);
    }

    public static void setCamelPose(Entity entity, CamelPose pose) {
        BRIDGE.setCamelPose(entity, pose);
    }

    public static void setCustomName(Entity entity, Object component, String string) {
        BRIDGE.setCustomName(entity, component, string);
    }

    public static void setDestination(org.bukkit.entity.Entity entity, double x, double y, double z, float speed) {
        BRIDGE.setDestination(entity, x, y, z, speed);
    }

    public static void setDimensions(Entity entity, EntityDim desired) {
        BRIDGE.setDimensions(entity, desired);
    }

    public static void setEndermanAngry(Enderman enderman, boolean angry) {
        BRIDGE.setEndermanAngry(enderman, angry);
    }

    public static void setHeadAndBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        BRIDGE.setHeadAndBodyYaw(entity, yaw);
    }

    public static void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        BRIDGE.setHeadYaw(entity, yaw);
    }

    public static void setKnockbackResistance(org.bukkit.entity.LivingEntity entity, double d) {
        BRIDGE.setKnockbackResistance(entity, d);
    }

    public static void setLocationDirectly(Entity entity, Location location) {
        BRIDGE.setLocationDirectly(entity, location);
    }

    public static void setLyingDown(Entity cat, boolean lying) {
        BRIDGE.setLyingDown(cat, lying);
    }

    public static void setNavigationTarget(org.bukkit.entity.Entity handle, org.bukkit.entity.Entity target,
            float speed) {
        BRIDGE.setNavigationTarget(handle, target, speed);
    }

    public static void setNavigationType(Entity entity, MinecraftNavigationType type) {
        BRIDGE.setNavigationType(entity, type);
    }

    public static void setNoGravity(Entity entity, boolean nogravity) {
        BRIDGE.setNoGravity(entity, nogravity);
    }

    public static void setPandaSitting(Entity entity, boolean sitting) {
        BRIDGE.setPandaSitting(entity, sitting);
    }

    public static void setPeekShulker(org.bukkit.entity.Entity entity, int peek) {
        if (!entity.getType().name().equals("SHULKER"))
            throw new IllegalArgumentException("entity must be a shulker");

        BRIDGE.setPeekShulker(entity, peek);
    }

    public static void setPiglinDancing(Entity entity, boolean dancing) {
        BRIDGE.setPiglinDancing(entity, dancing);
    }

    public static void setPitch(Entity entity, float pitch) {
        BRIDGE.setPitch(entity, pitch);
    }

    public static void setPolarBearRearing(Entity entity, boolean rearing) {
        BRIDGE.setPolarBearRearing(entity, rearing);
    }

    public static void setProfile(SkullMeta meta, GameProfile profile) {
        BRIDGE.setProfile(meta, profile);
    }

    public static void setShouldJump(org.bukkit.entity.Entity entity) {
        BRIDGE.setShouldJump(entity);
    }

    public static void setSitting(Ocelot ocelot, boolean sitting) {
        BRIDGE.setSitting(ocelot, sitting);
    }

    public static void setSitting(Tameable tameable, boolean sitting) {
        BRIDGE.setSitting(tameable, sitting);
    }

    public static void setSneaking(Entity entity, boolean sneaking) {
        BRIDGE.setSneaking(entity, sneaking);
    }

    public static void setSnifferState(Entity entity, SnifferState state) {
        BRIDGE.setSnifferState(entity, state);
    }

    public static void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        BRIDGE.setStepHeight(entity, height);
    }

    public static void setTeamNameTagVisible(Team team, boolean visible) {
        BRIDGE.setTeamNameTagVisible(team, visible);
    }

    public static void setVerticalMovement(Entity bukkitEntity, double d) {
        BRIDGE.setVerticalMovement(bukkitEntity, d);
    }

    public static void setWardenPose(Entity entity, Object pose) {
        BRIDGE.setWardenPose(entity, pose);
    }

    public static void setWitherInvulnerable(Wither wither, boolean charged) {
        BRIDGE.setWitherCharged(wither, charged);
    }

    public static boolean shouldJump(org.bukkit.entity.Entity entity) {
        return BRIDGE.shouldJump(entity);
    }

    public static void shutdown() {
        if (BRIDGE != null) {
            BRIDGE.shutdown();
            BRIDGE = null;
        }
    }

    public static void sleep(Player entity, boolean sleep) {
        BRIDGE.sleep(entity, sleep);
    }

    public static void trySwim(org.bukkit.entity.Entity entity) {
        trySwim(entity, SwimmingExaminer.isWaterMob(entity) ? 0.02F : 0.04F);
    }

    public static void trySwim(org.bukkit.entity.Entity entity, float power) {
        BRIDGE.trySwim(entity, power);
    }

    public static void updateInventoryTitle(Player player, InventoryView view, String newTitle) {
        BRIDGE.updateInventoryTitle(player, view, newTitle);
    }

    public static void updateNavigationWorld(org.bukkit.entity.Entity entity, org.bukkit.World world) {
        BRIDGE.updateNavigationWorld(entity, world);
    }

    public static void updatePathfindingRange(NPC npc, float pathfindingRange) {
        BRIDGE.updatePathfindingRange(npc, pathfindingRange);
    }

    private static Method ADD_OPENS;
    private static NMSBridge BRIDGE;
    private static MethodHandle FIND_PROFILES_BY_NAMES = null;
    private static Method GET_MODULE;
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static Field MODIFIERS_FIELD;
    private static boolean PAPER_KNOCKBACK_EVENT_EXISTS = true;
    private static boolean SUPPORT_KNOCKBACK_RESISTANCE = true;
    private static Object UNSAFE;
    private static MethodHandle UNSAFE_FIELD_OFFSET;
    private static MethodHandle UNSAFE_PUT_BOOLEAN;
    private static MethodHandle UNSAFE_PUT_DOUBLE;
    private static MethodHandle UNSAFE_PUT_FLOAT;
    private static MethodHandle UNSAFE_PUT_INT;
    private static MethodHandle UNSAFE_PUT_LONG;
    private static MethodHandle UNSAFE_PUT_OBJECT;
    private static MethodHandle UNSAFE_STATIC_FIELD_OFFSET;

    static {
        try {
            Class.forName("com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent");
        } catch (ClassNotFoundException e) {
            PAPER_KNOCKBACK_EVENT_EXISTS = false;
        }
        giveReflectiveAccess(Field.class, NMS.class);
        MODIFIERS_FIELD = NMS.getField(Field.class, "modifiers", false);
    }
}
