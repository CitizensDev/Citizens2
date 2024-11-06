package net.citizensnpcs.nms.v1_18_R2.util;

import java.lang.invoke.MethodHandle;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Collection;
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

import net.citizensnpcs.api.event.NPCMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftSound;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_18_R2.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftWither;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftPortalEvent;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryView;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
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
import com.google.common.collect.ImmutableMap;
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
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.gui.ForwardingInventory;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.EntityDim;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil.InventoryViewAPI;
import net.citizensnpcs.nms.v1_18_R2.entity.ArmorStandController;
import net.citizensnpcs.nms.v1_18_R2.entity.AxolotlController;
import net.citizensnpcs.nms.v1_18_R2.entity.BatController;
import net.citizensnpcs.nms.v1_18_R2.entity.BeeController;
import net.citizensnpcs.nms.v1_18_R2.entity.BlazeController;
import net.citizensnpcs.nms.v1_18_R2.entity.CatController;
import net.citizensnpcs.nms.v1_18_R2.entity.CaveSpiderController;
import net.citizensnpcs.nms.v1_18_R2.entity.ChickenController;
import net.citizensnpcs.nms.v1_18_R2.entity.CodController;
import net.citizensnpcs.nms.v1_18_R2.entity.CowController;
import net.citizensnpcs.nms.v1_18_R2.entity.CreeperController;
import net.citizensnpcs.nms.v1_18_R2.entity.DolphinController;
import net.citizensnpcs.nms.v1_18_R2.entity.DrownedController;
import net.citizensnpcs.nms.v1_18_R2.entity.EnderDragonController;
import net.citizensnpcs.nms.v1_18_R2.entity.EndermanController;
import net.citizensnpcs.nms.v1_18_R2.entity.EndermiteController;
import net.citizensnpcs.nms.v1_18_R2.entity.EvokerController;
import net.citizensnpcs.nms.v1_18_R2.entity.FoxController;
import net.citizensnpcs.nms.v1_18_R2.entity.GhastController;
import net.citizensnpcs.nms.v1_18_R2.entity.GiantController;
import net.citizensnpcs.nms.v1_18_R2.entity.GlowSquidController;
import net.citizensnpcs.nms.v1_18_R2.entity.GoatController;
import net.citizensnpcs.nms.v1_18_R2.entity.GuardianController;
import net.citizensnpcs.nms.v1_18_R2.entity.GuardianElderController;
import net.citizensnpcs.nms.v1_18_R2.entity.HoglinController;
import net.citizensnpcs.nms.v1_18_R2.entity.HorseController;
import net.citizensnpcs.nms.v1_18_R2.entity.HorseDonkeyController;
import net.citizensnpcs.nms.v1_18_R2.entity.HorseMuleController;
import net.citizensnpcs.nms.v1_18_R2.entity.HorseSkeletonController;
import net.citizensnpcs.nms.v1_18_R2.entity.HorseZombieController;
import net.citizensnpcs.nms.v1_18_R2.entity.HumanController;
import net.citizensnpcs.nms.v1_18_R2.entity.IllusionerController;
import net.citizensnpcs.nms.v1_18_R2.entity.IronGolemController;
import net.citizensnpcs.nms.v1_18_R2.entity.LlamaController;
import net.citizensnpcs.nms.v1_18_R2.entity.MagmaCubeController;
import net.citizensnpcs.nms.v1_18_R2.entity.MushroomCowController;
import net.citizensnpcs.nms.v1_18_R2.entity.OcelotController;
import net.citizensnpcs.nms.v1_18_R2.entity.PandaController;
import net.citizensnpcs.nms.v1_18_R2.entity.ParrotController;
import net.citizensnpcs.nms.v1_18_R2.entity.PhantomController;
import net.citizensnpcs.nms.v1_18_R2.entity.PigController;
import net.citizensnpcs.nms.v1_18_R2.entity.PigZombieController;
import net.citizensnpcs.nms.v1_18_R2.entity.PiglinBruteController;
import net.citizensnpcs.nms.v1_18_R2.entity.PiglinController;
import net.citizensnpcs.nms.v1_18_R2.entity.PillagerController;
import net.citizensnpcs.nms.v1_18_R2.entity.PolarBearController;
import net.citizensnpcs.nms.v1_18_R2.entity.PufferFishController;
import net.citizensnpcs.nms.v1_18_R2.entity.RabbitController;
import net.citizensnpcs.nms.v1_18_R2.entity.RavagerController;
import net.citizensnpcs.nms.v1_18_R2.entity.SalmonController;
import net.citizensnpcs.nms.v1_18_R2.entity.SheepController;
import net.citizensnpcs.nms.v1_18_R2.entity.ShulkerController;
import net.citizensnpcs.nms.v1_18_R2.entity.SilverfishController;
import net.citizensnpcs.nms.v1_18_R2.entity.SkeletonController;
import net.citizensnpcs.nms.v1_18_R2.entity.SkeletonStrayController;
import net.citizensnpcs.nms.v1_18_R2.entity.SkeletonWitherController;
import net.citizensnpcs.nms.v1_18_R2.entity.SlimeController;
import net.citizensnpcs.nms.v1_18_R2.entity.SnowmanController;
import net.citizensnpcs.nms.v1_18_R2.entity.SpiderController;
import net.citizensnpcs.nms.v1_18_R2.entity.SquidController;
import net.citizensnpcs.nms.v1_18_R2.entity.StriderController;
import net.citizensnpcs.nms.v1_18_R2.entity.TraderLlamaController;
import net.citizensnpcs.nms.v1_18_R2.entity.TropicalFishController;
import net.citizensnpcs.nms.v1_18_R2.entity.TurtleController;
import net.citizensnpcs.nms.v1_18_R2.entity.VexController;
import net.citizensnpcs.nms.v1_18_R2.entity.VillagerController;
import net.citizensnpcs.nms.v1_18_R2.entity.VindicatorController;
import net.citizensnpcs.nms.v1_18_R2.entity.WanderingTraderController;
import net.citizensnpcs.nms.v1_18_R2.entity.WitchController;
import net.citizensnpcs.nms.v1_18_R2.entity.WitherController;
import net.citizensnpcs.nms.v1_18_R2.entity.WolfController;
import net.citizensnpcs.nms.v1_18_R2.entity.ZoglinController;
import net.citizensnpcs.nms.v1_18_R2.entity.ZombieController;
import net.citizensnpcs.nms.v1_18_R2.entity.ZombieHuskController;
import net.citizensnpcs.nms.v1_18_R2.entity.ZombieVillagerController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.AreaEffectCloudController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.BoatController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.DragonFireballController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.EggController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.EnderPearlController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.EnderSignalController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.EvokerFangsController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.ExperienceOrbController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.FallingBlockController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.FireworkController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.FishingHookController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.GlowItemFrameController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.ItemController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.ItemFrameController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.LargeFireballController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.LeashController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.LlamaSpitController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MarkerController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MinecartChestController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MinecartSpawnerController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.PaintingController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.ShulkerBulletController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.SmallFireballController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.SnowballController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.SpectralArrowController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.ThrownPotionController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.ThrownTridentController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.TippedArrowController;
import net.citizensnpcs.nms.v1_18_R2.entity.nonliving.WitherSkullController;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.EntityPoseTrait.EntityPose;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.versioned.AreaEffectCloudTrait;
import net.citizensnpcs.trait.versioned.AxolotlTrait;
import net.citizensnpcs.trait.versioned.BeeTrait;
import net.citizensnpcs.trait.versioned.BoatTrait;
import net.citizensnpcs.trait.versioned.BossBarTrait;
import net.citizensnpcs.trait.versioned.CatTrait;
import net.citizensnpcs.trait.versioned.EnderDragonTrait;
import net.citizensnpcs.trait.versioned.FoxTrait;
import net.citizensnpcs.trait.versioned.LlamaTrait;
import net.citizensnpcs.trait.versioned.MushroomCowTrait;
import net.citizensnpcs.trait.versioned.PandaTrait;
import net.citizensnpcs.trait.versioned.ParrotTrait;
import net.citizensnpcs.trait.versioned.PhantomTrait;
import net.citizensnpcs.trait.versioned.PiglinTrait;
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
import net.citizensnpcs.util.NMS.MinecraftNavigationType;
import net.citizensnpcs.util.NMSBridge;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;

@SuppressWarnings("unchecked")
public class NMSImpl implements NMSBridge {
    public NMSImpl() {
        loadEntityTypes();
    }

    @Override
    public void activate(org.bukkit.entity.Entity entity) {
        getHandle(entity).activatedTick = MinecraftServer.currentTick;
    }

