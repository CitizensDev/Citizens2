package net.citizensnpcs.nms.v1_16_R2.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.CraftSound;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R2.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftWither;
import org.bukkit.craftbukkit.v1_16_R2.event.CraftEventFactory;
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
import com.google.common.collect.Maps;
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
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.nms.v1_16_R2.entity.BatController;
import net.citizensnpcs.nms.v1_16_R2.entity.BeeController;
import net.citizensnpcs.nms.v1_16_R2.entity.BlazeController;
import net.citizensnpcs.nms.v1_16_R2.entity.CatController;
import net.citizensnpcs.nms.v1_16_R2.entity.CaveSpiderController;
import net.citizensnpcs.nms.v1_16_R2.entity.ChickenController;
import net.citizensnpcs.nms.v1_16_R2.entity.CodController;
import net.citizensnpcs.nms.v1_16_R2.entity.CowController;
import net.citizensnpcs.nms.v1_16_R2.entity.CreeperController;
import net.citizensnpcs.nms.v1_16_R2.entity.DolphinController;
import net.citizensnpcs.nms.v1_16_R2.entity.DrownedController;
import net.citizensnpcs.nms.v1_16_R2.entity.EnderDragonController;
import net.citizensnpcs.nms.v1_16_R2.entity.EndermanController;
import net.citizensnpcs.nms.v1_16_R2.entity.EndermiteController;
import net.citizensnpcs.nms.v1_16_R2.entity.EntityHumanNPC;
import net.citizensnpcs.nms.v1_16_R2.entity.EvokerController;
import net.citizensnpcs.nms.v1_16_R2.entity.FoxController;
import net.citizensnpcs.nms.v1_16_R2.entity.GhastController;
import net.citizensnpcs.nms.v1_16_R2.entity.GiantController;
import net.citizensnpcs.nms.v1_16_R2.entity.GuardianController;
import net.citizensnpcs.nms.v1_16_R2.entity.GuardianElderController;
import net.citizensnpcs.nms.v1_16_R2.entity.HoglinController;
import net.citizensnpcs.nms.v1_16_R2.entity.HorseController;
import net.citizensnpcs.nms.v1_16_R2.entity.HorseDonkeyController;
import net.citizensnpcs.nms.v1_16_R2.entity.HorseMuleController;
import net.citizensnpcs.nms.v1_16_R2.entity.HorseSkeletonController;
import net.citizensnpcs.nms.v1_16_R2.entity.HorseZombieController;
import net.citizensnpcs.nms.v1_16_R2.entity.HumanController;
import net.citizensnpcs.nms.v1_16_R2.entity.IllusionerController;
import net.citizensnpcs.nms.v1_16_R2.entity.IronGolemController;
import net.citizensnpcs.nms.v1_16_R2.entity.LlamaController;
import net.citizensnpcs.nms.v1_16_R2.entity.MagmaCubeController;
import net.citizensnpcs.nms.v1_16_R2.entity.MushroomCowController;
import net.citizensnpcs.nms.v1_16_R2.entity.OcelotController;
import net.citizensnpcs.nms.v1_16_R2.entity.PandaController;
import net.citizensnpcs.nms.v1_16_R2.entity.ParrotController;
import net.citizensnpcs.nms.v1_16_R2.entity.PhantomController;
import net.citizensnpcs.nms.v1_16_R2.entity.PigController;
import net.citizensnpcs.nms.v1_16_R2.entity.PigZombieController;
import net.citizensnpcs.nms.v1_16_R2.entity.PiglinBruteController;
import net.citizensnpcs.nms.v1_16_R2.entity.PiglinController;
import net.citizensnpcs.nms.v1_16_R2.entity.PillagerController;
import net.citizensnpcs.nms.v1_16_R2.entity.PolarBearController;
import net.citizensnpcs.nms.v1_16_R2.entity.PufferFishController;
import net.citizensnpcs.nms.v1_16_R2.entity.RabbitController;
import net.citizensnpcs.nms.v1_16_R2.entity.RavagerController;
import net.citizensnpcs.nms.v1_16_R2.entity.SalmonController;
import net.citizensnpcs.nms.v1_16_R2.entity.SheepController;
import net.citizensnpcs.nms.v1_16_R2.entity.ShulkerController;
import net.citizensnpcs.nms.v1_16_R2.entity.SilverfishController;
import net.citizensnpcs.nms.v1_16_R2.entity.SkeletonController;
import net.citizensnpcs.nms.v1_16_R2.entity.SkeletonStrayController;
import net.citizensnpcs.nms.v1_16_R2.entity.SkeletonWitherController;
import net.citizensnpcs.nms.v1_16_R2.entity.SlimeController;
import net.citizensnpcs.nms.v1_16_R2.entity.SnowmanController;
import net.citizensnpcs.nms.v1_16_R2.entity.SpiderController;
import net.citizensnpcs.nms.v1_16_R2.entity.SquidController;
import net.citizensnpcs.nms.v1_16_R2.entity.StriderController;
import net.citizensnpcs.nms.v1_16_R2.entity.TraderLlamaController;
import net.citizensnpcs.nms.v1_16_R2.entity.TropicalFishController;
import net.citizensnpcs.nms.v1_16_R2.entity.TurtleController;
import net.citizensnpcs.nms.v1_16_R2.entity.VexController;
import net.citizensnpcs.nms.v1_16_R2.entity.VillagerController;
import net.citizensnpcs.nms.v1_16_R2.entity.VindicatorController;
import net.citizensnpcs.nms.v1_16_R2.entity.WanderingTraderController;
import net.citizensnpcs.nms.v1_16_R2.entity.WitchController;
import net.citizensnpcs.nms.v1_16_R2.entity.WitherController;
import net.citizensnpcs.nms.v1_16_R2.entity.WolfController;
import net.citizensnpcs.nms.v1_16_R2.entity.ZoglinController;
import net.citizensnpcs.nms.v1_16_R2.entity.ZombieController;
import net.citizensnpcs.nms.v1_16_R2.entity.ZombieHuskController;
import net.citizensnpcs.nms.v1_16_R2.entity.ZombieVillagerController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.AreaEffectCloudController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.ArmorStandController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.BoatController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.DragonFireballController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.EggController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.EnderPearlController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.EnderSignalController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.EvokerFangsController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.FallingBlockController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.FireworkController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.FishingHookController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.ItemController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.ItemFrameController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.LargeFireballController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.LeashController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.LlamaSpitController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.MinecartChestController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.PaintingController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.ShulkerBulletController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.SmallFireballController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.SnowballController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.SpectralArrowController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.ThrownPotionController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.ThrownTridentController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.TippedArrowController;
import net.citizensnpcs.nms.v1_16_R2.entity.nonliving.WitherSkullController;
import net.citizensnpcs.nms.v1_16_R2.network.EmptyChannel;
import net.citizensnpcs.nms.v1_16_R2.trait.Commands;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.versioned.BeeTrait;
import net.citizensnpcs.trait.versioned.BossBarTrait;
import net.citizensnpcs.trait.versioned.CatTrait;
import net.citizensnpcs.trait.versioned.FoxTrait;
import net.citizensnpcs.trait.versioned.LlamaTrait;
import net.citizensnpcs.trait.versioned.MushroomCowTrait;
import net.citizensnpcs.trait.versioned.PandaTrait;
import net.citizensnpcs.trait.versioned.ParrotTrait;
import net.citizensnpcs.trait.versioned.PhantomTrait;
import net.citizensnpcs.trait.versioned.PufferFishTrait;
import net.citizensnpcs.trait.versioned.ShulkerTrait;
import net.citizensnpcs.trait.versioned.SnowmanTrait;
import net.citizensnpcs.trait.versioned.TropicalFishTrait;
import net.citizensnpcs.trait.versioned.VillagerTrait;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.NMSBridge;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R2.AdvancementDataPlayer;
import net.minecraft.server.v1_16_R2.AttributeModifiable;
import net.minecraft.server.v1_16_R2.AxisAlignedBB;
import net.minecraft.server.v1_16_R2.BehaviorController;
import net.minecraft.server.v1_16_R2.Block;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.BossBattleServer;
import net.minecraft.server.v1_16_R2.ChunkProviderServer;
import net.minecraft.server.v1_16_R2.ControllerJump;
import net.minecraft.server.v1_16_R2.CrashReport;
import net.minecraft.server.v1_16_R2.CrashReportSystemDetails;
import net.minecraft.server.v1_16_R2.DamageSource;
import net.minecraft.server.v1_16_R2.DataWatcherObject;
import net.minecraft.server.v1_16_R2.EnchantmentManager;
import net.minecraft.server.v1_16_R2.Enchantments;
import net.minecraft.server.v1_16_R2.EnderDragonBattle;
import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityBird;
import net.minecraft.server.v1_16_R2.EntityCat;
import net.minecraft.server.v1_16_R2.EntityEnderDragon;
import net.minecraft.server.v1_16_R2.EntityEnderman;
import net.minecraft.server.v1_16_R2.EntityFish;
import net.minecraft.server.v1_16_R2.EntityFishSchool;
import net.minecraft.server.v1_16_R2.EntityFishingHook;
import net.minecraft.server.v1_16_R2.EntityHorse;
import net.minecraft.server.v1_16_R2.EntityHorseAbstract;
import net.minecraft.server.v1_16_R2.EntityHuman;
import net.minecraft.server.v1_16_R2.EntityInsentient;
import net.minecraft.server.v1_16_R2.EntityLiving;
import net.minecraft.server.v1_16_R2.EntityMinecartAbstract;
import net.minecraft.server.v1_16_R2.EntityPanda;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.EntityPose;
import net.minecraft.server.v1_16_R2.EntityPufferFish;
import net.minecraft.server.v1_16_R2.EntityRabbit;
import net.minecraft.server.v1_16_R2.EntityShulker;
import net.minecraft.server.v1_16_R2.EntitySize;
import net.minecraft.server.v1_16_R2.EntityTameableAnimal;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.EntityWither;
import net.minecraft.server.v1_16_R2.EnumMoveType;
import net.minecraft.server.v1_16_R2.Fluid;
import net.minecraft.server.v1_16_R2.GenericAttributes;
import net.minecraft.server.v1_16_R2.IRegistry;
import net.minecraft.server.v1_16_R2.MathHelper;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.MobEffects;
import net.minecraft.server.v1_16_R2.NavigationAbstract;
import net.minecraft.server.v1_16_R2.NetworkManager;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_16_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R2.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_16_R2.PathEntity;
import net.minecraft.server.v1_16_R2.PathPoint;
import net.minecraft.server.v1_16_R2.PathType;
import net.minecraft.server.v1_16_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_16_R2.PlayerChunkMap;
import net.minecraft.server.v1_16_R2.PlayerChunkMap.EntityTracker;
import net.minecraft.server.v1_16_R2.RegistryBlocks;
import net.minecraft.server.v1_16_R2.ReportedException;
import net.minecraft.server.v1_16_R2.ScoreboardTeam;
import net.minecraft.server.v1_16_R2.ScoreboardTeamBase.EnumNameTagVisibility;
import net.minecraft.server.v1_16_R2.SoundEffect;
import net.minecraft.server.v1_16_R2.TagsFluid;
import net.minecraft.server.v1_16_R2.Vec3D;
import net.minecraft.server.v1_16_R2.VoxelShape;
import net.minecraft.server.v1_16_R2.WorldServer;

