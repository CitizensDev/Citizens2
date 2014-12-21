package net.citizensnpcs.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.npc.network.EmptyChannel;
import net.minecraft.server.v1_8_R1.AttributeInstance;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.ControllerJump;
import net.minecraft.server.v1_8_R1.DamageSource;
import net.minecraft.server.v1_8_R1.EnchantmentManager;
import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityHorse;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EntityTypes;
import net.minecraft.server.v1_8_R1.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import net.minecraft.server.v1_8_R1.MathHelper;
import net.minecraft.server.v1_8_R1.NavigationAbstract;
import net.minecraft.server.v1_8_R1.NetworkManager;
import net.minecraft.server.v1_8_R1.Packet;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R1.World;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.CraftSound;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLoadOrder;

@SuppressWarnings("unchecked")
public class NMS {
    private NMS() {
        // util class
    }

    public static void addOrRemoveFromPlayerList(org.bukkit.entity.Entity entity, boolean remove) {
        if (entity == null)
            return;
        Entity handle = getHandle(entity);
        if (handle.world == null)
            return;
        if (remove) {
            handle.world.players.remove(handle);
        } else if (!handle.world.players.contains(handle)) {
            handle.world.players.add(handle);
        }
    }

    public static void attack(EntityLiving handle, Entity target) {
        AttributeInstance attackDamage = handle.getAttributeInstance(GenericAttributes.e);
        float f = (float) (attackDamage == null ? 1 : attackDamage.getValue());
        int i = 0;

        if (target instanceof EntityLiving) {
            f += EnchantmentManager.a(handle.bz(), ((EntityLiving) target).getMonsterType());
            i += EnchantmentManager.a(handle);
        }

        boolean flag = target.damageEntity(DamageSource.mobAttack(handle), f);

        if (!flag)
            return;
        if (i > 0) {
            target.g(-Math.sin(handle.yaw * Math.PI / 180.0F) * i * 0.5F, 0.1D, Math.cos(handle.yaw * Math.PI / 180.0F)
                    * i * 0.5F);
            handle.motX *= 0.6D;
            handle.motZ *= 0.6D;
        }

        int fireAspectLevel = EnchantmentManager.getFireAspectEnchantmentLevel(handle);

        if (fireAspectLevel > 0) {
            target.setOnFire(fireAspectLevel * 4);
        }
    }

    public static void changeWorlds(org.bukkit.entity.Entity entity, org.bukkit.World world) {
        getHandle(entity).world = ((CraftWorld) world).getHandle();
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

    public static void flyingMoveLogic(EntityLiving entity, float f, float f1) {
        if (entity.V()) {
            entity.a(f, f1, 0.02F);
            entity.move(entity.motX, entity.motY, entity.motZ);

            entity.motX *= 0.800000011920929D;
            entity.motY *= 0.800000011920929D;
            entity.motZ *= 0.800000011920929D;
        } else if (entity.ab()) {
            entity.a(f, f1, 0.02F);
            entity.move(entity.motX, entity.motY, entity.motZ);
            entity.motX *= 0.5D;
            entity.motY *= 0.5D;
            entity.motZ *= 0.5D;
        } else {
            float c1 = 0.91F;
            if (entity.onGround) {
                c1 = entity.world.getType(
                        new BlockPosition(MathHelper.floor(entity.locX),
                                MathHelper.floor(entity.getBoundingBox().b) - 1, MathHelper.floor(entity.locZ)))
                        .getBlock().frictionFactor * 0.91F;
            }

            float f2 = 0.1627714F / (c1 * c1 * c1);
            entity.a(f, f1, entity.onGround ? 0.1F * f2 : 0.02F);

            c1 = 0.91F;
            if (entity.onGround) {
                c1 = entity.world.getType(
                        new BlockPosition(MathHelper.floor(entity.locX),
                                MathHelper.floor(entity.getBoundingBox().b) - 1, MathHelper.floor(entity.locZ)))
                        .getBlock().frictionFactor * 0.91F;
            }

            entity.move(entity.motX, entity.motY, entity.motZ);

            entity.motX *= c1;
            entity.motY *= c1;
            entity.motZ *= c1;
        }
        entity.ay = entity.az;
        double d1 = entity.locX - entity.lastX;
        double d2 = entity.locZ - entity.lastZ;
        float f3 = MathHelper.sqrt(d1 * d1 + d2 * d2) * 4.0F;
        if (f3 > 1.0F) {
            f3 = 1.0F;
        }
        entity.az += (f3 - entity.az) * 0.4F;
        entity.aA += entity.az;
    }

    @SuppressWarnings("deprecation")
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
        if (clazz == null)
            return null;
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
        return (EntityLiving) getHandle((org.bukkit.entity.Entity) entity);
    }