    @SuppressWarnings("resource")
    @Override
    public boolean addEntityToWorld(org.bukkit.entity.Entity entity, SpawnReason custom) {
        int viewDistance = -1;
        ChunkMap chunkMap = null;
        try {
            if (entity instanceof Player) {
                chunkMap = ((ServerChunkCache) getHandle(entity).level.getChunkSource()).chunkMap;
                viewDistance = (int) PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER.invoke(chunkMap);
                PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER.invoke(chunkMap, -1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        boolean success = getHandle(entity).level.addFreshEntity(getHandle(entity), custom);
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
        ServerPlayer handle = (ServerPlayer) getHandle(entity);
        if (handle.level == null)
            return;

        List<? extends net.minecraft.world.entity.player.Player> players = handle.level.players();
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
            CHUNKMAP_UPDATE_PLAYER_STATUS.invoke(((ServerLevel) handle.level).getChunkSource().chunkMap, handle,
                    !remove);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // PlayerUpdateTask.addOrRemove(entity, remove);
    }

    @Override
    public void attack(org.bukkit.entity.LivingEntity attacker, org.bukkit.entity.LivingEntity btarget) {
        LivingEntity source = getHandle(attacker);
        LivingEntity target = getHandle(btarget);
        if (source instanceof net.minecraft.world.entity.player.Player) {
            ((net.minecraft.world.entity.player.Player) source).attack(target);
            PlayerAnimation.ARM_SWING.play((Player) source.getBukkitEntity());
            return;
        }
        if (source instanceof Mob) {
            ((Mob) source).doHurtTarget(target);
            return;
        }
        float f = (float) (source.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)
                ? source.getAttributeValue(Attributes.ATTACK_DAMAGE)
                : 1f);
        int i = 0;
        f += EnchantmentHelper.getDamageBonus(source.getMainHandItem(), target.getMobType());
        i += EnchantmentHelper.getKnockbackBonus(source);
        boolean flag = target.hurt(DamageSource.mobAttack(source), f);
        if (!flag)
            return;
        if (i > 0) {
            target.knockback(-Math.sin(source.getYRot() * Math.PI / 180.0F) * i * 0.5F, 0.1D,
                    Math.cos(source.getYRot() * Math.PI / 180.0F) * i * 0.5F);
            source.setDeltaMovement(source.getDeltaMovement().multiply(0.6, 1, 0.6));
        }
        int fireAspectLevel = EnchantmentHelper.getFireAspect(source);
        if (fireAspectLevel > 0) {
            target.setSecondsOnFire(fireAspectLevel * 4, false);
        }
        if (target instanceof ServerPlayer) {
            ServerPlayer entityhuman = (ServerPlayer) target;
            ItemStack itemstack = source.getMainHandItem();
            ItemStack itemstack1 = entityhuman.isUsingItem() ? entityhuman.getUseItem() : ItemStack.EMPTY;
            if (!itemstack.isEmpty() && !itemstack1.isEmpty()
                    && itemstack.getItem() instanceof net.minecraft.world.item.AxeItem && itemstack1.is(Items.SHIELD)) {
                float f2 = 0.25F + EnchantmentHelper.getBlockEfficiency(source) * 0.05F;
                if (new Random().nextFloat() < f2) {
                    entityhuman.getCooldowns().addCooldown(Items.SHIELD, 100);
                    source.level.broadcastEntityEvent(entityhuman, (byte) 30);
                }
            }
        }
        EnchantmentHelper.doPostHurtEffects(source, target);
        EnchantmentHelper.doPostDamageEffects(target, source);
    }

    @Override
    public void cancelMoveDestination(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        MobAI ai = MobAI.from(handle);
        if (ai == null)
            return;
        if (ai.getMoveControl() instanceof EntityMoveControl) {
            ((EntityMoveControl) ai.getMoveControl()).moving = false;
        } else {
            try {
                MOVE_CONTROLLER_MOVING.invoke(ai.getMoveControl(), null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public EntityPacketTracker createPacketTracker(org.bukkit.entity.Entity entity, PacketAggregator agg) {
        Entity handle = getHandle(entity);
        Set<ServerPlayerConnection> linked = Sets.newIdentityHashSet();
        ServerEntity tracker = new ServerEntity((ServerLevel) handle.level, handle, handle.getType().updateInterval(),
                handle.getType().trackDeltas(), agg::send, linked);
        Map<EquipmentSlot, ItemStack> equipment = Maps.newEnumMap(EquipmentSlot.class);
        return new EntityPacketTracker() {
            @Override
            public void link(Player player) {
                ServerPlayer p = (ServerPlayer) getHandle(player);
                handle.unsetRemoved();
                tracker.addPairing(p);
                linked.add(p.connection);
                agg.add(p.getUUID(), packet -> p.connection.send((Packet<?>) packet));
            }

            @Override
            public void run() {
                if (handle instanceof LivingEntity) {
                    boolean changed = false;
                    LivingEntity entity = (LivingEntity) handle;
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack old = equipment.getOrDefault(slot, ItemStack.EMPTY);
                        ItemStack curr = entity.getItemBySlot(slot);
                        if (!changed && !ItemStack.matches(old, curr)) {
                            changed = true;
                        }
                        equipment.put(slot, curr);
                    }
                    if (changed) {
                        List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> vals = Lists.newArrayList();
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            vals.add(com.mojang.datafixers.util.Pair.of(slot, equipment.get(slot)));
                        }
                        agg.send(new ClientboundSetEquipmentPacket(handle.getId(), vals));
                    }
                }
                tracker.sendChanges();
            }

            @Override
            public void unlink(Player player) {
                ServerPlayer p = (ServerPlayer) getHandle(player);
                tracker.removePairing(p);
                linked.remove(p.connection);
                agg.removeConnection(p.getUUID());
            }

            @Override
            public void unlinkAll(Consumer<Player> callback) {
                handle.remove(RemovalReason.KILLED);
                for (ServerPlayerConnection link : Lists.newArrayList(linked)) {
                    Player entity = link.getPlayer().getBukkitEntity();
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
        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer().getSessionService();
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
        ServerBossEvent bserver = null;
        try {
            if (entity.getType() == EntityType.WITHER) {
                bserver = ((WitherBoss) NMSImpl.getHandle(entity)).bossEvent;
            } else if (entity.getType() == EntityType.ENDER_DRAGON) {
                EndDragonFight df = ((EnderDragon) NMSImpl.getHandle(entity)).getDragonFight();
                if (df != null) {
                    bserver = df.dragonEvent;
                }
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
    public BoundingBox getCollisionBox(Object data) {
        return NMSBoundingBox.wrap(((CraftBlockData) data).getState()
                .getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()).bounds());
    }

    @Override
    public BoundingBox getCollisionBox(org.bukkit.block.Block block) {
        ServerLevel world = ((CraftWorld) block.getWorld()).getHandle();
        VoxelShape shape = ((CraftBlock) block).getNMS().getCollisionShape(world, ((CraftBlock) block).getPosition());
        return shape.isEmpty() ? BoundingBox.EMPTY : NMSBoundingBox.wrap(shape.bounds());
    }

    @Override
    public Location getDestination(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        MobAI ai = MobAI.from(handle);
        if (ai == null)
            return null;
        MoveControl controller = ai.getMoveControl();
        if (controller.hasWanted())
            return new Location(entity.getWorld(), controller.getWantedX(), controller.getWantedY(),
                    controller.getWantedZ());
        if (ai.getNavigation().isDone())
            return null;
        Vec3 vec = ai.getNavigation().getPath().getNextEntityPos(handle);
        return new Location(entity.getWorld(), vec.x(), vec.y(), vec.z());
    }

    @Override
    public float getForwardBackwardMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        LivingEntity handle = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) entity);
        return handle.zza;
    }

    @Override
    public GameProfileRepository getGameProfileRepository() {
        return ((CraftServer) Bukkit.getServer()).getServer().getProfileRepository();
    }

    @Override
    public float getHeadYaw(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity))
            return entity.getLocation().getYaw();
        return getHandle((org.bukkit.entity.LivingEntity) entity).getYHeadRot();
    }

    @Override
    public EntityPacketTracker getPacketTracker(org.bukkit.entity.Entity entity) {
        ServerLevel server = (ServerLevel) getHandle(entity).level;
        TrackedEntity entry = server.getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
        if (entry == null)
            return null;
        return new EntityPacketTracker() {
            @Override
            public void link(Player player) {
                entry.updatePlayer((ServerPlayer) getHandle(player));
            }

            @Override
            public void run() {
            }

            @Override
            public void unlink(Player player) {
                entry.removePlayer((ServerPlayer) getHandle(player));
            }

            @Override
            public void unlinkAll(Consumer<Player> callback) {
                entry.broadcastRemoved();
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
        return ((net.minecraft.world.entity.player.Player) getHandle(player)).getGameProfile();
    }

    @Override
    public GameProfile getProfile(SkullMeta meta) {
        if (SKULL_META_PROFILE == null) {
            SKULL_META_PROFILE = NMS.getFirstGetter(meta.getClass(), GameProfile.class);
            if (SKULL_META_PROFILE == null)
                return null;
        }
        try {
            return (GameProfile) SKULL_META_PROFILE.invoke(meta);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    public String getSoundPath(Sound flag) throws CommandException {
        try {
            SoundEvent effect = CraftSound.getSoundEffect(flag);
            if (effect == null)
                throw new CommandException(Messages.INVALID_SOUND);
            return effect.getLocation().getPath();
        } catch (Throwable e) {
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
        if (!npc.isSpawned() || !(npc.getEntity() instanceof org.bukkit.entity.LivingEntity))
            return DEFAULT_SPEED;
        LivingEntity handle = getHandle((org.bukkit.entity.LivingEntity) npc.getEntity());
        if (handle == null) {
            return DEFAULT_SPEED;
        }
        return (float) handle.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public float getStepHeight(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).maxUpStep;
    }

    @Override
    public MCNavigator getTargetNavigator(org.bukkit.entity.Entity entity, Iterable<Vector> dest,
            final NavigatorParameters params) {
        List<Node> list = Lists.<Node> newArrayList(Iterables.<Vector, Node> transform(dest,
                input -> new Node(input.getBlockX(), input.getBlockY(), input.getBlockZ())));
        Node last = list.size() > 0 ? list.get(list.size() - 1) : null;
        final Path path = new Path(list, last != null ? new BlockPos(last.x, last.y, last.z) : null, true);
        return getTargetNavigator(entity, params, input -> input.moveTo(path, params.speed()));
    }

    @Override
    public MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final Location dest,
            final NavigatorParameters params) {
        return getTargetNavigator(entity, params,
                input -> input.moveTo(dest.getX(), dest.getY(), dest.getZ(), params.speed()));
    }

    private MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final NavigatorParameters params,
            final Function<PathNavigation, Boolean> function) {
        net.minecraft.world.entity.Entity raw = getHandle(entity);
        raw.setOnGround(true);
        // not sure of a better way around this - if onGround is false, then
        // navigation won't execute, and calling entity.move doesn't
        // entirely fix the problem.
        final PathNavigation navigation = NMSImpl.getNavigation(entity);
        final float oldWater = raw instanceof MobAI ? ((MobAI) raw).getPathfindingMalus(BlockPathTypes.WATER)
                : ((Mob) raw).getPathfindingMalus(BlockPathTypes.WATER);
        if (params.avoidWater() && oldWater >= 0) {
            if (raw instanceof MobAI) {
                ((MobAI) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater + 1F);
            } else {
                ((Mob) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater + 1F);
            }
        }
        navigation.getNodeEvaluator().setCanOpenDoors(params.hasExaminer(DoorExaminer.class));
        return new MCNavigator() {
            float lastSpeed;
            CancelReason reason;

            private List<org.bukkit.block.Block> getBlocks(final org.bukkit.entity.Entity entity, Path path) {
                List<org.bukkit.block.Block> blocks = Lists.newArrayList();
                for (int i = 0; i < path.getNodeCount(); i++) {
                    Node pp = path.getNode(i);
                    blocks.add(entity.getWorld().getBlockAt(pp.x, pp.y, pp.z));
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
                Path path = getPathEntity(navigation);
                if (params.debug() && path != null) {
                    List<org.bukkit.block.Block> blocks = getBlocks(entity, path);
                    Util.sendBlockChanges(blocks, null);
                }
                if (oldWater >= 0) {
                    if (raw instanceof MobAI) {
                        ((MobAI) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater);
                    } else {
                        ((Mob) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater);
                    }
                }
                navigation.stop();
            }

            @Override
            public boolean update() {
                if (params.speed() != lastSpeed) {
                    Entity handle = getHandle(entity);
                    EntityDimensions size = null;
                    try {
                        size = (EntityDimensions) SIZE_FIELD_GETTER.invoke(handle);
                        if (handle instanceof AbstractHorse) {
                            SIZE_FIELD_SETTER.invoke(handle,
                                    new EntityDimensions(Math.min(0.99F, size.width), size.height, false));
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
                if (params.debug() && !navigation.isDone()) {
                    Util.sendBlockChanges(getBlocks(entity, getPathEntity(navigation)), Material.DANDELION);
                }
                navigation.setSpeedModifier(params.speed());
                return navigation.isDone();
            }
        };
    }

    @Override
    public TargetNavigator getTargetNavigator(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity target,
            NavigatorParameters parameters) {
        PathNavigation navigation = getNavigation(entity);
        return navigation == null ? null : new MCTargetNavigator(entity, navigation, target, parameters);
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
    public Collection<org.bukkit.entity.Player> getViewingPlayers(org.bukkit.entity.Entity entity) {
        ServerLevel server = (ServerLevel) getHandle(entity).level;
        TrackedEntity entry = server.getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
        return PlayerlistTracker.getSeenBy(entry);
    }

    @Override
    public double getWidth(org.bukkit.entity.Entity entity) {
        return entity.getWidth();
    }

    @Override
    public float getXZMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        LivingEntity handle = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) entity);
        return handle.xxa;
    }

    @Override
    public float getYaw(org.bukkit.entity.Entity entity) {
        return getHandle(entity).getYRot();
    }

    @Override
    public boolean isOnGround(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).isOnGround();
    }

    @Override
    public boolean isSneaking(org.bukkit.entity.Entity entity) {
        if (entity instanceof Player)
            return ((Player) entity).isSneaking();
        return getHandle(entity).getPose() == Pose.CROUCHING;
    }

    @Override
    public boolean isSolid(org.bukkit.block.Block in) {
        BlockState data = ((CraftBlock) in).getNMS();
        return data.isSuffocating(((CraftWorld) in.getWorld()).getHandle(),
                new BlockPos(in.getX(), in.getY(), in.getZ()));
    }

    @Override
    public boolean isValid(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle.valid && handle.isAlive();
    }

    @Override
    public void load(CommandManager manager) {
        registerTraitWithCommand(manager, BoatTrait.class);
        registerTraitWithCommand(manager, AreaEffectCloudTrait.class);
        registerTraitWithCommand(manager, EnderDragonTrait.class);
        registerTraitWithCommand(manager, AxolotlTrait.class);
        registerTraitWithCommand(manager, BeeTrait.class);
        registerTraitWithCommand(manager, BossBarTrait.class);
        registerTraitWithCommand(manager, CatTrait.class);
        registerTraitWithCommand(manager, FoxTrait.class);
        registerTraitWithCommand(manager, LlamaTrait.class);
        registerTraitWithCommand(manager, MushroomCowTrait.class);
        registerTraitWithCommand(manager, ParrotTrait.class);
        registerTraitWithCommand(manager, PandaTrait.class);
        registerTraitWithCommand(manager, PiglinTrait.class);
        registerTraitWithCommand(manager, PhantomTrait.class);
        registerTraitWithCommand(manager, PolarBearTrait.class);
        registerTraitWithCommand(manager, PufferFishTrait.class);
        registerTraitWithCommand(manager, ShulkerTrait.class);
        registerTraitWithCommand(manager, SpellcasterTrait.class);
        registerTraitWithCommand(manager, SnowmanTrait.class);
        registerTraitWithCommand(manager, TropicalFishTrait.class);
        registerTraitWithCommand(manager, VillagerTrait.class);
    }

    private void loadEntityTypes() {
        EntityControllers.setEntityControllerForType(EntityType.AREA_EFFECT_CLOUD, AreaEffectCloudController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARROW, TippedArrowController.class);
        EntityControllers.setEntityControllerForType(EntityType.ARMOR_STAND, ArmorStandController.class);
        EntityControllers.setEntityControllerForType(EntityType.AXOLOTL, AxolotlController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.GOAT, GoatController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.GLOW_ITEM_FRAME, GlowItemFrameController.class);
        EntityControllers.setEntityControllerForType(EntityType.LEASH_HITCH, LeashController.class);
        EntityControllers.setEntityControllerForType(EntityType.LLAMA, LlamaController.class);
        EntityControllers.setEntityControllerForType(EntityType.TRADER_LLAMA, TraderLlamaController.class);
        EntityControllers.setEntityControllerForType(EntityType.WANDERING_TRADER, WanderingTraderController.class);
        EntityControllers.setEntityControllerForType(EntityType.LLAMA_SPIT, LlamaSpitController.class);
        EntityControllers.setEntityControllerForType(EntityType.MARKER, MarkerController.class);
        EntityControllers.setEntityControllerForType(EntityType.MAGMA_CUBE, MagmaCubeController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART, MinecartRideableController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_CHEST, MinecartChestController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_COMMAND, MinecartCommandController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_FURNACE, MinecartFurnaceController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_HOPPER, MinecartHopperController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_TNT, MinecartTNTController.class);
        EntityControllers.setEntityControllerForType(EntityType.MUSHROOM_COW, MushroomCowController.class);
        EntityControllers.setEntityControllerForType(EntityType.MINECART_MOB_SPAWNER, MinecartSpawnerController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.EXPERIENCE_ORB, ExperienceOrbController.class);
        EntityControllers.setEntityControllerForType(EntityType.GLOW_SQUID, GlowSquidController.class);
        EntityControllers.setEntityControllerForType(EntityType.SPECTRAL_ARROW, SpectralArrowController.class);
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
        handle.setYRot(yaw);
        setHeadYaw(entity, yaw);
        handle.setXRot(pitch);
    }

    @Override
    public void look(org.bukkit.entity.Entity entity, Location to, boolean headOnly, boolean immediate) {
        Entity handle = NMSImpl.getHandle(entity);
        if (immediate || headOnly || BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())
                || !(handle instanceof Mob) && !(handle instanceof MobAI)) {
            Location fromLocation = entity.getLocation(FROM_LOCATION);
            double xDiff, yDiff, zDiff;
            xDiff = to.getX() - fromLocation.getX();
            yDiff = to.getY() - fromLocation.getY();
            zDiff = to.getZ() - fromLocation.getZ();
            double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);
            double yaw = distanceXZ == 0 ? 0 : Math.toDegrees(Math.acos(xDiff / distanceXZ));
            double pitch = distanceY == 0 ? 0
                    : Math.toDegrees(Math.acos(yDiff / distanceY))
                            - (handle.getBukkitEntity().getType() == EntityType.PHANTOM ? 45 : 90);
            if (zDiff < 0.0) {
                yaw += Math.abs(180 - yaw) * 2;
            }
            if (handle.getBukkitEntity().getType() == EntityType.ENDER_DRAGON) {
                yaw = Util.getYawFromVelocity(handle.getBukkitEntity(), xDiff, zDiff);
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
        if (handle instanceof Mob) {
            ((Mob) handle).getLookControl().setLookAt(to.getX(), to.getY(), to.getZ(), ((Mob) handle).getHeadRotSpeed(),
                    ((Mob) handle).getMaxHeadXRot());
            while (((LivingEntity) handle).yHeadRot >= 180F) {
                ((LivingEntity) handle).yHeadRot -= 360F;
            }
            while (((LivingEntity) handle).yHeadRot < -180F) {
                ((LivingEntity) handle).yHeadRot += 360F;
            }
        } else if (handle instanceof NPCHolder) {
            ((NPCHolder) handle).getNPC().getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToFace(to);
        }
    }

    @Override
    public void look(org.bukkit.entity.Entity from, org.bukkit.entity.Entity to) {
        Entity handle = NMSImpl.getHandle(from), target = NMSImpl.getHandle(to);
        if (BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())
                || !(handle instanceof Mob) && !(handle instanceof MobAI)) {
            if (to instanceof org.bukkit.entity.LivingEntity) {
                look(from, ((org.bukkit.entity.LivingEntity) to).getEyeLocation(), false, true);
            } else {
                look(from, to.getLocation(), false, true);
            }
        } else if (handle instanceof Mob) {
            ((Mob) handle).getLookControl().setLookAt(target, ((Mob) handle).getHeadRotSpeed(),
                    ((Mob) handle).getMaxHeadXRot());
            while (((LivingEntity) handle).yHeadRot >= 180F) {
                ((LivingEntity) handle).yHeadRot -= 360F;
            }
            while (((LivingEntity) handle).yHeadRot < -180F) {
                ((LivingEntity) handle).yHeadRot += 360F;
            }
        } else if (handle instanceof NPCHolder) {
            ((NPCHolder) handle).getNPC().getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToFace(to);
        }
    }

    @Override
    public void markPoseDirty(org.bukkit.entity.Entity entity) {
        getHandle(entity).getEntityData().markDirty(DATA_POSE);
    }

    @Override
    public void mount(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity passenger) {
        if (NMSImpl.getHandle(passenger) == null)
            return;
        NMSImpl.getHandle(passenger).startRiding(NMSImpl.getHandle(entity), true);
    }

    @Override
    public InventoryView openAnvilInventory(Player player, Inventory anvil, String title) {
        ServerPlayer handle = (ServerPlayer) getHandle(player);
        final AnvilMenu container = new AnvilMenu(handle.nextContainerCounter(), handle.getInventory(),
                ContainerLevelAccess.create(handle.level, new BlockPos(0, 0, 0))) {
            private CraftInventoryView bukkitEntity;

            @Override
            protected void clearContainer(net.minecraft.world.entity.player.Player entityhuman, Container iinventory) {
            }

            @Override
            public void createResult() {
                super.createResult();
                cost.set(0);
            }

            @Override
            public CraftInventoryView getBukkitView() {
                if (this.bukkitEntity == null) {
                    this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(),
                            new CitizensInventoryAnvil(this.access.getLocation(), this.inputSlots, this.resultSlots,
                                    this, anvil),
                            this);
                }
                return this.bukkitEntity;
            }
        };
        container.getBukkitView().setItem(0, anvil.getItem(0));
        container.getBukkitView().setItem(1, anvil.getItem(1));
        container.checkReachable = false;
        handle.connection.send(
                new ClientboundOpenScreenPacket(container.containerId, container.getType(), new TextComponent(title)));
        handle.containerMenu = container;
        handle.initMenu(container);
        return container.getBukkitView();
    }

    @Override
    public void openHorseInventory(Tameable horse, Player equipper) {
        LivingEntity handle = NMSImpl.getHandle(horse);
        ServerPlayer equipperHandle = (ServerPlayer) NMSImpl.getHandle(equipper);
        if (handle == null || equipperHandle == null)
            return;
        boolean wasTamed = horse.isTamed();
        horse.setTamed(true);
        ((AbstractHorse) handle).openInventory(equipperHandle);
        horse.setTamed(wasTamed);
    }

    @Override
    public void playAnimation(PlayerAnimation animation, Player player, Iterable<Player> to) {
        PlayerAnimationImpl.play(animation, player, to);
    }

    @Override
    public Runnable playerTicker(NPC npc, Player entity) {
        ServerPlayer player = (ServerPlayer) getHandle(entity);
        return () -> {
            if (!entity.isValid())
                return;
            player.doTick();
        };
    }

    @Override
    public void registerEntityClass(Class<?> clazz, Object raw) {
        if (ENTITY_REGISTRY == null)
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            net.minecraft.world.entity.EntityType<?> type = ENTITY_REGISTRY.findType(search);
            ResourceLocation key = ENTITY_REGISTRY.getKey(type);
            if (key == null || type == null) {
                continue;
            }
            CITIZENS_ENTITY_TYPES.put(clazz, type);
            int code = ENTITY_REGISTRY.getId(type);
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
        NMSImpl.getHandle(entity).remove(RemovalReason.KILLED);
    }

    @Override
    public void removeFromServerPlayerList(Player player) {
        ServerPlayer handle = (ServerPlayer) NMSImpl.getHandle(player);
        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(handle);
    }

    @Override
    public void removeFromWorld(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity);
        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        ((ServerLevel) nmsEntity.level).getChunkSource().removeEntity(nmsEntity);
    }

    @Override
    public void removeHookIfNecessary(FishHook entity) {
        FishingHook hook = (FishingHook) NMSImpl.getHandle(entity);
        Entity hooked = hook.getHookedIn();
        if (hooked == null)
            return;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(hooked.getBukkitEntity());
        if (npc != null && npc.isProtected()) {
            hook.hookedIn = null;
            hook.getBukkitEntity().remove();
        }
    }

    @Override
    public void replaceTrackerEntry(org.bukkit.entity.Entity entity) {
        ServerLevel server = (ServerLevel) NMSImpl.getHandle(entity).level;
        TrackedEntity entry = server.getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
        if (entry == null)
            return;
        entry.broadcastRemoved();
        PlayerlistTracker replace = new PlayerlistTracker(server.getChunkSource().chunkMap, entry);
        server.getChunkSource().chunkMap.entityMap.put(entity.getEntityId(), replace);
    }

    @Override
    public void sendPositionUpdate(org.bukkit.entity.Entity from, Collection<Player> to, boolean position,
            Float bodyYaw, Float pitch, Float headYaw) {
        Entity handle = getHandle(from);
        if (bodyYaw == null) {
            bodyYaw = handle.getYRot();
        }
        if (pitch == null) {
            pitch = handle.getXRot();
        }
        List<Packet<?>> toSend = Lists.newArrayList();
        if (position) {
            TrackedEntity entry = ((ServerLevel) handle.level).getChunkSource().chunkMap.entityMap.get(handle.getId());
            ServerEntity se = null;
            try {
                se = (ServerEntity) SERVER_ENTITY_GETTER.invoke(entry);
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }
            Vec3 pos = handle.position().subtract(se.sentPos());
            toSend.add(
                    new ClientboundMoveEntityPacket.PosRot(handle.getId(), (short) pos.x, (short) pos.y, (short) pos.z,
                            (byte) (bodyYaw * 256.0F / 360.0F), (byte) (pitch * 256.0F / 360.0F), handle.onGround));
        } else {
            toSend.add(new ClientboundMoveEntityPacket.Rot(handle.getId(), (byte) (bodyYaw * 256.0F / 360.0F),
                    (byte) (pitch * 256.0F / 360.0F), handle.onGround));
        }
        if (headYaw != null) {
            toSend.add(new ClientboundRotateHeadPacket(handle, (byte) (headYaw * 256.0F / 360.0F)));
        }
        for (Player player : to) {
            sendPackets(player, toSend);
        }
    }

    @Override
    public boolean sendTabListAdd(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);
        ServerPlayer entity = ((CraftPlayer) listPlayer).getHandle();
        NMSImpl.sendPacket(recipient,
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, entity));
        return true;
    }

    @Override
    public void sendTabListRemove(Player recipient, Collection<Player> skinnableNPCs) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(skinnableNPCs);
        ServerPlayer[] entities = new ServerPlayer[skinnableNPCs.size()];
        int i = 0;
        for (Player skinnable : skinnableNPCs) {
            entities[i] = (ServerPlayer) getHandle(skinnable);
            i++;
        }
        NMSImpl.sendPacket(recipient,
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, entities));
    }

    @Override
    public void sendTeamPacket(Player recipient, Team team, int mode) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(team);
        if (TEAM_FIELD == null) {
            TEAM_FIELD = NMS.getGetter(team.getClass(), "team");
        }
        try {
            PlayerTeam nmsTeam = (PlayerTeam) TEAM_FIELD.invoke(team);
            if (mode == 1) {
                sendPacket(recipient, ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam));
            } else {
                sendPacket(recipient,
                        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, mode == 0 ? true : false));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAggressive(org.bukkit.entity.Entity entity, boolean aggro) {
        Entity handle = getHandle(entity);
        if (!(handle instanceof Mob))
            return;
        ((Mob) handle).setAggressive(aggro);
    }

    @Override

    public void setBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        Entity handle = getHandle(entity);
        if (handle instanceof LivingEntity) {
            ((LivingEntity) handle).yBodyRotO = yaw;
        }
        handle.setYBodyRot(yaw);
        handle.setYRot(yaw);
    }

