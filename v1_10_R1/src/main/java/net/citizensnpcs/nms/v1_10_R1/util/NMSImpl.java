package net.citizensnpcs.nms.v1_10_R1.util;

import java.lang.invoke.MethodHandle;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftSound;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_10_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftWither;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

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
import net.citizensnpcs.api.LocationLookup.PerPlayerMetadata;
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
import net.citizensnpcs.nms.v1_10_R1.entity.ArmorStandController;
import net.citizensnpcs.nms.v1_10_R1.entity.BatController;
import net.citizensnpcs.nms.v1_10_R1.entity.BlazeController;
import net.citizensnpcs.nms.v1_10_R1.entity.CaveSpiderController;
import net.citizensnpcs.nms.v1_10_R1.entity.ChickenController;
import net.citizensnpcs.nms.v1_10_R1.entity.CowController;
import net.citizensnpcs.nms.v1_10_R1.entity.CreeperController;
import net.citizensnpcs.nms.v1_10_R1.entity.EnderDragonController;
import net.citizensnpcs.nms.v1_10_R1.entity.EndermanController;
import net.citizensnpcs.nms.v1_10_R1.entity.EndermiteController;
import net.citizensnpcs.nms.v1_10_R1.entity.EntityHumanNPC;
import net.citizensnpcs.nms.v1_10_R1.entity.GhastController;
import net.citizensnpcs.nms.v1_10_R1.entity.GiantController;
import net.citizensnpcs.nms.v1_10_R1.entity.GuardianController;
import net.citizensnpcs.nms.v1_10_R1.entity.HorseController;
import net.citizensnpcs.nms.v1_10_R1.entity.HumanController;
import net.citizensnpcs.nms.v1_10_R1.entity.IronGolemController;
import net.citizensnpcs.nms.v1_10_R1.entity.MagmaCubeController;
import net.citizensnpcs.nms.v1_10_R1.entity.MushroomCowController;
import net.citizensnpcs.nms.v1_10_R1.entity.OcelotController;
import net.citizensnpcs.nms.v1_10_R1.entity.PigController;
import net.citizensnpcs.nms.v1_10_R1.entity.PigZombieController;
import net.citizensnpcs.nms.v1_10_R1.entity.PolarBearController;
import net.citizensnpcs.nms.v1_10_R1.entity.RabbitController;
import net.citizensnpcs.nms.v1_10_R1.entity.SheepController;
import net.citizensnpcs.nms.v1_10_R1.entity.ShulkerController;
import net.citizensnpcs.nms.v1_10_R1.entity.SilverfishController;
import net.citizensnpcs.nms.v1_10_R1.entity.SkeletonController;
import net.citizensnpcs.nms.v1_10_R1.entity.SlimeController;
import net.citizensnpcs.nms.v1_10_R1.entity.SnowmanController;
import net.citizensnpcs.nms.v1_10_R1.entity.SpiderController;
import net.citizensnpcs.nms.v1_10_R1.entity.SquidController;
import net.citizensnpcs.nms.v1_10_R1.entity.VillagerController;
import net.citizensnpcs.nms.v1_10_R1.entity.WitchController;
import net.citizensnpcs.nms.v1_10_R1.entity.WitherController;
import net.citizensnpcs.nms.v1_10_R1.entity.WolfController;
import net.citizensnpcs.nms.v1_10_R1.entity.ZombieController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.AreaEffectCloudController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.BoatController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.DragonFireballController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.EggController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.EnderPearlController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.EnderSignalController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.FallingBlockController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.FireworkController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.FishingHookController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.ItemController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.ItemFrameController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.LargeFireballController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.LeashController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.MinecartChestController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.PaintingController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.ShulkerBulletController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.SmallFireballController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.SnowballController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.SpectralArrowController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.ThrownPotionController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.TippedArrowController;
import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.WitherSkullController;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.versioned.BossBarTrait;
import net.citizensnpcs.trait.versioned.EnderDragonTrait;
import net.citizensnpcs.trait.versioned.PolarBearTrait;
import net.citizensnpcs.trait.versioned.ShulkerTrait;
import net.citizensnpcs.trait.versioned.SnowmanTrait;
import net.citizensnpcs.util.EmptyChannel;
import net.citizensnpcs.util.EntityPacketTracker;
import net.citizensnpcs.util.EntityPacketTracker.PacketAggregator;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.NMSBridge;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_10_R1.AttributeInstance;
import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.Block;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.BossBattleServer;
import net.minecraft.server.v1_10_R1.ChatComponentText;
import net.minecraft.server.v1_10_R1.ChatMessage;
import net.minecraft.server.v1_10_R1.Container;
import net.minecraft.server.v1_10_R1.ContainerAnvil;
import net.minecraft.server.v1_10_R1.ControllerJump;
import net.minecraft.server.v1_10_R1.ControllerLook;
import net.minecraft.server.v1_10_R1.ControllerMove;
import net.minecraft.server.v1_10_R1.CrashReport;
import net.minecraft.server.v1_10_R1.CrashReportSystemDetails;
import net.minecraft.server.v1_10_R1.DamageSource;
import net.minecraft.server.v1_10_R1.DataWatcherObject;
import net.minecraft.server.v1_10_R1.EnchantmentManager;
import net.minecraft.server.v1_10_R1.EnderDragonBattle;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityEnderDragon;
import net.minecraft.server.v1_10_R1.EntityEnderman;
import net.minecraft.server.v1_10_R1.EntityFishingHook;
import net.minecraft.server.v1_10_R1.EntityHorse;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.EntityPolarBear;
import net.minecraft.server.v1_10_R1.EntityRabbit;
import net.minecraft.server.v1_10_R1.EntityShulker;
import net.minecraft.server.v1_10_R1.EntityTameableAnimal;
import net.minecraft.server.v1_10_R1.EntityTracker;
import net.minecraft.server.v1_10_R1.EntityTrackerEntry;
import net.minecraft.server.v1_10_R1.EntityTypes;
import net.minecraft.server.v1_10_R1.EntityWither;
import net.minecraft.server.v1_10_R1.EnumItemSlot;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.IInventory;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.MathHelper;
import net.minecraft.server.v1_10_R1.MinecraftKey;
import net.minecraft.server.v1_10_R1.MobEffects;
import net.minecraft.server.v1_10_R1.NavigationAbstract;
import net.minecraft.server.v1_10_R1.NetworkManager;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_10_R1.PacketPlayOutBed;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_10_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_10_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_10_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_10_R1.PathEntity;
import net.minecraft.server.v1_10_R1.PathPoint;
import net.minecraft.server.v1_10_R1.PathType;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_10_R1.ReportedException;
import net.minecraft.server.v1_10_R1.ScoreboardTeam;
import net.minecraft.server.v1_10_R1.SoundEffect;
import net.minecraft.server.v1_10_R1.Vec3D;
import net.minecraft.server.v1_10_R1.WorldServer;

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
        // TODO: configuration / use minecraft defaults for this
        int visibleDistance = handle instanceof EntityPlayer ? 512 : 80;
        int updateInterval = handle instanceof EntityPlayer ? 2 : 3;
        boolean deltaTracking = handle instanceof EntityPlayer ? false : true;
        EntityTrackerEntry tracker = new EntityTrackerEntry(handle, visibleDistance,
                handle.world.getMinecraftServer().getPlayerList().d(), updateInterval, deltaTracking);
        Map<EnumItemSlot, ItemStack> equipment = Maps.newEnumMap(EnumItemSlot.class);
        return new EntityPacketTracker() {
            @Override
            public void link(Player player) {
                EntityPlayer p = (EntityPlayer) getHandle(player);
                handle.dead = false;
                tracker.updatePlayer(p);
                handle.dead = true;
            }

            @Override
            public void run() {
                if (handle instanceof EntityLiving) {
                    boolean changed = false;
                    EntityLiving entity = (EntityLiving) handle;
                    for (EnumItemSlot slot : EnumItemSlot.values()) {
                        ItemStack old = equipment.getOrDefault(slot,
                                CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR)));
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
                tracker.track(Lists.newArrayList(tracker.trackedPlayers));
            }

            @Override
            public void unlink(Player player) {
                EntityPlayer p = (EntityPlayer) getHandle(player);
                tracker.clear(p);
                tracker.trackedPlayers.remove(p);
            }

            @Override
            public void unlinkAll(Consumer<Player> callback) {
                handle.die();
                for (EntityPlayer link : Lists.newArrayList(tracker.trackedPlayers)) {
                    Player entity = link.getBukkitEntity();
                    unlink(entity);
                    if (callback != null) {
                        callback.accept(entity);
                    }
                }
                tracker.trackedPlayers.clear();
            }
        };
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Exception {
        if (Bukkit.isPrimaryThread())
            throw new IllegalStateException("NMS.fillProfileProperties cannot be invoked from the main thread.");
        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer().ay();
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
                bserver = (BossBattleServer) WITHER_BOSS_BAR_FIELD.get(NMSImpl.getHandle(entity));
            } else if (entity.getType() == EntityType.ENDER_DRAGON) {
                bserver = (BossBattleServer) ENDERDRAGON_BATTLE_BAR_FIELD
                        .get(ENDERDRAGON_BATTLE_FIELD.get(NMSImpl.getHandle(entity)));
            }
        } catch (Exception e) {
        }
        if (bserver == null)
            return null;
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

    @Override
    public BoundingBox getCollisionBox(org.bukkit.block.Block block) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        AxisAlignedBB aabb = world.getType(pos).c(world, pos);
        if (aabb == null) {
            aabb = world.getType(pos).d(world, pos);
        }
        return new BoundingBox(aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f);
    }

    @Override
    public Location getDestination(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        ControllerMove controller = handle instanceof EntityInsentient ? ((EntityInsentient) handle).getControllerMove()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getControllerMove() : null;
        if (controller == null || !controller.a())
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
        return getHandle((LivingEntity) entity).aQ;
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
        return handle.bg;
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
        EntityTrackerEntry entry = server.getTracker().trackedEntities.get(entity.getEntityId());
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
                entry.a((EntityPlayer) getHandle(player));
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
        Entity source = ((CraftBlockCommandSender) sender).getTileEntity().f();
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
        return NMSImpl.getHandle(entity).P;
    }

    @Override
    public MCNavigator getTargetNavigator(org.bukkit.entity.Entity entity, Iterable<Vector> dest,
            final NavigatorParameters params) {
        final PathEntity path = new PathEntity(Iterables.toArray(
                Iterables.transform(dest,
                        input -> new PathPoint(input.getBlockX(), input.getBlockY(), input.getBlockZ())),
                PathPoint.class));
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
        net.minecraft.server.v1_10_R1.Entity raw = getHandle(entity);
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
                if (params.speed() != lastSpeed && lastSpeed > 0) {
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
        return handle.bf;
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
    public boolean isSolid(org.bukkit.block.Block in) {
        return in.getType().isSolid();
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
        registerTraitWithCommand(manager, PolarBearTrait.class);
        registerTraitWithCommand(manager, ShulkerTrait.class);
        registerTraitWithCommand(manager, SnowmanTrait.class);
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
        EntityControllers.setEntityControllerForType(EntityType.VILLAGER, VillagerController.class);
        EntityControllers.setEntityControllerForType(EntityType.WOLF, WolfController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITCH, WitchController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITHER, WitherController.class);
        EntityControllers.setEntityControllerForType(EntityType.WITHER_SKULL, WitherSkullController.class);
        EntityControllers.setEntityControllerForType(EntityType.ZOMBIE, ZombieController.class);
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
            double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90;
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
                    ((EntityInsentient) handle).cJ(), ((EntityInsentient) handle).N());
            while (((EntityInsentient) handle).aQ >= 180F) {
                ((EntityInsentient) handle).aQ -= 360F;
            }
            while (((EntityInsentient) handle).aQ < -180F) {
                ((EntityInsentient) handle).aQ += 360F;
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
            ((EntityInsentient) handle).getControllerLook().a(target, ((EntityInsentient) handle).cJ(),
                    ((EntityInsentient) handle).N());
            while (((EntityLiving) handle).aQ >= 180F) {
                ((EntityLiving) handle).aQ -= 360F;
            }
            while (((EntityLiving) handle).aQ < -180F) {
                ((EntityLiving) handle).aQ += 360F;
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
        final ContainerAnvil container = new ContainerAnvil(handle.inventory, handle.world, new BlockPosition(0, 0, 0),
                handle) {
            private CraftInventoryView bukkitEntity;

            @Override
            public void b(EntityHuman entityhuman) {
            }

            @Override
            public void e() {
                super.e();
                getBukkitView().getTopInventory().setItem(2, CraftItemStack.asCraftMirror(c.get(2).getItem()));
            }

            @Override
            public CraftInventoryView getBukkitView() {
                if (this.bukkitEntity != null) {
                    try {
                        this.bukkitEntity = new CraftInventoryView(player,
                                new CitizensInventoryAnvil(new Location(player.getWorld(), 0, 0, 0),
                                        (IInventory) REPAIR_INVENTORY.invoke(this),
                                        (IInventory) RESULT_INVENTORY.invoke(this), anvil),
                                this);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return super.getBukkitView();
                    }
                }
                return this.bukkitEntity;
            }
        };
        container.windowId = handle.nextContainerCounter();
        container.getBukkitView().setItem(0, anvil.getItem(0));
        container.getBukkitView().setItem(1, anvil.getItem(1));
        container.checkReachable = false;
        handle.playerConnection
                .sendPacket(new PacketPlayOutOpenWindow(container.windowId, "minecraft:anvil", new ChatMessage(title)));
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
        ((EntityHorse) handle).f((EntityHuman) equipperHandle);
        horse.setTamed(wasTamed);
    }

    @Override
    public void playAnimation(PlayerAnimation animation, Player player, Iterable<Player> to) {
        PlayerAnimationImpl.play(animation, player, to);
    }

    @Override
    public Runnable playerTicker(Player next) {
        return () -> {
            EntityPlayer entity = (EntityPlayer) getHandle(next);
            boolean removeFromPlayerList = ((NPCHolder) entity).getNPC().data().get("removefromplayerlist",
                    Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
            entity.k_();
            if (!removeFromPlayerList)
                return;
            Entity entity1 = entity.bB();
            if (entity1 != null) {
                if (entity1.dead || !entity1.w(entity)) {
                    entity.stopRiding();
                }
            } else {
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
                if (entity.dead) {
                    entity.world.removeEntity(entity);
                } else if (!removeFromPlayerList) {
                    if (!entity.world.players.contains(entity)) {
                        entity.world.players.add(entity);
                    }
                } else {
                    entity.world.players.remove(entity);
                }
            }
        };
    }

    @Override
    public void registerEntityClass(Class<?> clazz) {
        if (ENTITY_CLASS_TO_INT == null || ENTITY_CLASS_TO_INT.containsKey(clazz))
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            if (!ENTITY_CLASS_TO_INT.containsKey(search)) {
                continue;
            }
            int code = ENTITY_CLASS_TO_INT.get(search);
            ENTITY_CLASS_TO_INT.put(clazz, code);
            ENTITY_CLASS_TO_NAME.put(clazz, ENTITY_CLASS_TO_NAME.get(search));
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
        nmsEntity.world.removeEntity(nmsEntity);
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
        EntityTrackerEntry entry = server.getTracker().trackedEntities.get(entity.getEntityId());
        if (entry == null)
            return;
        entry.a();
        PlayerlistTrackerEntry replace = new PlayerlistTrackerEntry(entry);
        server.getTracker().trackedEntities.a(entity.getEntityId(), replace);
        if (TRACKED_ENTITY_SET != null) {
            try {
                Collection<Object> set = (Collection<Object>) TRACKED_ENTITY_SET.get(server.getTracker());
                set.remove(entry);
                set.add(replace);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (getHandle(entity) instanceof EntityHumanNPC) {
            ((EntityHumanNPC) getHandle(entity)).setTracked();
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
            EntityTrackerEntry entry = ((WorldServer) handle.world).getTracker().trackedEntities.get(handle.getId());
            long dx, dy, dz;
            try {
                dx = EntityTracker.a(handle.locX) - (long) ENTITY_TRACKER_ENTRY_X.invoke(entry);
                dy = EntityTracker.a(handle.locY) - (long) ENTITY_TRACKER_ENTRY_Y.invoke(entry);
                dz = EntityTracker.a(handle.locZ) - (long) ENTITY_TRACKER_ENTRY_Z.invoke(entry);
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }
            toSend.add(new PacketPlayOutRelEntityMoveLook(handle.getId(), (short) dx, (short) dy, (short) dz,
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
    public void setBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        getHandle(entity).yaw = yaw;
    }

    @Override
    public void setBoundingBox(org.bukkit.entity.Entity entity, BoundingBox box) {
        NMSImpl.getHandle(entity).a(new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ));
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, Object component, String string) {
        getHandle(entity).setCustomName(string);
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
        setSize(getHandle(bentity), desired.width, desired.height, false);
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
        handle.aP = yaw;
        if (!(handle instanceof EntityHuman)) {
            handle.aO = yaw;
        }
        handle.aQ = yaw;
    }

    @Override
    public void setKnockbackResistance(LivingEntity entity, double d) {
        EntityLiving handle = NMSImpl.getHandle(entity);
        handle.getAttributeInstance(GenericAttributes.c).setValue(d);
    }

    @Override
    public void setLocationDirectly(org.bukkit.entity.Entity entity, Location location) {
        getHandle(entity).setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
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
        ((EntityPolarBear) getHandle(entity)).p(rearing);
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
    public void setSneaking(org.bukkit.entity.Entity entity, boolean sneaking) {
        if (entity instanceof Player) {
            ((Player) entity).setSneaking(sneaking);
        }
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
        handle.bf = (float) d;
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
    }

    @Override
    public void sleep(Player entity, boolean sleep) {
        EntityPlayer from = (EntityPlayer) getHandle(entity);
        PerPlayerMetadata<Long> meta = CitizensAPI.getLocationLookup().registerMetadata("sleeping", null);
        if (sleep) {
            List<Player> nearbyPlayers = Lists.newArrayList(
                    Iterables.filter(CitizensAPI.getLocationLookup().getNearbyPlayers(entity.getLocation(), 64), p -> {
                        Long time = meta.getMarker(p.getUniqueId(), entity.getUniqueId().toString());
                        if (time == null || Math.abs(System.currentTimeMillis() - time) > 5000)
                            return true;
                        meta.set(p.getUniqueId(), entity.getUniqueId().toString(), System.currentTimeMillis());
                        return false;
                    }));
            if (nearbyPlayers.size() == 0)
                return;
            Location loc = from.getBukkitEntity().getLocation().clone();
            BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
            BlockFace facing = axis[Math.round(loc.getYaw() / 90f) & 0x3].getOppositeFace();
            byte facingByte = 0;
            switch (facing) {
                case EAST:
                    facingByte = (byte) 1;
                    break;
                case SOUTH:
                    facingByte = (byte) 2;
                    break;
                case WEST:
                    facingByte = (byte) 3;
                    break;
            }
            Location bedLoc = loc.clone().add(0, -loc.getY(), 0);
            PacketPlayOutBed bed = new PacketPlayOutBed(from,
                    new BlockPosition(bedLoc.getBlockX(), bedLoc.getBlockY(), bedLoc.getBlockZ()));
            List<Packet<?>> list = Lists.newArrayListWithCapacity(3);
            from.locX = bedLoc.getBlockX();
            from.locY = bedLoc.getBlockY();
            from.locZ = bedLoc.getBlockZ();
            list.add(new PacketPlayOutEntityTeleport(from));
            list.add(bed);
            from.locX = loc.getX();
            from.locY = loc.getY();
            from.locZ = loc.getZ();
            list.add(new PacketPlayOutEntityTeleport(from));
            for (Player nearby : nearbyPlayers) {
                nearby.sendBlockChange(bedLoc, Material.BED_BLOCK, facingByte);
                list.forEach(packet -> sendPacket(nearby, packet));
                meta.set(nearby.getUniqueId(), entity.getUniqueId().toString(), System.currentTimeMillis());
            }
        } else {
            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(from, 2);
            sendPacketNearby(entity, entity.getLocation(), packet, 64);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (meta.remove(player.getUniqueId(), entity.getUniqueId().toString())) {
                    sendPacket(player, packet);
                }
            }
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
        if (RANDOM.nextFloat() <= 0.85F && (handle.ak() || handle.ao())) {
            handle.motY += power;
        }
    }

    @Override
    public void updateInventoryTitle(Player player, InventoryView view, String newTitle) {
        EntityPlayer handle = (EntityPlayer) getHandle(player);
        Container active = handle.activeContainer;
        InventoryType type = view.getTopInventory().getType();
        Packet<?> packet = new PacketPlayOutOpenWindow(active.windowId, "minecraft:" + type.name().toLowerCase(),
                new ChatComponentText(newTitle), view.getTopInventory().getSize());
        handle.playerConnection.sendPacket(packet);
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

    private static class CitizensInventoryAnvil extends CraftInventoryAnvil implements ForwardingInventory {
        private final Inventory wrapped;

        public CitizensInventoryAnvil(Location location, IInventory inventory, IInventory resultInventory,
                Inventory wrapped) {
            super(location, inventory, resultInventory);
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

    public static void checkAndUpdateHeight(EntityLiving living, boolean flag, Consumer<Boolean> cb) {
        float oldw = living.width;
        float oldl = living.length;
        cb.accept(flag);
        if (oldw != living.width || living.length != oldl) {
            living.setPosition(living.locX - 0.01, living.locY, living.locZ - 0.01);
            living.setPosition(living.locX + 0.01, living.locY, living.locZ + 0.01);
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

    private static CompoundTag convertNBT(net.minecraft.server.v1_10_R1.NBTTagCompound tag) {
        if (tag == null)
            return new CompoundTag("", Collections.EMPTY_MAP);
        Map<String, Tag> tags = Maps.newHashMap();
        for (String key : tag.c()) {
            tags.put(key, convertNBT(key, tag.get(key)));
        }
        return new CompoundTag("", tags);
    }

    private static Tag convertNBT(String key, net.minecraft.server.v1_10_R1.NBTBase base) {
        if (base instanceof net.minecraft.server.v1_10_R1.NBTTagInt)
            return new IntTag(key, ((net.minecraft.server.v1_10_R1.NBTTagInt) base).e());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagFloat)
            return new FloatTag(key, ((net.minecraft.server.v1_10_R1.NBTTagFloat) base).i());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagDouble)
            return new DoubleTag(key, ((net.minecraft.server.v1_10_R1.NBTTagDouble) base).h());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagLong)
            return new LongTag(key, ((net.minecraft.server.v1_10_R1.NBTTagLong) base).d());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagShort)
            return new ShortTag(key, ((net.minecraft.server.v1_10_R1.NBTTagShort) base).f());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagByte)
            return new ByteTag(key, ((net.minecraft.server.v1_10_R1.NBTTagByte) base).g());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagByteArray)
            return new ByteArrayTag(key, ((net.minecraft.server.v1_10_R1.NBTTagByteArray) base).c());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagIntArray)
            return new IntArrayTag(key, ((net.minecraft.server.v1_10_R1.NBTTagIntArray) base).d());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagString)
            return new StringTag(key, base.toString());
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagList) {
            List<net.minecraft.server.v1_10_R1.NBTBase> list = (List<net.minecraft.server.v1_10_R1.NBTBase>) base;
            List<Tag> converted = Lists.newArrayList();
            if (list.size() > 0) {
                Class<? extends Tag> tagType = convertNBT("", list.get(0)).getClass();
                for (int i = 0; i < list.size(); i++) {
                    converted.add(convertNBT("", list.get(i)));
                }
                return new ListTag(key, tagType, converted);
            }
        } else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagCompound)
            return convertNBT((net.minecraft.server.v1_10_R1.NBTTagCompound) base);
        else if (base instanceof net.minecraft.server.v1_10_R1.NBTTagEnd)
            return new EndTag();
        return null;
    }

    public static void flyingMoveLogic(EntityLiving entity, float f, float f1) {
        if (entity.ct() || entity.bA()) {
            if (entity.isInWater()
                    && (!(entity instanceof EntityHuman) || !((EntityHuman) entity).abilities.isFlying)) {
                double d1 = entity.locY;
                float f4 = entity instanceof EntityPolarBear ? 0.98F : 0.8F;
                float f3 = 0.02F;
                float f2 = EnchantmentManager.d(entity);
                if (f2 > 3.0F) {
                    f2 = 3.0F;
                }
                if (!entity.onGround) {
                    f2 *= 0.5F;
                }
                if (f2 > 0.0F) {
                    f4 += (0.54600006F - f4) * f2 / 3.0F;
                    f3 += (entity.cp() - f3) * f2 / 3.0F;
                }
                entity.a(f, f1, f3);
                entity.move(entity.motX, entity.motY, entity.motZ);
                entity.motX *= f4;
                entity.motY *= 0.800000011920929D;
                entity.motZ *= f4;
                if (!entity.isNoGravity()) {
                    entity.motY -= 0.02D;
                }
                if (entity.positionChanged
                        && entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d1, entity.motZ)) {
                    entity.motY = 0.30000001192092896D;
                }
            } else if (entity.ao()
                    && (!(entity instanceof EntityHuman) || !((EntityHuman) entity).abilities.isFlying)) {
                double d1 = entity.locY;
                entity.a(f, f1, 0.02F);
                entity.move(entity.motX, entity.motY, entity.motZ);
                entity.motX *= 0.5D;
                entity.motY *= 0.5D;
                entity.motZ *= 0.5D;
                if (!entity.isNoGravity()) {
                    entity.motY -= 0.02D;
                }
                if (entity.positionChanged
                        && entity.c(entity.motX, entity.motY + 0.6000000238418579D - entity.locY + d1, entity.motZ)) {
                    entity.motY = 0.30000001192092896D;
                }
            } else if (entity.cG()) {
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
                if (entity.motY < 0.0D && d0 > 0.0D) {
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
                if (entity.positionChanged && !entity.world.isClientSide) {
                    double d4 = Math.sqrt(entity.motX * entity.motX + entity.motZ * entity.motZ);
                    double d5 = d2 - d4;
                    float f7 = (float) (d5 * 10.0D - 3.0D);
                    if (f7 > 0.0F) {
                        entity.a(entity.e((int) f7), 1.0F, 1.0F);
                        entity.damageEntity(DamageSource.FLY_INTO_WALL, f7);
                    }
                }
                if (entity.onGround && !entity.world.isClientSide && entity.getFlag(7)
                        && !CraftEventFactory.callToggleGlideEvent(entity, false).isCancelled()) {
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
                    f3 = entity.cp() * f4;
                } else {
                    f3 = entity.aS;
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
                    boolean flag = entity.isSneaking() && entity instanceof EntityHuman;
                    if (flag && entity.motY < 0.0D) {
                        entity.motY = 0.0D;
                    }
                }
                entity.move(entity.motX, entity.motY, entity.motZ);
                if (entity.positionChanged && entity.m_()) {
                    entity.motY = 0.2D;
                }
                if (entity.hasEffect(MobEffects.LEVITATION)) {
                    entity.motY += (0.05D * (entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - entity.motY)
                            * 0.2D;
                } else {
                    blockposition_pooledblockposition.e(entity.locX, 0.0D, entity.locZ);
                    if (entity.world.isClientSide && (!entity.world.isLoaded(blockposition_pooledblockposition)
                            || !entity.world.getChunkAtWorldCoords(blockposition_pooledblockposition).p())) {
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
        entity.aG = entity.aH;
        double d1 = entity.locX - entity.lastX;
        double d0 = entity.locZ - entity.lastZ;
        float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;
        if (f2 > 1.0F) {
            f2 = 1.0F;
        }
        entity.aH += (f2 - entity.aH) * 0.4F;
        entity.aI += entity.aH;
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
            return (DataWatcherObject<Integer>) RABBIT_FIELD.get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SoundEffect getSoundEffect(NPC npc, SoundEffect snd, NPC.Metadata meta) {
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
        int data = npc.data().get(NPC.Metadata.MINECART_ITEM_DATA, 0);
        int offset = npc.data().get(NPC.Metadata.MINECART_OFFSET, 0);
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

    public static void setLookControl(EntityInsentient mob, ControllerLook control) {
        try {
            LOOK_CONTROL_SETTER.invoke(mob, control);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setSize(Entity entity, float f, float f1, boolean justCreated) {
        if (f != entity.width || f1 != entity.length) {
            float f2 = entity.width;
            entity.width = f;
            entity.length = f1;
            entity.a(new AxisAlignedBB(entity.getBoundingBox().a, entity.getBoundingBox().b, entity.getBoundingBox().c,
                    entity.getBoundingBox().a + entity.width, entity.getBoundingBox().b + entity.length,
                    entity.getBoundingBox().c + entity.width));
            if (entity.width > f2 && !justCreated && !entity.world.isClientSide) {
                entity.move((f2 - entity.width) / 2, 0.0D, (f2 - entity.width) / 2);
            }
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
        navigation.l();
    }

    private static final Set<EntityType> BAD_CONTROLLER_LOOK = EnumSet.of(EntityType.POLAR_BEAR, EntityType.SILVERFISH,
            EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.BAT, EntityType.SLIME, EntityType.MAGMA_CUBE,
            EntityType.HORSE, EntityType.GHAST);

    private static final Field CRAFT_BOSSBAR_HANDLE_FIELD = NMS.getField(CraftBossBar.class, "handle");

    private static final float DEFAULT_SPEED = 1F;

    private static final Field ENDERDRAGON_BATTLE_BAR_FIELD = NMS.getField(EnderDragonBattle.class, "c");

    private static final Field ENDERDRAGON_BATTLE_FIELD = NMS.getField(EntityEnderDragon.class, "bK");
    public static MethodHandle ENDERDRAGON_CHECK_WALLS = NMS.getFirstMethodHandleWithReturnType(EntityEnderDragon.class,
            true, boolean.class, AxisAlignedBB.class);
    private static DataWatcherObject<Boolean> ENDERMAN_ANGRY;
    private static Map<Class<?>, Integer> ENTITY_CLASS_TO_INT;
    private static Map<Class<?>, String> ENTITY_CLASS_TO_NAME;
    private static final MethodHandle ENTITY_TRACKER_ENTRY_X = NMS.getGetter(EntityTrackerEntry.class, "xLoc");
    private static final MethodHandle ENTITY_TRACKER_ENTRY_Y = NMS.getGetter(EntityTrackerEntry.class, "yLoc");
    private static final MethodHandle ENTITY_TRACKER_ENTRY_Z = NMS.getGetter(EntityTrackerEntry.class, "zLoc");
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
    public static Field GOAL_FIELD = NMS.getField(PathfinderGoalSelector.class, "b");
    private static final Field JUMP_FIELD = NMS.getField(EntityLiving.class, "be");
    private static final MethodHandle LOOK_CONTROL_SETTER = NMS.getFirstSetter(EntityInsentient.class,
            ControllerLook.class);
    private static Method MAKE_REQUEST;
    private static MethodHandle MOVE_CONTROLLER_MOVING = NMS.getSetter(ControllerMove.class, "h");
    private static Field NAVIGATION_WORLD_FIELD = NMS.getField(NavigationAbstract.class, "b");
    public static Field NETWORK_ADDRESS = NMS.getField(NetworkManager.class, "l");
    public static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static Field PATHFINDING_RANGE = NMS.getField(NavigationAbstract.class, "f");
    private static final Field RABBIT_FIELD = NMS.getField(EntityRabbit.class, "bx");
    private static final Random RANDOM = Util.getFastRandom();
    private static final MethodHandle REPAIR_INVENTORY = NMS.getGetter(ContainerAnvil.class, "h");
    private static final MethodHandle RESULT_INVENTORY = NMS.getGetter(ContainerAnvil.class, "g");
    private static Field SKULL_PROFILE_FIELD;
    private static MethodHandle TEAM_FIELD;
    private static Field TRACKED_ENTITY_SET = NMS.getField(EntityTracker.class, "c");
    private static final Field WITHER_BOSS_BAR_FIELD = NMS.getField(EntityWither.class, "bG");
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
        try {
            ENDERMAN_ANGRY = (DataWatcherObject<Boolean>) NMS.getField(EntityEnderman.class, "by").get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
