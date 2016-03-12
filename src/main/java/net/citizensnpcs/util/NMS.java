package net.citizensnpcs.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftSound;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginLoadOrder;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.util.UUIDTypeAdapter;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.npc.network.EmptyChannel;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.nms.PlayerlistTrackerEntry;
import net.minecraft.server.v1_9_R1.AttributeInstance;
import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.ControllerJump;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.DataWatcherObject;
import net.minecraft.server.v1_9_R1.EnchantmentManager;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityFishingHook;
import net.minecraft.server.v1_9_R1.EntityHorse;
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.EntityInsentient;
import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EntityRabbit;
import net.minecraft.server.v1_9_R1.EntityTameableAnimal;
import net.minecraft.server.v1_9_R1.EntityTracker;
import net.minecraft.server.v1_9_R1.EntityTrackerEntry;
import net.minecraft.server.v1_9_R1.EntityTypes;
import net.minecraft.server.v1_9_R1.GenericAttributes;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.MobEffects;
import net.minecraft.server.v1_9_R1.NavigationAbstract;
import net.minecraft.server.v1_9_R1.NetworkManager;
import net.minecraft.server.v1_9_R1.Packet;
import net.minecraft.server.v1_9_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_9_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_9_R1.Vec3D;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldServer;

@SuppressWarnings("unchecked")
public class NMS {

    private NMS() {
        // util class
    }

    public static void addOrRemoveFromPlayerList(org.bukkit.entity.Entity entity, boolean remove) {
        if (entity == null)
            return;
        EntityHuman handle = (EntityHuman) getHandle(entity);
        if (handle.world == null)
            return;
        if (remove) {
            handle.world.players.remove(handle);
        } else if (!handle.world.players.contains(handle)) {
            handle.world.players.add(handle);
        }
        PlayerUpdateTask.addOrRemove(entity, remove);
    }

    public static boolean addToWorld(org.bukkit.World world, org.bukkit.entity.Entity entity,
            CreatureSpawnEvent.SpawnReason reason) {
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(entity);
        Preconditions.checkNotNull(reason);

        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        return ((CraftWorld) world).getHandle().addEntity(nmsEntity, reason);
    }

