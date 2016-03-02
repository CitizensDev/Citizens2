package net.citizensnpcs.npc.entity;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.network.EmptyNetHandler;
import net.citizensnpcs.npc.network.EmptyNetworkManager;
import net.citizensnpcs.npc.network.EmptySocket;
import net.citizensnpcs.npc.skin.SkinPacketTracker;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.citizensnpcs.util.nms.PlayerControllerJump;
import net.citizensnpcs.util.nms.PlayerControllerLook;
import net.citizensnpcs.util.nms.PlayerControllerMove;
import net.citizensnpcs.util.nms.PlayerNavigation;
import net.minecraft.server.v1_9_R1.AttributeInstance;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EnumItemSlot;
import net.minecraft.server.v1_9_R1.EnumProtocolDirection;
import net.minecraft.server.v1_9_R1.GenericAttributes;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.MinecraftServer;
import net.minecraft.server.v1_9_R1.NavigationAbstract;
import net.minecraft.server.v1_9_R1.NetworkManager;
import net.minecraft.server.v1_9_R1.Packet;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_9_R1.PathType;
import net.minecraft.server.v1_9_R1.PlayerInteractManager;
import net.minecraft.server.v1_9_R1.WorldServer;
import net.minecraft.server.v1_9_R1.WorldSettings.EnumGamemode;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder, SkinnableEntity {
    private final Map<PathType, Float> bz = Maps.newEnumMap(PathType.class);
    private PlayerControllerJump controllerJump;
    private PlayerControllerLook controllerLook;
    private PlayerControllerMove controllerMove;
    private boolean gravity = true;
    private int jumpTicks = 0;
    private PlayerNavigation navigation;
    private final CitizensNPC npc;
    private final Location packetLocationCache = new Location(null, 0, 0, 0);
    private final SkinPacketTracker skinTracker;
    private int updateCounter = 0;

    public EntityHumanNPC(MinecraftServer minecraftServer, WorldServer world, GameProfile gameProfile,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, gameProfile, playerInteractManager);

        this.npc = (CitizensNPC) npc;
        if (npc != null) {
            skinTracker = new SkinPacketTracker(this);
            playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);
            initialise(minecraftServer);
        } else {
            skinTracker = null;
        }
    }

    @Override
    protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
        if (npc == null || !npc.isFlyable()) {
            super.a(d0, flag, block, blockposition);
        }
    }

    public float a(PathType pathtype) {
        return this.bz.containsKey(pathtype) ? this.bz.get(pathtype).floatValue() : pathtype.a();
    }

    public void a(PathType pathtype, float f) {
        this.bz.put(pathtype, Float.valueOf(f));
    }

    @Override
    public void collide(net.minecraft.server.v1_9_R1.Entity entity) {
        // this method is called by both the entities involved - cancelling
        // it will not stop the NPC from moving.
        super.collide(entity);
        if (npc != null) {
            Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        // knock back velocity is cancelled and sent to client for handling when
        // the entity is a player. there is no client so make this happen
        // manually.
        boolean damaged = super.damageEntity(damagesource, f);
        if (damaged && velocityChanged) {
            velocityChanged = false;
            Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    EntityHumanNPC.this.velocityChanged = true;
                }
            });
        }
        return damaged;
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
                world.removeEntity(EntityHumanNPC.this);
            }
        }, 35); // give enough time for death and smoke animation
    }

    @Override
    public void e(float f, float f1) {
        if (npc == null || !npc.isFlyable()) {
            super.e(f, f1);
        }
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        if (npc == null)
            super.enderTeleportTo(d0, d1, d2);
        NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            super.enderTeleportTo(d0, d1, d2);
        }
    }

    @Override
    public void g(double x, double y, double z) {
        if (npc == null) {
            super.g(x, y, z);
            return;
        }
        if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
            if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)) {
                super.g(x, y, z);
            }
            return;
        }
        Vector vector = new Vector(x, y, z);
        NPCPushEvent event = Util.callPushEvent(npc, vector);
        if (!event.isCancelled()) {
            vector = event.getCollisionVector();
            super.g(vector.getX(), vector.getY(), vector.getZ());
        }
        // when another entity collides, this method is called to push the
        // NPC so we prevent it from doing anything if the event is
        // cancelled.
    }

    @Override
    public void g(float f, float f1) {
        if (npc == null || !npc.isFlyable()) {
            super.g(f, f1);
        } else {
            NMS.flyingMoveLogic(this, f, f1);
        }
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && bukkitEntity == null) {
            bukkitEntity = new PlayerNPC(this);
        }
        return super.getBukkitEntity();
    }

    public PlayerControllerJump getControllerJump() {
        return controllerJump;
    }

    public PlayerControllerMove getControllerMove() {
        return controllerMove;
    }

    public NavigationAbstract getNavigation() {
        return navigation;
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    @Override
    public String getSkinName() {
        MetadataStore meta = npc.data();

        String skinName = meta.get(NPC.PLAYER_SKIN_UUID_METADATA);
        if (skinName == null) {
            skinName = ChatColor.stripColor(getName());
        }
        return skinName.toLowerCase();
    }

    @Override
    public SkinPacketTracker getSkinTracker() {
        return skinTracker;
    }

    private void initialise(MinecraftServer minecraftServer) {
        Socket socket = new EmptySocket();
        NetworkManager conn = null;
        try {
            conn = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
            playerConnection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.setPacketListener(playerConnection);
            socket.close();
        } catch (IOException e) {
            // swallow
        }

        AttributeInstance range = getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        if (range == null) {
            range = getAttributeMap().b(GenericAttributes.FOLLOW_RANGE);
        }
        range.setValue(Setting.DEFAULT_PATHFINDING_RANGE.asDouble());

        controllerJump = new PlayerControllerJump(this);
        controllerLook = new PlayerControllerLook(this);
        controllerMove = new PlayerControllerMove(this);
        navigation = new PlayerNavigation(this, world);
        NMS.setStepHeight(this, 1); // the default (0) breaks step climbing

        setSkinFlags((byte) 0xFF);
    }

    public boolean isNavigating() {
        return npc.getNavigator().isNavigating();
    }

    @Override
    public void m() {
        super.m();
        if (npc == null)
            return;
        boolean navigating = npc.getNavigator().isNavigating();
        updatePackets(navigating);
        if (gravity && !navigating && getBukkitEntity() != null
                && Util.isLoaded(getBukkitEntity().getLocation(LOADED_LOCATION))) {
            g(0, 0);
        }

        if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON) {
            motX = motY = motZ = 0;
        }
        if (navigating) {
            if (!NMS.isNavigationFinished(navigation)) {
                NMS.updateNavigation(navigation);
            }
            moveOnCurrentHeading();
        }

        if (noDamageTicks > 0) {
            --noDamageTicks;
        }

        npc.update();
    }

    private void moveOnCurrentHeading() {
        NMS.updateAI(this);
        if (bc) {
            if (onGround && jumpTicks == 0) {
                ch();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        bd *= 0.98F;
        be *= 0.98F;
        bf *= 0.9F;
        g(bd, be); // movement method
        NMS.setHeadYaw(this, yaw);
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    @Override
    public boolean n_() {
        if (npc == null || !npc.isFlyable()) {
            return super.n_();
        } else {
            return false;
        }
    }

    public void setMoveDestination(double x, double y, double z, double speed) {
        controllerMove.a(x, y, z, speed);
    }

    public void setShouldJump() {
        controllerJump.a();
    }

    @Override
    public void setSkinFlags(byte flags) {
        // set skin flag byte
        getDataWatcher().set(bp, flags);
    }

    @Override
    public void setSkinName(String name) {
        Preconditions.checkNotNull(name);

        npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, name.toLowerCase());
        skinTracker.notifySkinChange();
    }

    public void setTargetLook(Entity target, float yawOffset, float renderOffset) {
        controllerLook.a(target, yawOffset, renderOffset);
    }

    public void updateAI() {
        controllerMove.c();
        controllerLook.a();
        controllerJump.b();
    }

    private void updatePackets(boolean navigating) {
        if (updateCounter++ > Setting.PACKET_UPDATE_DELAY.asInt()) {
            updateCounter = 0;
            Location current = getBukkitEntity().getLocation(packetLocationCache);
            Packet<?>[] packets = new Packet[navigating ? EnumItemSlot.values().length
                    : EnumItemSlot.values().length + 1];
            if (!navigating) {
                packets[5] = new PacketPlayOutEntityHeadRotation(this,
                        (byte) MathHelper.d(NMS.getHeadYaw(this) * 256.0F / 360.0F));
            }
            int i = 0;
            for (EnumItemSlot slot : EnumItemSlot.values()) {
                packets[i++] = new PacketPlayOutEntityEquipment(getId(), slot, getEquipment(slot));
            }
            boolean removeFromPlayerList = npc.data().get("removefromplayerlist",
                    Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
            NMS.addOrRemoveFromPlayerList(getBukkitEntity(), removeFromPlayerList);
            NMS.sendPacketsNearby(getBukkitEntity(), current, packets);
        }
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
            npc.getTrait(Inventory.class);
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

        public void setGravityEnabled(boolean enabled) {
            getHandle().gravity = enabled;
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
    }

    private static final float EPSILON = 0.005F;
    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}
