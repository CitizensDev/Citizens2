package net.citizensnpcs.nms.v1_14_R1.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
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
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.CraftSound;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_14_R1.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_14_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftWither;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import net.citizensnpcs.api.astar.pathfinder.DoorExaminer;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.gui.ForwardingInventory;
import net.citizensnpcs.api.jnbt.ByteArrayTag;
import net.citizensnpcs.api.jnbt.ByteTag;
import net.citizensnpcs.api.jnbt.CompoundTag;
import net.citizensnpcs.api.jnbt.DoubleTag;
import net.citizensnpcs.api.jnbt.EndTag;
import net.citizensnpcs.api.jnbt.FloatTag;
import net.citizensnpcs.api.jnbt.IntArrayTag;
import net.citizensnpcs.api.jnbt.IntTag;
import net.citizensnpcs.api.jnbt.ListTag;
import net.citizensnpcs.api.jnbt.LongTag;
import net.citizensnpcs.api.jnbt.ShortTag;
import net.citizensnpcs.api.jnbt.StringTag;
import net.citizensnpcs.api.jnbt.Tag;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.EntityDim;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.nms.v1_14_R1.entity.ArmorStandController;
import net.citizensnpcs.nms.v1_14_R1.entity.BatController;
import net.citizensnpcs.nms.v1_14_R1.entity.BlazeController;
import net.citizensnpcs.nms.v1_14_R1.entity.CatController;
import net.citizensnpcs.nms.v1_14_R1.entity.CaveSpiderController;
import net.citizensnpcs.nms.v1_14_R1.entity.ChickenController;
import net.citizensnpcs.nms.v1_14_R1.entity.CodController;
import net.citizensnpcs.nms.v1_14_R1.entity.CowController;
import net.citizensnpcs.nms.v1_14_R1.entity.CreeperController;
import net.citizensnpcs.nms.v1_14_R1.entity.DolphinController;
import net.citizensnpcs.nms.v1_14_R1.entity.DrownedController;
import net.citizensnpcs.nms.v1_14_R1.entity.EnderDragonController;
import net.citizensnpcs.nms.v1_14_R1.entity.EndermanController;
import net.citizensnpcs.nms.v1_14_R1.entity.EndermiteController;
import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.citizensnpcs.nms.v1_14_R1.entity.EvokerController;
import net.citizensnpcs.nms.v1_14_R1.entity.FoxController;
import net.citizensnpcs.nms.v1_14_R1.entity.GhastController;
import net.citizensnpcs.nms.v1_14_R1.entity.GiantController;
import net.citizensnpcs.nms.v1_14_R1.entity.GuardianController;
import net.citizensnpcs.nms.v1_14_R1.entity.GuardianElderController;
import net.citizensnpcs.nms.v1_14_R1.entity.HorseController;
import net.citizensnpcs.nms.v1_14_R1.entity.HorseDonkeyController;
import net.citizensnpcs.nms.v1_14_R1.entity.HorseMuleController;
import net.citizensnpcs.nms.v1_14_R1.entity.HorseSkeletonController;
import net.citizensnpcs.nms.v1_14_R1.entity.HorseZombieController;
import net.citizensnpcs.nms.v1_14_R1.entity.HumanController;
import net.citizensnpcs.nms.v1_14_R1.entity.IllusionerController;
import net.citizensnpcs.nms.v1_14_R1.entity.IronGolemController;
import net.citizensnpcs.nms.v1_14_R1.entity.LlamaController;
import net.citizensnpcs.nms.v1_14_R1.entity.MagmaCubeController;
import net.citizensnpcs.nms.v1_14_R1.entity.MushroomCowController;
import net.citizensnpcs.nms.v1_14_R1.entity.OcelotController;
import net.citizensnpcs.nms.v1_14_R1.entity.PandaController;
import net.citizensnpcs.nms.v1_14_R1.entity.ParrotController;
import net.citizensnpcs.nms.v1_14_R1.entity.PhantomController;
import net.citizensnpcs.nms.v1_14_R1.entity.PigController;
import net.citizensnpcs.nms.v1_14_R1.entity.PigZombieController;
import net.citizensnpcs.nms.v1_14_R1.entity.PillagerController;
import net.citizensnpcs.nms.v1_14_R1.entity.PolarBearController;
import net.citizensnpcs.nms.v1_14_R1.entity.PufferFishController;
import net.citizensnpcs.nms.v1_14_R1.entity.RabbitController;
import net.citizensnpcs.nms.v1_14_R1.entity.RavagerController;
import net.citizensnpcs.nms.v1_14_R1.entity.SalmonController;
import net.citizensnpcs.nms.v1_14_R1.entity.SheepController;
import net.citizensnpcs.nms.v1_14_R1.entity.ShulkerController;
import net.citizensnpcs.nms.v1_14_R1.entity.SilverfishController;
import net.citizensnpcs.nms.v1_14_R1.entity.SkeletonController;
import net.citizensnpcs.nms.v1_14_R1.entity.SkeletonStrayController;
import net.citizensnpcs.nms.v1_14_R1.entity.SkeletonWitherController;
import net.citizensnpcs.nms.v1_14_R1.entity.SlimeController;
import net.citizensnpcs.nms.v1_14_R1.entity.SnowmanController;
import net.citizensnpcs.nms.v1_14_R1.entity.SpiderController;
import net.citizensnpcs.nms.v1_14_R1.entity.SquidController;
import net.citizensnpcs.nms.v1_14_R1.entity.TraderLlamaController;
import net.citizensnpcs.nms.v1_14_R1.entity.TropicalFishController;
import net.citizensnpcs.nms.v1_14_R1.entity.TurtleController;
import net.citizensnpcs.nms.v1_14_R1.entity.VexController;
import net.citizensnpcs.nms.v1_14_R1.entity.VillagerController;
import net.citizensnpcs.nms.v1_14_R1.entity.VindicatorController;
import net.citizensnpcs.nms.v1_14_R1.entity.WanderingTraderController;
import net.citizensnpcs.nms.v1_14_R1.entity.WitchController;
import net.citizensnpcs.nms.v1_14_R1.entity.WitherController;
import net.citizensnpcs.nms.v1_14_R1.entity.WolfController;
import net.citizensnpcs.nms.v1_14_R1.entity.ZombieController;
import net.citizensnpcs.nms.v1_14_R1.entity.ZombieHuskController;
import net.citizensnpcs.nms.v1_14_R1.entity.ZombieVillagerController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.AreaEffectCloudController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.BoatController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.DragonFireballController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.EggController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.EnderPearlController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.EnderSignalController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.EvokerFangsController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.FallingBlockController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.FireworkController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.FishingHookController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.ItemController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.ItemFrameController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.LargeFireballController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.LeashController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.LlamaSpitController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.MinecartChestController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.PaintingController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.ShulkerBulletController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.SmallFireballController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.SnowballController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.SpectralArrowController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.ThrownPotionController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.ThrownTridentController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.TippedArrowController;
import net.citizensnpcs.nms.v1_14_R1.entity.nonliving.WitherSkullController;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.versioned.BossBarTrait;
import net.citizensnpcs.trait.versioned.CatTrait;
import net.citizensnpcs.trait.versioned.EnderDragonTrait;
import net.citizensnpcs.trait.versioned.FoxTrait;
import net.citizensnpcs.trait.versioned.LlamaTrait;
import net.citizensnpcs.trait.versioned.MushroomCowTrait;
import net.citizensnpcs.trait.versioned.PandaTrait;
import net.citizensnpcs.trait.versioned.ParrotTrait;
import net.citizensnpcs.trait.versioned.PhantomTrait;
import net.citizensnpcs.trait.versioned.PolarBearTrait;
import net.citizensnpcs.trait.versioned.PufferFishTrait;
import net.citizensnpcs.trait.versioned.ShulkerTrait;
import net.citizensnpcs.trait.versioned.SnowmanTrait;
import net.citizensnpcs.trait.versioned.SpellcasterTrait;
import net.citizensnpcs.trait.versioned.TropicalFishTrait;
import net.citizensnpcs.trait.versioned.VillagerTrait;
import net.citizensnpcs.util.EmptyChannel;
import net.citizensnpcs.util.EntityPacketTracker;
import net.citizensnpcs.util.EntityPacketTracker.PacketAggregator;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.NMSBridge;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_14_R1.AdvancementDataPlayer;
import net.minecraft.server.v1_14_R1.AttributeInstance;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BehaviorController;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.BossBattleServer;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.ChunkProviderServer;
import net.minecraft.server.v1_14_R1.Container;
import net.minecraft.server.v1_14_R1.ContainerAccess;
import net.minecraft.server.v1_14_R1.ContainerAnvil;
import net.minecraft.server.v1_14_R1.Containers;
import net.minecraft.server.v1_14_R1.ControllerJump;
import net.minecraft.server.v1_14_R1.ControllerLook;
import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.DataWatcherObject;
import net.minecraft.server.v1_14_R1.EnchantmentManager;
import net.minecraft.server.v1_14_R1.Enchantments;
import net.minecraft.server.v1_14_R1.EnderDragonBattle;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityBird;
import net.minecraft.server.v1_14_R1.EntityCat;
import net.minecraft.server.v1_14_R1.EntityEnderDragon;
import net.minecraft.server.v1_14_R1.EntityEnderman;
import net.minecraft.server.v1_14_R1.EntityFish;
import net.minecraft.server.v1_14_R1.EntityFishSchool;
import net.minecraft.server.v1_14_R1.EntityFishingHook;
import net.minecraft.server.v1_14_R1.EntityHorse;
import net.minecraft.server.v1_14_R1.EntityHorseAbstract;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityPolarBear;
import net.minecraft.server.v1_14_R1.EntityPose;
import net.minecraft.server.v1_14_R1.EntityPufferFish;
import net.minecraft.server.v1_14_R1.EntityRabbit;
import net.minecraft.server.v1_14_R1.EntityShulker;
import net.minecraft.server.v1_14_R1.EntitySize;
import net.minecraft.server.v1_14_R1.EntityTameableAnimal;
import net.minecraft.server.v1_14_R1.EntityTrackerEntry;
import net.minecraft.server.v1_14_R1.EntityTurtle;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityWither;
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.EnumMoveType;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.IInventory;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.MobEffects;
import net.minecraft.server.v1_14_R1.NavigationAbstract;
import net.minecraft.server.v1_14_R1.NetworkManager;
import net.minecraft.server.v1_14_R1.Packet;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_14_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_14_R1.PathEntity;
import net.minecraft.server.v1_14_R1.PathPoint;
import net.minecraft.server.v1_14_R1.PathType;
import net.minecraft.server.v1_14_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_14_R1.PlayerChunkMap;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;
import net.minecraft.server.v1_14_R1.RegistryBlocks;
import net.minecraft.server.v1_14_R1.ScoreboardTeam;
import net.minecraft.server.v1_14_R1.SoundEffect;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.VoxelShape;
import net.minecraft.server.v1_14_R1.WorldServer;

