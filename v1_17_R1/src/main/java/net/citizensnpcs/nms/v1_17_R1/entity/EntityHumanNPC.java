package net.citizensnpcs.nms.v1_17_R1.entity;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.nms.v1_17_R1.network.EmptyNetHandler;
import net.citizensnpcs.nms.v1_17_R1.network.EmptyNetworkManager;
import net.citizensnpcs.nms.v1_17_R1.util.EmptyAdvancementDataPlayer;
import net.citizensnpcs.nms.v1_17_R1.util.EmptyServerStatsCounter;
import net.citizensnpcs.nms.v1_17_R1.util.MobAI;
import net.citizensnpcs.nms.v1_17_R1.util.MobAI.ForwardingMobAI;
import net.citizensnpcs.nms.v1_17_R1.util.NMSImpl;
import net.citizensnpcs.nms.v1_17_R1.util.PlayerlistTracker;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinPacketTracker;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityHumanNPC extends ServerPlayer implements NPCHolder, SkinnableEntity, ForwardingMobAI {
    private MobAI ai;
    private int jumpTicks = 0;
    private final CitizensNPC npc;
    private PlayerlistTracker playerlistTracker;
    private final SkinPacketTracker skinTracker;
    private EmptyServerStatsCounter statsCache;

    public EntityHumanNPC(MinecraftServer minecraftServer, ServerLevel world, GameProfile gameProfile, NPC npc) {
        super(minecraftServer, world, gameProfile);
        this.npc = (CitizensNPC) npc;
        if (npc != null) {
            skinTracker = new SkinPacketTracker(this);
            ai = new BasicMobAI(this);
            try {
                GAMEMODE_SETTING.invoke(gameMode, GameType.SURVIVAL, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            initialise(minecraftServer);
        } else {
            skinTracker = null;
        }
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer entityplayer) {
        if (npc != null && playerlistTracker == null)
            return false;
        return super.broadcastToPlayer(entityplayer);
    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
        if (npc == null || !npc.isFlyable())
            return super.causeFallDamage(f, f1, damagesource);
        return false;
    }

    @Override
    protected void checkFallDamage(double d0, boolean flag, BlockState iblockdata, BlockPos blockposition) {
        if (npc == null || !npc.isFlyable()) {
            super.checkFallDamage(d0, flag, iblockdata, blockposition);
        }
    }

    @Override
    public void die(DamageSource damagesource) {
        // players that die are not normally removed from the world. when the
        // NPC dies, we are done with the instance and it should be removed.
        if (dead)
            return;
        super.die(damagesource);
        Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), (Runnable) () -> {
            EntityHumanNPC.this.getLevel().removePlayerImmediately(EntityHumanNPC.this, RemovalReason.KILLED);
            ((ServerLevel) level).getChunkProvider().removeEntity(EntityHumanNPC.this);
        }, 15); // give enough time for death and smoke animation
    }

    @Override
    public void doTick() {
        if (npc == null) {
            super.doTick();
            return;
        }
        super.baseTick();
        boolean navigating = npc.getNavigator().isNavigating() || ai.getMoveControl().hasWanted();
        if (!navigating && getBukkitEntity() != null
                && (!npc.hasTrait(Gravity.class) || npc.getOrAddTrait(Gravity.class).hasGravity())
                && Util.isLoaded(getBukkitEntity().getLocation(LOADED_LOCATION))
                && (!npc.isProtected() || SpigotUtil.checkYSafe(getY(), getBukkitEntity().getWorld()))) {
            moveWithFallDamage(Vec3.ZERO);
        }
        Vec3 mot = getDeltaMovement();
        if (Math.abs(mot.x) < EPSILON && Math.abs(mot.y) < EPSILON && Math.abs(mot.z) < EPSILON) {
            setDeltaMovement(Vec3.ZERO);
        }
        if (navigating) {
            if (!ai.getNavigation().isDone()) {
                ai.getNavigation().tick();
            }
            moveOnCurrentHeading();
        }
        ai.getJumpControl().tick();
        ai.getMoveControl().tick();
        detectEquipmentUpdates();
        this.noPhysics = isSpectator();
        if (isSpectator()) {
            this.onGround = false;
        }
        pushEntities();

        if (npc.data().get(NPC.Metadata.PICKUP_ITEMS, false)) {
            AABB axisalignedbb;
            if (this.isPassenger() && !this.getVehicle().isRemoved()) {
                axisalignedbb = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0);
            } else {
                axisalignedbb = this.getBoundingBox().inflate(1.0, 0.5, 1.0);
            }
            for (Entity entity : this.level.getEntities(this, axisalignedbb)) {
                entity.playerTouch(this);
            }
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        if (playerlistTracker != null) {
            playerlistTracker.updateLastPlayer();
        }
        return super.getAddEntityPacket();
    }

    @Override
    public MobAI getAI() {
        return ai;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
            NMSImpl.setBukkitEntity(this, new PlayerNPC(this));
        }
        return super.getBukkitEntity();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NMSImpl.getSoundEffect(npc, super.getDeathSound(), NPC.Metadata.DEATH_SOUND);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damagesource) {
        return NMSImpl.getSoundEffect(npc, super.getHurtSound(damagesource), NPC.Metadata.HURT_SOUND);
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    @Override
    public GameProfile getProfile() {
        return super.getGameProfile();
    }

    @Override
    public String getSkinName() {
        String skinName = npc.getOrAddTrait(SkinTrait.class).getSkinName();
        if (skinName == null) {
            skinName = npc.getName();
        }
        return skinName.toLowerCase();
    }

    @Override
    public SkinPacketTracker getSkinTracker() {
        return skinTracker;
    }

    @Override
    public ServerStatsCounter getStats() {
        return this.statsCache == null ? statsCache = new EmptyServerStatsCounter() : statsCache;
    }

    @Override
    public Component getTabListDisplayName() {
        if (Setting.DISABLE_TABLIST.asBoolean())
            return new TextComponent("");
        return super.getTabListDisplayName();
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        // knock back velocity is cancelled and sent to client for handling when
        // the entity is a player. there is no client so make this happen
        // manually.
        boolean damaged = super.hurt(damagesource, f);
        if (damaged && hurtMarked) {
            hurtMarked = false;
            Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(),
                    (Runnable) () -> EntityHumanNPC.this.hurtMarked = true);
        }
        return damaged;
    }

    private void initialise(MinecraftServer minecraftServer) {
        try {
            EmptyNetworkManager conn = new EmptyNetworkManager(PacketFlow.CLIENTBOUND);
            connection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.setListener(connection);
        } catch (IOException e) {
            // swallow
        }
        this.invulnerableTime = 0;
        NMS.setStepHeight(getBukkitEntity(), 1); // the default (0) breaks step climbing
        setSkinFlags((byte) 0xFF);
        EmptyAdvancementDataPlayer.clear(this.getAdvancements());
        NMSImpl.setAdvancement(this.getBukkitEntity(),
                new EmptyAdvancementDataPlayer(minecraftServer.getFixerUpper(), minecraftServer.getPlayerList(),
                        minecraftServer.getAdvancements(), CitizensAPI.getDataFolder().getParentFile(), this));
    }

    @Override
    public boolean isInWall() {
        if (npc == null || noPhysics || isSleeping())
            return super.isInWall();
        return Util.inBlock(getBukkitEntity());
    }

    @Override
    public boolean isPushable() {
        return npc == null ? super.isPushable()
                : npc.data().has(NPC.Metadata.COLLIDABLE) ? npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE)
                        : !npc.isProtected();
    }

    private void moveOnCurrentHeading() {
        if (jumping) {
            if (onGround && jumpTicks == 0) {
                jumpFromGround();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        xxa *= 0.98F;
        zza *= 0.98F;
        moveWithFallDamage(new Vec3(this.xxa, this.yya, this.zza));
        NMS.setHeadYaw(getBukkitEntity(), getYRot());
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    private void moveWithFallDamage(Vec3 vec) {
        double y = getY();
        travel(vec);
        if (!npc.isProtected()) {
            doCheckFallDamage(getY() - y, onGround);
        }
    }

    @Override
    public boolean onClimbable() {
        if (npc == null || !npc.isFlyable())
            return super.onClimbable();
        else
            return false;
    }

    @Override
    public void push(double x, double y, double z) {
        Vector vector = Util.callPushEvent(npc, x, y, z);
        if (vector != null) {
            super.push(vector.getX(), vector.getY(), vector.getZ());
        }
    }

    @Override
    public void push(Entity entity) {
        // this method is called by both the entities involved - cancelling
        // it will not stop the NPC from moving.
        super.push(entity);
        if (npc != null) {
            Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        getAdvancements().save();
    }

    @Override
    public void setSkinFlags(byte flags) {
        this.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, flags);
    }

    @Override
    public void setSkinName(String name) {
        npc.getOrAddTrait(SkinTrait.class).setSkinName(name);
    }

    @Override
    public void setSkinName(String name, boolean forceUpdate) {
        npc.getOrAddTrait(SkinTrait.class).setSkinName(name, forceUpdate);
    }

    @Override
    public void setSkinPersistent(String skinName, String signature, String data) {
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(skinName, signature, data);
    }

    public void setTracked(PlayerlistTracker tracker) {
        this.playerlistTracker = tracker;
    }

    @Override
    public void tick() {
        super.tick();
        if (npc == null)
            return;
        noPhysics = isSpectator();
        Bukkit.getServer().getPluginManager().unsubscribeFromPermission("bukkit.broadcast.user", getBukkitEntity());
        updatePackets(npc.getNavigator().isNavigating());
        npc.update();
    }

    @Override
    public void travel(Vec3 vec3d) {
        if (npc == null || !npc.isFlyable()) {
            super.travel(vec3d);
        } else {
            NMSImpl.flyingMoveLogic(this, vec3d);
        }
    }

    private void updatePackets(boolean navigating) {
        if (!npc.isUpdating(NPCUpdate.PACKET))
            return;

        effectsDirty = true;
    }

    public static class PlayerNPC extends CraftPlayer implements NPCHolder, SkinnableEntity {
        private final CraftServer cserver;
        private final CitizensNPC npc;

        private PlayerNPC(EntityHumanNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
            this.cserver = (CraftServer) Bukkit.getServer();
            npc.getOrAddTrait(Inventory.class);
        }

        @Override
        public Player getBukkitEntity() {
            return this;
        }

        @Override
        public EntityHumanNPC getHandle() {
            return (EntityHumanNPC) this.entity;
        }

        @Override
        public List<MetadataValue> getMetadata(String metadataKey) {
            return cserver.getEntityMetadata().getMetadata(this, metadataKey);
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public String getSkinName() {
            return ((SkinnableEntity) this.entity).getSkinName();
        }

        @Override
        public SkinPacketTracker getSkinTracker() {
            return ((SkinnableEntity) this.entity).getSkinTracker();
        }

        @Override
        public boolean hasMetadata(String metadataKey) {
            return cserver.getEntityMetadata().hasMetadata(this, metadataKey);
        }

        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {
            cserver.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
        }

        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
            cserver.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
        }

        @Override
        public void setSkinFlags(byte flags) {
            ((SkinnableEntity) this.entity).setSkinFlags(flags);
        }

        @Override
        public void setSkinName(String name) {
            ((SkinnableEntity) this.entity).setSkinName(name);
        }

        @Override
        public void setSkinName(String skinName, boolean forceUpdate) {
            ((SkinnableEntity) this.entity).setSkinName(skinName, forceUpdate);
        }

        @Override
        public void setSkinPersistent(String skinName, String signature, String data) {
            ((SkinnableEntity) this.entity).setSkinPersistent(skinName, signature, data);
        }
    }

    private static final float EPSILON = 0.003F;
    private static final MethodHandle GAMEMODE_SETTING = NMS.getFirstMethodHandle(ServerPlayerGameMode.class, true,
            GameType.class, GameType.class);
    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}