    public static void attack(EntityLiving handle, Entity target) {
        AttributeInstance attackDamage = handle.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
        float f = (float) (attackDamage == null ? 1 : attackDamage.getValue());
        int i = 0;

        if (target instanceof EntityLiving) {
            f += EnchantmentManager.a(handle.getItemInMainHand(), ((EntityLiving) target).getMonsterType());
            i += EnchantmentManager.a(handle);
        }

        boolean flag = target.damageEntity(DamageSource.mobAttack(handle), f);

        if (!flag)
            return;
        if (i > 0) {
            target.g(-Math.sin(handle.yaw * Math.PI / 180.0F) * i * 0.5F, 0.1D,
                    Math.cos(handle.yaw * Math.PI / 180.0F) * i * 0.5F);
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

    public static float clampYaw(float yaw) {
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }
        return yaw;
    }

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        if (GOAL_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                Collection<?> list = (Collection<?>) NMS.GOAL_FIELD.get(selector);
                list.clear();
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            }
        }
    }

    /*
     * Yggdrasil's default implementation of this method silently fails instead of throwing
     * an Exception like it should.
     */
    public static GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Exception {

        if (Bukkit.isPrimaryThread())
            throw new IllegalStateException("NMS.fillProfileProperties cannot be invoked from the main thread.");

        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer().ay();

        YggdrasilAuthenticationService auth = ((YggdrasilMinecraftSessionService) sessionService)
                .getAuthenticationService();

        URL url = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/"
                + UUIDTypeAdapter.fromUUID(profile.getId()));

        url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);

        MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse) MAKE_REQUEST.invoke(auth,
                url, null, MinecraftProfilePropertiesResponse.class);
        if (response == null)
            return profile;

        GameProfile result = new GameProfile(response.getId(), response.getName());
        result.getProperties().putAll(response.getProperties());
        profile.getProperties().putAll(response.getProperties());

        return result;
    }

    public static void flyingMoveLogic(EntityLiving entity, float f, float f1) {
        if (entity.co() || entity.bx()) {
            if (entity.isInWater()) {
                double d0 = entity.locY;
                float f3 = 0.8F;
                float f4 = 0.02F;
                float f2 = EnchantmentManager.d(entity);
                if (f2 > 3.0F) {
                    f2 = 3.0F;
                }

                if (!entity.onGround) {
                    f2 *= 0.5F;
                }

                if (f2 > 0.0F) {
                    f3 += (0.5460001F - f3) * f2 / 3.0F;
                    f4 += (entity.ck() * 1.0F - f4) * f2 / 3.0F;
                }

                entity.a(f, f1, f4);
                entity.move(entity.motX, entity.motY, entity.motZ);
                entity.motX *= f3;
                entity.motY *= 0.800000011920929D;
                entity.motZ *= f3;
                entity.motY -= 0.02D;
                if ((entity.positionChanged)
                        && (entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d0, entity.motZ)))
                    entity.motY = 0.300000011920929D;
            } else if (entity.an()) {
                double d0 = entity.locY;
                entity.a(f, f1, 0.02F);
                entity.move(entity.motX, entity.motY, entity.motZ);
                entity.motX *= 0.5D;
                entity.motY *= 0.5D;
                entity.motZ *= 0.5D;
                entity.motY -= 0.02D;
                if ((entity.positionChanged)
                        && (entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d0, entity.motZ)))
                    entity.motY = 0.300000011920929D;
            } else if (entity.cB()) {
                if (entity.motY > -0.5D) {
                    entity.fallDistance = 1.0F;
                }
                Vec3D vec3d = entity.aB();
                float f5 = entity.pitch * 0.017453292F;

                double d0 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                double d2 = Math.sqrt(entity.motX * entity.motX + entity.motZ * entity.motZ);
                double d3 = vec3d.b();
                float f6 = MathHelper.cos(f5);

                f6 = (float) (f6 * f6 * Math.min(1.0D, d3 / 0.4D));
                entity.motY += -0.08D + f6 * 0.06D;
                if ((entity.motY < 0.0D) && (d0 > 0.0D)) {
                    double d4 = entity.motY * -0.1D * f6;
                    entity.motY += d4;
                    entity.motX += vec3d.x * d4 / d0;
                    entity.motZ += vec3d.z * d4 / d0;
                }
                if (f5 < 0.0F) {
                    double d4 = d2 * -MathHelper.sin(f5) * 0.04D;
                    entity.motY += d4 * 3.2D;
                    entity.motX -= vec3d.x * d4 / d0;
                    entity.motZ -= vec3d.z * d4 / d0;
                }
                if (d0 > 0.0D) {
                    entity.motX += (vec3d.x / d0 * d2 - entity.motX) * 0.1D;
                    entity.motZ += (vec3d.z / d0 * d2 - entity.motZ) * 0.1D;
                }
                entity.motX *= 0.9900000095367432D;
                entity.motY *= 0.9800000190734863D;
                entity.motZ *= 0.9900000095367432D;
                entity.move(entity.motX, entity.motY, entity.motZ);
                if ((entity.positionChanged) && (!entity.world.isClientSide)) {
                    double d4 = Math.sqrt(entity.motX * entity.motX + entity.motZ * entity.motZ);
                    double d5 = d2 - d4;
                    float f7 = (float) (d5 * 10.0D - 3.0D);
                    if (f7 > 0.0F) {
                        entity.a(entity.e((int) f7), 1.0F, 1.0F);
                        entity.damageEntity(DamageSource.j, f7);
                    }
                }
                if ((entity.onGround) && (!entity.world.isClientSide)) {
                    entity.setFlag(7, false);
                }
            } else {
                float f8 = 0.91F;
                BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                        .c(entity.locX, entity.getBoundingBox().b - 1.0D, entity.locZ);
                if (entity.onGround) {
                    f8 = entity.world.getType(blockposition_pooledblockposition).getBlock().frictionFactor * 0.91F;
                }
                float f4 = 0.16277136F / (f8 * f8 * f8);
                float f3;
                if (entity.onGround) {
                    f3 = entity.ck() * f4;
                } else {
                    f3 = entity.aQ;
                }
                entity.a(f, f1, f3);
                f8 = 0.91F;
                if (entity.onGround) {
                    f8 = entity.world.getType(blockposition_pooledblockposition.d(entity.locX,
                            entity.getBoundingBox().b - 1.0D, entity.locZ)).getBlock().frictionFactor * 0.91F;
                }
                if (entity.n_()) {
                    float f2 = 0.15F;
                    entity.motX = MathHelper.a(entity.motX, -f2, f2);
                    entity.motZ = MathHelper.a(entity.motZ, -f2, f2);
                    entity.fallDistance = 0.0F;
                    if (entity.motY < -0.15D) {
                        entity.motY = -0.15D;
                    }
                    boolean flag = (entity.isSneaking()) && ((entity instanceof EntityHuman));
                    if ((flag) && (entity.motY < 0.0D)) {
                        entity.motY = 0.0D;
                    }
                }
                entity.move(entity.motX, entity.motY, entity.motZ);
                if ((entity.positionChanged) && (entity.n_())) {
                    entity.motY = 0.2D;
                }
                if (entity.hasEffect(MobEffects.LEVITATION)) {
                    entity.motY += (0.05D * (entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - entity.motY)
                            * 0.2D;
                } else {
                    blockposition_pooledblockposition.d(entity.locX, 0.0D, entity.locZ);
                    if ((entity.world.isClientSide) && ((!entity.world.isLoaded(blockposition_pooledblockposition))
                            || (!entity.world.getChunkAtWorldCoords(blockposition_pooledblockposition).p()))) {
                        if (entity.locY > 0.0D) {
                            entity.motY = -0.1D;
                        } else {
                            entity.motY = 0.0D;
                        }
                    } else {
                        entity.motY -= 0.08D;
                    }
                }
                entity.motY *= 0.9800000190734863D;
                entity.motX *= f8;
                entity.motZ *= f8;
                blockposition_pooledblockposition.t();
            }
        }

        entity.aE = entity.aF;
        double d0 = entity.locX - entity.lastX;
        double d1 = entity.locZ - entity.lastZ;

        float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
        if (f2 > 1.0F) {
            f2 = 1.0F;
        }

        entity.aF += (f2 - entity.aF) * 0.4F;
        entity.aG += entity.aF;
    }

    @SuppressWarnings("deprecation")
    private static Constructor<?> getCustomEntityConstructor(Class<?> clazz, EntityType type)
            throws SecurityException, NoSuchMethodException {
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

    public static GameProfileRepository getGameProfileRepository() {
        return ((CraftServer) Bukkit.getServer()).getServer().getGameProfileRepository();
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
        return handle.aO;
    }

    public static NavigationAbstract getNavigation(Entity handle) {
        return handle instanceof EntityInsentient ? ((EntityInsentient) handle).getNavigation()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getNavigation() : null;
    }

    public static GameProfile getProfile(SkullMeta meta) {
        if (SKULL_PROFILE_FIELD == null) {
            try {
                SKULL_PROFILE_FIELD = meta.getClass().getDeclaredField("profile");
                SKULL_PROFILE_FIELD.setAccessible(true);
            } catch (Exception e) {
                return null;
            }
        }
        try {
            return (GameProfile) SKULL_PROFILE_FIELD.get(meta);
        } catch (Exception e) {
            return null;
        }
    }

    public static DataWatcherObject<Integer> getRabbitTypeField() {
        if (RABBIT_FIELD == null)
            return null;
        try {
            return (DataWatcherObject<Integer>) RABBIT_FIELD.get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static SkinnableEntity getSkinnable(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity);

        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (nmsEntity instanceof SkinnableEntity) {
            return (SkinnableEntity) nmsEntity;
        }
        return null;
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

    public static float getStepHeight(org.bukkit.entity.Entity entity) {
        return NMS.getHandle(entity).P;
    }

    public static void initNetworkManager(NetworkManager network) {
        if (NETWORK_ADDRESS == null)
            return;
        try {
            network.channel = new EmptyChannel(null);
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
        return mcEntity.aj() || mcEntity.an();
    }

    public static boolean isNavigationFinished(NavigationAbstract navigation) {
        return navigation.n();
    }

    public static void loadPlugins() {
        ((CraftServer) Bukkit.getServer()).enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    public static void look(Entity handle, Entity target) {
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerLook().a(target, 10.0F, ((EntityInsentient) handle).N());
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setTargetLook(target, 10F, 40F);
        }
    }

    public static void look(org.bukkit.entity.Entity entity, float yaw, float pitch) {
        Entity handle = getHandle(entity);
        if (handle == null)
            return;
        yaw = clampYaw(yaw);
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
            minecart.setDisplayBlock(Block.getById(mat.getId()).fromLegacyData(data));
        }
        minecart.setDisplayBlockOffset(offset);
    }

    public static float modifiedSpeed(float baseSpeed, NPC npc) {
        return npc == null ? baseSpeed : baseSpeed * npc.getNavigator().getLocalParameters().speedModifier();
    }

    public static void mount(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity passenger) {
        if (NMS.getHandle(passenger) == null)
            return;
        NMS.getHandle(entity).startRiding(NMS.getHandle(passenger));
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
        if (ENTITY_CLASS_TO_INT == null || ENTITY_CLASS_TO_INT.containsKey(clazz))
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            if (!ENTITY_CLASS_TO_INT.containsKey(search))
                continue;
            int code = ENTITY_CLASS_TO_INT.get(search);
            ENTITY_CLASS_TO_INT.put(clazz, code);
            ENTITY_CLASS_TO_NAME.put(clazz, ENTITY_CLASS_TO_NAME.get(search));
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass for class " + clazz.toString());
    }

    public static void removeFromServerPlayerList(Player player) {
        EntityPlayer handle = (EntityPlayer) NMS.getHandle(player);
        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(handle);
    }

    public static void removeFromWorld(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity);

        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        nmsEntity.world.removeEntity(nmsEntity);
    }

    public static void removeHookIfNecessary(NPCRegistry npcRegistry, FishHook entity) {
        EntityFishingHook hook = (EntityFishingHook) NMS.getHandle(entity);
        if (hook.hooked == null)
            return;
        NPC npc = npcRegistry.getNPC(hook.hooked.getBukkitEntity());
        if (npc == null)
            return;
        if (npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)) {
            hook.hooked = null;
            hook.die();
        }
    }

    @SuppressWarnings("rawtypes")
    public static void replaceTrackerEntry(Player player) {
        WorldServer server = (WorldServer) NMS.getHandle(player).getWorld();
        EntityTrackerEntry entry = server.getTracker().trackedEntities.get(player.getEntityId());
        if (entry == null)
            return;
        PlayerlistTrackerEntry replace = new PlayerlistTrackerEntry(entry);
        server.getTracker().trackedEntities.a(player.getEntityId(), replace);
        if (TRACKED_ENTITY_SET != null) {
            try {
                Set set = (Set) TRACKED_ENTITY_SET.get(server.getTracker());
                set.remove(entry);
                set.add(replace);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        if (packet == null)
            return;
        ((EntityPlayer) NMS.getHandle(player)).playerConnection.sendPacket(packet);
    }

    public static void sendPacketNearby(Player from, Location location, Packet<?> packet) {
        NMS.sendPacketNearby(from, location, packet, 64);
    }

    public static void sendPacketNearby(Player from, Location location, Packet<?> packet, double radius) {
        List<Packet<?>> list = new ArrayList<Packet<?>>();
        list.add(packet);
        NMS.sendPacketsNearby(from, location, list, radius);
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet<?>> packets) {
        NMS.sendPacketsNearby(from, location, packets, 64);
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet<?>> packets, double radius) {
        radius *= radius;
        final org.bukkit.World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld() || (from != null && !ply.canSee(from))) {
                continue;
            }
            if (location.distanceSquared(ply.getLocation(PACKET_CACHE_LOCATION)) > radius) {
                continue;
            }
            for (Packet<?> packet : packets) {
                sendPacket(ply, packet);
            }
        }
    }

    public static void sendPacketsNearby(Player from, Location location, Packet<?>... packets) {
        NMS.sendPacketsNearby(from, location, Arrays.asList(packets), 64);
    }

    public static void sendTabListAdd(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);

        EntityPlayer entity = ((CraftPlayer) listPlayer).getHandle();

        sendPacket(recipient,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity));
    }

    public static void sendTabListRemove(Player recipient, Collection<? extends SkinnableEntity> skinnableNPCs) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(skinnableNPCs);

        EntityPlayer[] entities = new EntityPlayer[skinnableNPCs.size()];
        int i = 0;
        for (SkinnableEntity skinnable : skinnableNPCs) {
            entities[i] = (EntityPlayer) skinnable;
            i++;
        }

        sendPacket(recipient,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entities));
    }

    public static void sendTabListRemove(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);

        EntityPlayer entity = ((CraftPlayer) listPlayer).getHandle();

        sendPacket(recipient,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity));
    }

    public static void sendToOnline(Packet<?>... packets) {
        Validate.notNull(packets, "packets cannot be null");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline())
                continue;
            for (Packet<?> packet : packets) {
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
        yaw = clampYaw(yaw);
        handle.aO = yaw;
        if (!(handle instanceof EntityHuman))
            handle.aM = yaw;
        handle.aP = yaw;
    }

    public static void setProfile(SkullMeta meta, GameProfile profile) {
        if (SKULL_PROFILE_FIELD == null) {
            try {
                SKULL_PROFILE_FIELD = meta.getClass().getDeclaredField("profile");
                SKULL_PROFILE_FIELD.setAccessible(true);
            } catch (Exception e) {
                return;
            }
        }
        try {
            SKULL_PROFILE_FIELD.set(meta, profile);
        } catch (Exception e) {
        }
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

    public static void setSitting(Tameable tameable, boolean sitting) {
        ((EntityTameableAnimal) getHandle((LivingEntity) tameable)).setSitting(sitting);
    }

    public static void setSize(Entity entity, float f, float f1, boolean justCreated) {
        if ((f != entity.width) || (f1 != entity.length)) {
            float f2 = entity.width;

            entity.width = f;
            entity.length = f1;
            entity.a(new AxisAlignedBB(entity.getBoundingBox().a, entity.getBoundingBox().b, entity.getBoundingBox().c,
                    entity.getBoundingBox().a + entity.width, entity.getBoundingBox().b + entity.length,
                    entity.getBoundingBox().c + entity.width));
            if ((entity.width > f2) && (!justCreated) && (!entity.world.isClientSide))
                entity.move((f2 - entity.width) / 2, 0.0D, (f2 - entity.width) / 2);
        }
    }

    public static void setStepHeight(EntityLiving entity, float height) {
        entity.P = height;
    }

    public static void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        if (!bukkitEntity.getType().isAlive())
            return;
        EntityLiving handle = NMS.getHandle((LivingEntity) bukkitEntity);
        handle.be = (float) d;
    }

    public static boolean shouldJump(net.minecraft.server.v1_9_R1.Entity entity) {
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
        navigation.l();
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
    private static Field GOAL_FIELD = getField(PathfinderGoalSelector.class, "b");
    private static final Field JUMP_FIELD = getField(EntityLiving.class, "bc");
    private static Method MAKE_REQUEST;
    private static Field NAVIGATION_WORLD_FIELD = getField(NavigationAbstract.class, "b");
    private static Field NETWORK_ADDRESS = getField(NetworkManager.class, "l");
    private static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static Field PATHFINDING_RANGE = getField(NavigationAbstract.class, "g");
    private static final Field RABBIT_FIELD = getField(EntityRabbit.class, "bv");

    private static final Random RANDOM = Util.getFastRandom();

    private static Field SKULL_PROFILE_FIELD;

    private static Field TRACKED_ENTITY_SET = NMS.getField(EntityTracker.class, "c");

    static {
        try {
            Field field = getField(EntityTypes.class, "f");
            ENTITY_CLASS_TO_INT = (Map<Class<?>, Integer>) field.get(null);
            field = getField(EntityTypes.class, "d");
            ENTITY_CLASS_TO_NAME = (Map<Class<?>, String>) field.get(null);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_ID_MAPPING, e.getMessage());
        }

        try {
            MAKE_REQUEST = YggdrasilAuthenticationService.class.getDeclaredMethod("makeRequest", URL.class,
                    Object.class, Class.class);
            MAKE_REQUEST.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
