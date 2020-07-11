package net.citizensnpcs.nms.v1_8_R3.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWither;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.util.UUIDTypeAdapter;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.nms.v1_8_R3.entity.BatController;
import net.citizensnpcs.nms.v1_8_R3.entity.BlazeController;
import net.citizensnpcs.nms.v1_8_R3.entity.CaveSpiderController;
import net.citizensnpcs.nms.v1_8_R3.entity.ChickenController;
import net.citizensnpcs.nms.v1_8_R3.entity.CowController;
import net.citizensnpcs.nms.v1_8_R3.entity.CreeperController;
import net.citizensnpcs.nms.v1_8_R3.entity.EnderDragonController;
import net.citizensnpcs.nms.v1_8_R3.entity.EndermanController;
import net.citizensnpcs.nms.v1_8_R3.entity.EndermiteController;
import net.citizensnpcs.nms.v1_8_R3.entity.EntityHumanNPC;
import net.citizensnpcs.nms.v1_8_R3.entity.GhastController;
import net.citizensnpcs.nms.v1_8_R3.entity.GiantController;
import net.citizensnpcs.nms.v1_8_R3.entity.GuardianController;
import net.citizensnpcs.nms.v1_8_R3.entity.HorseController;
import net.citizensnpcs.nms.v1_8_R3.entity.HumanController;
import net.citizensnpcs.nms.v1_8_R3.entity.IronGolemController;
import net.citizensnpcs.nms.v1_8_R3.entity.MagmaCubeController;
import net.citizensnpcs.nms.v1_8_R3.entity.MushroomCowController;
import net.citizensnpcs.nms.v1_8_R3.entity.OcelotController;
import net.citizensnpcs.nms.v1_8_R3.entity.PigController;
import net.citizensnpcs.nms.v1_8_R3.entity.PigZombieController;
import net.citizensnpcs.nms.v1_8_R3.entity.RabbitController;
import net.citizensnpcs.nms.v1_8_R3.entity.SheepController;
import net.citizensnpcs.nms.v1_8_R3.entity.SilverfishController;
import net.citizensnpcs.nms.v1_8_R3.entity.SkeletonController;
import net.citizensnpcs.nms.v1_8_R3.entity.SlimeController;
import net.citizensnpcs.nms.v1_8_R3.entity.SnowmanController;
import net.citizensnpcs.nms.v1_8_R3.entity.SpiderController;
import net.citizensnpcs.nms.v1_8_R3.entity.SquidController;
import net.citizensnpcs.nms.v1_8_R3.entity.VillagerController;
import net.citizensnpcs.nms.v1_8_R3.entity.WitchController;
import net.citizensnpcs.nms.v1_8_R3.entity.WitherController;
import net.citizensnpcs.nms.v1_8_R3.entity.WolfController;
import net.citizensnpcs.nms.v1_8_R3.entity.ZombieController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.ArmorStandController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.BoatController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.EggController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.EnderPearlController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.EnderSignalController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.FallingBlockController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.FireworkController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.FishingHookController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.ItemController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.ItemFrameController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.LargeFireballController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.LeashController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.MinecartChestController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.PaintingController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.SmallFireballController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.SnowballController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.nms.v1_8_R3.entity.nonliving.WitherSkullController;
import net.citizensnpcs.nms.v1_8_R3.network.EmptyChannel;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.NMSBridge;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.PlayerUpdateTask;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ControllerJump;
import net.minecraft.server.v1_8_R3.CrashReport;
import net.minecraft.server.v1_8_R3.CrashReportSystemDetails;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTameableAnimal;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Navigation;
import net.minecraft.server.v1_8_R3.NavigationAbstract;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PathEntity;
import net.minecraft.server.v1_8_R3.PathPoint;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.ReportedException;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import net.minecraft.server.v1_8_R3.ScoreboardTeamBase.EnumNameTagVisibility;
import net.minecraft.server.v1_8_R3.WorldServer;

@SuppressWarnings("unchecked")
public class NMSImpl implements NMSBridge {
    public NMSImpl() {
        loadEntityTypes();
    }

    @Override
    public boolean addEntityToWorld(org.bukkit.entity.Entity entity, SpawnReason custom) {
        return getHandle(entity).world.addEntity(getHandle(entity), custom);
    }

