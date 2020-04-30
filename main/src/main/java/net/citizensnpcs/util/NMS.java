package net.citizensnpcs.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.skin.SkinnableEntity;

public class NMS {
    private NMS() {
        // util class
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

    /*
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

    public static MethodHandle getFinalSetter(Class<?> clazz, String field, boolean log) {
        Field f;
        if (MODIFIERS_FIELD == null) {
            if (UNSAFE == null) {
                try {
                    UNSAFE = NMS.getField(Class.forName("sun.misc.Unsafe"), "theUnsafe").get(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (log) {
                        Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, e.getLocalizedMessage());
                    }
                    return null;
                }
                UNSAFE_STATIC_FIELD_OFFSET = NMS
                        .getMethodHandle(UNSAFE.getClass(), "staticFieldOffset", true, Field.class).bindTo(UNSAFE);
                UNSAFE_FIELD_OFFSET = NMS.getMethodHandle(UNSAFE.getClass(), "objectFieldOffset", true, Field.class)
                        .bindTo(UNSAFE);
                UNSAFE_PUT_OBJECT = NMS
                        .getMethodHandle(UNSAFE.getClass(), "putObject", true, Object.class, long.class, Object.class)
                        .bindTo(UNSAFE);
            }
            f = NMS.getField(clazz, field, log);
            if (f == null) {
                return null;
            }
            try {
                boolean isStatic = Modifier.isStatic(f.getModifiers());
                long offset = (long) (isStatic ? UNSAFE_STATIC_FIELD_OFFSET.invoke(f) : UNSAFE_FIELD_OFFSET.invoke(f));
                return isStatic ? MethodHandles.insertArguments(UNSAFE_PUT_OBJECT, 0, clazz, offset)
                        : MethodHandles.insertArguments(UNSAFE_PUT_OBJECT, 1, offset);
            } catch (Throwable t) {
                t.printStackTrace();
                if (log) {
                    Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, t.getLocalizedMessage());
                }
                return null;
            }
        }
        f = getField(clazz, field, log);
        if (f == null) {
            return null;
        }
        try {
            MODIFIERS_FIELD.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, e.getLocalizedMessage());
            }
            return null;
        }
        try {
            return LOOKUP.unreflectSetter(f);
        } catch (Exception e) {
            if (log) {
                Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, e.getLocalizedMessage());
            }
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

    public static float getYaw(Entity entity) {
        return BRIDGE.getYaw(entity);
    }

    public static boolean isOnGround(org.bukkit.entity.Entity entity) {
        return BRIDGE.isOnGround(entity);
    }

    public static boolean isValid(Entity entity) {
        return BRIDGE.isValid(entity);
    }

    public static void load(CommandManager commands) {
        BRIDGE.load(commands);
    }

    public static void loadBridge(String rev) throws Exception {
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

    public static void sendPositionUpdate(Player excluding, org.bukkit.entity.Entity from, Location storedLocation) {
        BRIDGE.sendPositionUpdate(excluding, from, storedLocation);
    }

    public static void sendTabListAdd(Player recipient, Player listPlayer) {
        BRIDGE.sendTabListAdd(recipient, listPlayer);
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

    public static void setBodyYaw(Entity entity, float yaw) {
        BRIDGE.setBodyYaw(entity, yaw);
    }

    public static void setDestination(org.bukkit.entity.Entity entity, double x, double y, double z, float speed) {
        BRIDGE.setDestination(entity, x, y, z, speed);
    }

    public static void setDummyAdvancement(Player entity) {
        BRIDGE.setDummyAdvancement(entity);
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

    public static void setNoGravity(Entity entity, boolean enabled) {
        BRIDGE.setNoGravity(entity, enabled);
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

    public static void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        BRIDGE.setStepHeight(entity, height);
    }

    public static void setTeamNameTagVisible(Team team, boolean visible) {
        BRIDGE.setTeamNameTagVisible(team, visible);
    }

    public static void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        BRIDGE.setVerticalMovement(bukkitEntity, d);
    }

    public static void setWitherCharged(Wither wither, boolean charged) {
        BRIDGE.setWitherCharged(wither, charged);
    }

    public static boolean shouldJump(org.bukkit.entity.Entity entity) {
        return BRIDGE.shouldJump(entity);
    }

    public static void shutdown() {
        BRIDGE.shutdown();
    }

    public static boolean tick(Entity next) {
        return BRIDGE.tick(next);
    }

    public static void trySwim(org.bukkit.entity.Entity entity) {
        BRIDGE.trySwim(entity);
    }

    public static void trySwim(org.bukkit.entity.Entity entity, float power) {
        BRIDGE.trySwim(entity, power);
    }

    public static void updateNavigationWorld(org.bukkit.entity.Entity entity, org.bukkit.World world) {
        BRIDGE.updateNavigationWorld(entity, world);
    }

    public static void updatePathfindingRange(NPC npc, float pathfindingRange) {
        BRIDGE.updatePathfindingRange(npc, pathfindingRange);
    }

    private static NMSBridge BRIDGE;
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static Field MODIFIERS_FIELD = NMS.getField(Field.class, "modifiers", false);
    private static Object UNSAFE;
    private static MethodHandle UNSAFE_FIELD_OFFSET;
    private static MethodHandle UNSAFE_PUT_OBJECT;
    private static MethodHandle UNSAFE_STATIC_FIELD_OFFSET;
}
