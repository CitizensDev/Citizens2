package net.citizensnpcs.nms.v1_20_R3.entity;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
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
import net.citizensnpcs.nms.v1_20_R3.network.EmptyConnection;
import net.citizensnpcs.nms.v1_20_R3.network.EmptyPacketListener;
import net.citizensnpcs.nms.v1_20_R3.util.EmptyAdvancementDataPlayer;
import net.citizensnpcs.nms.v1_20_R3.util.EmptyServerStatsCounter;
import net.citizensnpcs.nms.v1_20_R3.util.MobAI;
import net.citizensnpcs.nms.v1_20_R3.util.MobAI.ForwardingMobAI;
import net.citizensnpcs.nms.v1_20_R3.util.NMSImpl;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityHumanNPC extends ServerPlayer implements NPCHolder, SkinnableEntity, ForwardingMobAI {
    private MobAI ai;
    private int jumpTicks = 0;
    private final CitizensNPC npc;
    private boolean setBukkitEntity;
    private final SkinPacketTracker skinTracker;
    private EmptyServerStatsCounter statsCache;

    public EntityHumanNPC(MinecraftServer minecraftServer, ServerLevel world, GameProfile gameProfile,
            ClientInformation ci, NPC npc) {
        super(minecraftServer, world, gameProfile, ci);
        this.npc = (CitizensNPC) npc;
        if (npc != null) {
            ai = new BasicMobAI(this);
            skinTracker = new SkinPacketTracker(this);
            try {
                GAMEMODE_SETTING.invoke(gameMode, GameType.SURVIVAL, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            initialise(minecraftServer, ci);
        } else {
            skinTracker = null;
        }
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
        Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), () -> {
            ((ServerLevel) level()).removePlayerImmediately(EntityHumanNPC.this, RemovalReason.KILLED);
            ((ServerLevel) level()).getChunkSource().removeEntity(EntityHumanNPC.this);
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
                && Util.isLoaded(getBukkitEntity().getLocation())
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
        tickAI();
        detectEquipmentUpdatesPublic();
        noPhysics = isSpectator();
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
            for (Entity entity : level().getEntities(this, axisalignedbb)) {
                entity.playerTouch(this);
            }
        }
    }

    @Override
    public MobAI getAI() {
        return ai;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && !setBukkitEntity) {
            NMSImpl.setBukkitEntity(this, new PlayerNPC(this));
            setBukkitEntity = true;
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
            return MutableComponent.create(new LiteralContents(""));
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
            Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), () -> EntityHumanNPC.this.hurtMarked = true);
        }
        return damaged;
    }

    private void initialise(MinecraftServer minecraftServer, ClientInformation clientInfo) {
        try {
            EmptyConnection conn = new EmptyConnection(PacketFlow.CLIENTBOUND);
            connection = new EmptyPacketListener(minecraftServer, conn, this,
                    new CommonListenerCookie(getProfile(), 0, clientInfo));
        } catch (IOException e) {
            e.printStackTrace();
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
        return npc == null ? super.isPushable() : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
    }

    @Override
    public void knockback(double strength, double dx, double dz) {
        NMS.callKnockbackEvent(npc, (float) strength, dx, dz, evt -> super.knockback((float) evt.getStrength(),
                evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
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
        NMS.setHeadAndBodyYaw(getBukkitEntity(), getYRot());
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    private void moveWithFallDamage(Vec3 vec) {
        double x = getX();
        double y = getY();
        double z = getZ();
        travel(vec);
        if (!npc.isProtected()) {
            doCheckFallDamage(getX() - x, getY() - y, getZ() - z, onGround);
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
        getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, flags);
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

    @Override
    public void tick() {
        super.tick();
        if (npc == null)
            return;

        Bukkit.getServer().getPluginManager().unsubscribeFromPermission("bukkit.broadcast.user", getBukkitEntity());
        updatePackets(npc.getNavigator().isNavigating());
        npc.update();
    }

    @Override
    public void tickAI() {
        ai.getMoveControl().tick();
        ai.getJumpControl().tick();
    }

    @Override
    public void travel(Vec3 vec3d) {
        if (npc == null || !npc.isFlyable()) {
            super.travel(vec3d);
        } else {
            NMSImpl.flyingMoveLogic(this, vec3d);
        }
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
        Vec3 old = getDeltaMovement().add(0, 0, 0);
        boolean res = super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
        if (!npc.isPushableByFluids()) {
            setDeltaMovement(old);
        }
        return res;
    }

    private void updatePackets(boolean navigating) {
        if (!npc.isUpdating(NPCUpdate.PACKET))
            return;

        effectsDirty = true;
    }

    public static class PlayerNPC extends CraftPlayer implements NPCHolder, SkinnableEntity {
        private final CitizensNPC npc;

        private PlayerNPC(EntityHumanNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
            npc.getOrAddTrait(Inventory.class);
        }

        @Override
        public boolean canSee(org.bukkit.entity.Entity entity) {
            if (entity != null && entity.getType().name().contains("ITEM_FRAME"))
                return false; // optimise for large maps in item frames
            return super.canSee(entity);
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
            return ((CraftServer) Bukkit.getServer()).getEntityMetadata().getMetadata(this, metadataKey);
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
            return ((CraftServer) Bukkit.getServer()).getEntityMetadata().hasMetadata(this, metadataKey);
        }

        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {
            ((CraftServer) Bukkit.getServer()).getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
        }

        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
            ((CraftServer) Bukkit.getServer()).getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
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
}