    @Override
    public void addOrRemoveFromPlayerList(org.bukkit.entity.Entity entity, boolean remove) {
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

    @Override
    public void attack(LivingEntity attacker, LivingEntity btarget) {
        EntityLiving handle = getHandle(attacker);
        EntityLiving target = getHandle(btarget);
        if (handle instanceof EntityPlayer) {
            EntityPlayer humanHandle = (EntityPlayer) handle;
            humanHandle.attack(target);
            PlayerAnimation.ARM_SWING.play(humanHandle.getBukkitEntity());
            return;
        }
        AttributeInstance attackDamage = handle.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
        float f = (float) (attackDamage == null ? 1 : attackDamage.getValue());
        int i = 0;

        if (target instanceof EntityLiving) {
            f += EnchantmentManager.a(handle.bA(), target.getMonsterType());
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

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Exception {
        if (Bukkit.isPrimaryThread())
            throw new IllegalStateException("NMS.fillProfileProperties cannot be invoked from the main thread.");

        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer().aD();

        if (!(sessionService instanceof YggdrasilMinecraftSessionService)) {
            return sessionService.fillProfileProperties(profile, requireSecure);
        }
        YggdrasilAuthenticationService auth = ((YggdrasilMinecraftSessionService) sessionService)
                .getAuthenticationService();

        URL url = HttpAuthenticationService
                .constantURL(getAuthServerBaseUrl() + UUIDTypeAdapter.fromUUID(profile.getId()));

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

    public String getAuthServerBaseUrl() {
        return Setting.AUTH_SERVER_URL.asString();
    }

    @Override
    public BlockBreaker getBlockBreaker(org.bukkit.entity.Entity entity, org.bukkit.block.Block targetBlock,
            BlockBreakerConfiguration config) {
        return new CitizensBlockBreaker(entity, targetBlock, config);
    }

    @Override
    public Object getBossBar(org.bukkit.entity.Entity entity) {
        return null;
    }

    @Override
    public BoundingBox getBoundingBox(org.bukkit.entity.Entity handle) {
        AxisAlignedBB bb = NMSImpl.getHandle(handle).getBoundingBox();
        return new BoundingBox(bb.a, bb.b, bb.c, bb.d, bb.e, bb.f);
    }

    @Override
    public BoundingBox getCollisionBox(org.bukkit.block.Block block) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        Block type = CraftMagicNumbers.getBlock(block);
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        AxisAlignedBB aabb = type.a(world, pos, world.getType(pos));
        return new BoundingBox(aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f);
    }

    private float getDragonYaw(Entity handle, double tX, double tZ) {
        if (handle.locZ > tZ)
            return (float) (-Math.toDegrees(Math.atan((handle.locX - tX) / (handle.locZ - tZ))));
        if (handle.locZ < tZ) {
            return (float) (-Math.toDegrees(Math.atan((handle.locX - tX) / (handle.locZ - tZ)))) + 180.0F;
        }
        return handle.yaw;
    }

    @Override
    public GameProfileRepository getGameProfileRepository() {
        return ((CraftServer) Bukkit.getServer()).getServer().getGameProfileRepository();
    }

    @Override
    public float getHeadYaw(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return entity.getLocation().getYaw();
        }
        return getHandle((LivingEntity) entity).aK;
    }

    @Override
    public double getHeight(org.bukkit.entity.Entity entity) {
        return getHandle(entity).length;
    }

    @Override
    public float getHorizontalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) entity);
        return handle.ba;
    }

    @Override
    public NPC getNPC(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof NPCHolder ? ((NPCHolder) handle).getNPC() : null;
    }

    @Override
    public List<org.bukkit.entity.Entity> getPassengers(org.bukkit.entity.Entity entity) {
        Entity passenger = NMSImpl.getHandle(entity).passenger;
        if (passenger == null)
            return Collections.emptyList();
        return Lists.<org.bukkit.entity.Entity> newArrayList(passenger.getBukkitEntity());
    }

    @Override
    public GameProfile getProfile(SkullMeta meta) {
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

    @Override
    public String getSound(String flag) throws CommandException {
        try {
            String ret = CraftSound.getSound(Sound.valueOf(flag.toUpperCase()));
            if (ret == null)
                throw new CommandException(Messages.INVALID_SOUND);
            return ret;
        } catch (Exception e) {
            throw new CommandException(Messages.INVALID_SOUND);
        }
    }

    @Override
    public float getSpeedFor(NPC npc) {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity))
            return DEFAULT_SPEED;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) npc.getEntity());
        if (handle == null)
            return DEFAULT_SPEED;
        return DEFAULT_SPEED;
        // return (float)
        // handle.getAttributeInstance(GenericAttributes.d).getValue();
    }

    @Override
    public float getStepHeight(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).S;
    }

    @Override
    public MCNavigator getTargetNavigator(org.bukkit.entity.Entity entity, Iterable<Vector> dest,
            final NavigatorParameters params) {
        final PathEntity path = new PathEntity(
                Iterables.toArray(Iterables.transform(dest, new Function<Vector, PathPoint>() {
                    @Override
                    public PathPoint apply(Vector input) {
                        return new PathPoint(input.getBlockX(), input.getBlockY(), input.getBlockZ());
                    }
                }), PathPoint.class));
        return getTargetNavigator(entity, params, new Function<NavigationAbstract, Boolean>() {
            @Override
            public Boolean apply(NavigationAbstract input) {
                return input.a(path, params.speed());
            }
        });
    }

    @Override
    public MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final Location dest,
            final NavigatorParameters params) {
        return getTargetNavigator(entity, params, new Function<NavigationAbstract, Boolean>() {
            @Override
            public Boolean apply(NavigationAbstract input) {
                return input.a(dest.getX(), dest.getY(), dest.getZ(), params.speed());
            }
        });
    }

    private MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final NavigatorParameters params,
            final Function<NavigationAbstract, Boolean> function) {
        net.minecraft.server.v1_8_R3.Entity raw = getHandle(entity);
        raw.onGround = true;
        // not sure of a better way around this - if onGround is false, then
        // navigation won't execute, and calling entity.move doesn't
        // entirely fix the problem.
        final NavigationAbstract navigation = NMSImpl.getNavigation(entity);
        final boolean oldAvoidsWater = navigation instanceof Navigation ? ((Navigation) navigation).e() : false;
        if (navigation instanceof Navigation) {
            ((Navigation) navigation).a(params.avoidWater());
        }
        return new MCNavigator() {
            float lastSpeed;
            CancelReason reason;

            @Override
            public CancelReason getCancelReason() {
                return reason;
            }

            @Override
            public Iterable<Vector> getPath() {
                return new NavigationIterable(navigation);
            }

            @Override
            public void stop() {
                if (navigation instanceof Navigation) {
                    ((Navigation) navigation).a(oldAvoidsWater);
                }
                stopNavigation(navigation);
            }

            @Override
            public boolean update() {
                if (params.speed() != lastSpeed) {
                    if (Messaging.isDebugging() && lastSpeed > 0) {
                        Messaging.debug(
                                "Repathfinding " + ((NPCHolder) entity).getNPC().getId() + " due to speed change");
                    }
                    Entity handle = getHandle(entity);
                    float oldWidth = handle.width;
                    if (handle instanceof EntityHorse) {
                        handle.width = Math.min(0.99f, oldWidth);
                    }
                    if (!function.apply(navigation)) {
                        reason = CancelReason.STUCK;
                    }
                    handle.width = oldWidth; // minecraft requires that an entity fit onto both blocks if width >= 1f,
                                             // but we'd prefer to make it just fit on 1 so hack around it a bit.
                    lastSpeed = params.speed();
                }
                navigation.a(params.speed());
                return NMSImpl.isNavigationFinished(navigation);
            }
        };
    }

    @Override
    public TargetNavigator getTargetNavigator(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity target,
            NavigatorParameters parameters) {
        NavigationAbstract navigation = getNavigation(entity);
        return navigation == null ? null : new NavigationFieldWrapper(navigation, target, parameters);
    }

    @Override
    public org.bukkit.entity.Entity getVehicle(org.bukkit.entity.Entity entity) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null) {
            return null;
        }
        Entity e = handle.vehicle;
        return (e == handle || e == null) ? null : e.getBukkitEntity();
    }

    @Override
    public float getVerticalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) entity);
        return handle.aZ;
    }

    @Override
    public double getWidth(org.bukkit.entity.Entity entity) {
        return getHandle(entity).width;
    }

    @Override
    public float getYaw(org.bukkit.entity.Entity entity) {
        return getHandle(entity).yaw;
    }

    @Override
    public boolean isOnGround(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).onGround;
    }

    @Override
    public boolean isValid(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle.valid && handle.isAlive();
    }

    @Override
    public void load(CommandManager commands) {
    }

    private void loadEntityTypes() {
        EntityControllers.setEntityControllerForType(EntityType.ARMOR_STAND, ArmorStandController.class);
        EntityControllers.setEntityControllerForType(EntityType.BAT, BatController.class);
        EntityControllers.setEntityControllerForType(EntityType.BLAZE, BlazeController.class);
        EntityControllers.setEntityControllerForType(EntityType.BOAT, BoatController.class);
        EntityControllers.setEntityControllerForType(EntityType.CAVE_SPIDER, CaveSpiderController.class);
        EntityControllers.setEntityControllerForType(EntityType.CHICKEN, ChickenController.class);
        EntityControllers.setEntityControllerForType(EntityType.COW, CowController.class);
        EntityControllers.setEntityControllerForType(EntityType.CREEPER, CreeperController.class);
        EntityControllers.setEntityControllerForType(EntityType.DROPPED_ITEM, ItemController.class);
        EntityControllers.setEntityControllerForType(EntityType.EGG, EggController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_CRYSTAL, EnderCrystalController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_DRAGON, EnderDragonController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_PEARL, EnderPearlController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_SIGNAL, EnderSignalController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDERMAN, EndermanController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDERMITE, EndermiteController.class);
        EntityControllers.setEntityControllerForType(EntityType.FALLING_BLOCK, FallingBlockController.class);
        EntityControllers.setEntityControllerForType(EntityType.FIREWORK, FireworkController.class);
        EntityControllers.setEntityControllerForType(EntityType.FIREBALL, LargeFireballController.class);
        EntityControllers.setEntityControllerForType(EntityType.FISHING_HOOK, FishingHookController.class);
        EntityControllers.setEntityControllerForType(EntityType.GHAST, GhastController.class);
        EntityControllers.setEntityControllerForType(EntityType.GIANT, GiantController.class);
        EntityControllers.setEntityControllerForType(EntityType.GUARDIAN, GuardianController.class);
        EntityControllers.setEntityControllerForType(EntityType.HORSE, HorseController.class);
        EntityControllers.setEntityControllerForType(EntityType.IRON_GOLEM, IronGolemController.class);
        EntityControllers.setEntityControllerForType(EntityType.ITEM_FRAME, ItemFrameController.class);
        EntityControllers.setEntityControllerForType(EntityType.LEASH_HITCH, LeashController.class);
        EntityControllers.setEntityControllerForType(EntityType.MAGMA_CUBE, MagmaCubeController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART, MinecartRideableController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_CHEST, MinecartChestController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_COMMAND, MinecartCommandController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_FURNACE, MinecartFurnaceController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_HOPPER, MinecartHopperController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_TNT, MinecartTNTController.class);
        EntityControllers.setEntityControllerForType(EntityType.MUSHROOM_COW, MushroomCowController.class);
        EntityControllers.setEntityControllerForType(EntityType.OCELOT, OcelotController.class);
        EntityControllers.setEntityControllerForType(EntityType.PAINTING, PaintingController.class);
        EntityControllers.setEntityControllerForType(EntityType.PIG, PigController.class);
        EntityControllers.setEntityControllerForType(EntityType.PIG_ZOMBIE, PigZombieController.class);
        EntityControllers.setEntityControllerForType(EntityType.PLAYER, HumanController.class);
        EntityControllers.setEntityControllerForType(EntityType.RABBIT, RabbitController.class);
        EntityControllers.setEntityControllerForType(EntityType.SHEEP, SheepController.class);
        EntityControllers.setEntityControllerForType(EntityType.SILVERFISH, SilverfishController.class);
        EntityControllers.setEntityControllerForType(EntityType.SKELETON, SkeletonController.class);
        EntityControllers.setEntityControllerForType(EntityType.SLIME, SlimeController.class);
        EntityControllers.setEntityControllerForType(EntityType.SMALL_FIREBALL, SmallFireballController.class);
        EntityControllers.setEntityControllerForType(EntityType.SNOWBALL, SnowballController.class);
        EntityControllers.setEntityControllerForType(EntityType.SNOWMAN, SnowmanController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPIDER, SpiderController.class);
        EntityControllers.setEntityControllerForType(EntityType.SQUID, SquidController.class);
        EntityControllers.setEntityControllerForType(EntityType.THROWN_EXP_BOTTLE, ThrownExpBottleController.class);
        EntityControllers.setEntityControllerForType(EntityType.PRIMED_TNT, TNTPrimedController.class);
        EntityControllers.setEntityControllerForType(EntityType.VILLAGER, VillagerController.class);
        EntityControllers.setEntityControllerForType(EntityType.WOLF, WolfController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITCH, WitchController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITHER, WitherController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITHER_SKULL, WitherSkullController.class);
        EntityControllers.setEntityControllerForType(EntityType.ZOMBIE, ZombieController.class);
    }

    @Override
    public void loadPlugins() {
        ((CraftServer) Bukkit.getServer()).enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    @Override
    public void look(org.bukkit.entity.Entity entity, float yaw, float pitch) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        yaw = Util.clampYaw(yaw);
        handle.yaw = yaw;
        setHeadYaw(entity, yaw);
        handle.pitch = pitch;
    }

    @Override
    public void look(org.bukkit.entity.Entity entity, Location to, boolean headOnly, boolean immediate) {
        Entity handle = NMSImpl.getHandle(entity);
        if (immediate || headOnly || BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())
                || (!(handle instanceof EntityInsentient) && !(handle instanceof EntityHumanNPC))) {
            Location fromLocation = entity.getLocation(FROM_LOCATION);
            double xDiff, yDiff, zDiff;
            xDiff = to.getX() - fromLocation.getX();
            yDiff = to.getY() - fromLocation.getY();
            zDiff = to.getZ() - fromLocation.getZ();

            double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

            double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
            double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90;
            if (zDiff < 0.0)
                yaw += Math.abs(180 - yaw) * 2;
            if (handle instanceof EntityEnderDragon) {
                yaw = getDragonYaw(handle, to.getX(), to.getZ());
            } else {
                yaw = yaw - 90;
            }
            if (headOnly) {
                setHeadYaw(entity, (float) yaw);
            } else {
                look(entity, (float) yaw, (float) pitch);
            }
            return;
        }
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerLook().a(to.getX(), to.getY(), to.getZ(), 10,
                    ((EntityInsentient) handle).bQ());
            while (((EntityInsentient) handle).aK >= 180F) {
                ((EntityInsentient) handle).aK -= 360F;
            }
            while (((EntityInsentient) handle).aK < -180F) {
                ((EntityInsentient) handle).aK += 360F;
            }
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setTargetLook(to);
        }
    }

    @Override
    public void look(org.bukkit.entity.Entity from, org.bukkit.entity.Entity to) {
        Entity handle = NMSImpl.getHandle(from), target = NMSImpl.getHandle(to);
        if (BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())) {
            if (to instanceof LivingEntity) {
                look(from, ((LivingEntity) to).getEyeLocation(), false, true);
            } else {
                look(from, to.getLocation(), false, true);
            }
        } else if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerLook().a(target, 10, ((EntityInsentient) handle).bQ());
            while (((EntityLiving) handle).aK >= 180F) {
                ((EntityLiving) handle).aK -= 360F;
            }
            while (((EntityLiving) handle).aK < -180F) {
                ((EntityLiving) handle).aK += 360F;
            }
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setTargetLook(target, 10F, 40F);
        }
    }

    @Override
    public void mount(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity passenger) {
        if (NMSImpl.getHandle(passenger) == null)
            return;
        NMSImpl.getHandle(passenger).mount(NMSImpl.getHandle(entity));
    }

    @Override
    public void openHorseScreen(Tameable horse, Player equipper) {
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) horse);
        EntityLiving equipperHandle = NMSImpl.getHandle(equipper);
        if (handle == null || equipperHandle == null)
            return;
        boolean wasTamed = horse.isTamed();
        horse.setTamed(true);
        ((EntityHorse) handle).g((EntityHuman) equipperHandle);
        horse.setTamed(wasTamed);
    }

    @Override
    public void playAnimation(PlayerAnimation animation, Player player, int radius) {
        PlayerAnimationImpl.play(animation, player, radius);
    }

    @Override
    public void playerTick(Player entity) {
        ((EntityPlayer) getHandle(entity)).l();
    }

    @Override
    public void registerEntityClass(Class<?> clazz) {
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

    @Override
    public void remove(org.bukkit.entity.Entity entity) {
        NMSImpl.getHandle(entity).die();
    }

    @Override
    public void removeFromServerPlayerList(Player player) {
        EntityPlayer handle = (EntityPlayer) NMSImpl.getHandle(player);
        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(handle);
    }

    @Override
    public void removeFromWorld(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity);

        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        nmsEntity.world.removeEntity(nmsEntity);
    }

    @Override
    public void removeHookIfNecessary(NPCRegistry npcRegistry, FishHook entity) {
        EntityFishingHook hook = (EntityFishingHook) NMSImpl.getHandle(entity);
        if (hook.hooked == null)
            return;
        NPC npc = npcRegistry.getNPC(hook.hooked.getBukkitEntity());
        if (npc == null)
            return;
        if (npc.isProtected()) {
            hook.hooked = null;
            hook.die();
        }
    }

    @Override
    public void replaceTrackerEntry(Player player) {
        WorldServer server = (WorldServer) NMSImpl.getHandle(player).getWorld();
        EntityTrackerEntry entry = server.getTracker().trackedEntities.get(player.getEntityId());
        if (entry == null)
            return;
        PlayerlistTrackerEntry replace = new PlayerlistTrackerEntry(entry);
        server.getTracker().trackedEntities.a(player.getEntityId(), replace);
        if (TRACKED_ENTITY_SET != null) {
            try {
                Set<Object> set = (Set<Object>) TRACKED_ENTITY_SET.get(server.getTracker());
                set.remove(entry);
                set.add(replace);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (getHandle(player) instanceof EntityHumanNPC) {
            ((EntityHumanNPC) getHandle(player)).setTracked(replace);
        }
    }

    @Override
    public void sendPositionUpdate(Player excluding, org.bukkit.entity.Entity from, Location storedLocation) {
        sendPacketNearby(excluding, storedLocation, new PacketPlayOutEntityTeleport(getHandle(from)));
    }

    @Override
    public void sendTabListAdd(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);

        EntityPlayer entity = ((CraftPlayer) listPlayer).getHandle();

        NMSImpl.sendPacket(recipient,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity));
    }

    @Override
    public void sendTabListRemove(Player recipient, Collection<? extends SkinnableEntity> skinnableNPCs) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(skinnableNPCs);

        EntityPlayer[] entities = new EntityPlayer[skinnableNPCs.size()];
        int i = 0;
        for (SkinnableEntity skinnable : skinnableNPCs) {
            entities[i] = (EntityPlayer) skinnable;
            i++;
        }

        NMSImpl.sendPacket(recipient,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entities));
    }

    @Override
    public void sendTabListRemove(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);

        EntityPlayer entity = ((CraftPlayer) listPlayer).getHandle();

        NMSImpl.sendPacket(recipient,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity));
    }

    @Override
    public void sendTeamPacket(Player recipient, Team team, int mode) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(team);

        if (TEAM_FIELD == null) {
            TEAM_FIELD = NMS.getField(team.getClass(), "team");
        }

        try {
            ScoreboardTeam nmsTeam = (ScoreboardTeam) TEAM_FIELD.get(team);
            sendPacket(recipient, new PacketPlayOutScoreboardTeam(nmsTeam, mode));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        getHandle(entity).yaw = yaw;
    }

    @Override
    public void setDestination(org.bukkit.entity.Entity entity, double x, double y, double z, float speed) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerMove().a(x, y, z, speed);
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setMoveDestination(x, y, z, speed);
        }
    }

    @Override
    public void setEndermanAngry(org.bukkit.entity.Enderman enderman, boolean angry) {
        getHandle(enderman).getDataWatcher().watch(17, Byte.valueOf((byte) (angry ? 1 : 0)));
    }

    @Override
    public void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        if (!(entity instanceof LivingEntity))
            return;
        EntityLiving handle = (EntityLiving) getHandle(entity);
        yaw = Util.clampYaw(yaw);
        handle.aJ = yaw;
        if (!(handle instanceof EntityHuman))
            handle.aI = yaw;
        handle.aK = yaw;
    }

    @Override
    public void setKnockbackResistance(LivingEntity entity, double d) {
        EntityLiving handle = NMSImpl.getHandle(entity);
        handle.getAttributeInstance(GenericAttributes.c).setValue(d);
    }

    @Override
    public void setLyingDown(org.bukkit.entity.Entity cat, boolean lying) {
    }

    @Override
    public void setNavigationTarget(org.bukkit.entity.Entity handle, org.bukkit.entity.Entity target, float speed) {
        NMSImpl.getNavigation(handle).a(NMSImpl.getHandle(target), speed);
    }

    @Override
    public void setNoGravity(org.bukkit.entity.Entity entity, boolean enabled) {
        if (!enabled)
            return;
        if (((NPCHolder) entity).getNPC().getNavigator().isNavigating())
            return; // use legacy gravity behaviour
        Vector vector = entity.getVelocity();
        vector.setY(Math.max(0, vector.getY()));
        entity.setVelocity(vector);
    }

    @Override
    public void setPandaSitting(org.bukkit.entity.Entity entity, boolean sitting) {
    }

    @Override
    public void setPeekShulker(org.bukkit.entity.Entity shulker, int peek) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProfile(SkullMeta meta, GameProfile profile) {
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

    @Override
    public void setShouldJump(org.bukkit.entity.Entity entity) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        if (handle instanceof EntityInsentient) {
            ControllerJump controller = ((EntityInsentient) handle).getControllerJump();
            controller.a();
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setShouldJump();
        }
    }

    @Override
    public void setSitting(Ocelot ocelot, boolean sitting) {
        setSitting((Tameable) ocelot, sitting);
    }

    @Override
    public void setSitting(Tameable tameable, boolean sitting) {
        ((EntityTameableAnimal) NMSImpl.getHandle((LivingEntity) tameable)).setSitting(sitting);
    }

    @Override
    public void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        NMSImpl.getHandle(entity).S = height;
    }

    @Override
    public void setTeamNameTagVisible(Team team, boolean visible) {
        if (TEAM_FIELD == null) {
            TEAM_FIELD = NMS.getField(team.getClass(), "team");
        }
        ScoreboardTeam nmsTeam;
        try {
            nmsTeam = (ScoreboardTeam) TEAM_FIELD.get(team);
            nmsTeam.setNameTagVisibility(visible ? EnumNameTagVisibility.ALWAYS : EnumNameTagVisibility.NEVER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        if (!bukkitEntity.getType().isAlive())
            return;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) bukkitEntity);
        handle.aZ = (float) d;
    }

    @Override
    public void setWitherCharged(Wither wither, boolean charged) {
        EntityWither handle = ((CraftWither) wither).getHandle();
        handle.r(charged ? 20 : 0);
    }

    @Override
    public boolean shouldJump(org.bukkit.entity.Entity entity) {
        if (JUMP_FIELD == null || !(entity instanceof LivingEntity))
            return false;
        try {
            return JUMP_FIELD.getBoolean(NMSImpl.getHandle(entity));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean tick(org.bukkit.entity.Entity next) {
        Entity entity = NMSImpl.getHandle(next);
        if (!entity.dead) {
            try {
                entity.world.g(entity);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Ticking player");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Player being ticked");

                entity.appendEntityCrashDetails(crashreportsystemdetails);
                throw new ReportedException(crashreport);
            }
        }
        boolean removeFromPlayerList = ((NPCHolder) entity).getNPC().data().get("removefromplayerlist",
                Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
        if (entity.dead) {
            entity.world.removeEntity(entity);
            return true;
        } else if (!removeFromPlayerList) {
            if (!entity.world.players.contains(entity)) {
                entity.world.players.add((EntityHuman) entity);
            }
            return true;
        } else {
            entity.world.players.remove(entity);
        }

        return false;
    }

    @Override
    public void trySwim(org.bukkit.entity.Entity entity) {
        trySwim(entity, 0.04F);
    }

    @Override
    public void trySwim(org.bukkit.entity.Entity entity, float power) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        if (RANDOM.nextFloat() < 0.8F && (handle.W() || handle.ab())) {
            handle.motY += power;
        }
    }

    @Override
    public void updateNavigationWorld(org.bukkit.entity.Entity entity, World world) {
        if (NAVIGATION_WORLD_FIELD == null)
            return;
        Entity en = NMSImpl.getHandle(entity);
        if (en == null || !(en instanceof EntityInsentient))
            return;
        EntityInsentient handle = (EntityInsentient) en;
        WorldServer worldHandle = ((CraftWorld) world).getHandle();
        try {
            NAVIGATION_WORLD_FIELD.set(handle.getNavigation(), worldHandle);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_UPDATING_NAVIGATION_WORLD, e.getMessage());
        }
    }

    @Override
    public void updatePathfindingRange(NPC npc, float pathfindingRange) {
        if (!npc.isSpawned() || !npc.getEntity().getType().isAlive())
            return;
        EntityLiving en = NMSImpl.getHandle((LivingEntity) npc.getEntity());
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

    private static class NavigationFieldWrapper implements TargetNavigator {
        private final NavigationAbstract navigation;
        private final NavigatorParameters parameters;
        private final org.bukkit.entity.Entity target;

        private NavigationFieldWrapper(NavigationAbstract navigation, org.bukkit.entity.Entity target,
                NavigatorParameters parameters) {
            this.navigation = navigation;
            this.target = target;
            this.parameters = parameters;
        }

        @Override
        public Iterable<Vector> getPath() {
            return new NavigationIterable(navigation);
        }

        @Override
        public void setPath() {
            Location location = parameters.entityTargetLocationMapper().apply(target);
            if (location == null) {
                throw new IllegalStateException("mapper should not return null");
            }
            navigation.a(location.getX(), location.getY(), location.getZ(), parameters.speed());
        }

        @Override
        public void stop() {
            stopNavigation(navigation);
        }

        @Override
        public void update() {
            updateNavigation(navigation);
        };
    }

    private static class NavigationIterable implements Iterable<Vector> {
        private final NavigationAbstract navigation;

        public NavigationIterable(NavigationAbstract nav) {
            this.navigation = nav;
        }

        @Override
        public Iterator<Vector> iterator() {
            final int npoints = navigation.j() == null ? 0 : navigation.j().d();
            return new Iterator<Vector>() {
                PathPoint curr = npoints > 0 ? navigation.j().a(0) : null;
                int i = 0;

                @Override
                public boolean hasNext() {
                    return curr != null;
                }

                @Override
                public Vector next() {
                    PathPoint old = curr;
                    curr = i + 1 < npoints ? navigation.j().a(++i) : null;
                    return new Vector(old.a, old.b, old.c);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        if (GOAL_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                Collection<?> list = (Collection<?>) GOAL_FIELD.get(selector);
                list.clear();
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            }
        }
    }

    public static void flyingMoveLogic(EntityLiving entity, float f, float f1) {
        if (entity.bM()) {
            if ((entity.V())) {
                double d0 = entity.locY;
                float f3 = 0.8F;
                float f4 = 0.02F;
                float f2 = EnchantmentManager.b(entity);
                if (f2 > 3.0F) {
                    f2 = 3.0F;
                }
                if (!entity.onGround) {
                    f2 *= 0.5F;
                }
                if (f2 > 0.0F) {
                    f3 += (0.54600006F - f3) * f2 / 3.0F;
                    f4 += (entity.bI() * 1.0F - f4) * f2 / 3.0F;
                }
                entity.a(f, f1, f4);
                entity.move(entity.motX, entity.motY, entity.motZ);
                entity.motX *= f3;
                entity.motY *= 0.800000011920929D;
                entity.motZ *= f3;
                entity.motY -= 0.02D;
                if ((entity.positionChanged)
                        && (entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d0, entity.motZ))) {
                    entity.motY = 0.30000001192092896D;
                }
            } else if ((entity.ab())) {
                double d0 = entity.locY;
                entity.a(f, f1, 0.02F);
                entity.move(entity.motX, entity.motY, entity.motZ);
                entity.motX *= 0.5D;
                entity.motY *= 0.5D;
                entity.motZ *= 0.5D;
                entity.motY -= 0.02D;
                if ((entity.positionChanged)
                        && (entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d0, entity.motZ))) {
                    entity.motY = 0.30000001192092896D;
                }
            } else {
                float f5 = 0.91F;
                if (entity.onGround) {
                    f5 = entity.world
                            .getType(new BlockPosition(MathHelper.floor(entity.locX),
                                    MathHelper.floor(entity.getBoundingBox().b) - 1, MathHelper.floor(entity.locZ)))
                            .getBlock().frictionFactor * 0.91F;
                }
                float f6 = 0.16277136F / (f5 * f5 * f5);
                float f3;
                if (entity.onGround) {
                    f3 = entity.bI() * f6;
                } else {
                    f3 = entity.aM;
                }
                entity.a(f, f1, f3);
                f5 = 0.91F;
                if (entity.onGround) {
                    f5 = entity.world
                            .getType(new BlockPosition(MathHelper.floor(entity.locX),
                                    MathHelper.floor(entity.getBoundingBox().b) - 1, MathHelper.floor(entity.locZ)))
                            .getBlock().frictionFactor * 0.91F;
                }
                if (entity.k_()) {
                    float f4 = 0.15F;
                    entity.motX = MathHelper.a(entity.motX, -f4, f4);
                    entity.motZ = MathHelper.a(entity.motZ, -f4, f4);
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
                if ((entity.positionChanged) && (entity.k_())) {
                    entity.motY = 0.2D;
                }
                if ((entity.world.isClientSide) && ((!entity.world
                        .isLoaded(new BlockPosition((int) entity.locX, 0, (int) entity.locZ)))
                        || (!entity.world
                                .getChunkAtWorldCoords(new BlockPosition((int) entity.locX, 0, (int) entity.locZ))
                                .o()))) {
                    if (entity.locY > 0.0D) {
                        entity.motY = -0.1D;
                    } else {
                        entity.motY = 0.0D;
                    }
                } else {
                    entity.motY -= 0.08D;
                }
                entity.motY *= 0.9800000190734863D;
                entity.motX *= f5;
                entity.motZ *= f5;
            }
        }
        entity.aA = entity.aB;
        double d0 = entity.locX - entity.lastX;
        double d1 = entity.locZ - entity.lastZ;

        float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
        if (f2 > 1.0F) {
            f2 = 1.0F;
        }
        entity.aB += (f2 - entity.aB) * 0.4F;
        entity.aC += entity.aB;
    }

    private static EntityLiving getHandle(LivingEntity entity) {
        return (EntityLiving) NMSImpl.getHandle((org.bukkit.entity.Entity) entity);
    }

    public static Entity getHandle(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

    public static float getHeadYaw(EntityLiving handle) {
        return handle.aJ;
    }

    public static NavigationAbstract getNavigation(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof EntityInsentient ? ((EntityInsentient) handle).getNavigation()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getNavigation() : null;
    }

    public static String getSoundEffect(NPC npc, String snd, String meta) {
        return npc == null || !npc.data().has(meta) ? snd : npc.data().get(meta, snd == null ? "" : snd.toString());
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

    public static boolean isNavigationFinished(NavigationAbstract navigation) {
        return navigation.m();
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
        minecart.SetDisplayBlockOffset(offset);
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        if (packet == null)
            return;
        ((EntityPlayer) NMSImpl.getHandle(player)).playerConnection.sendPacket(packet);
    }

    public static void sendPacketNearby(Player from, Location location, Packet<?> packet) {
        sendPacketNearby(from, location, packet, 64);
    }

    public static void sendPacketNearby(Player from, Location location, Packet<?> packet, double radius) {
        List<Packet<?>> list = new ArrayList<Packet<?>>();
        list.add(packet);
        sendPacketsNearby(from, location, list, radius);
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
                NMSImpl.sendPacket(ply, packet);
            }
        }
    }

    public static void sendPacketsNearby(Player from, Location location, Packet<?>... packets) {
        NMSImpl.sendPacketsNearby(from, location, Arrays.asList(packets), 64);
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

    public static void stopNavigation(NavigationAbstract navigation) {
        navigation.n();
    }

    public static void updateAI(EntityLiving entity) {
        if (entity instanceof EntityInsentient) {
            EntityInsentient handle = (EntityInsentient) entity;
            handle.getEntitySenses().a();
            NMSImpl.updateNavigation(handle.getNavigation());
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

    private static final Set<EntityType> BAD_CONTROLLER_LOOK = EnumSet.of(EntityType.SILVERFISH, EntityType.ENDERMITE,
            EntityType.ENDER_DRAGON, EntityType.BAT, EntityType.SLIME, EntityType.MAGMA_CUBE, EntityType.HORSE,
            EntityType.GHAST);
    private static final float DEFAULT_SPEED = 1F;
    private static Map<Class<?>, Integer> ENTITY_CLASS_TO_INT;
    private static Map<Class<?>, String> ENTITY_CLASS_TO_NAME;
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
    public static Field GOAL_FIELD = NMS.getField(PathfinderGoalSelector.class, "b");
    private static final Field JUMP_FIELD = NMS.getField(EntityLiving.class, "aY");
    private static Method MAKE_REQUEST;
    private static Field NAVIGATION_WORLD_FIELD = NMS.getField(NavigationAbstract.class, "c");
    public static Field NETWORK_ADDRESS = NMS.getField(NetworkManager.class, "l");
    public static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static Field PATHFINDING_RANGE = NMS.getField(NavigationAbstract.class, "a");
    private static final Random RANDOM = Util.getFastRandom();
    private static Field SKULL_PROFILE_FIELD;
    private static Field TEAM_FIELD;
    private static Field TRACKED_ENTITY_SET = NMS.getField(EntityTracker.class, "c");

    static {
        try {
            Field field = NMS.getField(EntityTypes.class, "f");
            ENTITY_CLASS_TO_INT = (Map<Class<?>, Integer>) field.get(null);
            field = NMS.getField(EntityTypes.class, "d");
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
