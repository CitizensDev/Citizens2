package net.citizensnpcs.npc.entity;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
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
import net.minecraft.server.v1_6_R3.AttributeInstance;
import net.minecraft.server.v1_6_R3.Connection;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.EnumGamemode;
import net.minecraft.server.v1_6_R3.GenericAttributes;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.MinecraftServer;
import net.minecraft.server.v1_6_R3.Navigation;
import net.minecraft.server.v1_6_R3.NetworkManager;
import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet201PlayerInfo;
import net.minecraft.server.v1_6_R3.Packet34EntityTeleport;
import net.minecraft.server.v1_6_R3.Packet35EntityHeadRotation;
import net.minecraft.server.v1_6_R3.Packet5EntityEquipment;
import net.minecraft.server.v1_6_R3.PlayerInteractManager;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private PlayerControllerJump controllerJump;
    private PlayerControllerLook controllerLook;
    private PlayerControllerMove controllerMove;
    private boolean gravity = true;
    private int jumpTicks = 0;
    private PlayerNavigation navigation;
    private final CitizensNPC npc;
    private final Location packetLocationCache = new Location(null, 0, 0, 0);
    private int packetUpdateCount;
    private int useListName = -1;

    public EntityHumanNPC(MinecraftServer minecraftServer, World world, String string,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, string, playerInteractManager);
        playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);

        this.npc = (CitizensNPC) npc;
        if (npc != null) {
            initialise(minecraftServer);
        }
    }

    @Override
    public void collide(net.minecraft.server.v1_6_R3.Entity entity) {
        // this method is called by both the entities involved - cancelling
        // it will not stop the NPC from moving.
        super.collide(entity);
        if (npc != null) {
            Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }
    }

    @Override
    public void g(double x, double y, double z) {
        if (npc == null) {
            super.g(x, y, z);
            return;
        }
        if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
            if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                super.g(x, y, z);
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
    public CraftPlayer getBukkitEntity() {
        if (npc != null && bukkitEntity == null)
            bukkitEntity = new PlayerNPC(this);
        return super.getBukkitEntity();
    }

    public PlayerControllerJump getControllerJump() {
        return controllerJump;
    }

    public Navigation getNavigation() {
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
            conn = new EmptyNetworkManager(minecraftServer.getLogger(), socket, "npc mgr", new Connection() {
                @Override
                public boolean a() {
                    return false;
                }
            }, minecraftServer.H().getPrivate());
            playerConnection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.a(playerConnection);
        } catch (IOException e) {
            // swallow
        }

        NMS.setStepHeight(this, 1);// stepHeight - must not stay as the default
                                   // 0 (breaks steps).

        try {
            socket.close();
        } catch (IOException ex) {
            // swallow
        }
        AttributeInstance range = this.getAttributeInstance(GenericAttributes.b);
        if (range == null) {
            range = this.aX().b(GenericAttributes.b);
        }
        range.setValue(Setting.DEFAULT_PATHFINDING_RANGE.asDouble());
        controllerJump = new PlayerControllerJump(this);
        controllerLook = new PlayerControllerLook(this);
        controllerMove = new PlayerControllerMove(this);
        navigation = new PlayerNavigation(this, world);
    }

    public boolean isNavigating() {
        return npc.getNavigator().isNavigating();
    }

    @Override
    public void l_() {
        super.l_();
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
        if (!npc.data().get("removefromplayerlist", Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean())) {
            h();
        }
        if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON)
            motX = motY = motZ = 0;

        if (navigating) {
            if (!NMS.isNavigationFinished(navigation)) {
                NMS.updateNavigation(navigation);
            }
            moveOnCurrentHeading();
        } else if (motX != 0 || motZ != 0 || motY != 0) {
            e(0, 0); // is this necessary? it does controllable but sometimes
                     // players sink into the ground
        }

        if (noDamageTicks > 0) {
            --noDamageTicks;
        }
        npc.update();
    }

    private void moveOnCurrentHeading() {
        NMS.updateAI(this);
        // taken from EntityLiving update method
        if (bd) {
            /* boolean inLiquid = H() || J();
             if (inLiquid) {
                 motY += 0.04;
             } else //(handled elsewhere)*/
            if (onGround && jumpTicks == 0) {
                be();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        be *= 0.98F;
        bf *= 0.98F;
        bg *= 0.9F;

        e(be, bf); // movement method
        NMS.setHeadYaw(this, yaw);
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    public void setMoveDestination(double x, double y, double z, float speed) {
        controllerMove.a(x, y, z, speed);
    }

    public void setShouldJump() {
        controllerJump.a();
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
        if (++packetUpdateCount >= 30) {
            Location current = getBukkitEntity().getLocation(packetLocationCache);
            Packet[] packets = new Packet[navigating ? 6 : 8];
            if (!navigating) {
                packets[6] = new Packet35EntityHeadRotation(id,
                        (byte) MathHelper.d(NMS.getHeadYaw(this) * 256.0F / 360.0F));
                packets[7] = new Packet34EntityTeleport(this);
            }
            for (int i = 0; i < 5; i++) {
                packets[i] = new Packet5EntityEquipment(id, i, getEquipment(i));
            }
            boolean removeFromPlayerList = Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean();
            NMS.addOrRemoveFromPlayerList(getBukkitEntity(),
                    npc.data().get("removefromplayerlist", removeFromPlayerList));
            int useListName = removeFromPlayerList ? 0 : 1;
            if (useListName != this.useListName || this.useListName == -1) {
                this.useListName = useListName;
                packets[5] = new Packet201PlayerInfo(getBukkitEntity().getPlayerListName(), !removeFromPlayerList,
                        removeFromPlayerList ? 9999 : ping);
            }
            NMS.sendPacketsNearby(current, packets);
            packetUpdateCount = 0;
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