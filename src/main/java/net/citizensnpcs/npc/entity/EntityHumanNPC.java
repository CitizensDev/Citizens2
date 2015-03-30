package net.citizensnpcs.npc.entity;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.network.EmptyNetHandler;
import net.citizensnpcs.npc.network.EmptyNetworkManager;
import net.citizensnpcs.npc.network.EmptySocket;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.citizensnpcs.util.nms.PlayerControllerJump;
import net.citizensnpcs.util.nms.PlayerControllerLook;
import net.citizensnpcs.util.nms.PlayerControllerMove;
import net.citizensnpcs.util.nms.PlayerNavigation;
import net.minecraft.server.v1_8_R2.AttributeInstance;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.DamageSource;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityPlayer;
import net.minecraft.server.v1_8_R2.EnumProtocolDirection;
import net.minecraft.server.v1_8_R2.GenericAttributes;
import net.minecraft.server.v1_8_R2.MathHelper;
import net.minecraft.server.v1_8_R2.MinecraftServer;
import net.minecraft.server.v1_8_R2.NavigationAbstract;
import net.minecraft.server.v1_8_R2.NetworkManager;
import net.minecraft.server.v1_8_R2.Packet;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R2.PlayerInteractManager;
import net.minecraft.server.v1_8_R2.WorldServer;
import net.minecraft.server.v1_8_R2.WorldSettings.EnumGamemode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private PlayerControllerJump controllerJump;
    private PlayerControllerLook controllerLook;
    private PlayerControllerMove controllerMove;
    private boolean gravity = true;
    private int jumpTicks = 0;
    private PlayerNavigation navigation;
    private final CitizensNPC npc;
    private final Location packetLocationCache = new Location(null, 0, 0, 0);

    public EntityHumanNPC(MinecraftServer minecraftServer, WorldServer world, GameProfile gameProfile,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, gameProfile, playerInteractManager);

        this.npc = (CitizensNPC) npc;
        if (npc != null) {
            playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);
            initialise(minecraftServer);
        }
    }

    @Override
    protected void a(double d0, boolean flag, Block block, BlockPosition blockposition) {
        if (npc == null || !npc.isFlyable()) {
            super.a(d0, flag, block, blockposition);
        }
    }

    @Override
    public void collide(net.minecraft.server.v1_8_R2.Entity entity) {
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

    private void initialise(MinecraftServer minecraftServer) {
        Socket socket = new EmptySocket();
        NetworkManager conn = null;
        try {
            conn = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
            playerConnection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.a(playerConnection);
            socket.close();
        } catch (IOException e) {
            // swallow
        }

        AttributeInstance range = getAttributeInstance(GenericAttributes.b);
        if (range == null) {
            range = getAttributeMap().b(GenericAttributes.b);
        }
        range.setValue(Setting.DEFAULT_PATHFINDING_RANGE.asDouble());

        controllerJump = new PlayerControllerJump(this);
        controllerLook = new PlayerControllerLook(this);
        controllerMove = new PlayerControllerMove(this);
        navigation = new PlayerNavigation(this, world);
        NMS.setStepHeight(this, 1); // the default (0) breaks step climbing
    }

    public boolean isNavigating() {
        return npc.getNavigator().isNavigating();
    }

    @Override
    public boolean k_() {
        if (npc == null || !npc.isFlyable()) {
            return super.k_();
        } else {
            return false;
        }
    }

    private void moveOnCurrentHeading() {
        NMS.updateAI(this);
        if (aY) {
            if (onGround && jumpTicks == 0) {
                bF();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        aZ *= 0.98F;
        ba *= 0.98F;
        bb *= 0.9F;
        g(aZ, ba); // movement method
        NMS.setHeadYaw(this, yaw);
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    public void setMoveDestination(double x, double y, double z, double speed) {
        controllerMove.a(x, y, z, speed);
    }

    public void setShouldJump() {
        controllerJump.a();
    }

    public void setTargetLook(Entity target, float yawOffset, float renderOffset) {
        controllerLook.a(target, yawOffset, renderOffset);
    }

    @Override
    public void t_() {
        super.t_();
        if (npc == null)
            return;
        boolean navigating = npc.getNavigator().isNavigating();
        updatePackets(navigating);
        if (gravity && !navigating && getBukkitEntity() != null
                && Util.isLoaded(getBukkitEntity().getLocation(LOADED_LOCATION)) && !NMS.inWater(getBukkitEntity())) {
            move(0, -0.2, 0);
            // gravity. also works around an entity.onGround not updating issue
            // (onGround is normally updated by the client)
        }

        if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON) {
            motX = motY = motZ = 0;
        }
        if (navigating) {
            if (!NMS.isNavigationFinished(navigation)) {
                NMS.updateNavigation(navigation);
            }
            moveOnCurrentHeading();
        } else if (motX != 0 || motZ != 0 || motY != 0) {
            g(0, 0); // is this necessary? it does controllable but sometimes
            // players sink into the ground
        }

        if (noDamageTicks > 0) {
            --noDamageTicks;
        }

        npc.update();
    }

    public void updateAI() {
        controllerMove.c();
        controllerLook.a();
        controllerJump.b();
    }

    private void updatePackets(boolean navigating) {
        if (world.getWorld().getFullTime() % Setting.PACKET_UPDATE_DELAY.asInt() == 0) {
            // set skin flag byte to all visible (DataWatcher API is lacking so
            // catch the NPE as a sign that this is a MC 1.7 server without the
            // skin flag)
            try {
                datawatcher.watch(10, Byte.valueOf((byte) 127));
            } catch (NullPointerException e) {
                datawatcher.a(10, Byte.valueOf((byte) 127));
            }

            Location current = getBukkitEntity().getLocation(packetLocationCache);
            Packet<?>[] packets = new Packet[navigating ? 5 : 6];
            if (!navigating) {
                packets[5] = new PacketPlayOutEntityHeadRotation(this,
                        (byte) MathHelper.d(NMS.getHeadYaw(this) * 256.0F / 360.0F));
            }
            for (int i = 0; i < 5; i++) {
                packets[i] = new PacketPlayOutEntityEquipment(getId(), i, getEquipment(i));
            }

            boolean removeFromPlayerList = npc.data().get("removefromplayerlist",
                    Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
            NMS.addOrRemoveFromPlayerList(getBukkitEntity(), removeFromPlayerList);
            NMS.sendPlayerlistPacket(false, getBukkitEntity());
            NMS.sendPacketsNearby(getBukkitEntity(), current, packets);
        }
    }

    public void updatePathfindingRange(float pathfindingRange) {
        this.navigation.setRange(pathfindingRange);
    }

    public static class PlayerNPC extends CraftPlayer implements NPCHolder {
        private final CraftServer cserver;
        private final CitizensNPC npc;

        private PlayerNPC(EntityHumanNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
            this.cserver = (CraftServer) Bukkit.getServer();
            npc.getTrait(Inventory.class);
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
    }

    private static final float EPSILON = 0.005F;
    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}