    @Override
    public void setBoundingBox(org.bukkit.entity.Entity entity, BoundingBox box) {
        NMSImpl.getHandle(entity).setBoundingBox(new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ));
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, Object component, String string) {
        getHandle(entity).setCustomName((Component) component);
    }

    @Override
    public void setDestination(org.bukkit.entity.Entity entity, double x, double y, double z, float speed) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        if (handle instanceof Mob) {
            ((Mob) handle).getMoveControl().setWantedPosition(x, y, z, speed);
        } else if (handle instanceof MobAI) {
            ((MobAI) handle).getMoveControl().setWantedPosition(x, y, z, speed);
        }
    }

    @Override
    public void setDimensions(org.bukkit.entity.Entity entity, EntityDim desired) {
        setSize(getHandle(entity), new EntityDimensions(desired.width, desired.height, true));
    }

    @Override
    public void setEndermanAngry(org.bukkit.entity.Enderman enderman, boolean angry) {
        if (ENDERMAN_CREEPY == null)
            return;
        getHandle(enderman).getEntityData().set(ENDERMAN_CREEPY, angry);
    }

    @Override
    public void setHeadAndBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        yaw = Util.clamp(yaw);
        setBodyYaw(entity, yaw);
        setHeadYaw(entity, yaw);
    }

    @Override
    public void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity))
            return;
        ((LivingEntity) getHandle(entity)).setYHeadRot(Util.clamp(yaw));
    }

    @Override
    public void setKnockbackResistance(org.bukkit.entity.LivingEntity entity, double d) {
        LivingEntity handle = NMSImpl.getHandle(entity);
        handle.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(d);
    }

    @Override
    public void setLocationDirectly(org.bukkit.entity.Entity entity, Location location) {
        getHandle(entity).moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
    }

    @Override
    public void setLyingDown(org.bukkit.entity.Entity cat, boolean lying) {
        ((Cat) getHandle(cat)).setLying(lying);
    }

    @Override
    public void setNavigationTarget(org.bukkit.entity.Entity handle, org.bukkit.entity.Entity target, float speed) {
        NMSImpl.getNavigation(handle).moveTo(NMSImpl.getHandle(target), speed);
    }

    @Override
    public void setNavigationType(org.bukkit.entity.Entity entity, MinecraftNavigationType type) {
        Entity handle = getHandle(entity);
        if (!(handle instanceof Mob))
            return;
        Mob ei = (Mob) handle;
        switch (type) {
            case GROUND:
                try {
                    ENTITY_NAVIGATION.invoke(ei, new GroundPathNavigation(ei, ei.level));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case WALL_CLIMB:
                try {
                    ENTITY_NAVIGATION.invoke(ei, new WallClimberNavigation(ei, ei.level));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void setNoGravity(org.bukkit.entity.Entity entity, boolean nogravity) {
        Entity handle = getHandle(entity);
        handle.setNoGravity(nogravity);
        if (!(handle instanceof Mob) || !(entity instanceof NPCHolder))
            return;
        Mob mob = (Mob) handle;
        NPC npc = ((NPCHolder) entity).getNPC();
        if (!(mob.getMoveControl() instanceof FlyingMoveControl) || npc.data().has("flying-nogravity-float"))
            return;
        try {
            if (nogravity) {
                boolean old = (boolean) FLYING_MOVECONTROL_FLOAT_GETTER.invoke(mob.getMoveControl());
                FLYING_MOVECONTROL_FLOAT_SETTER.invoke(mob.getMoveControl(), true);
                npc.data().set("flying-nogravity-float", old);
            } else if (npc.data().has("flying-nogravity-float")) {
                FLYING_MOVECONTROL_FLOAT_SETTER.invoke(mob.getMoveControl(), npc.data().get("flying-nogravity-float"));
                npc.data().remove("flying-nogravity-float");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void setOpWithoutSaving(Player player, boolean op) {
        if (player.isOp() == op)
            return;
        final ServerPlayer playerHandle = ((CraftPlayer) player).getHandle();
        final GameProfile profile = ((CraftPlayer) player).getProfile();
        final DedicatedPlayerList playerList = ((CraftServer) player.getServer()).getHandle();
        final DedicatedServer server = playerList.getServer();
        final ServerOpList opList = playerList.getOps();
        if (op) {
            opList.add(new ServerOpListEntry(profile, server.getOperatorUserPermissionLevel(),
                    opList.canBypassPlayerLimit(profile)));
        } else {
            opList.remove(profile);
        }
        playerList.sendPlayerPermissionLevel(playerHandle);
    }

    @Override
    public void setPandaSitting(org.bukkit.entity.Entity entity, boolean sitting) {
        ((Panda) getHandle(entity)).sit(sitting);
    }

    @Override
    public void setPeekShulker(org.bukkit.entity.Entity shulker, int peek) {
        ((Shulker) getHandle(shulker)).setRawPeekAmount(peek);
    }

    @Override
    public void setPiglinDancing(org.bukkit.entity.Entity entity, boolean dancing) {
        if (!(getHandle(entity) instanceof Piglin))
            return;
        ((Piglin) getHandle(entity)).setDancing(dancing);
    }

    @Override
    public void setPitch(org.bukkit.entity.Entity entity, float pitch) {
        getHandle(entity).setXRot(pitch);
    }

    @Override
    public void setPolarBearRearing(org.bukkit.entity.Entity entity, boolean rearing) {
        ((PolarBear) getHandle(entity)).setStanding(rearing);
    }

    @Override
    public void setPose(org.bukkit.entity.Entity entity, EntityPose pose) {
        getHandle(entity).setPose(Pose.valueOf(pose.name()));
    }

    @Override
    public void setProfile(SkullMeta meta, GameProfile profile) {
        if (SET_PROFILE_METHOD == null) {
            SET_PROFILE_METHOD = NMS.getMethodHandle(meta.getClass(), "setProfile", true, GameProfile.class);
            if (SET_PROFILE_METHOD == null)
                return;
        }
        try {
            SET_PROFILE_METHOD.invoke(meta, profile);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void setShouldJump(org.bukkit.entity.Entity entity) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        MobAI ai = MobAI.from(handle);
        if (ai != null) {
            ai.getJumpControl().jump();
        }
        if (handle instanceof LivingEntity) {
            ((LivingEntity) handle).setJumping(true);
        }
    }

    @Override
    public void setSitting(org.bukkit.entity.Ocelot ocelot, boolean sitting) {
        setSneaking(ocelot, sitting);
    }

    @Override
    public void setSitting(Tameable tameable, boolean sitting) {
        ((TamableAnimal) NMSImpl.getHandle(tameable)).setInSittingPose(sitting);
    }

    @Override
    public void setSneaking(org.bukkit.entity.Entity entity, boolean sneaking) {
        if (entity instanceof Player) {
            ((Player) entity).setSneaking(sneaking);
        }
        Pose pose = sneaking ? Pose.CROUCHING : Pose.STANDING;
        getHandle(entity).setPose(pose);
    }

    @Override
    public void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        NMSImpl.getHandle(entity).maxUpStep = height;
    }

    @Override
    public void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        if (!bukkitEntity.getType().isAlive())
            return;
        LivingEntity handle = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) bukkitEntity);
        handle.xxa = (float) d;
    }

    @Override
    public void setWitherInvulnerableTicks(Wither wither, int ticks) {
        WitherBoss handle = ((CraftWither) wither).getHandle();
        handle.setInvulnerableTicks(ticks);
    }

    @Override
    public boolean shouldJump(org.bukkit.entity.Entity entity) {
        if (JUMP_FIELD == null || !(entity instanceof org.bukkit.entity.LivingEntity))
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
        try {
            ENTITY_REGISTRY_SETTER.invoke(null, ENTITY_REGISTRY.get());
        } catch (Throwable e) {
        }
    }

    @Override
    public void sleep(org.bukkit.entity.Player entity, boolean sleep) {
        getHandle(entity).setPose(sleep ? Pose.SLEEPING : Pose.STANDING);
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
        if (RANDOM.nextFloat() <= 0.85F && handle.isInWater()) {
            handle.setDeltaMovement(handle.getDeltaMovement().x, handle.getDeltaMovement().y + power,
                    handle.getDeltaMovement().z);
        }
    }

    @Override
    public void updateInventoryTitle(Player player, InventoryViewAPI view, String newTitle) {
        ServerPlayer handle = (ServerPlayer) getHandle(player);
        MenuType<?> menuType = null;
        switch (view.getTopInventory().getType()) {
            case ANVIL:
                menuType = MenuType.ANVIL;
                break;
            case BARREL:
                menuType = MenuType.GENERIC_9x3;
                break;
            case BEACON:
                menuType = MenuType.BEACON;
                break;
            case BLAST_FURNACE:
                menuType = MenuType.BLAST_FURNACE;
                break;
            case BREWING:
                menuType = MenuType.BREWING_STAND;
                break;
            case CARTOGRAPHY:
                menuType = MenuType.CARTOGRAPHY_TABLE;
                break;
            case CHEST:
                int sz = view.getTopInventory().getSize();
                if (sz > 45) {
                    menuType = MenuType.GENERIC_9x6;
                } else if (sz > 36) {
                    menuType = MenuType.GENERIC_9x5;
                } else if (sz > 27) {
                    menuType = MenuType.GENERIC_9x4;
                } else if (sz > 18) {
                    menuType = MenuType.GENERIC_9x3;
                } else if (sz > 9) {
                    menuType = MenuType.GENERIC_9x2;
                } else {
                    menuType = MenuType.GENERIC_9x1;
                }
                break;
            case COMPOSTER:
                break;
            case PLAYER:
            case CRAFTING:
            case CREATIVE:
                return;
            case DISPENSER:
            case DROPPER:
                menuType = MenuType.GENERIC_3x3;
                break;
            case ENCHANTING:
                menuType = MenuType.ENCHANTMENT;
                break;
            case ENDER_CHEST:
                menuType = MenuType.GENERIC_9x3;
                break;
            case FURNACE:
                menuType = MenuType.FURNACE;
                break;
            case GRINDSTONE:
                menuType = MenuType.GRINDSTONE;
                break;
            case HOPPER:
                menuType = MenuType.HOPPER;
                break;
            case LECTERN:
                menuType = MenuType.LECTERN;
                break;
            case LOOM:
                menuType = MenuType.LOOM;
                break;
            case MERCHANT:
                menuType = MenuType.MERCHANT;
                break;
            case SHULKER_BOX:
                menuType = MenuType.SHULKER_BOX;
                break;
            case SMITHING:
                menuType = MenuType.SMITHING;
                break;
            case SMOKER:
                menuType = MenuType.SMOKER;
                break;
            case STONECUTTER:
                menuType = MenuType.STONECUTTER;
                break;
            case WORKBENCH:
                menuType = MenuType.CRAFTING;
                break;
        }
        handle.connection.send(new ClientboundOpenScreenPacket(handle.containerMenu.containerId, menuType,
                new TextComponent(newTitle)));
        player.updateInventory();
    }

    @Override
    public void updateNavigationWorld(org.bukkit.entity.Entity entity, World world) {
        if (NAVIGATION_WORLD_FIELD == null)
            return;
        Entity en = NMSImpl.getHandle(entity);
        if (en == null || !(en instanceof Mob))
            return;
        Mob handle = (Mob) en;
        ServerLevel worldHandle = ((CraftWorld) world).getHandle();
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
        LivingEntity en = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) npc.getEntity());
        if (en instanceof MobAI) {
            ((MobAI) en).updatePathfindingRange(pathfindingRange);
            return;
        }
        if (NAVIGATION_PATHFINDER == null)
            return;
        PathNavigation navigation = ((Mob) en).getNavigation();
        AttributeInstance inst = en.getAttribute(Attributes.FOLLOW_RANGE);
        inst.setBaseValue(pathfindingRange);
        int mc = Mth.floor(en.getAttributeBaseValue(Attributes.FOLLOW_RANGE) * 16.0D);
        try {
            NAVIGATION_PATHFINDER.invoke(navigation, NAVIGATION_CREATE_PATHFINDER.invoke(navigation, mc));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static class CitizensInventoryAnvil extends CraftInventoryAnvil implements ForwardingInventory {
        private final Inventory wrapped;

        public CitizensInventoryAnvil(Location location, Container inventory, Container resultInventory,
                AnvilMenu container, Inventory wrapped) {
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

    private static class MCTargetNavigator implements TargetNavigator {
        private final org.bukkit.entity.Entity entity;
        private final PathNavigation navigation;
        private final NavigatorParameters parameters;
        private final org.bukkit.entity.Entity target;

        private MCTargetNavigator(org.bukkit.entity.Entity entity, PathNavigation navigation,
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
            navigation.moveTo(location.getX(), location.getY(), location.getZ(), parameters.speed());
        }

        @Override
        public void stop() {
            navigation.stop();
        }

        @Override
        public void update() {
            navigation.tick();
        }
    }

    private static class NavigationIterable implements Iterable<Vector> {
        private final PathNavigation navigation;

        public NavigationIterable(PathNavigation nav) {
            this.navigation = nav;
        }

        @Override
        public Iterator<Vector> iterator() {
            Path path = getPathEntity(navigation);
            final int npoints = path == null ? 0 : path.getNodeCount();
            return new Iterator<Vector>() {
                Node curr = npoints > 0 ? path.getNode(0) : null;
                int i = 0;

                @Override
                public boolean hasNext() {
                    return curr != null;
                }

                @Override
                public Vector next() {
                    Node old = curr;
                    curr = i + 1 < npoints ? path.getNode(++i) : null;
                    return new Vector(old.x, old.y, old.z);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public static void checkAndUpdateHeight(LivingEntity living, EntityDataAccessor<?> datawatcherobject,
            Consumer<EntityDataAccessor<?>> cb) {
        EntityDimensions size;
        try {
            size = (EntityDimensions) SIZE_FIELD_GETTER.invoke(living);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        float oldw = size.width;
        float oldl = size.height;
        cb.accept(datawatcherobject);
        if (oldw != size.width || size.height != oldl) {
            living.setPos(living.getX() - 0.01, living.getY(), living.getZ() - 0.01);
            living.setPos(living.getX() + 0.01, living.getY(), living.getZ() + 0.01);
        }
    }

    public static void clearGoals(NPC npc, GoalSelector... goalSelectors) {
        if (goalSelectors == null)
            return;
        int i = 0;
        for (GoalSelector selector : goalSelectors) {
            try {
                Collection<?> list = selector.getAvailableGoals();
                if (!list.isEmpty()) {
                    npc.data().set("selector" + i, Lists.newArrayList(list));
                }
                list.clear();
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            } catch (Throwable e) {
                Messaging.logTr(Messages.ERROR_CLEARING_GOALS, e.getLocalizedMessage());
            }
            i++;
        }
    }

    public static void flyingMoveLogic(LivingEntity entity, Vec3 vec3d) {
        if (entity.isEffectiveAi() || entity.isControlledByLocalInstance()) {
            double d0 = 0.08D;
            boolean flag = entity.getDeltaMovement().y <= 0.0D;
            if (flag && entity.hasEffect(MobEffects.SLOW_FALLING)) {
                d0 = 0.01D;
                entity.fallDistance = 0.0F;
            }
            FluidState fluid = entity.level.getFluidState(entity.blockPosition());
            if (entity.isInWater() && !entity.canStandOnFluid(fluid)) {
                double d1 = entity.getY();
                float f = entity.isSprinting() ? 0.9F : 0.8F;
                float f1 = 0.02F;
                float f2 = EnchantmentHelper.getDepthStrider(entity);
                if (f2 > 3.0F) {
                    f2 = 3.0F;
                }
                if (!entity.isOnGround()) {
                    f2 *= 0.5F;
                }
                if (f2 > 0.0F) {
                    f += (0.546F - f) * f2 / 3.0F;
                    f1 += (entity.getSpeed() - f1) * f2 / 3.0F;
                }
                if (entity.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    f = 0.96F;
                }
                entity.moveRelative(f1, vec3d);
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                Vec3 vec3d1 = entity.getDeltaMovement();
                if (entity.horizontalCollision && entity.onClimbable()) {
                    vec3d1 = new Vec3(vec3d1.x, 0.2D, vec3d1.z);
                }
                entity.setDeltaMovement(vec3d1.multiply(f, 0.8D, f));
                Vec3 vec3d2 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
                entity.setDeltaMovement(vec3d2);
                if (entity.horizontalCollision
                        && entity.isFree(vec3d2.x, vec3d2.y + 0.6D - entity.getY() + d1, vec3d2.z)) {
                    entity.setDeltaMovement(vec3d2.x, 0.3D, vec3d2.z);
                }
            } else if (entity.isInLava() && !entity.canStandOnFluid(fluid)) {
                double d1 = entity.getY();
                entity.moveRelative(0.02F, vec3d);
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                if (entity.getFluidHeight(FluidTags.LAVA) <= entity.getFluidJumpThreshold()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, 0.8D, 0.5D));
                    Vec3 vec3 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
                    entity.setDeltaMovement(vec3);
                } else {
                    entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
                }
                if (!entity.isNoGravity()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
                }
                Vec3 vec3d3 = entity.getDeltaMovement();
                if (entity.horizontalCollision
                        && entity.isFree(vec3d3.x, vec3d3.y + 0.6D - entity.getY() + d1, vec3d3.z)) {
                    entity.setDeltaMovement(vec3d3.x, 0.3D, vec3d3.z);
                }
            } else if (entity.isFallFlying()) {
                Vec3 vec3d4 = entity.getDeltaMovement();
                if (vec3d4.y > -0.5D) {
                    entity.fallDistance = 1.0F;
                }
                Vec3 vec3d5 = entity.getLookAngle();
                float f = entity.getXRot() * 0.017453292F;
                double d2 = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
                double d3 = vec3d4.horizontalDistance();
                double d4 = vec3d5.length();
                float f3 = Mth.cos(f);
                f3 = (float) (f3 * f3 * Math.min(1.0D, d4 / 0.4D));
                vec3d4 = entity.getDeltaMovement().add(0.0D, d0 * (-1.0D + f3 * 0.75D), 0.0D);
                if (vec3d4.y < 0.0D && d2 > 0.0D) {
                    double d5 = vec3d4.y * -0.1D * f3;
                    vec3d4 = vec3d4.add(vec3d5.x * d5 / d2, d5, vec3d5.z * d5 / d2);
                }
                if (f < 0.0F && d2 > 0.0D) {
                    double d5 = d3 * -Mth.sin(f) * 0.04D;
                    vec3d4 = vec3d4.add(-vec3d5.x * d5 / d2, d5 * 3.2D, -vec3d5.z * d5 / d2);
                }
                if (d2 > 0.0D) {
                    vec3d4 = vec3d4.add((vec3d5.x / d2 * d3 - vec3d4.x) * 0.1D, 0.0D,
                            (vec3d5.z / d2 * d3 - vec3d4.z) * 0.1D);
                }
                entity.setDeltaMovement(vec3d4.multiply(0.99D, 0.98D, 0.99D));
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                if (entity.horizontalCollision && !entity.level.isClientSide) {
                    double d5 = entity.getDeltaMovement().horizontalDistance();
                    double d6 = d3 - d5;
                    float f4 = (float) (d6 * 10.0D - 3.0D);
                    if (f4 > 0.0F) {
                        try {
                            entity.playSound((SoundEvent) ENTITY_GET_SOUND_FALL.invoke(entity, (int) f4), 1.0F, 1.0F);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        entity.hurt(DamageSource.FLY_INTO_WALL, f4);
                    }
                }
                if (entity.isOnGround() && !entity.level.isClientSide && entity.getSharedFlag(7)
                        && !CraftEventFactory.callToggleGlideEvent(entity, false).isCancelled()) {
                    entity.setSharedFlag(7, false);
                }
            } else {
                BlockPos blockposition = new BlockPos(entity.getX(), entity.getBoundingBox().minY - 0.5D,
                        entity.getZ());
                float f5 = entity.level.getBlockState(blockposition).getBlock().getFriction();
                float f = entity.isOnGround() ? f5 * 0.91F : 0.91F;
                Vec3 vec3d6 = entity.handleRelativeFrictionAndCalculateMovement(vec3d, f5);
                double d7 = vec3d6.y;
                if (entity.hasEffect(MobEffects.LEVITATION)) {
                    d7 += (0.05D * (entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec3d6.y) * 0.2D;
                    entity.fallDistance = 0.0F;
                } else if (entity.level.isClientSide && !entity.level.hasChunkAt(blockposition)) {
                    if (entity.getY() > entity.level.getMinBuildHeight()) {
                        d7 = -0.1D;
                    } else {
                        d7 = 0.0D;
                    }
                } else if (!entity.isNoGravity()) {
                    d7 -= d0;
                }
                if (entity.shouldDiscardFriction()) {
                    entity.setDeltaMovement(vec3d6.x, d7, vec3d6.z);
                } else {
                    entity.setDeltaMovement(vec3d6.x * f, d7 * 0.98D, vec3d6.z * f);
                }
            }
        }
        entity.calculateEntityAnimation(entity, entity instanceof net.minecraft.world.entity.animal.FlyingAnimal);
    }

    public static <T extends Entity & NPCHolder> void callNPCMoveEvent(T what) {
        final NPC npc = what.getNPC();
        if (npc != null && NPCMoveEvent.getHandlerList().getRegisteredListeners().length > 0) {
            if (what.xo != what.getX() || what.yo != what.getY() || what.zo != what.getZ() || what.yRotO != what.getYRot() || what.xRotO != what.getXRot()) {
                Location from = new Location(what.level.getWorld(), what.xo, what.yo, what.zo, what.yRotO, what.xRotO);
                Location to = new Location(what.level.getWorld(), what.getX(), what.getY(), what.getZ(), what.getYRot(), what.getXRot());
                final NPCMoveEvent event = new NPCMoveEvent(npc, from, to.clone());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    final Location eventFrom = event.getFrom();
                    what.absMoveTo(eventFrom.getX(), eventFrom.getY(), eventFrom.getZ(), eventFrom.getYaw(), eventFrom.getPitch());
                } else if (!to.equals(event.getTo())) {
                    what.absMoveTo(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch());
                }
            }
        }
    }

    public static TreeMap<?, ?> getBehaviorMap(LivingEntity entity) {
        try {
            return (TreeMap<?, ?>) BEHAVIOR_TREE_MAP.invoke(entity.getBrain());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends Entity> net.minecraft.world.entity.EntityType<T> getEntityType(Class<?> clazz) {
        return (net.minecraft.world.entity.EntityType<T>) CITIZENS_ENTITY_TYPES.get(clazz);
    }

    public static Entity getHandle(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

    private static LivingEntity getHandle(org.bukkit.entity.LivingEntity entity) {
        return (LivingEntity) NMSImpl.getHandle((org.bukkit.entity.Entity) entity);
    }

    private static LivingEntity getHandle(Tameable entity) {
        return (LivingEntity) NMSImpl.getHandle((org.bukkit.entity.Entity) entity);
    }

    public static PathNavigation getNavigation(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof Mob ? ((Mob) handle).getNavigation()
                : handle instanceof MobAI ? ((MobAI) handle).getNavigation() : null;
    }

    private static Path getPathEntity(PathNavigation nav) {
        try {
            return nav instanceof EntityNavigation ? ((EntityNavigation) nav).getPathEntity()
                    : (Path) NAVIGATION_PATH.invoke(nav);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static EntityDataAccessor<Integer> getRabbitTypeField() {
        return RABBIT_TYPE_DATAWATCHER;
    }

    public static EntityDimensions getSize(Entity entity) {
        try {
            return (EntityDimensions) SIZE_FIELD_GETTER.invoke(entity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SoundEvent getSoundEffect(NPC npc, SoundEvent snd, NPC.Metadata meta) {
        if (npc == null)
            return snd;
        String data = npc.data().get(meta);
        return data == null ? snd : Registry.SOUND_EVENT.get(ResourceLocation.tryParse(data));
    }

    public static void initNetworkManager(Connection network) {
        network.channel = new EmptyChannel(null);
        SocketAddress socketAddress = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
        network.address = socketAddress;
    }

    public static boolean isLeashed(NPC npc, Supplier<Boolean> isLeashed, Mob entity) {
        return NMS.isLeashed(npc, isLeashed, () -> entity.dropLeash(true, false));
    }

    @SuppressWarnings("deprecation")
    public static void minecartItemLogic(AbstractMinecart minecart) {
        NPC npc = ((NPCHolder) minecart).getNPC();
        if (npc == null)
            return;
        Material mat = Material.getMaterial(npc.data().get(NPC.Metadata.MINECART_ITEM, ""), false);
        int data = npc.data().get(NPC.Metadata.MINECART_ITEM_DATA, 0); // TODO: migration for this
        int offset = npc.data().get(NPC.Metadata.MINECART_OFFSET, 0);
        minecart.setCustomDisplay(mat != null);
        if (mat != null) {
            minecart.setDisplayBlockState(Registry.BLOCK.byId(mat.getId()).defaultBlockState());
        }
        minecart.setDisplayOffset(offset);
    }

    public static boolean moveFish(NPC npc, Mob handle, Vec3 vec3d) {
        if (npc == null)
            return false;
        if (!npc.useMinecraftAI() && handle.isInWater() && !npc.getNavigator().isNavigating()) {
            handle.moveRelative(handle instanceof Dolphin || handle instanceof Axolotl ? handle.getSpeed()
                    : handle instanceof Turtle ? 0.1F : 0.01F, vec3d);
            handle.move(MoverType.SELF, handle.getDeltaMovement());
            handle.setDeltaMovement(handle.getDeltaMovement().scale(0.9));
            return true;
        }
        return false;
    }

    public static void resetPuffTicks(Pufferfish fish) {
        try {
            PUFFERFISH_C.invoke(fish, 0);
            PUFFERFISH_D.invoke(fish, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void restoreGoals(NPC npc, GoalSelector... goalSelectors) {
        if (goalSelectors == null)
            return;
        int i = 0;
        for (GoalSelector selector : goalSelectors) {
            try {
                Collection<?> list = selector.getAvailableGoals();
                list.clear();
                Collection old = npc.data().get("selector" + i);
                if (old != null) {
                    list.addAll(old);
                }
            } catch (Exception e) {
                Messaging.logTr(Messages.ERROR_RESTORING_GOALS, e.getLocalizedMessage());
            } catch (Throwable e) {
                Messaging.logTr(Messages.ERROR_RESTORING_GOALS, e.getLocalizedMessage());
            }
            i++;
        }
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        if (packet == null)
            return;
        ((ServerPlayer) NMSImpl.getHandle(player)).connection.send(packet);
    }

    public static void sendPackets(Player player, Iterable<Packet<?>> packets) {
        if (packets == null)
            return;
        for (Packet<?> packet : packets) {
            ((ServerPlayer) getHandle(player)).connection.send(packet);
        }
    }

    public static void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance attr = entity.getAttribute(attribute);
        if (attr == null) {
            try {
                AttributeSupplier provider = (AttributeSupplier) ATTRIBUTE_SUPPLIER.invoke(entity.getAttributes());
                Map<Attribute, AttributeInstance> all = Maps
                        .newHashMap((Map<Attribute, AttributeInstance>) ATTRIBUTE_PROVIDER_MAP.invoke(provider));
                all.put(attribute, new AttributeInstance(attribute, att -> {
                    throw new UnsupportedOperationException(
                            "Tried to change value for default attribute instance FOLLOW_RANGE");
                }));
                ATTRIBUTE_PROVIDER_MAP_SETTER.invoke(provider, ImmutableMap.copyOf(all));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            attr = entity.getAttribute(attribute);
        }
        attr.setBaseValue(value);
    }

    public static void setBukkitEntity(Entity entity, CraftEntity bukkitEntity) {
        try {
            BUKKITENTITY_FIELD_SETTER.invoke(entity, bukkitEntity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setFallingBlockState(FallingBlockEntity handle, BlockState state) {
        try {
            FALLING_BLOCK_STATE_SETTER.invoke(handle, state);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setLife(FishingHook entity, int life) {
        try {
            FISHING_HOOK_LIFE.invoke(entity, life);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setLookControl(Mob mob, LookControl control) {
        try {
            LOOK_CONTROL_SETTER.invoke(mob, control);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setNotInSchool(AbstractFish entity) {
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
            EntityDimensions entitysize = (EntityDimensions) SIZE_FIELD_GETTER.invoke(entity);
            Pose entitypose = entity.getPose();
            EntityDimensions entitysize1 = entity.getDimensions(entitypose);
            SIZE_FIELD_SETTER.invoke(entity, entitysize1);
            HEAD_HEIGHT.invoke(entity, HEAD_HEIGHT_METHOD.invoke(entity, entitypose, entitysize1));
            if (entitysize1.width < entitysize.width && false /* TODO: PREVIOUS CITIZENS ADDITION ?reason */) {
                double d0 = entitysize1.width / 2.0D;
                entity.setBoundingBox(new AABB(entity.getX() - d0, entity.getY(), entity.getZ() - d0,
                        entity.getX() + d0, entity.getY() + entitysize1.height, entity.getZ() + d0));
            } else {
                AABB axisalignedbb = entity.getBoundingBox();
                entity.setBoundingBox(new AABB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                        axisalignedbb.minX + entitysize1.width, axisalignedbb.minY + entitysize1.height,
                        axisalignedbb.minZ + entitysize1.width));
                if (entitysize1.width > entitysize.width && !justCreated && !entity.level.isClientSide) {
                    float f = entitysize.width - entitysize1.width;
                    entity.move(MoverType.SELF, new Vec3(f, 0.0D, f));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setSize(Entity entity, EntityDimensions size) {
        try {
            SIZE_FIELD_SETTER.invoke(entity, size);
            HEAD_HEIGHT.invoke(entity,
                    HEAD_HEIGHT_METHOD.invoke(entity, entity.getPose(), entity.getDimensions(entity.getPose())));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Entity teleportAcrossWorld(Entity entity, ServerLevel worldserver, BlockPos location) {
        if (FIND_DIMENSION_ENTRY_POINT == null || entity.isRemoved())
            return null;
        NPC npc = ((NPCHolder) entity).getNPC();
        PortalInfo sds = null;
        try {
            sds = location == null ? (PortalInfo) FIND_DIMENSION_ENTRY_POINT.invoke(entity, worldserver)
                    : new PortalInfo(new Vec3(location.getX(), location.getY(), location.getZ()), Vec3.ZERO,
                            entity.getYRot(), entity.getXRot(), worldserver, (CraftPortalEvent) null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (npc == null || sds == null)
            return null;
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.spawn(new Location(worldserver.getWorld(), sds.pos.x, sds.pos.y, sds.pos.z, sds.yRot, sds.xRot));
        Entity handle = ((CraftEntity) npc.getEntity()).getHandle();
        handle.setDeltaMovement(sds.speed);
        handle.portalCooldown = entity.portalCooldown;
        try {
            PORTAL_ENTRANCE_POS_SETTER.invoke(handle, PORTAL_ENTRANCE_POS_GETTER.invoke(entity));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return handle;
    }

    public static void updateAI(LivingEntity entity) {
        if (entity instanceof Mob) {
            Mob handle = (Mob) entity;
            handle.getSensing().tick();
            handle.getNavigation().tick();
            handle.getMoveControl().tick();
            handle.getLookControl().tick();
            handle.getJumpControl().tick();
        } else if (entity instanceof MobAI) {
            ((MobAI) entity).tickAI();
        }
    }

    public static void updateMinecraftAIState(NPC npc, Mob entity) {
        if (npc == null)
            return;
        if (npc.useMinecraftAI()) {
            NMSImpl.restoreGoals(npc, entity.goalSelector, entity.targetSelector);
            if (npc.data().has("behavior-map")) {
                TreeMap behavior = npc.data().get("behavior-map");
                getBehaviorMap(entity).putAll(behavior);
                npc.data().remove("behavior-map");
            }
        } else {
            NMSImpl.clearGoals(npc, entity.goalSelector, entity.targetSelector);
            TreeMap behaviorMap = getBehaviorMap(entity);
            if (behaviorMap.size() > 0) {
                npc.data().set("behavior-map", new TreeMap(behaviorMap));
                behaviorMap.clear();
            }
        }
    }

    private static final MethodHandle ATTRIBUTE_PROVIDER_MAP = NMS.getFirstGetter(AttributeSupplier.class, Map.class);
    private static final MethodHandle ATTRIBUTE_PROVIDER_MAP_SETTER = NMS.getFinalSetter(AttributeSupplier.class, "a");
    private static final MethodHandle ATTRIBUTE_SUPPLIER = NMS.getFirstGetter(AttributeMap.class,
            AttributeSupplier.class);
    private static final Set<EntityType> BAD_CONTROLLER_LOOK = EnumSet.of(EntityType.POLAR_BEAR, EntityType.BEE,
            EntityType.SILVERFISH, EntityType.SHULKER, EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.BAT,
            EntityType.SLIME, EntityType.DOLPHIN, EntityType.MAGMA_CUBE, EntityType.HORSE, EntityType.GHAST,
            EntityType.SHULKER, EntityType.PHANTOM);
    private static final MethodHandle BEHAVIOR_TREE_MAP = NMS.getGetter(Brain.class, "f");
    private static final MethodHandle BUKKITENTITY_FIELD_SETTER = NMS.getSetter(Entity.class, "bukkitEntity");
    private static final MethodHandle CHUNKMAP_UPDATE_PLAYER_STATUS = NMS.getMethodHandle(ChunkMap.class, "a", true,
            ServerPlayer.class, boolean.class);
    private static final Map<Class<?>, net.minecraft.world.entity.EntityType<?>> CITIZENS_ENTITY_TYPES = Maps
            .newHashMap();
    private static final MethodHandle CRAFT_BOSSBAR_HANDLE_FIELD = NMS.getSetter(CraftBossBar.class, "handle");
    private static final EntityDataAccessor<Pose> DATA_POSE = NMS.getStaticObject(Entity.class, "ad");
    private static final float DEFAULT_SPEED = 1F;
    public static MethodHandle ENDERDRAGON_CHECK_WALLS = NMS.getFirstMethodHandleWithReturnType(EnderDragon.class, true,
            boolean.class, AABB.class);
    private static final EntityDataAccessor<Boolean> ENDERMAN_CREEPY = NMS.getStaticObject(EnderMan.class, "bX");
    private static final MethodHandle ENTITY_FISH_NUM_IN_SCHOOL = NMS.getFirstSetter(AbstractSchoolingFish.class,
            int.class);
    private static final MethodHandle ENTITY_GET_SOUND_FALL = NMS.getMethodHandle(LivingEntity.class, "c", true,
            int.class);
    private static MethodHandle ENTITY_NAVIGATION = NMS.getFirstSetter(Mob.class, PathNavigation.class);
    private static CustomEntityRegistry ENTITY_REGISTRY;
    private static MethodHandle ENTITY_REGISTRY_SETTER;
    private static final MethodHandle FALLING_BLOCK_STATE_SETTER = NMS.getFirstSetter(FallingBlockEntity.class,
            BlockState.class);
    private static final MethodHandle FIND_DIMENSION_ENTRY_POINT = NMS.getFirstMethodHandleWithReturnType(Entity.class,
            true, PortalInfo.class, ServerLevel.class);
    private static final MethodHandle FISHING_HOOK_LIFE = NMS.getSetter(FishingHook.class, "aq");
    private static final MethodHandle FLYING_MOVECONTROL_FLOAT_GETTER = NMS.getFirstGetter(FlyingMoveControl.class,
            boolean.class);
    private static final MethodHandle FLYING_MOVECONTROL_FLOAT_SETTER = NMS.getFirstSetter(FlyingMoveControl.class,
            boolean.class);
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle HEAD_HEIGHT = NMS.getSetter(Entity.class, "ba");
    private static final MethodHandle HEAD_HEIGHT_METHOD = NMS.getFirstMethodHandle(Entity.class, true, Pose.class,
            EntityDimensions.class);
    private static final MethodHandle JUMP_FIELD = NMS.getGetter(LivingEntity.class, "bn");
    private static final MethodHandle LOOK_CONTROL_SETTER = NMS.getFirstSetter(Mob.class, LookControl.class);
    private static final MethodHandle MAKE_REQUEST = NMS.getMethodHandle(YggdrasilAuthenticationService.class,
            "makeRequest", true, URL.class, Object.class, Class.class);
    private static MethodHandle MOVE_CONTROLLER_MOVING = NMS.getSetter(MoveControl.class, "k");
    private static final MethodHandle NAVIGATION_CREATE_PATHFINDER = NMS
            .getFirstMethodHandleWithReturnType(PathNavigation.class, true, PathFinder.class, int.class);
    private static MethodHandle NAVIGATION_PATH = NMS.getFirstGetter(PathNavigation.class, Path.class);
    private static final MethodHandle NAVIGATION_PATHFINDER = NMS.getFinalSetter(PathNavigation.class, "t");
    private static final MethodHandle NAVIGATION_WORLD_FIELD = NMS.getFirstSetter(PathNavigation.class, Level.class);
    public static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    // Player.mobCounts: workaround for an issue which suppresses mobs being spawn near NPC players on Paper. Need to
    // check for every update.
    public static final MethodHandle PAPER_PLAYER_MOB_COUNTS = NMS.getGetter(ServerPlayer.class, "mobCounts", false);
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER = NMS.getGetter(ChunkMap.class, "N");
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER = NMS.getSetter(ChunkMap.class, "N");
    private static MethodHandle PORTAL_ENTRANCE_POS_GETTER = NMS.getGetter(Entity.class, "ai");
    private static MethodHandle PORTAL_ENTRANCE_POS_SETTER = NMS.getSetter(Entity.class, "ai");
    private static final MethodHandle PUFFERFISH_C = NMS.getSetter(Pufferfish.class, "bV");
    private static final MethodHandle PUFFERFISH_D = NMS.getSetter(Pufferfish.class, "bW");
    private static final EntityDataAccessor<Integer> RABBIT_TYPE_DATAWATCHER = NMS.getFirstStaticObject(Rabbit.class,
            EntityDataAccessor.class);
    private static final Random RANDOM = Util.getFastRandom();
    private static final MethodHandle SERVER_ENTITY_GETTER = NMS.getFirstGetter(TrackedEntity.class,
            ServerEntity.class);
    private static MethodHandle SET_PROFILE_METHOD;
    private static final MethodHandle SIZE_FIELD_GETTER = NMS.getFirstGetter(Entity.class, EntityDimensions.class);
    private static final MethodHandle SIZE_FIELD_SETTER = NMS.getFirstSetter(Entity.class, EntityDimensions.class);
    private static MethodHandle SKULL_META_PROFILE;
    private static MethodHandle TEAM_FIELD;
    static {
        try {
            ENTITY_REGISTRY = new CustomEntityRegistry(Registry.ENTITY_TYPE);
            ENTITY_REGISTRY_SETTER = NMS.getFinalSetter(Registry.class, "W");
            ENTITY_REGISTRY_SETTER.invoke(ENTITY_REGISTRY);
        } catch (Throwable e) {
            Messaging.logTr(Messages.ERROR_GETTING_ID_MAPPING, e.getMessage());
        }
    }
}
