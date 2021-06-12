package net.citizensnpcs.nms.v1_17_R1.util;

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
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftSound;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftWither;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
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
import net.citizensnpcs.nms.v1_17_R1.entity.AxolotlController;
import net.citizensnpcs.nms.v1_17_R1.entity.BatController;
import net.citizensnpcs.nms.v1_17_R1.entity.BeeController;
import net.citizensnpcs.nms.v1_17_R1.entity.BlazeController;
import net.citizensnpcs.nms.v1_17_R1.entity.CatController;
import net.citizensnpcs.nms.v1_17_R1.entity.CaveSpiderController;
import net.citizensnpcs.nms.v1_17_R1.entity.ChickenController;
import net.citizensnpcs.nms.v1_17_R1.entity.CodController;
import net.citizensnpcs.nms.v1_17_R1.entity.CowController;
import net.citizensnpcs.nms.v1_17_R1.entity.CreeperController;
import net.citizensnpcs.nms.v1_17_R1.entity.DolphinController;
import net.citizensnpcs.nms.v1_17_R1.entity.DrownedController;
import net.citizensnpcs.nms.v1_17_R1.entity.EnderDragonController;
import net.citizensnpcs.nms.v1_17_R1.entity.EndermanController;
import net.citizensnpcs.nms.v1_17_R1.entity.EndermiteController;
import net.citizensnpcs.nms.v1_17_R1.entity.EntityHumanNPC;
import net.citizensnpcs.nms.v1_17_R1.entity.EvokerController;
import net.citizensnpcs.nms.v1_17_R1.entity.FoxController;
import net.citizensnpcs.nms.v1_17_R1.entity.GhastController;
import net.citizensnpcs.nms.v1_17_R1.entity.GiantController;
import net.citizensnpcs.nms.v1_17_R1.entity.GlowSquidController;
import net.citizensnpcs.nms.v1_17_R1.entity.GoatController;
import net.citizensnpcs.nms.v1_17_R1.entity.GuardianController;
import net.citizensnpcs.nms.v1_17_R1.entity.GuardianElderController;
import net.citizensnpcs.nms.v1_17_R1.entity.HoglinController;
import net.citizensnpcs.nms.v1_17_R1.entity.HorseController;
import net.citizensnpcs.nms.v1_17_R1.entity.HorseDonkeyController;
import net.citizensnpcs.nms.v1_17_R1.entity.HorseMuleController;
import net.citizensnpcs.nms.v1_17_R1.entity.HorseSkeletonController;
import net.citizensnpcs.nms.v1_17_R1.entity.HorseZombieController;
import net.citizensnpcs.nms.v1_17_R1.entity.HumanController;
import net.citizensnpcs.nms.v1_17_R1.entity.IllusionerController;
import net.citizensnpcs.nms.v1_17_R1.entity.IronGolemController;
import net.citizensnpcs.nms.v1_17_R1.entity.LlamaController;
import net.citizensnpcs.nms.v1_17_R1.entity.MagmaCubeController;
import net.citizensnpcs.nms.v1_17_R1.entity.MushroomCowController;
import net.citizensnpcs.nms.v1_17_R1.entity.OcelotController;
import net.citizensnpcs.nms.v1_17_R1.entity.PandaController;
import net.citizensnpcs.nms.v1_17_R1.entity.ParrotController;
import net.citizensnpcs.nms.v1_17_R1.entity.PhantomController;
import net.citizensnpcs.nms.v1_17_R1.entity.PigController;
import net.citizensnpcs.nms.v1_17_R1.entity.PigZombieController;
import net.citizensnpcs.nms.v1_17_R1.entity.PiglinBruteController;
import net.citizensnpcs.nms.v1_17_R1.entity.PiglinController;
import net.citizensnpcs.nms.v1_17_R1.entity.PillagerController;
import net.citizensnpcs.nms.v1_17_R1.entity.PolarBearController;
import net.citizensnpcs.nms.v1_17_R1.entity.PufferFishController;
import net.citizensnpcs.nms.v1_17_R1.entity.RabbitController;
import net.citizensnpcs.nms.v1_17_R1.entity.RavagerController;
import net.citizensnpcs.nms.v1_17_R1.entity.SalmonController;
import net.citizensnpcs.nms.v1_17_R1.entity.SheepController;
import net.citizensnpcs.nms.v1_17_R1.entity.ShulkerController;
import net.citizensnpcs.nms.v1_17_R1.entity.SilverfishController;
import net.citizensnpcs.nms.v1_17_R1.entity.SkeletonController;
import net.citizensnpcs.nms.v1_17_R1.entity.SkeletonStrayController;
import net.citizensnpcs.nms.v1_17_R1.entity.SkeletonWitherController;
import net.citizensnpcs.nms.v1_17_R1.entity.SlimeController;
import net.citizensnpcs.nms.v1_17_R1.entity.SnowmanController;
import net.citizensnpcs.nms.v1_17_R1.entity.SpiderController;
import net.citizensnpcs.nms.v1_17_R1.entity.SquidController;
import net.citizensnpcs.nms.v1_17_R1.entity.StriderController;
import net.citizensnpcs.nms.v1_17_R1.entity.TraderLlamaController;
import net.citizensnpcs.nms.v1_17_R1.entity.TropicalFishController;
import net.citizensnpcs.nms.v1_17_R1.entity.TurtleController;
import net.citizensnpcs.nms.v1_17_R1.entity.VexController;
import net.citizensnpcs.nms.v1_17_R1.entity.VillagerController;
import net.citizensnpcs.nms.v1_17_R1.entity.VindicatorController;
import net.citizensnpcs.nms.v1_17_R1.entity.WanderingTraderController;
import net.citizensnpcs.nms.v1_17_R1.entity.WitchController;
import net.citizensnpcs.nms.v1_17_R1.entity.WitherController;
import net.citizensnpcs.nms.v1_17_R1.entity.WolfController;
import net.citizensnpcs.nms.v1_17_R1.entity.ZoglinController;
import net.citizensnpcs.nms.v1_17_R1.entity.ZombieController;
import net.citizensnpcs.nms.v1_17_R1.entity.ZombieHuskController;
import net.citizensnpcs.nms.v1_17_R1.entity.ZombieVillagerController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.AreaEffectCloudController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.ArmorStandController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.BoatController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.DragonFireballController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.EggController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.EnderPearlController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.EnderSignalController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.EvokerFangsController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.FallingBlockController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.FireworkController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.FishingHookController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.GlowItemFrameController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.ItemController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.ItemFrameController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.LargeFireballController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.LeashController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.LlamaSpitController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.MarkerController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.MinecartChestController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.PaintingController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.ShulkerBulletController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.SmallFireballController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.SnowballController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.SpectralArrowController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.ThrownPotionController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.ThrownTridentController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.TippedArrowController;
import net.citizensnpcs.nms.v1_17_R1.entity.nonliving.WitherSkullController;
import net.citizensnpcs.nms.v1_17_R1.network.EmptyChannel;
import net.citizensnpcs.nms.v1_17_R1.trait.Commands;
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
import net.citizensnpcs.trait.versioned.PolarBearTrait;
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
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;