@SuppressWarnings("unchecked")
public class NMSImpl implements NMSBridge {
    public NMSImpl() {
        loadEntityTypes();
    }

    @Override
    public boolean addEntityToWorld(org.bukkit.entity.Entity entity, SpawnReason custom) {
        int viewDistance = -1;
        PlayerChunkMap chunkMap = null;
        try {
            if (entity instanceof Player) {
                chunkMap = (PlayerChunkMap) PLAYER_CHUNK_MAP_GETTER.invoke(getHandle(entity).world.getChunkProvider());
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
        List<? extends EntityHuman> players = handle.world.getPlayers();
        boolean changed = false;
        if (remove && players.contains(handle)) {
            players.remove(handle);
            changed = true;
        } else if (!remove && !players.contains(handle)) {
            ((List) players).add(handle);
            changed = true;
        }
        if (!changed)
            return;
        try {
            CHUNKMAP_UPDATE_PLAYER_STATUS.invoke(((WorldServer) handle.world).getChunkProvider().playerChunkMap, handle,
                    !remove);
        } catch (Throwable e) {
            e.printStackTrace();
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
            handle.setMot(handle.getMot().d(0.6, 1, 0.6));
        }
        int fireAspectLevel = EnchantmentManager.getFireAspectEnchantmentLevel(handle);
        if (fireAspectLevel > 0) {
            target.setOnFire(fireAspectLevel * 4);
        }
    }

    @Override
    public void cancelMoveDestination(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        if (handle instanceof EntityInsentient) {
            try {
                MOVE_CONTROLLER_MOVING.invoke(((EntityInsentient) handle).getControllerMove(), null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).getControllerMove().f = false;
        }
    }

    @Override
    public EntityPacketTracker createPacketTracker(org.bukkit.entity.Entity entity, PacketAggregator agg) {
        Entity handle = getHandle(entity);
        Set<EntityPlayer> linked = Sets.newIdentityHashSet();
        EntityTrackerEntry tracker = new EntityTrackerEntry((WorldServer) handle.world, handle,
                handle.getEntityType().getUpdateInterval(), handle.getEntityType().isDeltaTracking(), agg::send,
                linked);
        Map<EnumItemSlot, ItemStack> equipment = Maps.newEnumMap(EnumItemSlot.class);
        return new EntityPacketTracker() {
            @Override
            public void link(Player player) {
                EntityPlayer p = (EntityPlayer) getHandle(player);
                handle.dead = false;
                tracker.b(p);
                linked.add(p);
                agg.add(p.getUniqueID(), packet -> p.playerConnection.sendPacket((Packet<?>) packet));
                handle.dead = true;
            }

            @Override
            public void run() {
                if (handle instanceof EntityLiving) {
                    boolean changed = false;
                    EntityLiving entity = (EntityLiving) handle;
                    for (EnumItemSlot slot : EnumItemSlot.values()) {
                        ItemStack old = equipment.getOrDefault(slot, ItemStack.a);
                        ItemStack curr = entity.getEquipment(slot);
                        if (!changed && !ItemStack.matches(old, curr)) {
                            changed = true;
                        }
                        equipment.put(slot, curr);
                    }
                    if (changed) {
                        for (EnumItemSlot slot : EnumItemSlot.values()) {
                            agg.send(new PacketPlayOutEntityEquipment(handle.getId(), slot, equipment.get(slot)));
                        }
                    }
                }
                tracker.a();
            }

            @Override
            public void unlink(Player player) {
                EntityPlayer p = (EntityPlayer) getHandle(player);
                tracker.a(p);
                linked.remove(p);
                agg.removeConnection(p.getUniqueID());
            }

            @Override
            public void unlinkAll(Consumer<Player> callback) {
                handle.die();
                for (EntityPlayer link : Lists.newArrayList(linked)) {
                    Player entity = link.getBukkitEntity();
                    unlink(entity);
                    if (callback != null) {
                        callback.accept(entity);
                    }
                }
                linked.clear();
            }
        };
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Throwable {
        if (Bukkit.isPrimaryThread())
            throw new IllegalStateException("NMS.fillProfileProperties cannot be invoked from the main thread.");
        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer()
                .getMinecraftSessionService();
        if (!(sessionService instanceof YggdrasilMinecraftSessionService))
            return sessionService.fillProfileProperties(profile, requireSecure);
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
                if (battleObject == null)
                    return null;
                bserver = ((EnderDragonBattle) battleObject).bossBattle;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (bserver == null)
            return null;
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
        return shape.isEmpty() ? new BoundingBox(0, 0, 0, 0, 0, 0) : NMSBoundingBox.wrap(shape.getBoundingBox());
    }

    @Override
    public Location getDestination(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        ControllerMove controller = handle instanceof EntityInsentient ? ((EntityInsentient) handle).getControllerMove()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getControllerMove() : null;
        if (controller == null || !controller.b())
            return null;
        return new Location(entity.getWorld(), controller.d(), controller.e(), controller.f());
    }

    @Override
    public GameProfileRepository getGameProfileRepository() {
        return ((CraftServer) Bukkit.getServer()).getServer().getGameProfileRepository();
    }

    @Override
    public float getHeadYaw(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof LivingEntity))
            return entity.getLocation().getYaw();
        return getHandle((LivingEntity) entity).aM;
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
        return handle.bd;
    }

    @Override
    public CompoundTag getNBT(org.bukkit.inventory.ItemStack item) {
        return convertNBT(CraftItemStack.asNMSCopy(item).getTag());
    }

    @Override
    public NPC getNPC(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof NPCHolder ? ((NPCHolder) handle).getNPC() : null;
    }

    @Override
    public EntityPacketTracker getPacketTracker(org.bukkit.entity.Entity entity) {
        WorldServer server = (WorldServer) NMSImpl.getHandle(entity).getWorld();
        EntityTracker entry = server.getChunkProvider().playerChunkMap.trackedEntities.get(entity.getEntityId());
        if (entry == null)
            return null;
        return new EntityPacketTracker() {
            @Override
            public void link(Player player) {
                entry.updatePlayer((EntityPlayer) getHandle(player));
            }

            @Override
            public void run() {
            }

            @Override
            public void unlink(Player player) {
                entry.clear((EntityPlayer) getHandle(player));
            }

            @Override
            public void unlinkAll(Consumer<Player> callback) {
                entry.a();
            }
        };
    }

    @Override
    public List<org.bukkit.entity.Entity> getPassengers(org.bukkit.entity.Entity entity) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null || handle.passengers == null)
            return Lists.newArrayList();
        return Lists.transform(handle.passengers, Entity::getBukkitEntity);
    }

