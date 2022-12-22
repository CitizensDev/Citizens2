package net.citizensnpcs.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.astar.pathfinder.SwimmingExaminer;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.jnbt.CompoundTag;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.versioned.CamelTrait.CamelPose;

public class NMS {
    private NMS() {
        // util class
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

    public static void cancelMoveDestination(Entity entity) {
        BRIDGE.cancelMoveDestination(entity);
    }/*
     * Yggdrasil's default implementation of this method silently fails instead of throwing
     * an Exception like it should.
     */

    public static GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Throwable {
        return BRIDGE.fillProfileProperties(profile, requireSecure);
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

    public static BoundingBox getCollisionBox(Block block) {
        if (block.getType() == Material.AIR) {
            return BoundingBox.EMPTY;
        }
        return BRIDGE.getCollisionBox(block).add(block.getX(), block.getY(), block.getZ());
    }

    public static Location getDestination(Entity entity) {
        return BRIDGE.getDestination(entity);
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
            }
            return null;
        }
    }

    public static MethodHandle getFinalSetter(Class<?> clazz, String field) {
        return getFinalSetter(clazz, field, true);
    }

    public static MethodHandle getFinalSetter(Class<?> clazz, String fieldName, boolean log) {
        Field field;
        if (MODIFIERS_FIELD == null) {
            if (UNSAFE == null) {
                try {
                    UNSAFE = NMS.getField(Class.forName("sun.misc.Unsafe"), "theUnsafe").get(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (log) {
                        Messaging.logTr(Messages.ERROR_GETTING_FIELD, fieldName, e.getLocalizedMessage());
                    }
                    return null;
                }
                UNSAFE_STATIC_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "staticFieldOffset", true, Field.class)
                        .bindTo(UNSAFE);
                UNSAFE_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "objectFieldOffset", true, Field.class)
                        .bindTo(UNSAFE);
                UNSAFE_PUT_OBJECT = getMethodHandle(UNSAFE.getClass(), "putObject", true, Object.class, long.class,
                        Object.class).bindTo(UNSAFE);
            }
            field = NMS.getField(clazz, fieldName, log);
            if (field == null) {
                return null;
            }
            try {
                boolean isStatic = Modifier.isStatic(field.getModifiers());
                long offset = (long) (isStatic ? UNSAFE_STATIC_FIELD_OFFSET.invoke(field)
                        : UNSAFE_FIELD_OFFSET.invoke(field));
                return isStatic ? MethodHandles.insertArguments(UNSAFE_PUT_OBJECT, 0, clazz, offset)
                        : MethodHandles.insertArguments(UNSAFE_PUT_OBJECT, 1, offset);
            } catch (Throwable t) {
                t.printStackTrace();
                if (log) {
                    Messaging.logTr(Messages.ERROR_GETTING_FIELD, fieldName, t.getLocalizedMessage());
                }
                return null;
            }
        }
        field = getField(clazz, fieldName, log);
        if (field == null) {
            return null;
        }
        try {
            MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, fieldName, e.getLocalizedMessage());
            }
            return null;
        }
        try {
            return LOOKUP.unreflectSetter(field);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, fieldName, e.getLocalizedMessage());
            }
        }
        return null;
    }

    private static Field getFirstFieldMatchingType(Class<?> clazz, Class<?> type, boolean allowStatic) {
        Field found = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (allowStatic ^ Modifier.isStatic(field.getModifiers()))
                continue;
            if (field.getType() == type) {
                found = field;
                break;
            }
        }
        if (found != null) {
            found.setAccessible(true);
        }
        return found;
    }

    public static MethodHandle getFirstGetter(Class<?> clazz, Class<?> type) {
        try {
            Field found = getFirstFieldMatchingType(clazz, type, false);
            if (found == null)
                return null;
            return LOOKUP.unreflectGetter(found);
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
                if (returnType != null && !returnType.equals(method.getReturnType()))
                    continue;
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == params.length) {
                    first = method;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (paramTypes[i] != params[i]) {
                            first = null;
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
            }
        }
        return null;
    }

    public static MethodHandle getFirstSetter(Class<?> clazz, Class<?> type) {
        try {
            Field found = getFirstFieldMatchingType(clazz, type, false);
            if (found == null)
                return null;
            return LOOKUP.unreflectSetter(found);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, type, e.getLocalizedMessage());
        }
        return null;
    }

    public static MethodHandle getFirstStaticGetter(Class<?> clazz, Class<?> type) {
        try {
            Field found = getFirstFieldMatchingType(clazz, type, true);
            if (found == null)
                return null;
            return LOOKUP.unreflectGetter(found);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, type, e.getLocalizedMessage());
        }
        return null;
    }

    public static GameProfileRepository getGameProfileRepository() {
        return BRIDGE.getGameProfileRepository();
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
            }
        }
        return null;
    }

    public static float getHeadYaw(org.bukkit.entity.Entity entity) {
        return BRIDGE.getHeadYaw(entity);
    }

    public static double getHeight(Entity entity) {
        return BRIDGE.getHeight(entity);
    }

    public static float getHorizontalMovement(org.bukkit.entity.Entity bukkitEntity) {
        return BRIDGE.getHorizontalMovement(bukkitEntity);
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
            }
        }
        return null;
    }

    public static CompoundTag getNBT(ItemStack item) {
        return BRIDGE.getNBT(item);
    }

    public static NPC getNPC(Entity entity) {
        return BRIDGE.getNPC(entity);
    }

    public static List<org.bukkit.entity.Entity> getPassengers(org.bukkit.entity.Entity entity) {
        return BRIDGE.getPassengers(entity);
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
            }
        }
        return null;
    }

    public static String getSound(String flag) throws CommandException {
        return BRIDGE.getSound(flag);
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

    public static boolean isOnGround(org.bukkit.entity.Entity entity) {
        return BRIDGE.isOnGround(entity);
    }

    public static boolean isSolid(Block in) {
        return BRIDGE.isSolid(in);
    }

    public static boolean isValid(Entity entity) {
        return BRIDGE.isValid(entity);
    }

    public static void load(CommandManager commands) {
        BRIDGE.load(commands);
    }

    public static void loadBridge(String rev) throws Exception {
        Class<?> entity = null;
        try {
            entity = Class.forName("net.minecraft.server.v" + rev + ".Entity");
        } catch (ClassNotFoundException ex) {
            entity = Class.forName("net.minecraft.world.entity.Entity");
        }
        giveReflectiveAccess(entity, NMS.class);
        BRIDGE = (NMSBridge) Class.forName("net.citizensnpcs.nms.v" + rev + ".util.NMSImpl").getConstructor()
                .newInstance();
    }

    public static void loadPlugins() {
        BRIDGE.loadPlugins();
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

    public static InventoryView openAnvilInventory(Player player, Inventory inventory, String title) {
        return BRIDGE.openAnvilInventory(player, inventory, title);
    }

    public static void openHorseScreen(Tameable horse, Player equipper) {
        BRIDGE.openHorseScreen(horse, equipper);
    }

    public static void playAnimation(PlayerAnimation animation, Player player, int radius) {
        BRIDGE.playAnimation(animation, player, radius);
    }

    public static void playerTick(Player entity) {
        BRIDGE.playerTick(entity);
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

    public static void removeHookIfNecessary(NPCRegistry npcRegistry, FishHook entity) {
        BRIDGE.removeHookIfNecessary(npcRegistry, entity);
    }

    public static void replaceTrackerEntry(Player player) {
        BRIDGE.replaceTrackerEntry(player);
    }

    public static void sendPositionUpdate(Player excluding, org.bukkit.entity.Entity from, Location location) {
        BRIDGE.sendPositionUpdate(excluding, from, location);
    }

    public static void sendRotationNearby(Entity entity, float bodyYaw, float headYaw, float pitch) {
        BRIDGE.sendRotationNearby(entity, bodyYaw, headYaw, pitch);
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

    public static void setAllayDancing(Entity entity, boolean dancing) {
        BRIDGE.setAllayDancing(entity, dancing);
    }

    public static void setBodyYaw(Entity entity, float yaw) {
        BRIDGE.setBodyYaw(entity, yaw);
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

    public static void setEndermanAngry(Enderman enderman, boolean angry) {
        BRIDGE.setEndermanAngry(enderman, angry);
    }

    public static void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        BRIDGE.setHeadYaw(entity, yaw);
    }

    public static void setKnockbackResistance(org.bukkit.entity.LivingEntity entity, double d) {
        BRIDGE.setKnockbackResistance(entity, d);
    }

    public static void setLyingDown(Entity cat, boolean lying) {
        BRIDGE.setLyingDown(cat, lying);
    }

    public static void setNavigationTarget(org.bukkit.entity.Entity handle, org.bukkit.entity.Entity target,
            float speed) {
        BRIDGE.setNavigationTarget(handle, target, speed);
    }

    public static void setNoGravity(Entity entity, boolean nogravity) {
        BRIDGE.setNoGravity(entity, nogravity);
    }

    public static void setPandaSitting(Entity entity, boolean sitting) {
        BRIDGE.setPandaSitting(entity, sitting);
    }

    public static void setPeekShulker(org.bukkit.entity.Entity entity, int peek) {
        if (!entity.getType().name().equals("SHULKER")) {
            throw new IllegalArgumentException("entity must be a shulker");
        }
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

    public static void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        BRIDGE.setStepHeight(entity, height);
    }

    public static void setTeamNameTagVisible(Team team, boolean visible) {
        BRIDGE.setTeamNameTagVisible(team, visible);
    }

    public static void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        BRIDGE.setVerticalMovement(bukkitEntity, d);
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

    public static boolean tick(Entity next) {
        return BRIDGE.tick(next);
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
    private static Method GET_MODULE;
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static Field MODIFIERS_FIELD;
    private static Object UNSAFE;
    private static MethodHandle UNSAFE_FIELD_OFFSET;
    private static MethodHandle UNSAFE_PUT_OBJECT;
    private static MethodHandle UNSAFE_STATIC_FIELD_OFFSET;

    static {
        giveReflectiveAccess(Field.class, NMS.class);
        MODIFIERS_FIELD = NMS.getField(Field.class, "modifiers", false);
    }
}