    public static Entity getHandle(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

    public static float getHeadYaw(EntityLiving handle) {
        return handle.aI;
    }

    public static NavigationAbstract getNavigation(Entity handle) {
        return handle instanceof EntityInsentient ? ((EntityInsentient) handle).getNavigation()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getNavigation() : null;
    }

    public static String getSound(String flag) throws CommandException {
        try {
            String ret = CraftSound.getSound(Sound.valueOf(flag.toUpperCase()));
            if (ret == null)
                throw new CommandException(Messages.INVALID_SOUND);
            return ret;
        } catch (Exception e) {
            throw new CommandException(Messages.INVALID_SOUND);
        }
    }

    public static float getSpeedFor(NPC npc) {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity))
            return DEFAULT_SPEED;
        EntityLiving handle = NMS.getHandle((LivingEntity) npc.getEntity());
        if (handle == null)
            return DEFAULT_SPEED;
        return DEFAULT_SPEED;
        // return (float)
        // handle.getAttributeInstance(GenericAttributes.d).getValue();
    }

    public static float getStepHeight(LivingEntity entity) {
        return NMS.getHandle(entity).S;
    }

    public static void initNetworkManager(NetworkManager network) {
        if (NETWORK_CHANNEL == null || NETWORK_ADDRESS == null)
            return;
        try {
            NETWORK_CHANNEL.set(network, new EmptyChannel(null));
            NETWORK_ADDRESS.set(network, new SocketAddress() {
                private static final long serialVersionUID = 8207338859896320185L;
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static boolean inWater(org.bukkit.entity.Entity entity) {
        Entity mcEntity = getHandle(entity);
        if (mcEntity == null)
            return false;
        return mcEntity.W() || mcEntity.ab();
    }

    public static boolean isNavigationFinished(NavigationAbstract navigation) {
        return navigation.m();
    }

    public static void loadPlugins() {
        ((CraftServer) Bukkit.getServer()).enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    public static void look(Entity handle, Entity target) {
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerLook().a(target, 10.0F, ((EntityInsentient) handle).bP());
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setTargetLook(target, 10F, 40F);
        }
    }

    public static void look(org.bukkit.entity.Entity entity, float yaw, float pitch) {
        Entity handle = getHandle(entity);
        if (handle == null)
            return;
        handle.yaw = yaw;
        setHeadYaw(handle, yaw);
        handle.pitch = pitch;
    }

    @SuppressWarnings("deprecation")
    public static void minecartItemLogic(EntityMinecartAbstract minecart) {
        NPC npc = ((NPCHolder) minecart).getNPC();
        if (npc == null)
            return;
        Material mat = Material.getMaterial(npc.data().get(NPC.MINECART_ITEM_METADATA, ""));
        int data = npc.data().get(NPC.MINECART_ITEM_DATA_METADATA, 0);
        int offset = npc.data().get(NPC.MINECART_OFFSET_METADATA, 0);
        minecart.a(mat != null);
        if (mat != null) {
            minecart.a(Block.getById(mat.getId()).fromLegacyData(data));
        }
        minecart.l(offset);
    }

    public static float modifiedSpeed(float baseSpeed, NPC npc) {
        return npc == null ? baseSpeed : baseSpeed * npc.getNavigator().getLocalParameters().speedModifier();
    }

    public static void mount(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity passenger) {
        if (NMS.getHandle(passenger) == null)
            return;
        NMS.getHandle(passenger).mount(NMS.getHandle(entity));
    }

    public static void openHorseScreen(Horse horse, Player equipper) {
        EntityLiving handle = NMS.getHandle(horse);
        EntityLiving equipperHandle = NMS.getHandle(equipper);
        if (handle == null || equipperHandle == null)
            return;
        boolean wasTamed = horse.isTamed();
        horse.setTamed(true);
        ((EntityHorse) handle).a((EntityHuman) equipperHandle);
        horse.setTamed(wasTamed);
    }

    public static void registerEntityClass(Class<?> clazz) {
        if (ENTITY_CLASS_TO_INT == null) {
            ENTITY_CLASS_TO_INT = MC_ENTITY_CLASS_TO_INT;
            ENTITY_INT_TO_CLASS = MC_ENTITY_INT_TO_CLASS;
        }
        if (ENTITY_CLASS_TO_INT == null || ENTITY_CLASS_TO_INT.containsKey(clazz))
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            if (!ENTITY_CLASS_TO_INT.containsKey(search))
                continue;
            int code = ENTITY_CLASS_TO_INT.get(search);
            ENTITY_INT_TO_CLASS.put(code, clazz);
            ENTITY_CLASS_TO_INT.put(clazz, code);
            ENTITY_CLASS_TO_NAME.put(clazz, ENTITY_CLASS_TO_NAME.get(search));
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass");
    }

    public static void removeFromServerPlayerList(Player player) {
        EntityPlayer handle = (EntityPlayer) NMS.getHandle(player);
        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(handle);
    }

    private static void sendDestroyPacket(final Player player, final NPC npc) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(npc.getEntity()
                .getEntityId()));
    }

    public static void sendPacket(Player player, Packet packet) {
        if (packet == null)
            return;
        ((EntityPlayer) NMS.getHandle(player)).playerConnection.sendPacket(packet);
    }

    public static void sendPacketNearby(Player from, Location location, Packet packet) {
        NMS.sendPacketsNearby(from, location, Arrays.asList(packet), 64);
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet> packets) {
        NMS.sendPacketsNearby(from, location, packets, 64);
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet> packets, double radius) {
        radius *= radius;
        final org.bukkit.World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld() || (from != null && !ply.canSee(from))) {
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

    public static void sendPacketsNearby(Player from, Location location, Packet... packets) {
        NMS.sendPacketsNearby(from, location, Arrays.asList(packets), 64);
    }

    /**
     * Send a PlayerInfo packet (adds or removes the NPC to or from the tab
     * list) to the player.
     *
     * @param player
     *            The player to send the packet to, or null for all players.
     */
    public static void sendPlayerlistPacket(boolean showInPlayerlist, Player player, NPC npc) {
        sendPlayerlistPacket(showInPlayerlist, player, (CraftPlayer) npc.getEntity());
    }

    public static void sendPlayerlistPacket(boolean showInPlayerlist, Player player, Player npc) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(showInPlayerlist ? EnumPlayerInfoAction.ADD_PLAYER
                : EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) npc).getHandle());
        if (player == null) {
            sendToOnline(packet);
        } else {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private static void sendSpawnPacket(final Player player, final NPC npc) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(
                ((CraftPlayer) npc.getEntity()).getHandle()));
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

    public static void setDestination(org.bukkit.entity.Entity entity, double x, double y, double z, float speed) {
        Entity handle = NMS.getHandle(entity);
        if (handle == null)
            return;
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerMove().a(x, y, z, speed);
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setMoveDestination(x, y, z, speed);
        }
    }

    public static void setHeadYaw(Entity en, float yaw) {
        if (!(en instanceof EntityLiving))
            return;
        EntityLiving handle = (EntityLiving) en;
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }
        handle.aI = yaw;
        if (!(handle instanceof EntityHuman))
            handle.aG = yaw;
        handle.aJ = yaw;
    }

    public static void setShouldJump(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        if (handle == null)
            return;
        if (handle instanceof EntityInsentient) {
            ControllerJump controller = ((EntityInsentient) handle).getControllerJump();
            controller.a();
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setShouldJump();
        }
    }

    public static void setStepHeight(EntityLiving entity, float height) {
        entity.S = height;
    }

    public static void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        if (!bukkitEntity.getType().isAlive())
            return;
        EntityLiving handle = NMS.getHandle((LivingEntity) bukkitEntity);
        handle.aY = (float) d;
    }

    public static boolean shouldJump(net.minecraft.server.v1_8_R1.Entity entity) {
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

    public static void showNPCReset(final Player player, final NPC npc) {
        sendDestroyPacket(player, npc);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (player.isOnline() && player.isValid() && npc.isSpawned()
                        && npc.getEntity().getType() == EntityType.PLAYER) {
                    sendPlayerlistPacket(true, player, npc);
                    sendSpawnPacket(player, npc);
                }
            }
        }, 1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (player.isOnline() && player.isValid() && npc.isSpawned()
                        && npc.getEntity().getType() == EntityType.PLAYER) {
                    sendPlayerlistPacket(false, player, npc);
                }
            }
        }, 2);
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

    public static void stopNavigation(NavigationAbstract navigation) {
        navigation.n();
    }

    public static void trySwim(org.bukkit.entity.Entity entity) {
        trySwim(entity, 0.04F);
    }

    public static void trySwim(org.bukkit.entity.Entity entity, float power) {
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

    public static void updateNavigation(NavigationAbstract navigation) {
        navigation.k();
    }

    public static void updateNavigationWorld(org.bukkit.entity.Entity entity, org.bukkit.World world) {
        if (NAVIGATION_WORLD_FIELD == null)
            return;
        Entity en = NMS.getHandle(entity);
        if (en == null || !(en instanceof EntityInsentient))
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
        if (!npc.isSpawned() || !npc.getEntity().getType().isAlive())
            return;
        EntityLiving en = NMS.getHandle((LivingEntity) npc.getEntity());
        if (!(en instanceof EntityInsentient)) {
            if (en instanceof EntityHumanNPC) {
                ((EntityHumanNPC) en).updatePathfindingRange(pathfindingRange);
            }
            return;
        }
        if (PATHFINDING_RANGE == null)
            return;
        EntityInsentient handle = (EntityInsentient) en;
        NavigationAbstract navigation = handle.getNavigation();
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
    private static Map<Class<?>, String> ENTITY_CLASS_TO_NAME;
    private static final Map<Class<?>, Constructor<?>> ENTITY_CONSTRUCTOR_CACHE = new WeakHashMap<Class<?>, Constructor<?>>();
    private static Map<Integer, Class<?>> ENTITY_INT_TO_CLASS;
    private static Field GOAL_FIELD = getField(PathfinderGoalSelector.class, "b");
    private static final Field JUMP_FIELD = getField(EntityLiving.class, "aW");
    private static Map<Class<?>, Integer> MC_ENTITY_CLASS_TO_INT = null;
    private static Map<Integer, Class<?>> MC_ENTITY_INT_TO_CLASS = null;
    private static Field NAVIGATION_WORLD_FIELD = getField(NavigationAbstract.class, "c");
    private static Field NETWORK_ADDRESS = getField(NetworkManager.class, "j");
    private static Field NETWORK_CHANNEL = getField(NetworkManager.class, "i");
    private static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static Field PATHFINDING_RANGE = getField(NavigationAbstract.class, "a");
    private static final Random RANDOM = Util.getFastRandom();

    static {
        try {
            Field field = getField(EntityTypes.class, "e");
            ENTITY_INT_TO_CLASS = (Map<Integer, Class<?>>) field.get(null);
            field = getField(EntityTypes.class, "f");
            ENTITY_CLASS_TO_INT = (Map<Class<?>, Integer>) field.get(null);
            field = getField(EntityTypes.class, "c");
            ENTITY_CLASS_TO_NAME = (Map<Class<?>, String>) field.get(null);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_ID_MAPPING, e.getMessage());
            try {
                Field field = getField(Class.forName("ns"), "d");
                MC_ENTITY_INT_TO_CLASS = (Map<Integer, Class<?>>) field.get(null);
                field = getField(Class.forName("ns"), "e");
                MC_ENTITY_CLASS_TO_INT = (Map<Class<?>, Integer>) field.get(null);
            } catch (Exception e2) {
            }
        }
    }
}