@SuppressWarnings("unchecked")
public class NMSImpl implements NMSBridge {
    public NMSImpl() {
        loadEntityTypes();
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
        boolean success = getHandle(entity).level.addEntity(getHandle(entity), custom);
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
        if (remove) {
            handle.level.players().remove(handle);
        } else if (!handle.level.players().contains(handle)) {
            ((List) handle.level.players()).add(handle);
        }
        // PlayerUpdateTask.addOrRemove(entity, remove);
    }

    @Override
    public void attack(org.bukkit.entity.LivingEntity attacker, org.bukkit.entity.LivingEntity btarget) {
        LivingEntity handle = getHandle(attacker);
        LivingEntity target = getHandle(btarget);
        if (handle instanceof ServerPlayer) {
            ((ServerPlayer) handle).attack(target);
            PlayerAnimation.ARM_SWING.play((Player) handle.getBukkitEntity());
            return;
        }
        if (handle instanceof Mob) {
            ((Mob) handle).doHurtTarget(target);
            return;
        }
        AttributeInstance attackDamage = handle.getAttribute(Attributes.ATTACK_DAMAGE);
        float f = (float) (attackDamage == null ? 1 : attackDamage.getValue());
        int i = 0;

        f += EnchantmentHelper.getDamageBonus(handle.getMainHandItem(), target.getMobType());
        i += EnchantmentHelper.getKnockbackBonus(handle);

        boolean flag = target.hurt(DamageSource.mobAttack(handle), f);

        if (!flag)
            return;

        if (i > 0) {
            target.knockback(-Math.sin(handle.getYRot() * Math.PI / 180.0F) * i * 0.5F, 0.1D,
                    Math.cos(handle.getYRot() * Math.PI / 180.0F) * i * 0.5F);
            handle.setDeltaMovement(handle.getDeltaMovement().multiply(0.6, 1, 0.6));
        }

        int fireAspectLevel = EnchantmentHelper.getFireAspect(handle);
        if (fireAspectLevel > 0) {
            target.setOnFire(fireAspectLevel * 4, false);
        }

        if (target instanceof ServerPlayer) {
            ServerPlayer entityhuman = (ServerPlayer) target;
            ItemStack itemstack = handle.getMainHandItem();
            ItemStack itemstack1 = entityhuman.isUsingItem() ? entityhuman.getUseItem() : ItemStack.EMPTY;
            if (!itemstack.isEmpty() && !itemstack1.isEmpty()
                    && itemstack.getItem() instanceof net.minecraft.world.item.AxeItem && itemstack1.is(Items.SHIELD)) {
                float f2 = 0.25F + EnchantmentHelper.getBlockEfficiency(handle) * 0.05F;
                if (new Random().nextFloat() < f2) {
                    entityhuman.getCooldowns().addCooldown(Items.SHIELD, 100);
                    handle.level.broadcastEntityEvent(entityhuman, (byte) 30);
                }
            }
        }

        EnchantmentHelper.doPostHurtEffects(handle, target);
        EnchantmentHelper.doPostDamageEffects(target, handle);
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Throwable {
        if (Bukkit.isPrimaryThread())
            throw new IllegalStateException("NMS.fillProfileProperties cannot be invoked from the main thread.");

        MinecraftSessionService sessionService = ((CraftServer) Bukkit.getServer()).getServer().getSessionService();
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
        ServerBossEvent bserver = null;
        try {
            if (entity.getType() == EntityType.WITHER) {
                bserver = ((WitherBoss) NMSImpl.getHandle(entity)).bossEvent;
            } else if (entity.getType() == EntityType.ENDER_DRAGON) {
                bserver = ((EnderDragon) NMSImpl.getHandle(entity)).getDragonFight().dragonEvent;
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
        ServerLevel world = ((CraftWorld) block.getWorld()).getHandle();
        VoxelShape shape = ((CraftBlock) block).getNMS().getCollisionShape(world, ((CraftBlock) block).getPosition());
        AABB aabb = shape.isEmpty()
                ? ((CraftBlock) block).getNMS().getShape(world, ((CraftBlock) block).getPosition()).bounds()
                : shape.bounds();
        return aabb == null ? BoundingBox.convert(block.getBoundingBox()) : NMSBoundingBox.wrap(aabb);
    }

    private float getDragonYaw(Entity handle, double tX, double tZ) {
        if (handle.getZ() > tZ)
            return (float) (-Math.toDegrees(Math.atan((handle.getX() - tX) / (handle.getZ() - tZ))));
        if (handle.getZ() < tZ) {
            return (float) (-Math.toDegrees(Math.atan((handle.getX() - tX) / (handle.getZ() - tZ)))) + 180.0F;
        }
        return handle.getYRot();
    }

    @Override
    public GameProfileRepository getGameProfileRepository() {
        return ((CraftServer) Bukkit.getServer()).getServer().getProfileRepository();
    }

    @Override
    public float getHeadYaw(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity)) {
            return entity.getLocation().getYaw();
        }
        return getHandle((org.bukkit.entity.LivingEntity) entity).getYHeadRot();
    }

    @Override
    public double getHeight(org.bukkit.entity.Entity entity) {
        return entity.getHeight();
    }

    @Override
    public float getHorizontalMovement(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive())
            return Float.NaN;
        LivingEntity handle = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) entity);
        return handle.zza;
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
            Sound sound = Sound.valueOf(flag.toUpperCase());
            if (CRAFTSOUND_GETSOUND != null) {
                String ret = (String) CRAFTSOUND_GETSOUND.invoke(sound);
                if (ret == null)
                    throw new CommandException(Messages.INVALID_SOUND);
                return ret;
            } else {
                SoundEvent effect = CraftSound.getSoundEffect(sound);
                if (effect == null)
                    throw new CommandException(Messages.INVALID_SOUND);
                ResourceLocation key = (ResourceLocation) SOUNDEFFECT_LOCATION.invoke(effect);
                return key.getPath();
            }
        } catch (Throwable e) {
            throw new CommandException(Messages.INVALID_SOUND);
        }
    }

    @Override
    public float getSpeedFor(NPC npc) {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof org.bukkit.entity.LivingEntity))
            return DEFAULT_SPEED;
        LivingEntity handle = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) npc.getEntity());
        if (handle == null)
            return DEFAULT_SPEED;
        return DEFAULT_SPEED;
        // return (float)
        // handle.getAttribute(Attributes.d).getValue();
    }

    @Override
    public float getStepHeight(org.bukkit.entity.Entity entity) {
        return NMSImpl.getHandle(entity).maxUpStep;
    }

    @Override
    public MCNavigator getTargetNavigator(org.bukkit.entity.Entity entity, Iterable<Vector> dest,
            final NavigatorParameters params) {
        List<Node> list = Lists.<Node> newArrayList(Iterables.<Vector, Node> transform(dest, (input) -> {
            return new Node(input.getBlockX(), input.getBlockY(), input.getBlockZ());
        }));
        Node last = list.size() > 0 ? list.get(list.size() - 1) : null;
        final Path path = new Path(list, last != null ? new BlockPos(last.x, last.y, last.z) : null, true);
        return getTargetNavigator(entity, params, new Function<PathNavigation, Boolean>() {
            @Override
            public Boolean apply(PathNavigation input) {
                return input.moveTo(path, params.speed());
            }
        });
    }

    @Override
    public MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final Location dest,
            final NavigatorParameters params) {
        return getTargetNavigator(entity, params, new Function<PathNavigation, Boolean>() {
            @Override
            public Boolean apply(PathNavigation input) {
                return input.moveTo(dest.getX(), dest.getY(), dest.getZ(), params.speed());
            }
        });
    }

    private MCNavigator getTargetNavigator(final org.bukkit.entity.Entity entity, final NavigatorParameters params,
            final Function<PathNavigation, Boolean> function) {
        net.minecraft.world.entity.Entity raw = getHandle(entity);
        raw.setOnGround(true);
        // not sure of a better way around this - if onGround is false, then
        // navigation won't execute, and calling entity.move doesn't
        // entirely fix the problem.
        final PathNavigation navigation = NMSImpl.getNavigation(entity);
        final float oldWater = raw instanceof ServerPlayer
                ? ((EntityHumanNPC) raw).getPathfindingMalus(BlockPathTypes.WATER)
                : ((Mob) raw).getPathfindingMalus(BlockPathTypes.WATER);
        if (params.avoidWater() && oldWater >= 0) {
            if (raw instanceof ServerPlayer) {
                ((EntityHumanNPC) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater + 1F);
            } else {
                ((Mob) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater + 1F);
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
                if (params.debug() && navigation.getPath() != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (int i = 0; i < navigation.getPath().getNodeCount(); i++) {
                            Node pp = navigation.getPath().getNode(i);
                            org.bukkit.block.Block block = new Vector(pp.x, pp.y, pp.z).toLocation(player.getWorld())
                                    .getBlock();
                            player.sendBlockChange(block.getLocation(), block.getBlockData());
                        }
                    }
                }
                if (oldWater >= 0) {
                    if (raw instanceof ServerPlayer) {
                        ((EntityHumanNPC) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater);
                    } else {
                        ((Mob) raw).setPathfindingMalus(BlockPathTypes.WATER, oldWater);
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
                if (params.debug() && !NMSImpl.isNavigationFinished(navigation)) {
                    BlockData data = Material.DANDELION.createBlockData();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (int i = 0; i < navigation.getPath().getNodeCount(); i++) {
                            Node pp = navigation.getPath().getNode(i);
                            player.sendBlockChange(new Vector(pp.x, pp.y, pp.z).toLocation(player.getWorld()), data);
                        }
                    }
                }
                navigation.setSpeedModifier(params.speed());
                return NMSImpl.isNavigationFinished(navigation);
            }
        };
    }

    @Override
    public TargetNavigator getTargetNavigator(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity target,
            NavigatorParameters parameters) {
        PathNavigation navigation = getNavigation(entity);
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
        LivingEntity handle = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) entity);
        return handle.xxa;
    }

    @Override
    public double getWidth(org.bukkit.entity.Entity entity) {
        return entity.getWidth();
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
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BeeTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BossBarTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CatTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FoxTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(LlamaTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MushroomCowTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ParrotTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PandaTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PhantomTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PolarBearTrait.class));
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
        EntityControllers.setEntityControllerForType(EntityType.SPLASH_POTION, ThrownPotionController.class);
        EntityControllers.setEntityControllerForType(EntityType.MARKER, MarkerController.class);
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
        EntityControllers.setEntityControllerForType(EntityType.GLOW_SQUID, GlowSquidController.class);
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
        handle.setYRot(yaw);
        setHeadYaw(entity, yaw);
        handle.setXRot(pitch);
    }

    @Override
    public void look(org.bukkit.entity.Entity entity, Location to, boolean headOnly, boolean immediate) {
        Entity handle = NMSImpl.getHandle(entity);
        if (immediate || headOnly || BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())
                || (!(handle instanceof Mob) && !(handle instanceof EntityHumanNPC))) {
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
        if (handle instanceof Mob) {
            ((Mob) handle).getLookControl().setLookAt(to.getX(), to.getY(), to.getZ(), ((Mob) handle).getHeadRotSpeed(),
                    ((Mob) handle).getMaxHeadXRot());

            while (((LivingEntity) handle).yHeadRot >= 180F) {
                ((LivingEntity) handle).yHeadRot -= 360F;
            }
            while (((LivingEntity) handle).yHeadRot < -180F) {
                ((LivingEntity) handle).yHeadRot += 360F;
            }
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setTargetLook(to);
        }
    }

    @Override
    public void look(org.bukkit.entity.Entity from, org.bukkit.entity.Entity to) {
        Entity handle = NMSImpl.getHandle(from), target = NMSImpl.getHandle(to);
        if (BAD_CONTROLLER_LOOK.contains(handle.getBukkitEntity().getType())) {
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
    public void playAnimation(PlayerAnimation animation, Player player, int radius) {
        PlayerAnimationImpl.play(animation, player, radius);
    }

    @Override
    public void playerTick(Player entity) {
        ((ServerPlayer) getHandle(entity)).doTick();
    }

    @Override
    public void registerEntityClass(Class<?> clazz) {
        if (ENTITY_REGISTRY == null)
            return;

        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            net.minecraft.world.entity.EntityType<?> type = ENTITY_REGISTRY.findType(search);
            ResourceLocation key = ENTITY_REGISTRY.getKey(type);
            if (key == null || type == null)
                continue;
            CITIZENS_ENTITY_TYPES.put(clazz, type);
            int code = ENTITY_REGISTRY.getId(type);
            ENTITY_REGISTRY.put(code, key, type);
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass for class " + clazz.toString());
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
        ((ServerLevel) nmsEntity.level).getChunkProvider().removeEntity(nmsEntity);
    }

    @Override
    public void removeHookIfNecessary(NPCRegistry npcRegistry, FishHook entity) {
        if (FISHING_HOOK_HOOKED == null)
            return;
        FishingHook hook = (FishingHook) NMSImpl.getHandle(entity);
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
            hook.setRemoved(RemovalReason.KILLED);
        }
    }

    @Override
    public void replaceTrackerEntry(Player player) {
        ServerLevel server = (ServerLevel) NMSImpl.getHandle(player).level;

        TrackedEntity entry = server.getChunkProvider().chunkMap.G.get(player.getEntityId());
        if (entry == null)
            return;
        PlayerlistTracker replace = new PlayerlistTracker(server.getChunkProvider().chunkMap, entry);
        server.getChunkProvider().chunkMap.G.put(player.getEntityId(), replace);
        if (getHandle(player) instanceof EntityHumanNPC) {
            ((EntityHumanNPC) getHandle(player)).setTracked(replace);
        }
    }

    @Override
    public void sendPositionUpdate(Player excluding, org.bukkit.entity.Entity from, Location storedLocation) {
        sendPacketNearby(excluding, storedLocation, new ClientboundTeleportEntityPacket(getHandle(from)));
    }

    @Override
    public void sendTabListAdd(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);

        ServerPlayer entity = ((CraftPlayer) listPlayer).getHandle();

        NMSImpl.sendPacket(recipient,
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, entity));
    }

    @Override
    public void sendTabListRemove(Player recipient, Collection<? extends SkinnableEntity> skinnableNPCs) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(skinnableNPCs);

        ServerPlayer[] entities = new ServerPlayer[skinnableNPCs.size()];
        int i = 0;
        for (SkinnableEntity skinnable : skinnableNPCs) {
            entities[i] = (ServerPlayer) skinnable;
            i++;
        }

        NMSImpl.sendPacket(recipient,
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, entities));
    }

    @Override
    public void sendTabListRemove(Player recipient, Player listPlayer) {
        Preconditions.checkNotNull(recipient);
        Preconditions.checkNotNull(listPlayer);

        ServerPlayer entity = ((CraftPlayer) listPlayer).getHandle();

        NMSImpl.sendPacket(recipient,
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, entity));
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
    public void setBodyYaw(org.bukkit.entity.Entity entity, float yaw) {
        getHandle(entity).setYBodyRot(yaw);
    }

    @Override
    public void setDestination(org.bukkit.entity.Entity entity, double x, double y, double z, float speed) {
        Entity handle = NMSImpl.getHandle(entity);
        if (handle == null)
            return;
        if (handle instanceof Mob) {
            ((Mob) handle).getMoveControl().setWantedPosition(x, y, z, speed);
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setMoveDestination(x, y, z, speed);
        }
    }

    @Override
    public void setEndermanAngry(org.bukkit.entity.Enderman enderman, boolean angry) {
        if (ENDERMAN_ANGRY == null)
            return;
        getHandle(enderman).getEntityData().set(ENDERMAN_ANGRY, angry);
    }

    @Override
    public void setHeadYaw(org.bukkit.entity.Entity entity, float yaw) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity))
            return;
        LivingEntity handle = (LivingEntity) getHandle(entity);
        yaw = Util.clampYaw(yaw);
        handle.yBodyRot = yaw;
        if (!(handle instanceof ServerPlayer)) {
            handle.yHeadRot = yaw; // TODO: why this
        }
        handle.setYHeadRot(yaw);
    }

    @Override
    public void setKnockbackResistance(org.bukkit.entity.LivingEntity entity, double d) {
        LivingEntity handle = NMSImpl.getHandle(entity);
        handle.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(d);
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
    public void setNoGravity(org.bukkit.entity.Entity entity, boolean enabled) {
        getHandle(entity).setNoGravity(enabled);
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
    public void setPolarBearRearing(org.bukkit.entity.Entity entity, boolean rearing) {
        ((PolarBear) getHandle(entity)).setStanding(rearing);
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
        if (handle instanceof Mob) {
            JumpControl controller = ((Mob) handle).getJumpControl();
            controller.jump();
        } else if (handle instanceof EntityHumanNPC) {
            ((EntityHumanNPC) handle).setShouldJump();
        }
    }

    @Override
    public void setSitting(org.bukkit.entity.Ocelot ocelot, boolean sitting) {
        Pose pose = sitting ? Pose.CROUCHING : Pose.STANDING;
        getHandle(ocelot).setPose(pose);
    }

    @Override
    public void setSitting(Tameable tameable, boolean sitting) {
        ((TamableAnimal) NMSImpl.getHandle(tameable)).setInSittingPose(sitting);
    }

    @Override
    public void setStepHeight(org.bukkit.entity.Entity entity, float height) {
        NMSImpl.getHandle(entity).maxUpStep = height;
    }

    @Override
    public void setTeamNameTagVisible(Team team, boolean visible) {
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, visible ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
    }

    @Override
    public void setVerticalMovement(org.bukkit.entity.Entity bukkitEntity, double d) {
        if (!bukkitEntity.getType().isAlive())
            return;
        LivingEntity handle = NMSImpl.getHandle((org.bukkit.entity.LivingEntity) bukkitEntity);
        handle.xxa = (float) d;
    }

    @Override
    public void setWitherCharged(Wither wither, boolean charged) {
        WitherBoss handle = ((CraftWither) wither).getHandle();
        handle.setInvulnerableTicks(charged ? 20 : 0);
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
            ENTITY_REGISTRY_SETTER.invoke(null, ENTITY_REGISTRY.getWrapped());
        } catch (Throwable e) {
        }
    }

    @Override
    public boolean tick(org.bukkit.entity.Entity next) {
        Entity entity = NMSImpl.getHandle(next);
        Entity entity1 = entity.getVehicle();
        if (entity1 != null) {
            if ((!entity1.isAlive()) || (!entity1.hasPassenger(entity))) {
                entity.stopRiding();
            }
        } else {
            if (entity.isAlive()) {
                try {
                    ((ServerLevel) entity.level).tickNonPassenger(entity);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking player");
                    CrashReportCategory crashreportsystemdetails = crashreport.addCategory("Player being ticked");
                    entity.fillCrashReportCategory(crashreportsystemdetails);
                    throw new ReportedException(crashreport);
                }
            }
            boolean removeFromPlayerList = ((NPCHolder) entity).getNPC().data().get("removefromplayerlist",
                    Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
            if (!entity.isAlive()) {
                ((ServerLevel) entity.level).getChunkProvider().removeEntity(entity);
                return true;
            } else if (!removeFromPlayerList) {
                if (!entity.level.players().contains(entity)) {
                    List list = entity.level.players();
                    list.add(entity);
                }
                return true;
            } else {
                entity.level.players().remove(entity);
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
            handle.setDeltaMovement(handle.getDeltaMovement().x, handle.getDeltaMovement().y + power,
                    handle.getDeltaMovement().z);
        }
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
        if (!(en instanceof Mob)) {
            if (en instanceof EntityHumanNPC) {
                ((EntityHumanNPC) en).updatePathfindingRange(pathfindingRange);
            }
            return;
        }
        if (NAVIGATION_S == null)
            return;
        PathNavigation navigation = ((Mob) en).getNavigation();
        AttributeInstance inst = en.getAttribute(Attributes.FOLLOW_RANGE);
        inst.setBaseValue(pathfindingRange);
        int mc = Mth.floor(en.getAttributeBaseValue(Attributes.FOLLOW_RANGE) * 16.0D);
        try {
            NAVIGATION_S.invoke(navigation, NAVIGATION_A.invoke(navigation, mc));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static class NavigationFieldWrapper implements TargetNavigator {
        private final PathNavigation navigation;
        private final NavigatorParameters parameters;
        private final org.bukkit.entity.Entity target;

        private NavigationFieldWrapper(PathNavigation navigation, org.bukkit.entity.Entity target,
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
            navigation.moveTo(location.getX(), location.getY(), location.getZ(), parameters.speed());
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
        private final PathNavigation navigation;

        public NavigationIterable(PathNavigation nav) {
            this.navigation = nav;
        }

        @Override
        public Iterator<Vector> iterator() {
            final int npoints = navigation.getPath() == null ? 0 : navigation.getPath().getNodeCount();
            return new Iterator<Vector>() {
                Node curr = npoints > 0 ? navigation.getPath().getNode(0) : null;
                int i = 0;

                @Override
                public boolean hasNext() {
                    return curr != null;
                }

                @Override
                public Vector next() {
                    Node old = curr;
                    curr = i + 1 < npoints ? navigation.getPath().getNode(++i) : null;
                    return new Vector(old.x, old.y, old.z);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public static void checkAndUpdateHeight(LivingEntity living, EntityDataAccessor<?> datawatcherobject) {
        EntityDimensions size;
        try {
            size = (EntityDimensions) SIZE_FIELD_GETTER.invoke(living);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        float oldw = size.width;
        float oldl = size.height;
        living.onSyncedDataUpdated(datawatcherobject);
        if (oldw != size.width || size.height != oldl) {
            living.setPos(living.getX() - 0.01, living.getY(), living.getZ() - 0.01);
            living.setPos(living.getX() + 0.01, living.getY(), living.getZ() + 0.01);
        }
    }

    public static void clearGoals(NPC npc, GoalSelector... goalSelectors) {
        if (GOAL_SET_FIELD == null || goalSelectors == null)
            return;
        int i = 0;
        for (GoalSelector selector : goalSelectors) {
            try {
                Collection<?> list = (Collection<?>) GOAL_SET_FIELD.invoke(selector);
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
            boolean flag = ((entity.getDeltaMovement()).y <= 0.0D);
            if (flag && entity.hasEffect(MobEffects.SLOW_FALLING)) {
                d0 = 0.01D;
                entity.fallDistance = 0.0F;
            }
            FluidState fluid = entity.level.getFluidState(entity.blockPosition());
            if (entity.isInWater() && !entity.canStandOnFluid(fluid.getType())) {
                double d1 = entity.getY();
                float f = entity.isSprinting() ? 0.9F : 0.8F;
                float f1 = 0.02F;
                float f2 = EnchantmentHelper.getDepthStrider(entity);
                if (f2 > 3.0F)
                    f2 = 3.0F;
                if (!entity.isOnGround())
                    f2 *= 0.5F;
                if (f2 > 0.0F) {
                    f += (0.54600006F - f) * f2 / 3.0F;
                    f1 += (entity.getSpeed() - f1) * f2 / 3.0F;
                }
                if (entity.hasEffect(MobEffects.DOLPHINS_GRACE))
                    f = 0.96F;
                entity.moveRelative(f1, vec3d);
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                Vec3 vec3d1 = entity.getDeltaMovement();
                if (entity.horizontalCollision && entity.onClimbable())
                    vec3d1 = new Vec3(vec3d1.x, 0.2D, vec3d1.z);
                entity.setDeltaMovement(vec3d1.multiply(f, 0.800000011920929D, f));
                Vec3 vec3d2 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
                entity.setDeltaMovement(vec3d2);
                if (entity.horizontalCollision
                        && entity.isFree(vec3d2.x, vec3d2.y + 0.6000000238418579D - entity.getY() + d1, vec3d2.z))
                    entity.setDeltaMovement(vec3d2.x, 0.30000001192092896D, vec3d2.z);
            } else if (entity.isInLava() && !entity.canStandOnFluid(fluid.getType())) {
                double d1 = entity.getY();
                entity.moveRelative(0.02F, vec3d);
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                if (entity.getFluidHeight(FluidTags.LAVA) <= entity.getFluidJumpThreshold()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, 0.800000011920929D, 0.5D));
                    Vec3 vec3 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
                    entity.setDeltaMovement(vec3);
                } else {
                    entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
                }
                if (!entity.isNoGravity())
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
                Vec3 vec3d3 = entity.getDeltaMovement();
                if (entity.horizontalCollision
                        && entity.isFree(vec3d3.x, vec3d3.y + 0.6000000238418579D - entity.getY() + d1, vec3d3.z))
                    entity.setDeltaMovement(vec3d3.x, 0.30000001192092896D, vec3d3.z);
            } else if (entity.isFallFlying()) {
                Vec3 vec3d4 = entity.getDeltaMovement();
                if (vec3d4.y > -0.5D)
                    entity.fallDistance = 1.0F;
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
                if (d2 > 0.0D)
                    vec3d4 = vec3d4.add((vec3d5.x / d2 * d3 - vec3d4.x) * 0.1D, 0.0D,
                            (vec3d5.z / d2 * d3 - vec3d4.z) * 0.1D);
                entity.setDeltaMovement(vec3d4.multiply(0.9900000095367432D, 0.9800000190734863D, 0.9900000095367432D));
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
                        && !CraftEventFactory.callToggleGlideEvent(entity, false).isCancelled())
                    entity.setSharedFlag(7, false);
            } else {
                BlockPos blockposition = new BlockPos(entity.getX(), (entity.getBoundingBox()).minY - 0.5000001D,
                        entity.getZ());
                float f5 = entity.level.getBlockState(blockposition).getBlock().getFriction();
                float f = entity.isOnGround() ? (f5 * 0.91F) : 0.91F;
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
                    entity.setDeltaMovement(vec3d6.x * f, d7 * 0.9800000190734863D, vec3d6.z * f);
                }
            }
        }
        entity.calculateEntityAnimation(entity, entity instanceof net.minecraft.world.entity.animal.FlyingAnimal);
    }

    public static TreeMap<?, ?> getBehaviorMap(LivingEntity entity) {
        try {
            return (TreeMap<?, ?>) BEHAVIOR_MAP.invoke(entity.getBrain());
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

    public static float getHeadYaw(LivingEntity handle) {
        return handle.getYHeadRot();
    }

    public static PathNavigation getNavigation(org.bukkit.entity.Entity entity) {
        Entity handle = getHandle(entity);
        return handle instanceof Mob ? ((Mob) handle).getNavigation()
                : handle instanceof EntityHumanNPC ? ((EntityHumanNPC) handle).getNavigation() : null;
    }

    public static EntityDataAccessor<Integer> getRabbitTypeField() {
        if (RABBIT_DATAWATCHER_FIELD == null)
            return null;
        try {
            return (EntityDataAccessor<Integer>) RABBIT_DATAWATCHER_FIELD.invoke();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EntityDimensions getSize(Entity entity) {
        try {
            return (EntityDimensions) SIZE_FIELD_GETTER.invoke(entity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SoundEvent getSoundEffect(NPC npc, SoundEvent snd, String meta) {
        return npc == null || !npc.data().has(meta) ? snd
                : Registry.SOUND_EVENT
                        .get(new ResourceLocation(npc.data().get(meta, snd == null ? "" : snd.toString())));
    }

    public static void initNetworkManager(Connection network) {
        network.channel = new EmptyChannel(null);
        SocketAddress socketAddress = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
        network.address = socketAddress;
    }

    public static boolean isNavigationFinished(PathNavigation navigation) {
        return navigation.isDone();
    }

    @SuppressWarnings("deprecation")
    public static void minecartItemLogic(AbstractMinecart minecart) {
        NPC npc = ((NPCHolder) minecart).getNPC();
        if (npc == null)
            return;
        Material mat = Material.getMaterial(npc.data().get(NPC.MINECART_ITEM_METADATA, ""));
        int data = npc.data().get(NPC.MINECART_ITEM_DATA_METADATA, 0); // TODO: migration for this
        int offset = npc.data().get(NPC.MINECART_OFFSET_METADATA, 0);
        minecart.setCustomDisplay(mat != null);
        if (mat != null) {
            minecart.setDisplayBlockState(Registry.BLOCK.byId(mat.getId()).defaultBlockState());
        }
        minecart.setDisplayOffset(offset);
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
        if (GOAL_SET_FIELD == null || goalSelectors == null)
            return;
        int i = 0;
        for (GoalSelector selector : goalSelectors) {
            try {
                Collection<Object> list = (Collection<Object>) GOAL_SET_FIELD.invoke(selector);
                list.clear();

                Collection<Object> old = npc.data().get("selector" + i);
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

    public static void setAdvancement(Player entity, PlayerAdvancements instance) {
        try {
            ADVANCEMENTS_PLAYER_FIELD.invoke(getHandle(entity), instance);
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

    public static void setDespawnDelay(TraderLlama llama, int ticks) {
        if (TRADER_DESPAWN_DELAY == null)
            return;
        try {
            TRADER_DESPAWN_DELAY.invoke(llama, ticks);
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void stopNavigation(PathNavigation navigation) {
        navigation.stop();
    }

    public static void updateAI(LivingEntity entity) {
        if (entity instanceof Mob) {
            Mob handle = (Mob) entity;
            handle.getSensing().tick();
            NMSImpl.updateNavigation(handle.getNavigation());
            handle.getMoveControl().tick();
            handle.getLookControl().tick();
            handle.getJumpControl().tick();
        } else if (entity instanceof EntityHumanNPC) {
            ((EntityHumanNPC) entity).updateAI();
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

    public static void updateNavigation(PathNavigation navigation) {
        navigation.tick();
    }

    private static final MethodHandle ADVANCEMENTS_PLAYER_FIELD = NMS.getFinalSetter(ServerPlayer.class, "cr");
    private static final Set<EntityType> BAD_CONTROLLER_LOOK = EnumSet.of(EntityType.POLAR_BEAR, EntityType.BEE,
            EntityType.SILVERFISH, EntityType.SHULKER, EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.BAT,
            EntityType.SLIME, EntityType.DOLPHIN, EntityType.MAGMA_CUBE, EntityType.HORSE, EntityType.GHAST,
            EntityType.SHULKER, EntityType.PHANTOM);
    private static final MethodHandle BEHAVIOR_MAP = NMS.getGetter(Brain.class, "f");
    private static final MethodHandle BUKKITENTITY_FIELD_SETTER = NMS.getSetter(Entity.class, "bukkitEntity");
    private static final Map<Class<?>, net.minecraft.world.entity.EntityType<?>> CITIZENS_ENTITY_TYPES = Maps
            .newHashMap();
    private static final MethodHandle CRAFT_BOSSBAR_HANDLE_FIELD = NMS.getSetter(CraftBossBar.class, "handle");
    private static MethodHandle CRAFTSOUND_GETSOUND = NMS.getMethodHandle(CraftSound.class, "getSound", false,
            Sound.class);
    private static final float DEFAULT_SPEED = 1F;
    private static EntityDataAccessor<Boolean> ENDERMAN_ANGRY = null;
    private static final MethodHandle ENTITY_FISH_NUM_IN_SCHOOL = NMS.getSetter(AbstractSchoolingFish.class, "c",
            false);
    private static final MethodHandle ENTITY_GET_SOUND_FALL = NMS.getMethodHandle(LivingEntity.class, "getSoundFall",
            true, int.class);
    private static CustomEntityRegistry ENTITY_REGISTRY;
    private static MethodHandle ENTITY_REGISTRY_SETTER;
    private static final MethodHandle FISHING_HOOK_HOOKED = NMS.getGetter(FishingHook.class, "av");
    private static final MethodHandle FISHING_HOOK_HOOKED_SETTER = NMS.getSetter(FishingHook.class, "av");
    private static final MethodHandle FISHING_HOOK_LIFE = NMS.getSetter(FishingHook.class, "ap");
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle GOAL_SET_FIELD = NMS.getGetter(GoalSelector.class, "d");
    private static final MethodHandle HEAD_HEIGHT = NMS.getSetter(Entity.class, "aX");
    private static final MethodHandle HEAD_HEIGHT_METHOD = NMS.getFirstMethodHandle(Entity.class, true, Pose.class,
            EntityDimensions.class);
    private static final MethodHandle JUMP_FIELD = NMS.getGetter(LivingEntity.class, "bn");
    private static final MethodHandle MAKE_REQUEST = NMS.getMethodHandle(YggdrasilAuthenticationService.class,
            "makeRequest", true, URL.class, Object.class, Class.class);
    private static final MethodHandle NAVIGATION_A = NMS.getMethodHandle(PathNavigation.class, "a", true, int.class);
    private static final MethodHandle NAVIGATION_S = NMS.getFinalSetter(PathNavigation.class, "t");
    private static final MethodHandle NAVIGATION_WORLD_FIELD = NMS.getSetter(PathNavigation.class, "b");
    public static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_GETTER = NMS.getGetter(ChunkMap.class, "J");
    private static final MethodHandle PLAYER_CHUNK_MAP_VIEW_DISTANCE_SETTER = NMS.getSetter(ChunkMap.class, "J");
    private static final MethodHandle PUFFERFISH_C = NMS.getSetter(Pufferfish.class, "bS");
    private static final MethodHandle PUFFERFISH_D = NMS.getSetter(Pufferfish.class, "bT");
    private static final MethodHandle RABBIT_DATAWATCHER_FIELD = NMS.getGetter(Rabbit.class, "ch");
    private static final Random RANDOM = Util.getFastRandom();
    private static final MethodHandle SIZE_FIELD_GETTER = NMS.getGetter(Entity.class, "aW");
    private static final MethodHandle SIZE_FIELD_SETTER = NMS.getSetter(Entity.class, "aW");
    private static Field SKULL_PROFILE_FIELD;
    private static MethodHandle SOUNDEFFECT_LOCATION = NMS.getGetter(SoundEvent.class, "b");
    private static MethodHandle TEAM_FIELD;
    private static final MethodHandle TRADER_DESPAWN_DELAY = NMS.getSetter(TraderLlama.class, "ci");
    public static final MethodHandle UUID_FIELD = NMS.getSetter(net.minecraft.world.entity.Entity.class, "aj");
    static {
        try {
            ENTITY_REGISTRY = new CustomEntityRegistry((DefaultedRegistry<net.minecraft.world.entity.EntityType<?>>) NMS
                    .getGetter(Registry.class, "Y").invoke());
            ENTITY_REGISTRY_SETTER = NMS.getFinalSetter(Registry.class, "Y");
            ENTITY_REGISTRY_SETTER.invoke(ENTITY_REGISTRY);
        } catch (Throwable e) {
            Messaging.logTr(Messages.ERROR_GETTING_ID_MAPPING, e.getMessage());
        }
        try {
            ENDERMAN_ANGRY = (EntityDataAccessor<Boolean>) NMS.getField(EnderMan.class, "bU").get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
