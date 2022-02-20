package net.citizensnpcs.nms.v1_18_R1.entity;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.nms.v1_18_R1.network.EmptyNetHandler;
import net.citizensnpcs.nms.v1_18_R1.network.EmptyNetworkManager;
import net.citizensnpcs.nms.v1_18_R1.network.EmptySocket;
import net.citizensnpcs.nms.v1_18_R1.util.EmptyAdvancementDataPlayer;
import net.citizensnpcs.nms.v1_18_R1.util.EmptyServerStatsCounter;
import net.citizensnpcs.nms.v1_18_R1.util.NMSImpl;
import net.citizensnpcs.nms.v1_18_R1.util.PlayerControllerJump;
import net.citizensnpcs.nms.v1_18_R1.util.PlayerLookControl;
import net.citizensnpcs.nms.v1_18_R1.util.PlayerMoveControl;
import net.citizensnpcs.nms.v1_18_R1.util.PlayerNavigation;
import net.citizensnpcs.nms.v1_18_R1.util.PlayerlistTracker;
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
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class EntityHumanNPC extends ServerPlayer implements NPCHolder, SkinnableEntity {
    private PlayerControllerJump controllerJump;
    private PlayerLookControl controllerLook;
    private PlayerMoveControl controllerMove;
    private final Map<EquipmentSlot, ItemStack> equipmentCache = Maps.newEnumMap(EquipmentSlot.class);
    private int jumpTicks = 0;
    private final Map<BlockPathTypes, Float> malus = Maps.newEnumMap(BlockPathTypes.class);
    private PlayerNavigation navigation;
    private final CitizensNPC npc;
    private final Location packetLocationCache = new Location(null, 0, 0, 0);
    private PlayerlistTracker playerlistTracker;
    private final SkinPacketTracker skinTracker;
    private EmptyServerStatsCounter statsCache;
    private int updateCounter = 0;

    public EntityHumanNPC(MinecraftServer minecraftServer, ServerLevel world, GameProfile gameProfile, NPC npc) {
        super(minecraftServer, world, gameProfile);
        this.npc = (CitizensNPC) npc;

        if (npc != null) {
            skinTracker = new SkinPacketTracker(this);
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
        if (npc != null && playerlistTracker == null) {
            return false;
        }
        return super.broadcastToPlayer(entityplayer);
    }

    public boolean canCutCorner(BlockPathTypes pathtype) {
        return (pathtype != BlockPathTypes.DANGER_FIRE && pathtype != BlockPathTypes.DANGER_CACTUS
                && pathtype != BlockPathTypes.DANGER_OTHER && pathtype != BlockPathTypes.WALKABLE_DOOR);
    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
        if (npc == null || !npc.isFlyable()) {
            return super.causeFallDamage(f, f1, damagesource);
        }
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
        if (dead) {
            return;
        }
        super.die(damagesource);
        Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                EntityHumanNPC.this.getLevel().removePlayerImmediately(EntityHumanNPC.this, RemovalReason.KILLED);
                ((ServerLevel) level).getChunkSource().removeEntity(EntityHumanNPC.this);
            }
        }, 15); // give enough time for death and smoke animation
    }

    @Override
    public void dismountTo(double d0, double d1, double d2) {
        if (npc == null) {
            super.dismountTo(d0, d1, d2);
            return;
        }
        NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            super.dismountTo(d0, d1, d2);
        }
    }

    @Override
    public void doTick() {
        if (npc == null) {
            super.doTick();
            return;
        }
        super.baseTick();
        boolean navigating = npc.getNavigator().isNavigating();
        if (!navigating && getBukkitEntity() != null
                && (!npc.hasTrait(Gravity.class) || npc.getOrAddTrait(Gravity.class).hasGravity())
                && Util.isLoaded(getBukkitEntity().getLocation(LOADED_LOCATION))) {
            moveWithFallDamage(Vec3.ZERO);
        }

        Vec3 mot = getDeltaMovement();
        if (Math.abs(mot.x) < EPSILON && Math.abs(mot.y) < EPSILON && Math.abs(mot.z) < EPSILON) {
            setDeltaMovement(Vec3.ZERO);
        }

        if (navigating) {
            if (!navigation.isDone()) {
                navigation.tick();
            }
            moveOnCurrentHeading();
        }
        NMSImpl.updateAI(this);

        if (isSpectator()) {
            this.noPhysics = true;
            this.onGround = false;
        }

        if (this.hurtTime > 0) {
            this.hurtTime--;
        }

        if (isDeadOrDying()) {
            tickDeath();
        }

        if (this.lastHurtByPlayerTime > 0) {
            this.lastHurtByPlayerTime--;
        } else {
            this.lastHurtByPlayer = null;
        }

        if (this.lastHurtByMob != null) {
            if (!this.lastHurtByMob.isAlive()) {
                setLastHurtByMob((LivingEntity) null);
            } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                setLastHurtByMob((LivingEntity) null);
            }
        }

        if (npc.data().get(NPC.Metadata.COLLIDABLE, !npc.isProtected())) {
            pushEntities();
        }

        tickEffects();
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = getYRot();
        this.xRotO = getXRot();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        if (playerlistTracker != null) {
            playerlistTracker.updateLastPlayer();
        }
        return super.getAddEntityPacket();
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
            NMSImpl.setBukkitEntity(this, new PlayerNPC(this));
        }
        return super.getBukkitEntity();
    }

    public PlayerControllerJump getControllerJump() {
        return controllerJump;
    }

    public PlayerMoveControl getMoveControl() {
        return controllerMove;
    }

    public PathNavigation getNavigation() {
        return navigation;
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    public float getPathfindingMalus(BlockPathTypes pathtype) {
        return this.malus.containsKey(pathtype) ? this.malus.get(pathtype) : pathtype.getMalus();
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
        if (npc.data().get(NPC.REMOVE_FROM_PLAYERLIST_METADATA, Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean())) {
            return new TextComponent("");
        }
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
            Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    EntityHumanNPC.this.hurtMarked = true;
                }
            });
        }
        return damaged;
    }

    private void initialise(MinecraftServer minecraftServer) {
        Socket socket = new EmptySocket();
        EmptyNetworkManager conn = null;
        try {
            conn = new EmptyNetworkManager(PacketFlow.CLIENTBOUND);
            connection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.setListener(connection);
            socket.close();
        } catch (IOException e) {
            // swallow
        }

        AttributeInstance range = getAttribute(Attributes.FOLLOW_RANGE);
        if (range == null) {
            try {
                AttributeSupplier provider = (AttributeSupplier) ATTRIBUTE_SUPPLIER.invoke(getAttributes());
                Map<Attribute, AttributeInstance> all = Maps
                        .newHashMap((Map<Attribute, AttributeInstance>) ATTRIBUTE_PROVIDER_MAP.invoke(provider));
                all.put(Attributes.FOLLOW_RANGE,
                        new AttributeInstance(Attributes.FOLLOW_RANGE, new Consumer<AttributeInstance>() {
                            @Override
                            public void accept(AttributeInstance att) {
                                throw new UnsupportedOperationException(
                                        "Tried to change value for default attribute instance FOLLOW_RANGE");
                            }
                        }));
                ATTRIBUTE_PROVIDER_MAP_SETTER.invoke(provider, ImmutableMap.copyOf(all));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            range = getAttribute(Attributes.FOLLOW_RANGE);
        }
        range.setBaseValue(Setting.DEFAULT_PATHFINDING_RANGE.asDouble());

        controllerJump = new PlayerControllerJump(this);
        controllerLook = new PlayerLookControl(this);
        controllerMove = new PlayerMoveControl(this);
        navigation = new PlayerNavigation(this, level);
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
        if (npc == null || noPhysics || isSleeping()) {
            return super.isInWall();
        }
        return Util.inBlock(getBukkitEntity());
    }

    public boolean isNavigating() {
        return npc.getNavigator().isNavigating();
    }

    @Override
    public boolean isPushable() {
        return npc == null ? super.isPushable() : npc.data().<Boolean> get(NPC.COLLIDABLE_METADATA, !npc.isProtected());
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
        if (npc == null || !npc.isFlyable()) {
            return super.onClimbable();
        } else {
            return false;
        }
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

    public void setMoveDestination(double x, double y, double z, double speed) {
        controllerMove.setWantedPosition(x, y, z, speed);
    }

    public void setPathfindingMalus(BlockPathTypes pathtype, float f) {
        this.malus.put(pathtype, f);
    }

    public void setShouldJump() {
        controllerJump.jump();
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

    public void setTargetLook(Entity target, float yawOffset, float renderOffset) {
        controllerLook.a(target, yawOffset, renderOffset);
    }

    public void setTargetLook(Location target) {
        controllerLook.a(target.getX(), target.getY(), target.getZ());
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
        if (updateCounter + 1 > Setting.PACKET_UPDATE_DELAY.asInt()) {
            effectsDirty = true;
        }
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

    public void updateAI() {
        controllerMove.tick();
        controllerLook.tick();
        controllerJump.tick();
    }

    private void updatePackets(boolean navigating) {
        updateCounter++;
        boolean itemChanged = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipment = getItemBySlot(slot);
            ItemStack cache = equipmentCache.get(slot);
            if (!(cache == null && equipment == null)
                    && (cache == null ^ equipment == null || !ItemStack.isSame(cache, equipment))) {
                itemChanged = true;
            }
            equipmentCache.put(slot, equipment);
        }
        if (updateCounter++ <= npc.data().<Integer> get(NPC.Metadata.PACKET_UPDATE_DELAY,
                Setting.PACKET_UPDATE_DELAY.asInt()) && !itemChanged)
            return;

        updateCounter = 0;
        Location current = getBukkitEntity().getLocation(packetLocationCache);
        List<Pair<EquipmentSlot, ItemStack>> vals = Lists.newArrayList();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            vals.add(new Pair<EquipmentSlot, ItemStack>(slot, getItemBySlot(slot)));
        }
        Packet<?>[] packets = { new ClientboundSetEquipmentPacket(getId(), vals) };
        NMSImpl.sendPacketsNearby(getBukkitEntity(), current, packets);
    }

    public void updatePathfindingRange(float pathfindingRange) {
        this.navigation.setRange(pathfindingRange);
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
        public boolean canSee(org.bukkit.entity.Entity entity) {
            if (entity != null && entity.getType() == EntityType.ITEM_FRAME) {
                return false; // optimise for large maps in item frames
            }
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

    private static final MethodHandle ATTRIBUTE_PROVIDER_MAP = NMS.getFirstGetter(AttributeSupplier.class, Map.class);
    private static final MethodHandle ATTRIBUTE_PROVIDER_MAP_SETTER = NMS.getFinalSetter(AttributeSupplier.class, "a");
    private static final MethodHandle ATTRIBUTE_SUPPLIER = NMS.getFirstGetter(AttributeMap.class,
            AttributeSupplier.class);
    private static final float EPSILON = 0.003F;
    private static final MethodHandle GAMEMODE_SETTING = NMS.getFirstMethodHandle(ServerPlayerGameMode.class, true,
            GameType.class, GameType.class);
    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}
