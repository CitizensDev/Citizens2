package net.citizensnpcs.nms.v1_11_R1.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftSound;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftWither;
import org.bukkit.craftbukkit.v1_11_R1.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginLoadOrder;
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
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.nms.v1_11_R1.entity.BatController;
import net.citizensnpcs.nms.v1_11_R1.entity.BlazeController;
import net.citizensnpcs.nms.v1_11_R1.entity.CaveSpiderController;
import net.citizensnpcs.nms.v1_11_R1.entity.ChickenController;
import net.citizensnpcs.nms.v1_11_R1.entity.CowController;
import net.citizensnpcs.nms.v1_11_R1.entity.CreeperController;
import net.citizensnpcs.nms.v1_11_R1.entity.EnderDragonController;
import net.citizensnpcs.nms.v1_11_R1.entity.EndermanController;
import net.citizensnpcs.nms.v1_11_R1.entity.EndermiteController;
import net.citizensnpcs.nms.v1_11_R1.entity.EntityHumanNPC;
import net.citizensnpcs.nms.v1_11_R1.entity.EvokerController;
import net.citizensnpcs.nms.v1_11_R1.entity.GhastController;
import net.citizensnpcs.nms.v1_11_R1.entity.GiantController;
import net.citizensnpcs.nms.v1_11_R1.entity.GuardianController;
import net.citizensnpcs.nms.v1_11_R1.entity.GuardianElderController;
import net.citizensnpcs.nms.v1_11_R1.entity.HorseController;
import net.citizensnpcs.nms.v1_11_R1.entity.HorseDonkeyController;
import net.citizensnpcs.nms.v1_11_R1.entity.HorseMuleController;
import net.citizensnpcs.nms.v1_11_R1.entity.HorseSkeletonController;
import net.citizensnpcs.nms.v1_11_R1.entity.HorseZombieController;
import net.citizensnpcs.nms.v1_11_R1.entity.HumanController;
import net.citizensnpcs.nms.v1_11_R1.entity.IronGolemController;
import net.citizensnpcs.nms.v1_11_R1.entity.LlamaController;
import net.citizensnpcs.nms.v1_11_R1.entity.MagmaCubeController;
import net.citizensnpcs.nms.v1_11_R1.entity.MushroomCowController;
import net.citizensnpcs.nms.v1_11_R1.entity.OcelotController;
import net.citizensnpcs.nms.v1_11_R1.entity.PigController;
import net.citizensnpcs.nms.v1_11_R1.entity.PigZombieController;
import net.citizensnpcs.nms.v1_11_R1.entity.PolarBearController;
import net.citizensnpcs.nms.v1_11_R1.entity.RabbitController;
import net.citizensnpcs.nms.v1_11_R1.entity.SheepController;
import net.citizensnpcs.nms.v1_11_R1.entity.ShulkerController;
import net.citizensnpcs.nms.v1_11_R1.entity.SilverfishController;
import net.citizensnpcs.nms.v1_11_R1.entity.SkeletonController;
import net.citizensnpcs.nms.v1_11_R1.entity.SkeletonStrayController;
import net.citizensnpcs.nms.v1_11_R1.entity.SkeletonWitherController;
import net.citizensnpcs.nms.v1_11_R1.entity.SlimeController;
import net.citizensnpcs.nms.v1_11_R1.entity.SnowmanController;
import net.citizensnpcs.nms.v1_11_R1.entity.SpiderController;
import net.citizensnpcs.nms.v1_11_R1.entity.SquidController;
import net.citizensnpcs.nms.v1_11_R1.entity.VexController;
import net.citizensnpcs.nms.v1_11_R1.entity.VillagerController;
import net.citizensnpcs.nms.v1_11_R1.entity.VindicatorController;
import net.citizensnpcs.nms.v1_11_R1.entity.WitchController;
import net.citizensnpcs.nms.v1_11_R1.entity.WitherController;
import net.citizensnpcs.nms.v1_11_R1.entity.WolfController;
import net.citizensnpcs.nms.v1_11_R1.entity.ZombieController;
import net.citizensnpcs.nms.v1_11_R1.entity.ZombieHuskController;
import net.citizensnpcs.nms.v1_11_R1.entity.ZombieVillagerController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.AreaEffectCloudController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.ArmorStandController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.BoatController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.DragonFireballController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.EggController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.EnderPearlController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.EnderSignalController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.EvokerFangsController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.FallingBlockController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.FireworkController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.FishingHookController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.ItemController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.ItemFrameController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.LargeFireballController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.LeashController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.LlamaSpitController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.MinecartChestController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.PaintingController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.ShulkerBulletController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.SmallFireballController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.SnowballController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.SpectralArrowController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.ThrownPotionController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.TippedArrowController;
import net.citizensnpcs.nms.v1_11_R1.entity.nonliving.WitherSkullController;
import net.citizensnpcs.nms.v1_11_R1.network.EmptyChannel;
import net.citizensnpcs.nms.v1_11_R1.trait.Commands;
import net.citizensnpcs.nms.v1_11_R1.trait.LlamaTrait;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.BoundingBox;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.NMSBridge;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.PlayerUpdateTask;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_11_R1.AttributeInstance;
import net.minecraft.server.v1_11_R1.AxisAlignedBB;
import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.BossBattleServer;
import net.minecraft.server.v1_11_R1.ControllerJump;
import net.minecraft.server.v1_11_R1.CrashReport;
import net.minecraft.server.v1_11_R1.CrashReportSystemDetails;
import net.minecraft.server.v1_11_R1.DamageSource;
import net.minecraft.server.v1_11_R1.DataWatcherObject;
import net.minecraft.server.v1_11_R1.EnchantmentManager;
import net.minecraft.server.v1_11_R1.Enchantments;
import net.minecraft.server.v1_11_R1.EnderDragonBattle;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityEnderDragon;
import net.minecraft.server.v1_11_R1.EntityFishingHook;
import net.minecraft.server.v1_11_R1.EntityHorse;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.EntityInsentient;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EntityPolarBear;
import net.minecraft.server.v1_11_R1.EntityRabbit;
import net.minecraft.server.v1_11_R1.EntityShulker;
import net.minecraft.server.v1_11_R1.EntityTameableAnimal;
import net.minecraft.server.v1_11_R1.EntityTracker;
import net.minecraft.server.v1_11_R1.EntityTrackerEntry;
import net.minecraft.server.v1_11_R1.EntityTypes;
import net.minecraft.server.v1_11_R1.EntityWither;
import net.minecraft.server.v1_11_R1.EnumMoveType;
import net.minecraft.server.v1_11_R1.GenericAttributes;
import net.minecraft.server.v1_11_R1.MathHelper;
import net.minecraft.server.v1_11_R1.MinecraftKey;
import net.minecraft.server.v1_11_R1.MobEffects;
import net.minecraft.server.v1_11_R1.NavigationAbstract;
import net.minecraft.server.v1_11_R1.NetworkManager;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_11_R1.PathEntity;
import net.minecraft.server.v1_11_R1.PathPoint;
import net.minecraft.server.v1_11_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_11_R1.RegistryMaterials;
import net.minecraft.server.v1_11_R1.ReportedException;
import net.minecraft.server.v1_11_R1.SoundEffect;
import net.minecraft.server.v1_11_R1.Vec3D;
import net.minecraft.server.v1_11_R1.WorldServer;

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
            f += EnchantmentManager.a(handle.getItemInMainHand(), target.getMonsterType());
            i += EnchantmentManager.a(Enchantments.KNOCKBACK, handle);
        }

        boolean flag = target.damageEntity(DamageSource.mobAttack(handle), f);

        if (!flag)
            return;
        if (i > 0) {
            target.f(-Math.sin(handle.yaw * Math.PI / 180.0F) * i * 0.5F, 0.1D,
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

        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer().az();

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
    public BossBar getBossBar(org.bukkit.entity.Entity entity) {
        BossBattleServer bserver = null;
        try {
            if (entity.getType() == EntityType.WITHER) {
                bserver = (BossBattleServer) WITHER_BOSS_BAR_FIELD.get(NMSImpl.getHandle(entity));
            } else if (entity.getType() == EntityType.ENDER_DRAGON) {
                bserver = (BossBattleServer) ENDERDRAGON_BATTLE_BAR_FIELD
                        .get(ENDERDRAGON_BATTLE_FIELD.get(NMSImpl.getHandle(entity)));
            }
        } catch (Exception e) {
        }
        if (bserver == null) {
            return null;
        }
        BossBar ret = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_10);
        try {
            CRAFT_BOSSBAR_HANDLE_FIELD.set(ret, bserver);
        } catch (Exception e) {
        }
        return ret;
    }

    @Override
    public BoundingBox getBoundingBox(org.bukkit.entity.Entity handle) {
        AxisAlignedBB bb = NMSImpl.getHandle(handle).getBoundingBox();
        return new BoundingBox(bb.a, bb.b, bb.c, bb.d, bb.e, bb.f);
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
        return getHandle((LivingEntity) entity).aP;
    }

    @Override
    public float getHorizontalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) entity);
        return handle.bf;
    }

    @Override
    public NPC getNPC(org.bukkit.entity.Entity entity) {
        return getHandle(entity) instanceof NPCHolder ? ((NPCHolder) getHandle(entity)).getNPC() : null;
    }

    @Override
    public List<org.bukkit.entity.Entity> getPassengers(org.bukkit.entity.Entity entity) {
        return Lists.transform(NMSImpl.getHandle(entity).passengers, new Function<Entity, org.bukkit.entity.Entity>() {
            @Override
            public org.bukkit.entity.Entity apply(Entity input) {
                return input.getBukkitEntity();
            }
        });
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
        return NMSImpl.getHandle(entity).P;
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
        net.minecraft.server.v1_11_R1.Entity raw = getHandle(entity);
        raw.onGround = true;
        // not sure of a better way around this - if onGround is false, then
        // navigation won't execute, and calling entity.move doesn't
        // entirely fix the problem.
        final NavigationAbstract navigation = NMSImpl.getNavigation(entity);
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
                if (navigation.k() != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (int i = 0; i < navigation.k().d(); i++) {
                            PathPoint pp = navigation.k().a(i);
                            org.bukkit.block.Block block = new Vector(pp.a, pp.b, pp.c).toLocation(player.getWorld())
                                    .getBlock();
                            player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
                        }
                    }
                }
                stopNavigation(navigation);
            }

            @Override
            public boolean update() {
                if (params.speed() != lastSpeed) {
                    if (Messaging.isDebugging()) {
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
                if (params.debug() && !NMSImpl.isNavigationFinished(navigation)) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (int i = 0; i < navigation.k().d(); i++) {
                            PathPoint pp = navigation.k().a(i);
                            player.sendBlockChange(new Vector(pp.a, pp.b, pp.c).toLocation(player.getWorld()),
                                    Material.YELLOW_FLOWER, (byte) 0);
                        }
                    }
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
        Entity e = handle.getVehicle();
        return (e == handle || e == null) ? null : e.getBukkitEntity();
    }

    @Override
    public float getVerticalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) entity);
        return handle.be;
    }

    @Override
    public boolean isOnGround(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).onGround;
    }

    @Override
    public void load(CommandManager manager) {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(LlamaTrait.class));
        manager.register(Commands.class);
    }

    private void loadEntityTypes() {
        EntityControllers.setEntityControllerForType(EntityType.AREA_EFFECT_CLOUD, AreaEffectCloudController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARROW, TippedArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARMOR_STAND, ArmorStandController.class);
        EntityControllers.setEntityControllerForType(EntityType.BAT, BatController.class);
        EntityControllers.setEntityControllerForType(EntityType.BLAZE, BlazeController.class);
        EntityControllers.setEntityControllerForType(EntityType.BOAT, BoatController.class);
        EntityControllers.setEntityControllerForType(EntityType.CAVE_SPIDER, CaveSpiderController.class);
        EntityControllers.setEntityControllerForType(EntityType.CHICKEN, ChickenController.class);
        EntityControllers.setEntityControllerForType(EntityType.COW, CowController.class);
        EntityControllers.setEntityControllerForType(EntityType.CREEPER, CreeperController.class);
        EntityControllers.setEntityControllerForType(EntityType.DRAGON_FIREBALL, DragonFireballController.class);
        EntityControllers.setEntityControllerForType(EntityType.DROPPED_ITEM, ItemController.class);
        EntityControllers.setEntityControllerForType(EntityType.EGG, EggController.class);
        EntityControllers.setEntityControllerForType(EntityType.ELDER_GUARDIAN, GuardianElderController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_CRYSTAL, EnderCrystalController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_DRAGON, EnderDragonController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_PEARL, EnderPearlController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDER_SIGNAL, EnderSignalController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDERMAN, EndermanController.class);
        EntityControllers.setEntityControllerForType(EntityType.ENDERMITE, EndermiteController.class);
        EntityControllers.setEntityControllerForType(EntityType.EVOKER, EvokerController.class);
        EntityControllers.setEntityControllerForType(EntityType.EVOKER_FANGS, EvokerFangsController.class);
        EntityControllers.setEntityControllerForType(EntityType.FALLING_BLOCK, FallingBlockController.class);
        EntityControllers.setEntityControllerForType(EntityType.FIREWORK, FireworkController.class);
        EntityControllers.setEntityControllerForType(EntityType.FIREBALL, LargeFireballController.class);
        EntityControllers.setEntityControllerForType(EntityType.FISHING_HOOK, FishingHookController.class);
        EntityControllers.setEntityControllerForType(EntityType.GHAST, GhastController.class);
        EntityControllers.setEntityControllerForType(EntityType.GIANT, GiantController.class);
        EntityControllers.setEntityControllerForType(EntityType.GUARDIAN, GuardianController.class);
        EntityControllers.setEntityControllerForType(EntityType.HORSE, HorseController.class);
        EntityControllers.setEntityControllerForType(EntityType.DONKEY, HorseDonkeyController.class);
        EntityControllers.setEntityControllerForType(EntityType.MULE, HorseMuleController.class);
        EntityControllers.setEntityControllerForType(EntityType.SKELETON_HORSE, HorseSkeletonController.class);
        EntityControllers.setEntityControllerForType(EntityType.ZOMBIE_HORSE, HorseZombieController.class);
        EntityControllers.setEntityControllerForType(EntityType.HUSK, ZombieHuskController.class);
        EntityControllers.setEntityControllerForType(EntityType.IRON_GOLEM, IronGolemController.class);
        EntityControllers.setEntityControllerForType(EntityType.ITEM_FRAME, ItemFrameController.class);
        EntityControllers.setEntityControllerForType(EntityType.LEASH_HITCH, LeashController.class);
        EntityControllers.setEntityControllerForType(EntityType.LLAMA, LlamaController.class);
        EntityControllers.setEntityControllerForType(EntityType.LLAMA_SPIT, LlamaSpitController.class);
        EntityControllers.setEntityControllerForType(EntityType.LINGERING_POTION, ThrownPotionController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.POLAR_BEAR, PolarBearController.class);
        EntityControllers.setEntityControllerForType(EntityType.PLAYER, HumanController.class);
        EntityControllers.setEntityControllerForType(EntityType.RABBIT, RabbitController.class);
        EntityControllers.setEntityControllerForType(EntityType.SHEEP, SheepController.class);
        EntityControllers.setEntityControllerForType(EntityType.SHULKER, ShulkerController.class);
        EntityControllers.setEntityControllerForType(EntityType.SHULKER_BULLET, ShulkerBulletController.class);
        EntityControllers.setEntityControllerForType(EntityType.SILVERFISH, SilverfishController.class);
        EntityControllers.setEntityControllerForType(EntityType.SKELETON, SkeletonController.class);
        EntityControllers.setEntityControllerForType(EntityType.STRAY, SkeletonStrayController.class);
        EntityControllers.setEntityControllerForType(EntityType.SLIME, SlimeController.class);
        EntityControllers.setEntityControllerForType(EntityType.SMALL_FIREBALL, SmallFireballController.class);
        EntityControllers.setEntityControllerForType(EntityType.SNOWBALL, SnowballController.class);
        EntityControllers.setEntityControllerForType(EntityType.SNOWMAN, SnowmanController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPECTRAL_ARROW, SpectralArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPIDER, SpiderController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPLASH_POTION, ThrownPotionController.class);
        EntityControllers.setEntityControllerForType(EntityType.SQUID, SquidController.class);
        EntityControllers.setEntityControllerForType(EntityType.TIPPED_ARROW, TippedArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.THROWN_EXP_BOTTLE, ThrownExpBottleController.class);
        EntityControllers.setEntityControllerForType(EntityType.PRIMED_TNT, TNTPrimedController.class);
        EntityControllers.setEntityControllerForType(EntityType.VEX, VexController.class);
        EntityControllers.setEntityControllerForType(EntityType.VILLAGER, VillagerController.class);
        EntityControllers.setEntityControllerForType(EntityType.VINDICATOR, VindicatorController.class);
        EntityControllers.setEntityControllerForType(EntityType.WOLF, WolfController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITCH, WitchController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITHER, WitherController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITHER_SKULL, WitherSkullController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITHER_SKELETON, SkeletonWitherController.class);
        EntityControllers.setEntityControllerForType(EntityType.ZOMBIE, ZombieController.class);
        EntityControllers.setEntityControllerForType(EntityType.ZOMBIE_VILLAGER, ZombieVillagerController.class);
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
            ((EntityInsentient) handle).getControllerLook().a(to.getX(), to.getY(), to.getZ(),
                    ((EntityInsentient) handle).cL(), ((EntityInsentient) handle).N());

            while (((EntityLiving) handle).aP >= 180F) {
                ((EntityLiving) handle).aP -= 360F;
            }
            while (((EntityLiving) handle).aP < -180F) {
                ((EntityLiving) handle).aP += 360F;
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
            ((EntityInsentient) handle).getControllerLook().a(target, ((EntityInsentient) handle).cL(),
                    ((EntityInsentient) handle).N());
            while (((EntityLiving) handle).aP >= 180F) {
                ((EntityLiving) handle).aP -= 360F;
            }
            while (((EntityLiving) handle).aP < -180F) {
                ((EntityLiving) handle).aP += 360F;
            }
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setTargetLook(target, 10F, 40F);
        }
    }

    @Override
    public void mount(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity passenger) {
        if (NMSImpl.getHandle(passenger) == null)
            return;
        NMSImpl.getHandle(passenger).startRiding(NMSImpl.getHandle(entity));
    }

    @Override
    public void openHorseScreen(Horse horse, Player equipper) {
        EntityLiving handle = NMSImpl.getHandle(horse);
        EntityLiving equipperHandle = NMSImpl.getHandle(equipper);
        if (handle == null || equipperHandle == null)
            return;
        boolean wasTamed = horse.isTamed();
        horse.setTamed(true);
        ((EntityHorse) handle).a((EntityHuman) equipperHandle);
        horse.setTamed(wasTamed);
    }

    @Override
    public void playAnimation(PlayerAnimation animation, Player player, int radius) {
        PlayerAnimationImpl.play(animation, player, radius);
    }

    @Override
    public void registerEntityClass(Class<?> clazz) {
        if (ENTITY_REGISTRY == null)
            return;

        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            MinecraftKey key = ENTITY_REGISTRY.b(search);
            if (key == null)
                continue;
            int code = ENTITY_REGISTRY.a(search);
            ENTITY_REGISTRY.put(code, key, (Class<? extends Entity>) clazz);
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass for class " + clazz.toString());
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
    public void setDummyAdvancement(Player entity) {
    }

    @Override
    public void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        if (!(entity instanceof LivingEntity))
            return;
        EntityLiving handle = (EntityLiving) getHandle(entity);
        yaw = Util.clampYaw(yaw);
        handle.aO = yaw;
        if (!(handle instanceof EntityHuman)) {
            handle.aN = yaw;
        }
        handle.aP = yaw;
    }

    @Override
    public void setKnockbackResistance(LivingEntity entity, double d) {
        EntityLiving handle = NMSImpl.getHandle(entity);
        handle.getAttributeInstance(GenericAttributes.c).setValue(d);
    }

    @Override
    public void setNavigationTarget(org.bukkit.entity.Entity handle, org.bukkit.entity.Entity target, float speed) {
        NMSImpl.getNavigation(handle).a(NMSImpl.getHandle(target), speed);
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
    public void setShulkerPeek(Shulker shulker, int peek) {
        ((EntityShulker) getHandle(shulker)).a((byte) peek);
    }

    @Override
    public void setSitting(Tameable tameable, boolean sitting) {
        ((EntityTameableAnimal) NMSImpl.getHandle((LivingEntity) tameable)).setSitting(sitting);
    }

    @Override
    public void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        NMSImpl.getHandle(entity).P = height;
    }

    @Override
    public void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        if (!bukkitEntity.getType().isAlive())
            return;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) bukkitEntity);
        handle.be = (float) d;
    }

    @Override
    public void setWitherCharged(Wither wither, boolean charged) {
        EntityWither handle = ((CraftWither) wither).getHandle();
        handle.g(charged ? 20 : 0);
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
        if (ENTITY_REGISTRY == null)
            return;
        Field field = NMS.getField(EntityTypes.class, "b");
        Field modifiersField = NMS.getField(Field.class, "modifiers");
        try {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, ENTITY_REGISTRY.getWrapped());
        } catch (Exception e) {

        }
    }

    @Override
    public boolean tick(org.bukkit.entity.Entity next) {
        Entity entity = NMSImpl.getHandle(next);
        Entity entity1 = entity.bB();
        if (entity1 != null) {
            if ((entity1.dead) || (!entity1.w(entity))) {
                entity.stopRiding();
            }
        } else {
            if (!entity.dead) {
                try {
                    entity.world.entityJoinedWorld(entity, true);
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
        if (RANDOM.nextFloat() < 0.8F && (handle.ak() || handle.ao())) {
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
        }
    }

    private static class NavigationIterable implements Iterable<Vector> {
        private final NavigationAbstract navigation;

        public NavigationIterable(NavigationAbstract nav) {
            this.navigation = nav;
        }

        @Override
        public Iterator<Vector> iterator() {
            final int npoints = navigation.k() == null ? 0 : navigation.k().d();
            return new Iterator<Vector>() {
                PathPoint curr = npoints > 0 ? navigation.k().a(0) : null;
                int i = 0;

                @Override
                public boolean hasNext() {
                    return curr != null;
                }

                @Override
                public Vector next() {
                    PathPoint old = curr;
                    curr = i + 1 < npoints ? navigation.k().a(++i) : null;
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
        if ((entity.cu()) || (entity.bA())) {
            if (entity.isInWater()) {
                double d1 = entity.locY;
                float f4 = entity instanceof EntityPolarBear ? 0.98F : 0.8F;
                float f3 = 0.02F;
                float f2 = EnchantmentManager.a(Enchantments.DEPTH_STRIDER, entity);
                if (f2 > 3.0F) {
                    f2 = 3.0F;
                }
                if (!entity.onGround) {
                    f2 *= 0.5F;
                }
                if (f2 > 0.0F) {
                    f4 += (0.54600006F - f4) * f2 / 3.0F;
                    f3 += (entity.cq() - f3) * f2 / 3.0F;
                }
                entity.a(f, f1, f3);
                entity.move(EnumMoveType.SELF, entity.motX, entity.motY, entity.motZ);
                entity.motX *= f4;
                entity.motY *= 0.800000011920929D;
                entity.motZ *= f4;
                if (!entity.isNoGravity()) {
                    entity.motY -= 0.02D;
                }
                if ((entity.positionChanged)
                        && (entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d1, entity.motZ))) {
                    entity.motY = 0.30000001192092896D;
                }
            } else if ((entity.ao())
                    && ((!(entity instanceof EntityHuman)) || (!((EntityHuman) entity).abilities.isFlying))) {
                double d1 = entity.locY;
                entity.a(f, f1, 0.02F);
                entity.move(EnumMoveType.SELF, entity.motX, entity.motY, entity.motZ);
                entity.motX *= 0.5D;
                entity.motY *= 0.5D;
                entity.motZ *= 0.5D;
                if (!entity.isNoGravity()) {
                    entity.motY -= 0.02D;
                }
                if ((entity.positionChanged)
                        && (entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d1, entity.motZ))) {
                    entity.motY = 0.30000001192092896D;
                }
            } else if (entity.cH()) {
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
                entity.move(EnumMoveType.SELF, entity.motX, entity.motY, entity.motZ);
                if ((entity.positionChanged) && (!entity.world.isClientSide)) {
                    double d4 = Math.sqrt(entity.motX * entity.motX + entity.motZ * entity.motZ);
                    double d5 = d2 - d4;
                    float f7 = (float) (d5 * 10.0D - 3.0D);
                    if (f7 > 0.0F) {
                        entity.a(entity.e((int) f7), 1.0F, 1.0F);
                        entity.damageEntity(DamageSource.FLY_INTO_WALL, f7);
                    }
                }
                if ((entity.onGround) && (!entity.world.isClientSide) && (entity.getFlag(7))
                        && (!CraftEventFactory.callToggleGlideEvent(entity, false).isCancelled())) {
                    entity.setFlag(7, false);
                }
            } else {
                float f8 = 0.91F;
                BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                        .d(entity.locX, entity.getBoundingBox().b - 1.0D, entity.locZ);
                if (entity.onGround) {
                    f8 = entity.world.getType(blockposition_pooledblockposition).getBlock().frictionFactor * 0.91F;
                }
                float f4 = 0.16277136F / (f8 * f8 * f8);
                float f3;
                if (entity.onGround) {
                    f3 = entity.cq() * f4;
                } else {
                    f3 = entity.aR;
                }
                entity.a(f, f1, f3);
                f8 = 0.91F;
                if (entity.onGround) {
                    f8 = entity.world.getType(blockposition_pooledblockposition.e(entity.locX,
                            entity.getBoundingBox().b - 1.0D, entity.locZ)).getBlock().frictionFactor * 0.91F;
                }
                if (entity.m_()) {
                    entity.motX = MathHelper.a(entity.motX, -0.15000000596046448D, 0.15000000596046448D);
                    entity.motZ = MathHelper.a(entity.motZ, -0.15000000596046448D, 0.15000000596046448D);
                    entity.fallDistance = 0.0F;
                    if (entity.motY < -0.15D) {
                        entity.motY = -0.15D;
                    }
                    boolean flag = (entity.isSneaking()) && ((entity instanceof EntityHuman));
                    if ((flag) && (entity.motY < 0.0D)) {
                        entity.motY = 0.0D;
                    }
                }
                entity.move(EnumMoveType.SELF, entity.motX, entity.motY, entity.motZ);
                if ((entity.positionChanged) && (entity.m_())) {
                    entity.motY = 0.2D;
                }
                if (entity.hasEffect(MobEffects.LEVITATION)) {
                    entity.motY += (0.05D * (entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - entity.motY)
                            * 0.2D;
                } else {
                    blockposition_pooledblockposition.e(entity.locX, 0.0D, entity.locZ);
                    if ((entity.world.isClientSide) && ((!entity.world.isLoaded(blockposition_pooledblockposition))
                            || (!entity.world.getChunkAtWorldCoords(blockposition_pooledblockposition).p()))) {
                        if (entity.locY > 0.0D) {
                            entity.motY = -0.1D;
                        } else {
                            entity.motY = 0.0D;
                        }
                    } else if (!entity.isNoGravity()) {
                        entity.motY -= 0.08D;
                    }
                }
                entity.motY *= 0.9800000190734863D;
                entity.motX *= f8;
                entity.motZ *= f8;
                blockposition_pooledblockposition.t();
            }
        }
        entity.aF = entity.aG;
        double d1 = entity.locX - entity.lastX;
        double d0 = entity.locZ - entity.lastZ;
        float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;
        if (f2 > 1.0F) {
            f2 = 1.0F;
        }
        entity.aG += (f2 - entity.aG) * 0.4F;
        entity.aH += entity.aG;
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
        return handle.getHeadRotation();
    }

    public static NavigationAbstract getNavigation(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof EntityInsentient ? ((EntityInsentient) handle).getNavigation()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getNavigation() : null;
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

    public static SoundEffect getSoundEffect(NPC npc, SoundEffect snd, String meta) {
        return npc == null || !npc.data().has(meta) ? snd
                : SoundEffect.a.get(new MinecraftKey(npc.data().get(meta, snd == null ? "" : snd.toString())));
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
        return navigation.n();
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
                entity.move(EnumMoveType.SELF, (f2 - entity.width) / 2, 0.0D, (f2 - entity.width) / 2);
        }
    }

    public static void stopNavigation(NavigationAbstract navigation) {
        navigation.o();
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
        navigation.l();
    }

    private static final Set<EntityType> BAD_CONTROLLER_LOOK = EnumSet.of(EntityType.POLAR_BEAR, EntityType.SILVERFISH,
            EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.BAT, EntityType.SLIME, EntityType.MAGMA_CUBE,
            EntityType.HORSE, EntityType.GHAST);
    private static final Field CRAFT_BOSSBAR_HANDLE_FIELD = NMS.getField(CraftBossBar.class, "handle");
    private static final float DEFAULT_SPEED = 1F;
    private static final Field ENDERDRAGON_BATTLE_BAR_FIELD = NMS.getField(EnderDragonBattle.class, "c");
    private static final Field ENDERDRAGON_BATTLE_FIELD = NMS.getField(EntityEnderDragon.class, "bJ");
    private static CustomEntityRegistry ENTITY_REGISTRY;
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
    public static Field GOAL_FIELD = NMS.getField(PathfinderGoalSelector.class, "b");
    private static final Field JUMP_FIELD = NMS.getField(EntityLiving.class, "bd");
    private static Method MAKE_REQUEST;
    private static Field NAVIGATION_WORLD_FIELD = NMS.getField(NavigationAbstract.class, "b");
    public static Field NETWORK_ADDRESS = NMS.getField(NetworkManager.class, "l");
    public static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static Field PATHFINDING_RANGE = NMS.getField(NavigationAbstract.class, "f");
    private static final Field RABBIT_FIELD = NMS.getField(EntityRabbit.class, "bw");
    private static final Random RANDOM = Util.getFastRandom();
    private static Field SKULL_PROFILE_FIELD;
    private static Field TRACKED_ENTITY_SET = NMS.getField(EntityTracker.class, "c");
    private static final Field WITHER_BOSS_BAR_FIELD = NMS.getField(EntityWither.class, "bF");

    static {
        try {
            Field field = NMS.getField(EntityTypes.class, "b");
            Field modifiersField = NMS.getField(Field.class, "modifiers");
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            ENTITY_REGISTRY = new CustomEntityRegistry(
                    (RegistryMaterials<MinecraftKey, Class<? extends Entity>>) field.get(null));
            field.set(null, ENTITY_REGISTRY);
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