    @Override
    public GameProfile getProfile(Player player) {
        return ((EntityHuman) getHandle(player)).getProfile();
    }

    @Override
    public GameProfile getProfile(SkullMeta meta) {
        if (SKULL_PROFILE_FIELD == null) {
            SKULL_PROFILE_FIELD = NMS.getField(meta.getClass(), "profile", false);
            if (SKULL_PROFILE_FIELD == null)
                return null;
        }
        try {
            return (GameProfile) SKULL_PROFILE_FIELD.get(meta);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getSoundPath(Sound flag) throws CommandException {
        try {
            String ret = CraftSound.getSound(flag);
            if (ret == null)
                throw new CommandException(Messages.INVALID_SOUND);
            return ret;
        } catch (Exception e) {
            throw new CommandException(Messages.INVALID_SOUND);
        }
    }

    @Override
    public org.bukkit.entity.Entity getSource(BlockCommandSender sender) {
        Entity source = ((CraftBlockCommandSender) sender).getWrapper().getEntity();
        return source != null ? source.getBukkitEntity() : null;
    }

    @Override
    public float getSpeedFor(NPC npc) {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity))
            return DEFAULT_SPEED;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) npc.getEntity());
        if (handle == null) {
        }
        return DEFAULT_SPEED;
        // return (float)
        // handle.getAttributeInstance(GenericAttributes.d).getValue();
    }

    @Override
    public float getStepHeight(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).K;
    }

    @Override
    public MCNavigator getTargetNavigator(org.bukkit.entity.Entity entity, Iterable<Vector> dest,
            final NavigatorParameters params) {
        List<PathPoint> list = Lists.<PathPoint> newArrayList(Iterables.<Vector, PathPoint> transform(dest,
                input -> new PathPoint(input.getBlockX(), input.getBlockY(), input.getBlockZ())));
        PathPoint last = list.size() > 0 ? list.get(list.size() - 1) : null;
        final PathEntity path = new PathEntity(list, last != null ? new BlockPosition(last.a, last.b, last.c) : null,
                true);
        return getTargetNavigator(entity, params, input -> input.a(path, params.speed()));
    }

    @Override
    public MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final Location dest,
            final NavigatorParameters params) {
        return getTargetNavigator(entity, params,
                input -> input.a(dest.getX(), dest.getY(), dest.getZ(), params.speed()));
    }

    private MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final NavigatorParameters params,
            final Function<NavigationAbstract, Boolean> function) {
        net.minecraft.server.v1_14_R1.Entity raw = getHandle(entity);
        raw.onGround = true;
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
        navigation.q().b(params.hasExaminer(DoorExaminer.class));
        return new MCNavigator() {
            float lastSpeed;
            CancelReason reason;

            private List<org.bukkit.block.Block> getBlocks(final org.bukkit.entity.Entity entity,
                    final NavigationAbstract navigation) {
                List<org.bukkit.block.Block> blocks = Lists.newArrayList();
                for (int i = 0; i < navigation.l().e(); i++) {
                    PathPoint pp = navigation.l().a(i);
                    blocks.add(entity.getWorld().getBlockAt(pp.a, pp.b, pp.c));
                }
                return blocks;
            }

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
                if (params.debug() && navigation.l() != null) {
                    List<org.bukkit.block.Block> blocks = getBlocks(entity, navigation);
                    Util.sendBlockChanges(blocks, null);
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
                    Util.sendBlockChanges(getBlocks(entity, navigation), Material.DANDELION);
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
        return navigation == null ? null : new NavigationFieldWrapper(entity, navigation, target, parameters);
    }

    @Override
    public org.bukkit.entity.Entity getVehicle(org.bukkit.entity.Entity entity) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return null;
        Entity e = handle.getVehicle();
        return e == handle || e == null ? null : e.getBukkitEntity();
    }

    @Override
    public float getVerticalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) entity);
        return handle.bb;
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
        return NMSImpl.getHandle(entity).onGround;
    }

    @Override
    public boolean isSolid(org.bukkit.block.Block in) {
        IBlockData data = ((CraftBlock) in).getNMS();
        return data.m(((CraftWorld) in.getWorld()).getHandle(), new BlockPosition(in.getX(), in.getY(), in.getZ()));
    }

    @Override
    public boolean isValid(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle.valid && handle.isAlive();
    }

    @Override
    public void load(CommandManager manager) {
        registerTraitWithCommand(manager, EnderDragonTrait.class);
        registerTraitWithCommand(manager, BossBarTrait.class);
        registerTraitWithCommand(manager, CatTrait.class);
        registerTraitWithCommand(manager, FoxTrait.class);
        registerTraitWithCommand(manager, LlamaTrait.class);
        registerTraitWithCommand(manager, MushroomCowTrait.class);
        registerTraitWithCommand(manager, ParrotTrait.class);
        registerTraitWithCommand(manager, PandaTrait.class);
        registerTraitWithCommand(manager, PhantomTrait.class);
        registerTraitWithCommand(manager, PolarBearTrait.class);
        registerTraitWithCommand(manager, PufferFishTrait.class);
        registerTraitWithCommand(manager, SpellcasterTrait.class);
        registerTraitWithCommand(manager, ShulkerTrait.class);
        registerTraitWithCommand(manager, SnowmanTrait.class);
        registerTraitWithCommand(manager, TropicalFishTrait.class);
        registerTraitWithCommand(manager, VillagerTrait.class);
    }

    private void loadEntityTypes() {
        EntityControllers.setEntityControllerForType(EntityType.AREA_EFFECT_CLOUD, AreaEffectCloudController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARROW, TippedArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARMOR_STAND, ArmorStandController.class);
        EntityControllers.setEntityControllerForType(EntityType.BAT, BatController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.PIG_ZOMBIE, PigZombieController.class);
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
    public void look(org.bukkit.entity.Entity entity, float yaw, float pitch) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        yaw = Util.clamp(yaw);
        handle.yaw = yaw;
        setHeadYaw(entity, yaw);
        handle.pitch = pitch;
    }

    @Override
    public void look(org.bukkit.entity.Entity entity, Location to, boolean headOnly, boolean immediate) {
        Entity handle = NMSImpl.getHandle(entity);
        if (immediate || headOnly || BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())
                || !(handle instanceof EntityInsentient) && !(handle instanceof EntityHumanNPC)) {
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
            if (zDiff < 0.0) {
                yaw += Math.abs(180 - yaw) * 2;
            }
            if (handle instanceof EntityEnderDragon) {
                yaw = Util.getDragonYaw(handle.getBukkitEntity(), xDiff, zDiff);
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
                    ((EntityInsentient) handle).dB(), ((EntityInsentient) handle).M());
            while (((EntityLiving) handle).aM >= 180F) {
                ((EntityLiving) handle).aM -= 360F;
            }
            while (((EntityLiving) handle).aM < -180F) {
                ((EntityLiving) handle).aM += 360F;
            }
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).getNPC().getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToFace(to);
        }
    }

    @Override
    public void look(org.bukkit.entity.Entity from, org.bukkit.entity.Entity to) {
        Entity handle = NMSImpl.getHandle(from), target = NMSImpl.getHandle(to);
        if (BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())
                || !(handle instanceof EntityInsentient) && !(handle instanceof EntityHumanNPC)) {
            if (to instanceof LivingEntity) {
                look(from, ((LivingEntity) to).getEyeLocation(), false, true);
            } else {
                look(from, to.getLocation(), false, true);
            }
        } else if (handle instanceof EntityInsentient) {
            ((EntityInsentient) handle).getControllerLook().a(target, ((EntityInsentient) handle).dB(),
                    ((EntityInsentient) handle).M());
            while (((EntityLiving) handle).aM >= 180F) {
                ((EntityLiving) handle).aM -= 360F;
            }
            while (((EntityLiving) handle).aM < -180F) {
                ((EntityLiving) handle).aM += 360F;
            }
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).getNPC().getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToFace(to);
        }
    }

    @Override
    public void mount(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity passenger) {
        if (NMSImpl.getHandle(passenger) == null)
            return;
        NMSImpl.getHandle(passenger).a(NMSImpl.getHandle(entity), true);
    }

    @Override
    public InventoryView openAnvilInventory(Player player, Inventory anvil, String title) {
        EntityPlayer handle = (EntityPlayer) getHandle(player);
        ContainerAccess access = ContainerAccess.at(handle.world, new BlockPosition(0, 0, 0));
        final ContainerAnvil container = new ContainerAnvil(handle.nextContainerCounter(), handle.inventory, access) {
            private CraftInventoryView bukkitEntity;

            @Override
            protected void a(EntityHuman entityhuman, net.minecraft.server.v1_14_R1.World world,
                    IInventory iinventory) {
            }

            @Override
            public void e() {
                super.e();
                levelCost.set(0);
                getBukkitView().getTopInventory().setItem(2, CraftItemStack.asCraftMirror(slots.get(2).getItem()));
            }

            @Override
            public CraftInventoryView getBukkitView() {
                if (this.bukkitEntity != null) {
                    try {
                        this.bukkitEntity = new CraftInventoryView(player,
                                new CitizensInventoryAnvil(access.getLocation(),
                                        (IInventory) REPAIR_INVENTORY.invoke(this),
                                        (IInventory) RESULT_INVENTORY.invoke(this), this, anvil),
                                this);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return super.getBukkitView();
                    }
                }
                return this.bukkitEntity;
            }
        };
        container.getBukkitView().setItem(0, anvil.getItem(0));
        container.getBukkitView().setItem(1, anvil.getItem(1));
        container.checkReachable = false;
        handle.playerConnection.sendPacket(
                new PacketPlayOutOpenWindow(container.windowId, container.getType(), new ChatMessage(title)));
        handle.activeContainer = container;
        handle.syncInventory();
        return container.getBukkitView();
    }

    @Override
    public void openHorseScreen(Tameable horse, Player equipper) {
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) horse);
        EntityLiving equipperHandle = NMSImpl.getHandle(equipper);
        if (handle == null || equipperHandle == null)
            return;
        boolean wasTamed = horse.isTamed();
        horse.setTamed(true);
        ((EntityHorseAbstract) handle).c(equipperHandle);
        horse.setTamed(wasTamed);
    }

    @Override
    public void playAnimation(PlayerAnimation animation, Player player, Iterable<Player> to) {
        PlayerAnimationImpl.play(animation, player, to);
    }

    @Override
    public Runnable playerTicker(Player entity) {
        return ((EntityPlayer) getHandle(entity))::playerTick;
    }

    @Override
    public void registerEntityClass(Class<?> clazz) {
        if (ENTITY_REGISTRY == null)
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            EntityTypes<?> type = ENTITY_REGISTRY.findType(search);
            MinecraftKey key = ENTITY_REGISTRY.getKey(type);
            if (key == null || type == null) {
                continue;
            }
            CITIZENS_ENTITY_TYPES.put(clazz, type);
            int code = ENTITY_REGISTRY.a(type);
            ENTITY_REGISTRY.put(code, key, type);
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass for class " + clazz.toString());
    }

    private void registerTraitWithCommand(CommandManager manager, Class<? extends Trait> clazz) {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(clazz));
        manager.register(clazz);
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
    public void removeHookIfNecessary(FishHook entity) {
        EntityFishingHook hook = (EntityFishingHook) NMSImpl.getHandle(entity);
        if (hook.hooked == null)
            return;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(hook.hooked.getBukkitEntity());
        if (npc == null)
            return;
        if (npc.isProtected()) {
            hook.hooked = null;
            hook.getBukkitEntity().remove();
        }
    }

    @Override
    public void replaceTrackerEntry(org.bukkit.entity.Entity entity) {
        WorldServer server = (WorldServer) NMSImpl.getHandle(entity).getWorld();
        EntityTracker entry = server.getChunkProvider().playerChunkMap.trackedEntities.get(entity.getEntityId());
        if (entry == null)
            return;
        entry.a();
        PlayerlistTracker replace = new PlayerlistTracker(server.getChunkProvider().playerChunkMap, entry);
        server.getChunkProvider().playerChunkMap.trackedEntities.put(entity.getEntityId(), replace);
        if (getHandle(entity) instanceof EntityHumanNPC) {
            ((EntityHumanNPC) getHandle(entity)).setTracked(replace);
        }
    }

    @Override
    public void sendPositionUpdate(org.bukkit.entity.Entity from, boolean position, Float bodyYaw, Float pitch,
            Float headYaw) {
        Entity handle = getHandle(from);
        if (bodyYaw == null) {
            bodyYaw = handle.yaw;
        }
        if (pitch == null) {
            pitch = handle.pitch;
        }
        List<Packet<?>> toSend = Lists.newArrayList();
        if (position) {
            EntityTracker entry = ((WorldServer) handle.world).getChunkProvider().playerChunkMap.trackedEntities
                    .get(handle.getId());
            EntityTrackerEntry ete = null;
            try {
                ete = (EntityTrackerEntry) ENTITY_TRACKER_ENTRY_GETTER.invoke(entry);
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }
            Vec3D pos = handle.getPositionVector().d(ete.b());
            toSend.add(new PacketPlayOutRelEntityMoveLook(handle.getId(), (short) pos.x, (short) pos.y, (short) pos.z,
                    (byte) (bodyYaw * 256.0F / 360.0F), (byte) (pitch * 256.0F / 360.0F), handle.onGround));
        } else {
            toSend.add(new PacketPlayOutEntityLook(handle.getId(), (byte) (bodyYaw * 256.0F / 360.0F),
                    (byte) (pitch * 256.0F / 360.0F), handle.onGround));
        }
        if (headYaw != null) {
            toSend.add(new PacketPlayOutEntityHeadRotation(handle, (byte) (headYaw * 256.0F / 360.0F)));
        }
        sendPacketsNearby(null, from.getLocation(), toSend, 64);
    }

    @Override
    public boolean sendTabListAdd(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);
        EntityPlayer entity = ((CraftPlayer) listPlayer).getHandle();
        NMSImpl.sendPacket(recipient,
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity));
        return true;
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
    public void setAggressive(org.bukkit.entity.Entity entity, boolean aggro) {
        Entity handle = getHandle(entity);
        if (!(handle instanceof EntityInsentient))
            return;
        ((EntityInsentient) handle).q(aggro);
    }

    @Override
    public void setBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        getHandle(entity).yaw = yaw;
    }

    @Override
    public void setBoundingBox(org.bukkit.entity.Entity entity, BoundingBox box) {
        NMSImpl.getHandle(entity).a(new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ));
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, Object component, String string) {
        getHandle(entity).setCustomName((IChatBaseComponent) component);
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
    public void setDimensions(org.bukkit.entity.Entity bentity, EntityDim desired) {
        EntitySize entitysize = new EntitySize(desired.width, desired.height, true);
        Entity entity = getHandle(bentity);
        try {
            SIZE_FIELD_SETTER.invoke(entity, entitysize);
            HEAD_HEIGHT.invoke(entity, HEAD_HEIGHT_METHOD.invoke(entity, entity.getPose(), entity.a(entity.getPose())));
        } catch (Throwable ex) {
            ex.printStackTrace();
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
        yaw = Util.clamp(yaw);
        handle.aL = yaw;
        if (!(handle instanceof EntityHuman)) {
            handle.aK = yaw;
        }
        handle.aM = yaw;
    }

    @Override
    public void setKnockbackResistance(LivingEntity entity, double d) {
        EntityLiving handle = NMSImpl.getHandle(entity);
        handle.getAttributeInstance(GenericAttributes.KNOCKBACK_RESISTANCE).setValue(d);
    }

    @Override
    public void setLocationDirectly(org.bukkit.entity.Entity entity, Location location) {
        getHandle(entity).setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
    }

    @Override
    public void setLyingDown(org.bukkit.entity.Entity cat, boolean lying) {
        ((EntityCat) getHandle(cat)).u(lying);
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
    public void setPeekShulker(org.bukkit.entity.Entity shulker, int peek) {
        ((EntityShulker) getHandle(shulker)).a((byte) peek);
    }

    @Override
    public void setPitch(org.bukkit.entity.Entity entity, float pitch) {
        getHandle(entity).pitch = pitch;
    }

    @Override
    public void setPolarBearRearing(org.bukkit.entity.Entity entity, boolean rearing) {
        ((EntityPolarBear) getHandle(entity)).r(rearing);
    }

    @Override
    public void setProfile(SkullMeta meta, GameProfile profile) {
        if (SKULL_PROFILE_FIELD == null) {
            SKULL_PROFILE_FIELD = NMS.getField(meta.getClass(), "profile", false);
            if (SKULL_PROFILE_FIELD == null)
                return;
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
        // sitting removed
    }

    @Override
    public void setSitting(Tameable tameable, boolean sitting) {
        ((EntityTameableAnimal) NMSImpl.getHandle((LivingEntity) tameable)).setSitting(sitting);
    }

    @Override
    public void setSneaking(org.bukkit.entity.Entity entity, boolean sneaking) {
        if (entity instanceof Player) {
            ((Player) entity).setSneaking(sneaking);
        }
        EntityPose pose = sneaking ? EntityPose.SNEAKING : EntityPose.STANDING;
        try {
            ENTITY_SETPOSE.invoke(getHandle(entity), pose);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        NMSImpl.getHandle(entity).K = height;
    }

    @Override
    public void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        if (!bukkitEntity.getType().isAlive())
            return;
        EntityLiving handle = NMSImpl.getHandle((LivingEntity) bukkitEntity);
        handle.bb = (float) d;
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
            field.invoke(null, ENTITY_REGISTRY.get());
        } catch (Throwable e) {
        }
    }

    @Override
    public void sleep(Player player, boolean sleep) {
        try {
            ENTITY_SETPOSE_METHOD.invoke(getHandle(player), sleep ? EntityPose.SLEEPING : EntityPose.STANDING);
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
        if (RANDOM.nextFloat() <= 0.85F && (handle.isInWater() || handle.ay())) {
            handle.setMot(handle.getMot().getX(), handle.getMot().getY() + power, handle.getMot().getZ());
        }
    }

    @Override
    public void updateInventoryTitle(Player player, InventoryView view, String newTitle) {
        EntityPlayer handle = (EntityPlayer) getHandle(player);
        Containers<?> menuType = null;
        switch (view.getTopInventory().getType()) {
            case ANVIL:
                menuType = Containers.ANVIL;
                break;
            case BARREL:
                menuType = Containers.GENERIC_9X3;
                break;
            case BEACON:
                menuType = Containers.BEACON;
                break;
            case BLAST_FURNACE:
                menuType = Containers.BLAST_FURNACE;
                break;
            case BREWING:
                menuType = Containers.BREWING_STAND;
                break;
            case CARTOGRAPHY:
                menuType = Containers.CARTOGRAPHY;
                break;
            case CHEST:
                int sz = view.getTopInventory().getSize();
                if (sz > 45) {
                    menuType = Containers.GENERIC_9X6;
                } else if (sz > 36) {
                    menuType = Containers.GENERIC_9X5;
                } else if (sz > 27) {
                    menuType = Containers.GENERIC_9X4;
                } else if (sz > 18) {
                    menuType = Containers.GENERIC_9X3;
                } else if (sz > 9) {
                    menuType = Containers.GENERIC_9X2;
                } else {
                    menuType = Containers.GENERIC_9X1;
                }
                break;
            case PLAYER:
            case CRAFTING:
            case CREATIVE:
                return;
            case DISPENSER:
            case DROPPER:
                menuType = Containers.GENERIC_3X3;
                break;
            case ENCHANTING:
                menuType = Containers.ENCHANTMENT;
                break;
            case ENDER_CHEST:
                menuType = Containers.GENERIC_9X3;
                break;
            case FURNACE:
                menuType = Containers.FURNACE;
                break;
            case GRINDSTONE:
                menuType = Containers.GRINDSTONE;
                break;
            case HOPPER:
                menuType = Containers.HOPPER;
                break;
            case LECTERN:
                menuType = Containers.LECTERN;
                break;
            case LOOM:
                menuType = Containers.LOOM;
                break;
            case MERCHANT:
                menuType = Containers.MERCHANT;
                break;
            case SHULKER_BOX:
                menuType = Containers.SHULKER_BOX;
                break;
            case SMOKER:
                menuType = Containers.SMOKER;
                break;
            case STONECUTTER:
                menuType = Containers.STONECUTTER;
                break;
            case WORKBENCH:
                menuType = Containers.CRAFTING;
                break;
        }
        Container active = handle.activeContainer;
        handle.playerConnection
                .sendPacket(new PacketPlayOutOpenWindow(active.windowId, menuType, new ChatComponentText(newTitle)));
        player.updateInventory();
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
        if (PATHFINDING_RANGE == null)
            return;
        EntityInsentient handle = (EntityInsentient) en;
        NavigationAbstract navigation = handle.getNavigation();
        try {
            AttributeInstance inst = (AttributeInstance) PATHFINDING_RANGE.invoke(navigation);
            inst.setValue(pathfindingRange);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static class CitizensInventoryAnvil extends CraftInventoryAnvil implements ForwardingInventory {
        private final Inventory wrapped;

        public CitizensInventoryAnvil(Location location, IInventory inventory, IInventory resultInventory,
                ContainerAnvil container, Inventory wrapped) {
            super(location, inventory, resultInventory, container);
            this.wrapped = wrapped;
        }

        @Override
        public Inventory getWrapped() {
            return wrapped;
        }

        @Override
        public void setItem(int slot, org.bukkit.inventory.ItemStack item) {
            super.setItem(slot, item);
            wrapped.setItem(slot, item);
        }
    }

    private static class NavigationFieldWrapper implements TargetNavigator {
        private final org.bukkit.entity.Entity entity;
        private final NavigationAbstract navigation;
        private final NavigatorParameters parameters;
        private final org.bukkit.entity.Entity target;

        private NavigationFieldWrapper(org.bukkit.entity.Entity entity, NavigationAbstract navigation,
                org.bukkit.entity.Entity target, NavigatorParameters parameters) {
            this.entity = entity;
            this.navigation = navigation;
            this.target = target;
            this.parameters = parameters;
        }

        @Override
        public Location getCurrentDestination() {
            return NMS.getDestination(entity);
        }

        @Override
        public Iterable<Vector> getPath() {
            return new NavigationIterable(navigation);
        }

        @Override
        public void setPath() {
            Location location = parameters.entityTargetLocationMapper().apply(target);
            if (location == null)
                throw new IllegalStateException("mapper should not return null");
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
            final int npoints = navigation.l() == null ? 0 : navigation.l().e();
            return new Iterator<Vector>() {
                PathPoint curr = npoints > 0 ? navigation.l().a(0) : null;
                int i = 0;

                @Override
                public boolean hasNext() {
                    return curr != null;
                }

                @Override
                public Vector next() {
                    PathPoint old = curr;
                    curr = i + 1 < npoints ? navigation.l().a(++i) : null;
                    return new Vector(old.a, old.b, old.c);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public static void checkAndUpdateHeight(EntityLiving living, DataWatcherObject<?> datawatcherobject,
            Consumer<DataWatcherObject<?>> cb) {
        EntitySize size;
        try {
            size = (EntitySize) SIZE_FIELD_GETTER.invoke(living);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        float oldw = size.width;
        float oldl = size.height;
        cb.accept(datawatcherobject);
        if (oldw != size.width || size.height != oldl) {
            living.setPosition(living.locX - 0.01, living.locY, living.locZ - 0.01);
            living.setPosition(living.locX + 0.01, living.locY, living.locZ + 0.01);
        }
    }

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        if (GOAL_FIELD == null || goalSelectors == null)
            return;
        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                Collection<?> list = (Collection<?>) GOAL_FIELD.invoke(selector);
                list.clear();
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            } catch (Throwable e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            }
        }
    }

    private static CompoundTag convertNBT(net.minecraft.server.v1_14_R1.NBTTagCompound tag) {
        if (tag == null)
            return new CompoundTag("", Collections.EMPTY_MAP);
        Map<String, Tag> tags = Maps.newHashMap();
        for (String key : tag.getKeys()) {
            tags.put(key, convertNBT(key, tag.get(key)));
        }
        return new CompoundTag("", tags);
    }

    private static Tag convertNBT(String key, net.minecraft.server.v1_14_R1.NBTBase base) {
        if (base instanceof net.minecraft.server.v1_14_R1.NBTTagInt)
            return new IntTag(key, ((net.minecraft.server.v1_14_R1.NBTTagInt) base).asInt());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagFloat)
            return new FloatTag(key, ((net.minecraft.server.v1_14_R1.NBTTagFloat) base).asFloat());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagDouble)
            return new DoubleTag(key, ((net.minecraft.server.v1_14_R1.NBTTagDouble) base).asDouble());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagLong)
            return new LongTag(key, ((net.minecraft.server.v1_14_R1.NBTTagLong) base).asLong());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagShort)
            return new ShortTag(key, ((net.minecraft.server.v1_14_R1.NBTTagShort) base).asShort());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagByte)
            return new ByteTag(key, ((net.minecraft.server.v1_14_R1.NBTTagByte) base).asByte());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagByteArray)
            return new ByteArrayTag(key, ((net.minecraft.server.v1_14_R1.NBTTagByteArray) base).getBytes());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagIntArray)
            return new IntArrayTag(key, ((net.minecraft.server.v1_14_R1.NBTTagIntArray) base).getInts());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagString)
            return new StringTag(key, base.asString());
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagList) {
            List<net.minecraft.server.v1_14_R1.NBTBase> list = (List<net.minecraft.server.v1_14_R1.NBTBase>) base;
            List<Tag> converted = Lists.newArrayList();
            if (list.size() > 0) {
                Class<? extends Tag> tagType = convertNBT("", list.get(0)).getClass();
                for (int i = 0; i < list.size(); i++) {
                    converted.add(convertNBT("", list.get(i)));
                }
                return new ListTag(key, tagType, converted);
            }
        } else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagCompound)
            return convertNBT((net.minecraft.server.v1_14_R1.NBTTagCompound) base);
        else if (base instanceof net.minecraft.server.v1_14_R1.NBTTagEnd)
            return new EndTag();
        return null;
    }

    public static void flyingMoveLogic(EntityLiving entity, Vec3D vec3d) {
        double d0;
        float f;
        if (entity.df() || entity.ca()) {
            d0 = 0.08D;
            boolean flag = entity.getMot().y <= 0.0D;
            if (flag && entity.hasEffect(MobEffects.SLOW_FALLING)) {
                d0 = 0.01D;
                entity.fallDistance = 0.0F;
            }
            double d1;
            float f1;
            double d2;
            Vec3D vec3d5;
            Vec3D vec3d2;
            if (!entity.isInWater() || entity instanceof EntityHuman && ((EntityHuman) entity).abilities.isFlying) {
                Vec3D vec3d4;
                if (entity.aD() && (!(entity instanceof EntityHuman) || !((EntityHuman) entity).abilities.isFlying)) {
                    d1 = entity.locY;
                    entity.a(0.02F, vec3d);
                    entity.move(EnumMoveType.SELF, entity.getMot());
                    entity.setMot(entity.getMot().a(0.5D));
                    if (!entity.isNoGravity()) {
                        entity.setMot(entity.getMot().add(0.0D, -d0 / 4.0D, 0.0D));
                    }
                    vec3d4 = entity.getMot();
                    if (entity.positionChanged
                            && entity.d(vec3d4.x, vec3d4.y + 0.6000000238418579D - entity.locY + d1, vec3d4.z)) {
                        entity.setMot(vec3d4.x, 0.30000001192092896D, vec3d4.z);
                    }
                } else if (entity.isGliding()) {
                    vec3d4 = entity.getMot();
                    if (vec3d4.y > -0.5D) {
                        entity.fallDistance = 1.0F;
                    }
                    vec3d5 = entity.getLookDirection();
                    f1 = entity.pitch * 0.017453292F;
                    double d3 = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
                    double d4 = Math.sqrt(Entity.b(vec3d4));
                    d2 = vec3d5.f();
                    float f3 = MathHelper.cos(f1);
                    f3 = (float) ((double) f3 * (double) f3 * Math.min(1.0D, d2 / 0.4D));
                    vec3d4 = entity.getMot().add(0.0D, d0 * (-1.0D + f3 * 0.75D), 0.0D);
                    double d5;
                    if (vec3d4.y < 0.0D && d3 > 0.0D) {
                        d5 = vec3d4.y * -0.1D * f3;
                        vec3d4 = vec3d4.add(vec3d5.x * d5 / d3, d5, vec3d5.z * d5 / d3);
                    }
                    if (f1 < 0.0F && d3 > 0.0D) {
                        d5 = d4 * -MathHelper.sin(f1) * 0.04D;
                        vec3d4 = vec3d4.add(-vec3d5.x * d5 / d3, d5 * 3.2D, -vec3d5.z * d5 / d3);
                    }
                    if (d3 > 0.0D) {
                        vec3d4 = vec3d4.add((vec3d5.x / d3 * d4 - vec3d4.x) * 0.1D, 0.0D,
                                (vec3d5.z / d3 * d4 - vec3d4.z) * 0.1D);
                    }
                    entity.setMot(vec3d4.d(0.9900000095367432D, 0.9800000190734863D, 0.9900000095367432D));
                    entity.move(EnumMoveType.SELF, entity.getMot());
                    if (entity.positionChanged && !entity.world.isClientSide) {
                        d5 = Math.sqrt(Entity.b(entity.getMot()));
                        double d6 = d4 - d5;
                        float f4 = (float) (d6 * 10.0D - 3.0D);
                        if (f4 > 0.0F) {
                            try {
                                entity.a((SoundEffect) ENTITY_GET_SOUND_FALL.invoke(entity, (int) f4), 1.0F, 1.0F);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            entity.damageEntity(DamageSource.FLY_INTO_WALL, f4);
                        }
                    }
                    if (entity.onGround && !entity.world.isClientSide && entity.getFlag(7)
                            && !CraftEventFactory.callToggleGlideEvent(entity, false).isCancelled()) {
                        entity.setFlag(7, false);
                    }
                } else {
                    BlockPosition blockposition = new BlockPosition(entity.locX, entity.getBoundingBox().minY - 1.0D,
                            entity.locZ);
                    float f5 = entity.world.getType(blockposition).getBlock().m();
                    f1 = entity.onGround ? f5 * 0.91F : 0.91F;
                    try {
                        entity.a((float) ENTITY_R.invoke(entity, f5), vec3d);
                        entity.setMot((Vec3D) ISCLIMBING_METHOD.invoke(entity, entity.getMot()));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    entity.move(EnumMoveType.SELF, entity.getMot());
                    vec3d2 = entity.getMot();
                    try {
                        if ((entity.positionChanged || (boolean) JUMP_FIELD.invoke(entity)) && entity.isClimbing()) {
                            vec3d2 = new Vec3D(vec3d2.x, 0.2D, vec3d2.z);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    double d7 = vec3d2.y;
                    if (entity.hasEffect(MobEffects.LEVITATION)) {
                        d7 += (0.05D * (entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec3d2.y) * 0.2D;
                        entity.fallDistance = 0.0F;
                    } else if (entity.world.isClientSide && !entity.world.isLoaded(blockposition)) {
                        if (entity.locY > 0.0D) {
                            d7 = -0.1D;
                        } else {
                            d7 = 0.0D;
                        }
                    } else if (!entity.isNoGravity()) {
                        d7 -= d0;
                    }
                    entity.setMot(vec3d2.x * f1, d7 * 0.9800000190734863D, vec3d2.z * f1);
                }
            } else {
                d1 = entity.locY;
                f1 = entity.isSprinting() ? 0.9F : entity instanceof EntityPolarBear ? 0.98F : 0.8F;
                f = 0.02F;
                float f2 = EnchantmentManager.e(entity);
                if (f2 > 3.0F) {
                    f2 = 3.0F;
                }
                if (!entity.onGround) {
                    f2 *= 0.5F;
                }
                if (f2 > 0.0F) {
                    f1 += (0.54600006F - f1) * f2 / 3.0F;
                    f += (entity.db() - f) * f2 / 3.0F;
                }
                if (entity.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    f1 = 0.96F;
                }
                entity.a(f, vec3d);
                entity.move(EnumMoveType.SELF, entity.getMot());
                vec3d5 = entity.getMot();
                if (entity.positionChanged && entity.isClimbing()) {
                    vec3d5 = new Vec3D(vec3d5.x, 0.2D, vec3d5.z);
                }
                entity.setMot(vec3d5.d(f1, 0.800000011920929D, f1));
                if (!entity.isNoGravity() && !entity.isSprinting()) {
                    vec3d2 = entity.getMot();
                    if (flag && Math.abs(vec3d2.y - 0.005D) >= 0.003D && Math.abs(vec3d2.y - d0 / 16.0D) < 0.003D) {
                        d2 = -0.003D;
                    } else {
                        d2 = vec3d2.y - d0 / 16.0D;
                    }
                    entity.setMot(vec3d2.x, d2, vec3d2.z);
                }
                vec3d2 = entity.getMot();
                if (entity.positionChanged
                        && entity.d(vec3d2.x, vec3d2.y + 0.6000000238418579D - entity.locY + d1, vec3d2.z)) {
                    entity.setMot(vec3d2.x, 0.30000001192092896D, vec3d2.z);
                }
            }
        }
        entity.aE = entity.aF;
        d0 = entity.locX - entity.lastX;
        double d8 = entity.locZ - entity.lastZ;
        double d9 = entity instanceof EntityBird ? entity.locY - entity.lastY : 0.0D;
        f = MathHelper.sqrt(d0 * d0 + d9 * d9 + d8 * d8) * 4.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        entity.aF += (f - entity.aF) * 0.4F;
        entity.aG += entity.aF;
    }

    public static TreeMap<?, ?> getBehaviorMap(EntityLiving entity) {
        try {
            return (TreeMap<?, ?>) BEHAVIOR_MAP.invoke(entity.getBehaviorController());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static BlockPosition.PooledBlockPosition getBlockPositionBE(BlockPosition.PooledBlockPosition blockPos,
            double x, double y, double z) {
        try {
            return blockPos.c(x, y, z);
        } catch (NoSuchMethodError ex) {
            try {
                return (BlockPosition.PooledBlockPosition) BLOCK_POSITION_B_D.invoke(blockPos, x, y, z);
            } catch (Throwable ex2) {
                ex2.printStackTrace();
                return null;
            }
        }
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

    public static NavigationAbstract getNavigation(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof EntityInsentient ? ((EntityInsentient) handle).getNavigation()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getNavigation() : null;
    }

    public static DataWatcherObject<Integer> getRabbitTypeField() {
        if (RABBIT_FIELD == null)
            return null;
        try {
            return (DataWatcherObject<Integer>) RABBIT_FIELD.invoke();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SoundEffect getSoundEffect(NPC npc, SoundEffect snd, NPC.Metadata meta) {
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

    public static boolean isLeashed(NPC npc, Supplier<Boolean> isLeashed, EntityInsentient entity) {
        return NMS.isLeashed(npc, isLeashed, () -> entity.unleash(true, false));
    }

    public static boolean isNavigationFinished(NavigationAbstract navigation) {
        return navigation.n();
    }

    @SuppressWarnings("deprecation")
    public static void minecartItemLogic(EntityMinecartAbstract minecart) {
        NPC npc = ((NPCHolder) minecart).getNPC();
        if (npc == null)
            return;
        Material mat = Material.getMaterial(npc.data().get(NPC.Metadata.MINECART_ITEM, ""));
        int data = npc.data().get(NPC.Metadata.MINECART_ITEM_DATA, 0); // TODO: migration for this
        int offset = npc.data().get(NPC.Metadata.MINECART_OFFSET, 0);
        minecart.a(mat != null);
        if (mat != null) {
            minecart.setDisplayBlock(Block.getByCombinedId(mat.getId()).getBlock().getBlockData());
        }
        minecart.setDisplayBlockOffset(offset);
    }

    public static boolean moveFish(NPC npc, EntityInsentient handle, Vec3D vec3d) {
        return moveFish(npc, handle, vec3d, -1);
    }

    public static boolean moveFish(NPC npc, EntityInsentient handle, Vec3D vec3d, float speed) {
        if (npc == null)
            return false;
        if (!npc.useMinecraftAI() && handle.isInWater() && !npc.getNavigator().isNavigating()) {
            handle.a(speed > 0 ? speed : handle instanceof EntityTurtle ? 0.1F : 0.01F, vec3d);
            handle.move(EnumMoveType.SELF, handle.getMot());
            handle.setMot(handle.getMot().a(0.9));
            return true;
        }
        return false;
    }

    public static void resetPuffTicks(EntityPufferFish fish) {
        try {
            PUFFERFISH_C.invoke(fish, 0);
            PUFFERFISH_D.invoke(fish, 0);
        } catch (Throwable e) {
            e.printStackTrace();
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
        List<Packet<?>> list = new ArrayList<>();
        list.add(packet);
        sendPacketsNearby(from, location, list, radius);
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet<?>> packets, double radius) {
        radius *= radius;
        final org.bukkit.World world = location.getWorld();
        for (Player player : CitizensAPI.getLocationLookup().getNearbyPlayers(location, radius)) {
            if (world != player.getWorld() || from != null && !player.canSee(from)
                    || location.distanceSquared(player.getLocation(PACKET_CACHE_LOCATION)) > radius) {
                continue;
            }
            for (Packet<?> packet : packets) {
                NMSImpl.sendPacket(player, packet);
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

    public static void setLookControl(EntityInsentient mob, ControllerLook control) {
        try {
            LOOK_CONTROL_SETTER.invoke(mob, control);
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
                entity.a(new AxisAlignedBB(entity.locX - d0, entity.locY, entity.locZ - d0, entity.locX + d0,
                        entity.locY + entitysize1.height, entity.locZ + d0));
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

    public static void updateNavigation(NavigationAbstract navigation) {
        navigation.c();
    }

    private static final MethodHandle ADVANCEMENT_PLAYER_FIELD = NMS.getFinalSetter(EntityPlayer.class,
            "advancementDataPlayer");

    private static final Set<EntityType> BAD_CONTROLLER_LOOK = EnumSet.of(EntityType.POLAR_BEAR, EntityType.SILVERFISH,
            EntityType.SHULKER, EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.BAT, EntityType.SLIME,
            EntityType.DOLPHIN, EntityType.MAGMA_CUBE, EntityType.HORSE, EntityType.GHAST, EntityType.SHULKER,
            EntityType.PHANTOM);
    private static final MethodHandle BEHAVIOR_MAP = NMS.getGetter(BehaviorController.class, "c");
    private static final MethodHandle BLOCK_POSITION_B_D = NMS.getMethodHandle(BlockPosition.PooledBlockPosition.class,
            "c", false, double.class, double.class, double.class);
    private static final MethodHandle BUKKITENTITY_FIELD_SETTER = NMS.getSetter(Entity.class, "bukkitEntity");
    private static final MethodHandle CHUNKMAP_UPDATE_PLAYER_STATUS = NMS.getMethodHandle(PlayerChunkMap.class, "a",
            true, EntityPlayer.class, boolean.class);
    private static final Map<Class<?>, EntityTypes<?>> CITIZENS_ENTITY_TYPES = Maps.newHashMap();
    private static final MethodHandle CRAFT_BOSSBAR_HANDLE_FIELD = NMS.getSetter(CraftBossBar.class, "handle");
    private static final float DEFAULT_SPEED = 1F;
    private static final MethodHandle ENDERDRAGON_BATTLE_FIELD = NMS.getGetter(EntityEnderDragon.class, "bP");
    public static MethodHandle ENDERDRAGON_CHECK_WALLS = NMS.getFirstMethodHandleWithReturnType(EntityEnderDragon.class,
            true, boolean.class, AxisAlignedBB.class);
    private static DataWatcherObject<Boolean> ENDERMAN_ANGRY;
    private static final MethodHandle ENTITY_FISH_NUM_IN_SCHOOL = NMS.getSetter(EntityFishSchool.class, "c", false);
    private static final MethodHandle ENTITY_GET_SOUND_FALL = NMS.getMethodHandle(EntityLiving.class, "getSoundFall",
            true, int.class);
    private static final MethodHandle ENTITY_R = NMS.getMethodHandle(EntityLiving.class, "r", true, float.class);
    private static CustomEntityRegistry ENTITY_REGISTRY;
    private static final MethodHandle ENTITY_SETPOSE = NMS.getMethodHandle(Entity.class, "setPose", false,
            EntityPose.class);
    private static final MethodHandle ENTITY_SETPOSE_METHOD = NMS.getMethodHandle(Entity.class, "setPose", true,
            EntityPose.class);
    private static final MethodHandle ENTITY_TRACKER_ENTRY_GETTER = NMS.getFirstGetter(EntityTracker.class,
            EntityTrackerEntry.class);
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle GOAL_FIELD = NMS.getGetter(PathfinderGoalSelector.class, "d");
    private static final MethodHandle HEAD_HEIGHT = NMS.getSetter(Entity.class, "headHeight");
    private static final MethodHandle HEAD_HEIGHT_METHOD = NMS.getMethodHandle(Entity.class, "getHeadHeight", true,
            EntityPose.class, EntitySize.class);
    private static final MethodHandle ISCLIMBING_METHOD = NMS.getMethodHandle(EntityLiving.class, "f", true,
            Vec3D.class);
    private static final MethodHandle JUMP_FIELD = NMS.getGetter(EntityLiving.class, "jumping");
    private static final MethodHandle LOOK_CONTROL_SETTER = NMS.getFirstSetter(EntityInsentient.class,
            ControllerLook.class);
    private static final MethodHandle MAKE_REQUEST = NMS.getMethodHandle(YggdrasilAuthenticationService.class,
            "makeRequest", true, URL.class, Object.class, Class.class);
    private static MethodHandle MOVE_CONTROLLER_MOVING = NMS.getSetter(ControllerMove.class, "h");
    private static final MethodHandle NAVIGATION_WORLD_FIELD = NMS.getSetter(NavigationAbstract.class, "b");
    public static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle PATHFINDING_RANGE = NMS.getGetter(NavigationAbstract.class, "p");
    private static final MethodHandle PLAYER_CHUNK_MAP_GETTER = NMS.getGetter(ChunkProviderServer.class,
            "playerChunkMap");
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER = NMS.getGetter(PlayerChunkMap.class,
            "viewDistance");
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER = NMS.getSetter(PlayerChunkMap.class,
            "viewDistance");
    private static final MethodHandle PUFFERFISH_C = NMS.getSetter(EntityPufferFish.class, "c");
    private static final MethodHandle PUFFERFISH_D = NMS.getSetter(EntityPufferFish.class, "d");
    private static final MethodHandle RABBIT_FIELD = NMS.getGetter(EntityRabbit.class, "bz");
    private static final Random RANDOM = Util.getFastRandom();
    private static final MethodHandle REPAIR_INVENTORY = NMS.getGetter(ContainerAnvil.class, "repairInventory");
    private static final MethodHandle RESULT_INVENTORY = NMS.getGetter(ContainerAnvil.class, "resultInventory");
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
            ENDERMAN_ANGRY = (DataWatcherObject<Boolean>) NMS.getField(EntityEnderman.class, "bz").get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