@SuppressWarnings("unchecked")
public class NMSImpl implements NMSBridge {
    public NMSImpl() {
        loadEntityTypes();
    }

    @SuppressWarnings("resource")
    @Override
    public boolean addEntityToWorld(org.bukkit.entity.Entity entity, SpawnReason custom) {
        int viewDistance = -1;
        PlayerChunkMap chunkMap = null;
        try {
            if (entity instanceof Player) {
                chunkMap = ((ChunkProviderServer) getHandle(entity).world.getChunkProvider()).playerChunkMap;
                viewDistance = (int) PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER.invoke(chunkMap);
                PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER.invoke(chunkMap, -1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        boolean success = getHandle(entity).world.addEntity(getHandle(entity), custom);
        try {
            if (chunkMap != null) {
                PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER.invoke(chunkMap, viewDistance);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void addOrRemoveFromPlayerList(org.bukkit.entity.Entity entity, boolean remove) {
        if (entity == null)
            return;
        EntityPlayer handle = (EntityPlayer) getHandle(entity);
        if (handle.world == null)
            return;
        if (remove) {
            handle.world.getPlayers().remove(handle);
        } else if (!handle.world.getPlayers().contains(handle)) {
            ((List) handle.world.getPlayers()).add(handle);
        }
        // PlayerUpdateTask.addOrRemove(entity, remove);
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
        if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).attackEntity(target);
            return;
        }
        AttributeModifiable attackDamage = handle.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
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
            handle.setMot(handle.getMot().d(0.6, 1, 0.6));
        }

        int fireAspectLevel = EnchantmentManager.getFireAspectEnchantmentLevel(handle);

        if (fireAspectLevel > 0) {
            target.setOnFire(fireAspectLevel * 4);
        }
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Throwable {
        if (Bukkit.isPrimaryThread())
            throw new IllegalStateException("NMS.fillProfileProperties cannot be invoked from the main thread.");

        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer()
                .getMinecraftSessionService();
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
    public BossBar getBossBar(org.bukkit.entity.Entity entity) {
        BossBattleServer bserver = null;
        try {
            if (entity.getType() == EntityType.WITHER) {
                bserver = ((EntityWither) NMSImpl.getHandle(entity)).bossBattle;
            } else if (entity.getType() == EntityType.ENDER_DRAGON) {
                Object battleObject = ENDERDRAGON_BATTLE_FIELD.invoke(NMSImpl.getHandle(entity));
                if (battleObject == null) {
                    return null;
                }
                bserver = ((EnderDragonBattle) battleObject).bossBattle;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (bserver == null) {
            return null;
        }
        BossBar ret = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_10);
        try {
            CRAFT_BOSSBAR_HANDLE_FIELD.invoke(ret, bserver);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public BoundingBox getBoundingBox(org.bukkit.entity.Entity handle) {
        return NMSBoundingBox.wrap(NMSImpl.getHandle(handle).getBoundingBox());
    }

    @Override
    public BoundingBox getCollisionBox(org.bukkit.block.Block block) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        VoxelShape shape = ((CraftBlock) block).getNMS().getCollisionShape(world, ((CraftBlock) block).getPosition());
        AxisAlignedBB aabb = shape.isEmpty()
                ? ((CraftBlock) block).getNMS().getShape(world, ((CraftBlock) block).getPosition()).getBoundingBox()
                : shape.getBoundingBox();
        return aabb == null ? BoundingBox.convert(block.getBoundingBox()) : NMSBoundingBox.wrap(aabb);
    }

    private float getDragonYaw(Entity handle, double tX, double tZ) {
        if (handle.locZ() > tZ)
            return (float) (-Math.toDegrees(Math.atan((handle.locX() - tX) / (handle.locZ() - tZ))));
        if (handle.locZ() < tZ) {
            return (float) (-Math.toDegrees(Math.atan((handle.locX() - tX) / (handle.locZ() - tZ)))) + 180.0F;
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
        return getHandle((LivingEntity) entity).getHeadRotation();
    }

    @Override
    public double getHeight(org.bukkit.entity.Entity entity) {
        return entity.getHeight();
    }

    @Override
    public float getHorizontalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) entity);
        return handle.aT;
    }

    @Override
    public NPC getNPC(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof NPCHolder ? ((NPCHolder) handle).getNPC() : null;
    }

    @Override
    public List<org.bukkit.entity.Entity> getPassengers(org.bukkit.entity.Entity entity) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null || handle.passengers == null)
            return Lists.newArrayList();
        return Lists.transform(handle.passengers, new Function<Entity, org.bukkit.entity.Entity>() {
            @Override
            public org.bukkit.entity.Entity apply(Entity input) {
                return input.getBukkitEntity();
            }
        });
    }

    @Override
    public GameProfile getProfile(SkullMeta meta) {
        if (SKULL_PROFILE_FIELD == null) {
            SKULL_PROFILE_FIELD = NMS.getField(meta.getClass(), "profile", false);
            if (SKULL_PROFILE_FIELD == null) {
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
        return NMSImpl.getHandle(entity).G;
    }

    @Override
    public MCNavigator getTargetNavigator(org.bukkit.entity.Entity entity, Iterable<Vector> dest,
            final NavigatorParameters params) {
        List<PathPoint> list = Lists.<PathPoint> newArrayList(
                Iterables.<Vector, PathPoint> transform(dest, new Function<Vector, PathPoint>() {
                    @Override
                    public PathPoint apply(Vector input) {
                        return new PathPoint(input.getBlockX(), input.getBlockY(), input.getBlockZ());
                    }
                }));
        PathPoint last = list.size() > 0 ? list.get(list.size() - 1) : null;
        final PathEntity path = new PathEntity(list, last != null ? new BlockPosition(last.a, last.b, last.c) : null,
                true);
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
        net.minecraft.server.v1_16_R2.Entity raw = getHandle(entity);
        raw.setOnGround(true);
        // not sure of a better way around this - if onGround is false, then
        // navigation won't execute, and calling entity.move doesn't
        // entirely fix the problem.
        final NavigationAbstract navigation = NMSImpl.getNavigation(entity);
        final float oldWater = raw instanceof EntityPlayer ? ((EntityHumanNPC) raw).a(PathType.WATER)
                : ((EntityInsentient) raw).a(PathType.WATER);
        if (params.avoidWater() && oldWater >= 0) {
            if (raw instanceof EntityPlayer) {
                ((EntityHumanNPC) raw).a(PathType.WATER, oldWater + 1F);
            } else {
                ((EntityInsentient) raw).a(PathType.WATER, oldWater + 1F);
            }
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
                if (params.debug() && navigation.k() != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (int i = 0; i < navigation.k().e(); i++) {
                            PathPoint pp = navigation.k().a(i);
                            org.bukkit.block.Block block = new Vector(pp.a, pp.b, pp.c).toLocation(player.getWorld())
                                    .getBlock();
                            player.sendBlockChange(block.getLocation(), block.getBlockData());
                        }
                    }
                }
                if (oldWater >= 0) {
                    if (raw instanceof EntityPlayer) {
                        ((EntityHumanNPC) raw).a(PathType.WATER, oldWater);
                    } else {
                        ((EntityInsentient) raw).a(PathType.WATER, oldWater);
                    }
                }
                stopNavigation(navigation);
            }

            @Override
            public boolean update() {
                if (params.speed() != lastSpeed) {
                    if (Messaging.isDebugging() && lastSpeed > 0) {
                        Messaging.debug(
                                "Repathfinding " + ((NPCHolder) entity).getNPC().getId() + " due to speed change from",
                                lastSpeed, "to", params.speed());
                    }
                    Entity handle = getHandle(entity);
                    EntitySize size = null;
                    try {
                        size = (EntitySize) SIZE_FIELD_GETTER.invoke(handle);

                        if (handle instanceof EntityHorse) {
                            SIZE_FIELD_SETTER.invoke(handle,
                                    new EntitySize(Math.min(0.99F, size.width), size.height, false));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    if (!function.apply(navigation)) {
                        reason = CancelReason.STUCK;
                    }
                    try {
                        SIZE_FIELD_SETTER.invoke(handle, size);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        // minecraft requires that an entity fit onto both blocks if width >= 1f, but we'd prefer to
                        // make it just fit on 1 so hack around it a bit.
                    }
                    lastSpeed = params.speed();
                }
                if (params.debug() && !NMSImpl.isNavigationFinished(navigation)) {
                    BlockData data = Material.DANDELION.createBlockData();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (int i = 0; i < navigation.k().e(); i++) {
                            PathPoint pp = navigation.k().a(i);
                            player.sendBlockChange(new Vector(pp.a, pp.b, pp.c).toLocation(player.getWorld()), data);
                        }
                    }
                }
                navigation.a((double) params.speed());
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
        Entity e = handle.getVehicle();
        return (e == handle || e == null) ? null : e.getBukkitEntity();
    }

    @Override
    public float getVerticalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) entity);
        return handle.aR;
    }

    @Override
    public double getWidth(org.bukkit.entity.Entity entity) {
        return entity.getWidth();
    }

    @Override
    public float getYaw(org.bukkit.entity.Entity entity) {
        return getHandle(entity).yaw;
    }

    @Override
    public boolean isOnGround(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).isOnGround();
    }

    @Override
    public boolean isValid(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle.valid && handle.isAlive();
    }

    @Override
    public void load(CommandManager manager) {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BeeTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BossBarTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CatTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FoxTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(LlamaTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MushroomCowTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ParrotTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PandaTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PhantomTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PufferFishTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ShulkerTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SnowmanTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TropicalFishTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(VillagerTrait.class));
        manager.register(Commands.class);
    }

    private void loadEntityTypes() {
        EntityControllers.setEntityControllerForType(EntityType.AREA_EFFECT_CLOUD, AreaEffectCloudController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARROW, TippedArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARMOR_STAND, ArmorStandController.class);
        EntityControllers.setEntityControllerForType(EntityType.BAT, BatController.class);
        EntityControllers.setEntityControllerForType(EntityType.BEE, BeeController.class);
        EntityControllers.setEntityControllerForType(EntityType.BLAZE, BlazeController.class);
        EntityControllers.setEntityControllerForType(EntityType.BOAT, BoatController.class);
        EntityControllers.setEntityControllerForType(EntityType.CAT, CatController.class);
        EntityControllers.setEntityControllerForType(EntityType.CAVE_SPIDER, CaveSpiderController.class);
        EntityControllers.setEntityControllerForType(EntityType.CHICKEN, ChickenController.class);
        EntityControllers.setEntityControllerForType(EntityType.COD, CodController.class);
        EntityControllers.setEntityControllerForType(EntityType.COW, CowController.class);
        EntityControllers.setEntityControllerForType(EntityType.CREEPER, CreeperController.class);
        EntityControllers.setEntityControllerForType(EntityType.DOLPHIN, DolphinController.class);
        EntityControllers.setEntityControllerForType(EntityType.DRAGON_FIREBALL, DragonFireballController.class);
        EntityControllers.setEntityControllerForType(EntityType.DROPPED_ITEM, ItemController.class);
        EntityControllers.setEntityControllerForType(EntityType.DROWNED, DrownedController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.FOX, FoxController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.ILLUSIONER, IllusionerController.class);
        EntityControllers.setEntityControllerForType(EntityType.ITEM_FRAME, ItemFrameController.class);
        EntityControllers.setEntityControllerForType(EntityType.LEASH_HITCH, LeashController.class);
        EntityControllers.setEntityControllerForType(EntityType.LLAMA, LlamaController.class);
        EntityControllers.setEntityControllerForType(EntityType.TRADER_LLAMA, TraderLlamaController.class);
        EntityControllers.setEntityControllerForType(EntityType.WANDERING_TRADER, WanderingTraderController.class);
        EntityControllers.setEntityControllerForType(EntityType.LLAMA_SPIT, LlamaSpitController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPLASH_POTION, ThrownPotionController.class);
        EntityControllers.setEntityControllerForType(EntityType.MAGMA_CUBE, MagmaCubeController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART, MinecartRideableController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_CHEST, MinecartChestController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_COMMAND, MinecartCommandController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_FURNACE, MinecartFurnaceController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_HOPPER, MinecartHopperController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_TNT, MinecartTNTController.class);
        EntityControllers.setEntityControllerForType(EntityType.MUSHROOM_COW, MushroomCowController.class);
        EntityControllers.setEntityControllerForType(EntityType.OCELOT, OcelotController.class);
        EntityControllers.setEntityControllerForType(EntityType.PANDA, PandaController.class);
        EntityControllers.setEntityControllerForType(EntityType.PAINTING, PaintingController.class);
        EntityControllers.setEntityControllerForType(EntityType.PARROT, ParrotController.class);
        EntityControllers.setEntityControllerForType(EntityType.PHANTOM, PhantomController.class);
        EntityControllers.setEntityControllerForType(EntityType.PILLAGER, PillagerController.class);
        EntityControllers.setEntityControllerForType(EntityType.PIG, PigController.class);
        EntityControllers.setEntityControllerForType(EntityType.PIGLIN, PiglinController.class);
        EntityControllers.setEntityControllerForType(EntityType.PIGLIN_BRUTE, PiglinBruteController.class);
        EntityControllers.setEntityControllerForType(EntityType.HOGLIN, HoglinController.class);
        EntityControllers.setEntityControllerForType(EntityType.ZOMBIFIED_PIGLIN, PigZombieController.class);
        EntityControllers.setEntityControllerForType(EntityType.ZOGLIN, ZoglinController.class);
        EntityControllers.setEntityControllerForType(EntityType.POLAR_BEAR, PolarBearController.class);
        EntityControllers.setEntityControllerForType(EntityType.PLAYER, HumanController.class);
        EntityControllers.setEntityControllerForType(EntityType.PUFFERFISH, PufferFishController.class);
        EntityControllers.setEntityControllerForType(EntityType.RABBIT, RabbitController.class);
        EntityControllers.setEntityControllerForType(EntityType.RAVAGER, RavagerController.class);
        EntityControllers.setEntityControllerForType(EntityType.SALMON, SalmonController.class);
        EntityControllers.setEntityControllerForType(EntityType.SHEEP, SheepController.class);
        EntityControllers.setEntityControllerForType(EntityType.SHULKER, ShulkerController.class);
        EntityControllers.setEntityControllerForType(EntityType.SHULKER_BULLET, ShulkerBulletController.class);
        EntityControllers.setEntityControllerForType(EntityType.SILVERFISH, SilverfishController.class);
        EntityControllers.setEntityControllerForType(EntityType.SKELETON, SkeletonController.class);
        EntityControllers.setEntityControllerForType(EntityType.STRAY, SkeletonStrayController.class);
        EntityControllers.setEntityControllerForType(EntityType.STRIDER, StriderController.class);
        EntityControllers.setEntityControllerForType(EntityType.SLIME, SlimeController.class);
        EntityControllers.setEntityControllerForType(EntityType.SMALL_FIREBALL, SmallFireballController.class);
        EntityControllers.setEntityControllerForType(EntityType.SNOWBALL, SnowballController.class);
        EntityControllers.setEntityControllerForType(EntityType.SNOWMAN, SnowmanController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPECTRAL_ARROW, SpectralArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPIDER, SpiderController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPLASH_POTION, ThrownPotionController.class);
        EntityControllers.setEntityControllerForType(EntityType.SQUID, SquidController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPECTRAL_ARROW, TippedArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.THROWN_EXP_BOTTLE, ThrownExpBottleController.class);
        EntityControllers.setEntityControllerForType(EntityType.TRIDENT, ThrownTridentController.class);
        EntityControllers.setEntityControllerForType(EntityType.TROPICAL_FISH, TropicalFishController.class);
        EntityControllers.setEntityControllerForType(EntityType.TURTLE, TurtleController.class);
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
            double pitch = Math.toDegrees(Math.acos(yDiff / distanceY))
                    - (handle.getBukkitEntity().getType() == EntityType.PHANTOM ? 45 : 90);
            if (zDiff < 0.0)
                yaw += Math.abs(180 - yaw) * 2;
            if (handle.getBukkitEntity().getType() == EntityType.ENDER_DRAGON) {
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
                    ((EntityInsentient) handle).ep(), ((EntityInsentient) handle).O());

            while (((EntityLiving) handle).aC >= 180F) {
                ((EntityLiving) handle).aC -= 360F;
            }
            while (((EntityLiving) handle).aC < -180F) {
                ((EntityLiving) handle).aC += 360F;
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
            ((EntityInsentient) handle).getControllerLook().a(target, ((EntityInsentient) handle).ep(),
                    ((EntityInsentient) handle).O());
            while (((EntityLiving) handle).aC >= 180F) {
                ((EntityLiving) handle).aC -= 360F;
            }
            while (((EntityLiving) handle).aC < -180F) {
                ((EntityLiving) handle).aC += 360F;
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
    public void openHorseScreen(Tameable horse, Player equipper) {
        EntityLiving handle = NMSImpl.getHandle(horse);
        EntityHuman equipperHandle = (EntityHuman) NMSImpl.getHandle(equipper);
        if (handle == null || equipperHandle == null)
            return;
        boolean wasTamed = horse.isTamed();
        horse.setTamed(true);
        ((EntityHorseAbstract) handle).f(equipperHandle);
        horse.setTamed(wasTamed);
    }

    @Override
    public void playAnimation(PlayerAnimation animation, Player player, int radius) {
        PlayerAnimationImpl.play(animation, player, radius);
    }

    @Override
    public void playerTick(Player entity) {
        ((EntityPlayer) getHandle(entity)).playerTick();
    }

    @Override
    public void registerEntityClass(Class<?> clazz) {
        if (ENTITY_REGISTRY == null)
            return;

        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            EntityTypes<?> type = ENTITY_REGISTRY.findType(search);
            MinecraftKey key = ENTITY_REGISTRY.getKey(type);
            if (key == null || type == null)
                continue;
            CITIZENS_ENTITY_TYPES.put(clazz, type);
            int code = ENTITY_REGISTRY.a(type);
            ENTITY_REGISTRY.put(code, key, type);
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
        ((WorldServer) nmsEntity.world).removeEntity(nmsEntity);
    }

    @Override
    public void removeHookIfNecessary(NPCRegistry npcRegistry, FishHook entity) {
        if (FISHING_HOOK_HOOKED == null)
            return;
        EntityFishingHook hook = (EntityFishingHook) NMSImpl.getHandle(entity);
        Entity hooked = null;
        try {
            hooked = (Entity) FISHING_HOOK_HOOKED.invoke(hook);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (hooked == null)
            return;
        NPC npc = npcRegistry.getNPC(hooked.getBukkitEntity());
        if (npc == null)
            return;
        if (npc.isProtected()) {
            try {
                FISHING_HOOK_HOOKED_SETTER.invoke(hook, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            hook.die();
        }
    }

    @Override
    public void replaceTrackerEntry(Player player) {
        WorldServer server = (WorldServer) NMSImpl.getHandle(player).getWorld();

        EntityTracker entry = server.getChunkProvider().playerChunkMap.trackedEntities.get(player.getEntityId());
        if (entry == null)
            return;
        PlayerlistTracker replace = new PlayerlistTracker(server.getChunkProvider().playerChunkMap, entry);
        server.getChunkProvider().playerChunkMap.trackedEntities.put(player.getEntityId(), replace);
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
            TEAM_FIELD = NMS.getGetter(team.getClass(), "team");
        }

        try {
            ScoreboardTeam nmsTeam = (ScoreboardTeam) TEAM_FIELD.invoke(team);
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
        if (ENDERMAN_ANGRY == null)
            return;
        getHandle(enderman).getDataWatcher().set(ENDERMAN_ANGRY, angry);
    }

    @Override
    public void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        if (!(entity instanceof LivingEntity))
            return;
        EntityLiving handle = (EntityLiving) getHandle(entity);
        yaw = Util.clampYaw(yaw);
        handle.aB = yaw;
        if (!(handle instanceof EntityHuman)) {
            handle.aA = yaw; // TODO: why this
        }
        handle.setHeadRotation(yaw);
    }

    @Override
    public void setKnockbackResistance(LivingEntity entity, double d) {
        EntityLiving handle = NMSImpl.getHandle(entity);
        handle.getAttributeInstance(GenericAttributes.KNOCKBACK_RESISTANCE).setValue(d);
    }

    @Override
    public void setLyingDown(org.bukkit.entity.Entity cat, boolean lying) {
        ((EntityCat) getHandle(cat)).x(lying);
    }

    @Override
    public void setNavigationTarget(org.bukkit.entity.Entity handle, org.bukkit.entity.Entity target, float speed) {
        NMSImpl.getNavigation(handle).a(NMSImpl.getHandle(target), speed);
    }

    @Override
    public void setNoGravity(org.bukkit.entity.Entity entity, boolean enabled) {
        getHandle(entity).setNoGravity(enabled);
    }

    @Override
    public void setPandaSitting(org.bukkit.entity.Entity entity, boolean sitting) {
        ((EntityPanda) getHandle(entity)).t(sitting);
    }

    @Override
    public void setPeekShulker(org.bukkit.entity.Entity shulker, int peek) {
        ((EntityShulker) getHandle(shulker)).a(peek);
    }

    @Override
    public void setProfile(SkullMeta meta, GameProfile profile) {
        if (SKULL_PROFILE_FIELD == null) {
            SKULL_PROFILE_FIELD = NMS.getField(meta.getClass(), "profile", false);
            if (SKULL_PROFILE_FIELD == null) {
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
            controller.jump();
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setShouldJump();
        }
    }

    @Override
    public void setSitting(Ocelot ocelot, boolean sitting) {
        EntityPose pose = sitting ? EntityPose.CROUCHING : EntityPose.STANDING;
        getHandle(ocelot).setPose(pose);
    }

    @Override
    public void setSitting(Tameable tameable, boolean sitting) {
        ((EntityTameableAnimal) NMSImpl.getHandle(tameable)).setSitting(sitting);
    }

    @Override
    public void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        NMSImpl.getHandle(entity).G = height;
    }

    @Override
    public void setTeamNameTagVisible(Team team, boolean visible) {
        if (TEAM_FIELD == null) {
            TEAM_FIELD = NMS.getGetter(team.getClass(), "team");
        }
        ScoreboardTeam nmsTeam;
        try {
            nmsTeam = (ScoreboardTeam) TEAM_FIELD.invoke(team);
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
        handle.aR = (float) d;
    }

    @Override
    public void setWitherCharged(Wither wither, boolean charged) {
        EntityWither handle = ((CraftWither) wither).getHandle();
        handle.setInvul(charged ? 20 : 0);
    }

    @Override
    public boolean shouldJump(org.bukkit.entity.Entity entity) {
        if (JUMP_FIELD == null || !(entity instanceof LivingEntity))
            return false;
        try {
            return (boolean) JUMP_FIELD.invoke(NMSImpl.getHandle(entity));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void shutdown() {
        if (ENTITY_REGISTRY == null)
            return;
        MethodHandle field = NMS.getFinalSetter(IRegistry.class, "ENTITY_TYPE");
        try {
            field.invoke(null, ENTITY_REGISTRY.getWrapped());
        } catch (Throwable e) {
        }
    }

    @Override
    public boolean tick(org.bukkit.entity.Entity next) {
        Entity entity = NMSImpl.getHandle(next);
        Entity entity1 = entity.getVehicle();
        if (entity1 != null) {
            if ((entity1.dead) || (!entity1.w(entity))) {
                entity.stopRiding();
            }
        } else {
            if (!entity.dead) {
                try {
                    ((WorldServer) entity.world).entityJoinedWorld(entity);
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
                ((WorldServer) entity.world).removeEntity(entity);
                return true;
            } else if (!removeFromPlayerList) {
                if (!entity.world.getPlayers().contains(entity)) {
                    List list = entity.world.getPlayers();
                    list.add(entity);
                }
                return true;
            } else {
                entity.world.getPlayers().remove(entity);
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
        if (RANDOM.nextFloat() < 0.8F && handle.isInWater()) {
            handle.setMot(handle.getMot().getX(), handle.getMot().getY() + power, handle.getMot().getZ());
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
            NAVIGATION_WORLD_FIELD.invoke(handle.getNavigation(), worldHandle);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_UPDATING_NAVIGATION_WORLD, e.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
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
        if (NAVIGATION_S == null)
            return;
        NavigationAbstract navigation = ((EntityInsentient) en).getNavigation();
        AttributeModifiable inst = en.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        inst.setValue(pathfindingRange);
        int mc = MathHelper.floor(en.b(GenericAttributes.FOLLOW_RANGE) * 16.0D);
        try {
            NAVIGATION_S.invoke(navigation, NAVIGATION_A.invoke(navigation, mc));
        } catch (Throwable e) {
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
            final int npoints = navigation.k() == null ? 0 : navigation.k().e();
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

    public static void checkAndUpdateHeight(EntityLiving living, DataWatcherObject<?> datawatcherobject) {
        EntitySize size;
        try {
            size = (EntitySize) SIZE_FIELD_GETTER.invoke(living);
        } catch (Throwable e) {
            e.printStackTrace();
            living.a(datawatcherobject);
            return;
        }
        float oldw = size.width;
        float oldl = size.height;
        living.a(datawatcherobject);
        if (oldw != size.width || size.height != oldl) {
            living.setPosition(living.locX() - 0.01, living.locY(), living.locZ() - 0.01);
            living.setPosition(living.locX() + 0.01, living.locY(), living.locZ() + 0.01);
        }
    }

    public static void clearGoals(NPC npc, PathfinderGoalSelector... goalSelectors) {
        if (GOAL_SET_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                Collection<?> list = (Collection<?>) GOAL_SET_FIELD.invoke(selector);
                if (!list.isEmpty()) {
                    npc.data().set("goal-selector", Lists.newArrayList(list));
                }
                list.clear();
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            } catch (Throwable e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            }
        }
    }

    public static void flyingMoveLogic(EntityLiving entity, Vec3D vec3d) {
        if (entity.doAITick() || entity.cr()) {
            double d0 = 0.08D;
            boolean flag = ((entity.getMot()).y <= 0.0D);
            if (flag && entity.hasEffect(MobEffects.SLOW_FALLING)) {
                d0 = 0.01D;
                entity.fallDistance = 0.0F;
            }
            Fluid fluid = entity.world.getFluid(entity.getChunkCoordinates());
            if (entity.isInWater() /*&& entity.cS() */ && !entity.a(fluid.getType())) {
                double d1 = entity.locY();
                float f = entity.isSprinting() ? 0.9F : 0.8F;
                float f1 = 0.02F;
                float f2 = EnchantmentManager.e(entity);
                if (f2 > 3.0F)
                    f2 = 3.0F;
                if (!entity.isOnGround())
                    f2 *= 0.5F;
                if (f2 > 0.0F) {
                    f += (0.54600006F - f) * f2 / 3.0F;
                    f1 += (entity.dM() - f1) * f2 / 3.0F;
                }
                if (entity.hasEffect(MobEffects.DOLPHINS_GRACE))
                    f = 0.96F;
                entity.a(f1, vec3d);
                entity.move(EnumMoveType.SELF, entity.getMot());
                Vec3D vec3d1 = entity.getMot();
                if (entity.positionChanged && entity.isClimbing())
                    vec3d1 = new Vec3D(vec3d1.x, 0.2D, vec3d1.z);
                entity.setMot(vec3d1.d(f, 0.800000011920929D, f));
                Vec3D vec3d2 = entity.a(d0, flag, entity.getMot());
                entity.setMot(vec3d2);
                if (entity.positionChanged && entity.e(vec3d2.x, vec3d2.y + 0.6D - entity.locY() + d1, vec3d2.z))
                    entity.setMot(vec3d2.x, 0.30000001192092896D, vec3d2.z);
            } else if (entity.aP() /*&& entity.cS()*/ && !entity.a(fluid.getType())) {
                double d1 = entity.locY();
                entity.a(0.02F, vec3d);
                entity.move(EnumMoveType.SELF, entity.getMot());
                if (entity.b(TagsFluid.LAVA) <= entity.cw()) {
                    entity.setMot(entity.getMot().d(0.5D, 0.8D, 0.5D));
                    Vec3D vec3D = entity.a(d0, flag, entity.getMot());
                    entity.setMot(vec3D);
                } else {
                    entity.setMot(entity.getMot().a(0.5D));
                }
                if (!entity.isNoGravity())
                    entity.setMot(entity.getMot().add(0.0D, -d0 / 4.0D, 0.0D));
                Vec3D vec3d3 = entity.getMot();
                if (entity.positionChanged && entity.e(vec3d3.x, vec3d3.y + 0.6D - entity.locY() + d1, vec3d3.z))
                    entity.setMot(vec3d3.x, 0.3D, vec3d3.z);
            } else if (entity.isGliding()) {
                Vec3D vec3d4 = entity.getMot();
                if (vec3d4.y > -0.5D)
                    entity.fallDistance = 1.0F;
                Vec3D vec3d5 = entity.getLookDirection();
                float f = entity.pitch * 0.017453292F;
                double d2 = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
                double d3 = Math.sqrt(entity.c(vec3d4));
                double d4 = vec3d5.f();
                float f3 = MathHelper.cos(f);
                f3 = (float) (f3 * f3 * Math.min(1.0D, d4 / 0.4D));
                vec3d4 = entity.getMot().add(0.0D, d0 * (-1.0D + f3 * 0.75D), 0.0D);
                if (vec3d4.y < 0.0D && d2 > 0.0D) {
                    double d5 = vec3d4.y * -0.1D * f3;
                    vec3d4 = vec3d4.add(vec3d5.x * d5 / d2, d5, vec3d5.z * d5 / d2);
                }
                if (f < 0.0F && d2 > 0.0D) {
                    double d5 = d3 * -MathHelper.sin(f) * 0.04D;
                    vec3d4 = vec3d4.add(-vec3d5.x * d5 / d2, d5 * 3.2D, -vec3d5.z * d5 / d2);
                }
                if (d2 > 0.0D)
                    vec3d4 = vec3d4.add((vec3d5.x / d2 * d3 - vec3d4.x) * 0.1D, 0.0D,
                            (vec3d5.z / d2 * d3 - vec3d4.z) * 0.1D);
                entity.setMot(vec3d4.d(0.9900000095367432D, 0.9800000190734863D, 0.9900000095367432D));
                entity.move(EnumMoveType.SELF, entity.getMot());
                if (entity.positionChanged && !entity.world.isClientSide) {
                    double d5 = Math.sqrt(entity.c(entity.getMot()));
                    double d6 = d3 - d5;
                    float f4 = (float) (d6 * 10.0D - 3.0D);
                    if (f4 > 0.0F) {
                        try {
                            entity.playSound((SoundEffect) ENTITY_GET_SOUND_FALL.invoke(entity, (int) f4), 1.0F, 1.0F);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        entity.damageEntity(DamageSource.FLY_INTO_WALL, f4);
                    }
                }
                if (entity.isOnGround() && !entity.world.isClientSide && entity.getFlag(7)
                        && !CraftEventFactory.callToggleGlideEvent(entity, false).isCancelled())
                    entity.setFlag(7, false);
            } else {
                BlockPosition blockposition = new BlockPosition(entity.locX(),
                        (entity.getBoundingBox()).minY - 0.5000001D, entity.locZ());// entity.ar();
                float f5 = entity.world.getType(blockposition).getBlock().getFrictionFactor();
                float f = entity.isOnGround() ? (f5 * 0.91F) : 0.91F;
                Vec3D vec3d6 = entity.a(vec3d, f5);
                double d7 = vec3d6.y;
                if (entity.hasEffect(MobEffects.LEVITATION)) {
                    d7 += (0.05D * (entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec3d6.y) * 0.2D;
                    entity.fallDistance = 0.0F;
                } else if (entity.world.isClientSide && !entity.world.isLoaded(blockposition)) {
                    if (entity.locY() > 0.0D) {
                        d7 = -0.1D;
                    } else {
                        d7 = 0.0D;
                    }
                } else if (!entity.isNoGravity()) {
                    d7 -= d0;
                }
                entity.setMot(vec3d6.x * f, d7 * 0.9800000190734863D, vec3d6.z * f);
            }
        }
        entity.a(entity, entity instanceof EntityBird);
    }

    public static TreeMap<?, ?> getBehaviorMap(EntityLiving entity) {
        try {
            return (TreeMap<?, ?>) BEHAVIOR_MAP.invoke(entity.getBehaviorController());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends Entity> EntityTypes<T> getEntityType(Class<?> clazz) {
        return (EntityTypes<T>) CITIZENS_ENTITY_TYPES.get(clazz);
    }

    private static EntityLiving getHandle(LivingEntity entity) {
        return (EntityLiving) NMSImpl.getHandle((org.bukkit.entity.Entity) entity);
    }

    public static Entity getHandle(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

    private static EntityLiving getHandle(Tameable entity) {
        return (EntityLiving) NMSImpl.getHandle((org.bukkit.entity.Entity) entity);
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
        if (RABBIT_DATAWATCHER_FIELD == null)
            return null;
        try {
            return (DataWatcherObject<Integer>) RABBIT_DATAWATCHER_FIELD.invoke();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EntitySize getSize(Entity entity) {
        try {
            return (EntitySize) SIZE_FIELD_GETTER.invoke(entity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SoundEffect getSoundEffect(NPC npc, SoundEffect snd, String meta) {
        return npc == null || !npc.data().has(meta) ? snd
                : IRegistry.SOUND_EVENT.get(new MinecraftKey(npc.data().get(meta, snd == null ? "" : snd.toString())));
    }

    public static void initNetworkManager(NetworkManager network) {
        network.channel = new EmptyChannel(null);
        SocketAddress socketAddress = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
        network.socketAddress = socketAddress;
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
        int data = npc.data().get(NPC.MINECART_ITEM_DATA_METADATA, 0); // TODO: migration for this
        int offset = npc.data().get(NPC.MINECART_OFFSET_METADATA, 0);
        minecart.a(mat != null);
        if (mat != null) {
            minecart.setDisplayBlock(Block.getByCombinedId(mat.getId()).getBlock().getBlockData());
        }
        minecart.setDisplayBlockOffset(offset);
    }

    public static void resetPuffTicks(EntityPufferFish fish) {
        try {
            PUFFERFISH_C.invoke(fish, 0);
            PUFFERFISH_D.invoke(fish, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void restoreGoals(NPC npc, PathfinderGoalSelector... goalSelectors) {
        if (GOAL_SET_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                Collection<Object> list = (Collection<Object>) GOAL_SET_FIELD.invoke(selector);
                list.clear();

                Collection<Object> old = npc.data().get("goal-selector");
                if (old != null) {
                    list.addAll(old);
                }
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_RESTORING_GOALS, e.getLocalizedMessage());
            } catch (Throwable e) {
                Messaging.logTr(Messages.ERROR_RESTORING_GOALS, e.getLocalizedMessage());
            }
        }
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

    public static void setAdvancement(Player entity, AdvancementDataPlayer instance) {
        try {
            ADVANCEMENT_PLAYER_FIELD.invoke(getHandle(entity), instance);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setBukkitEntity(Entity entity, CraftEntity bukkitEntity) {
        try {
            BUKKITENTITY_FIELD_SETTER.invoke(entity, bukkitEntity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setNotInSchool(EntityFish entity) {
        try {
            if (ENTITY_FISH_NUM_IN_SCHOOL != null) {
                ENTITY_FISH_NUM_IN_SCHOOL.invoke(entity, 2);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void setSize(Entity entity, boolean justCreated) {
        try {
            EntitySize entitysize = (EntitySize) SIZE_FIELD_GETTER.invoke(entity);
            EntityPose entitypose = entity.getPose();
            EntitySize entitysize1 = entity.a(entitypose);
            SIZE_FIELD_SETTER.invoke(entity, entitysize1);
            HEAD_HEIGHT.invoke(entity, HEAD_HEIGHT_METHOD.invoke(entity, entitypose, entitysize1));
            if (entitysize1.width < entitysize.width && false /* TODO: PREVIOUS CITIZENS ADDITION ?reason */) {
                double d0 = entitysize1.width / 2.0D;
                entity.a(new AxisAlignedBB(entity.locX() - d0, entity.locY(), entity.locZ() - d0, entity.locX() + d0,
                        entity.locY() + entitysize1.height, entity.locZ() + d0));
            } else {
                AxisAlignedBB axisalignedbb = entity.getBoundingBox();
                entity.a(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                        axisalignedbb.minX + entitysize1.width, axisalignedbb.minY + entitysize1.height,
                        axisalignedbb.minZ + entitysize1.width));
                if (entitysize1.width > entitysize.width && !justCreated && !entity.world.isClientSide) {
                    float f = entitysize.width - entitysize1.width;
                    entity.move(EnumMoveType.SELF, new Vec3D(f, 0.0D, f));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setSize(Entity entity, EntitySize size) {
        try {
            SIZE_FIELD_SETTER.invoke(entity, size);
        } catch (Throwable e) {
            e.printStackTrace();
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
            handle.getControllerMove().a();
            handle.getControllerLook().a();
            handle.getControllerJump().b();
        } else if (entity instanceof EntityHumanNPC) {
            ((EntityHumanNPC) entity).updateAI();
        }
    }

    public static void updateMinecraftAIState(NPC npc, EntityInsentient entity) {
        if (npc == null)
            return;
        if (npc.useMinecraftAI()) {
            NMSImpl.restoreGoals(npc, entity.goalSelector, entity.targetSelector);
        } else {
            NMSImpl.clearGoals(npc, entity.goalSelector, entity.targetSelector);
        }
    }

    public static void updateNavigation(NavigationAbstract navigation) {
        navigation.c();
    }

    private static final MethodHandle ADVANCEMENT_PLAYER_FIELD = NMS.getFinalSetter(EntityPlayer.class,
            "advancementDataPlayer");
    private static final Set<EntityType> BAD_CONTROLLER_LOOK = EnumSet.of(EntityType.POLAR_BEAR, EntityType.BEE,
            EntityType.SILVERFISH, EntityType.SHULKER, EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.BAT,
            EntityType.SLIME, EntityType.DOLPHIN, EntityType.MAGMA_CUBE, EntityType.HORSE, EntityType.GHAST,
            EntityType.SHULKER, EntityType.PHANTOM);
    private static final MethodHandle BEHAVIOR_MAP = NMS.getGetter(BehaviorController.class, "e");
    private static final MethodHandle BUKKITENTITY_FIELD_SETTER = NMS.getSetter(Entity.class, "bukkitEntity");
    private static final Map<Class<?>, EntityTypes<?>> CITIZENS_ENTITY_TYPES = Maps.newHashMap();
    private static final MethodHandle CRAFT_BOSSBAR_HANDLE_FIELD = NMS.getSetter(CraftBossBar.class, "handle");
    private static final float DEFAULT_SPEED = 1F;
    private static final MethodHandle ENDERDRAGON_BATTLE_FIELD = NMS.getGetter(EntityEnderDragon.class, "bF");
    private static DataWatcherObject<Boolean> ENDERMAN_ANGRY = null;
    private static final MethodHandle ENTITY_FISH_NUM_IN_SCHOOL = NMS.getSetter(EntityFishSchool.class, "c", false);
    private static final MethodHandle ENTITY_GET_SOUND_FALL = NMS.getMethodHandle(EntityLiving.class, "getSoundFall",
            true, int.class);
    private static CustomEntityRegistry ENTITY_REGISTRY;
    private static final MethodHandle FISHING_HOOK_HOOKED = NMS.getGetter(EntityFishingHook.class, "hooked");
    private static final MethodHandle FISHING_HOOK_HOOKED_SETTER = NMS.getSetter(EntityFishingHook.class, "hooked");
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle GOAL_SET_FIELD = NMS.getGetter(PathfinderGoalSelector.class, "d");
    private static final MethodHandle HEAD_HEIGHT = NMS.getSetter(Entity.class, "headHeight");
    private static final MethodHandle HEAD_HEIGHT_METHOD = NMS.getMethodHandle(Entity.class, "getHeadHeight", true,
            EntityPose.class, EntitySize.class);
    private static final MethodHandle JUMP_FIELD = NMS.getGetter(EntityLiving.class, "jumping");
    private static final MethodHandle MAKE_REQUEST = NMS.getMethodHandle(YggdrasilAuthenticationService.class,
            "makeRequest", true, URL.class, Object.class, Class.class);
    private static final MethodHandle NAVIGATION_A = NMS.getMethodHandle(NavigationAbstract.class, "a", true,
            int.class);
    private static final MethodHandle NAVIGATION_S = NMS.getFinalSetter(NavigationAbstract.class, "s");
    private static final MethodHandle NAVIGATION_WORLD_FIELD = NMS.getSetter(NavigationAbstract.class, "b");
    public static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER = NMS.getGetter(PlayerChunkMap.class,
            "viewDistance");
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER = NMS.getSetter(PlayerChunkMap.class,
            "viewDistance");
    private static final MethodHandle PUFFERFISH_C = NMS.getSetter(EntityPufferFish.class, "c");
    private static final MethodHandle PUFFERFISH_D = NMS.getSetter(EntityPufferFish.class, "d");
    private static final MethodHandle RABBIT_DATAWATCHER_FIELD = NMS.getGetter(EntityRabbit.class, "bo");
    private static final Random RANDOM = Util.getFastRandom();
    private static final MethodHandle SIZE_FIELD_GETTER = NMS.getGetter(Entity.class, "size");
    private static final MethodHandle SIZE_FIELD_SETTER = NMS.getSetter(Entity.class, "size");
    private static Field SKULL_PROFILE_FIELD;
    private static MethodHandle TEAM_FIELD;

    static {
        try {
            ENTITY_REGISTRY = new CustomEntityRegistry(
                    (RegistryBlocks<EntityTypes<?>>) NMS.getGetter(IRegistry.class, "ENTITY_TYPE").invoke());
            NMS.getFinalSetter(IRegistry.class, "ENTITY_TYPE").invoke(ENTITY_REGISTRY);
        } catch (Throwable e) {
            Messaging.logTr(Messages.ERROR_GETTING_ID_MAPPING, e.getMessage());
        }
        try {
            ENDERMAN_ANGRY = (DataWatcherObject<Boolean>) NMS.getField(EntityEnderman.class, "bo").get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
