package net.citizensnpcs.npc.entity;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
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
import net.minecraft.server.v1_7_R4.AttributeInstance;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EnumGamemode;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.Navigation;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
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
    private int useListName = -1;

    public EntityHumanNPC(MinecraftServer minecraftServer, WorldServer world, GameProfile gameProfile,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, gameProfile, playerInteractManager);
        playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);

        this.npc = (CitizensNPC) npc;
        if (npc != null) {
            initialise(minecraftServer);
        }
    }

    @Override
    protected void a(double d0, boolean flag) {
        if (npc == null || !npc.isFlyable()) {
            super.a(d0, flag);
        }
    }

    @Override
    protected void b(float f) {
        if (npc == null || !npc.isFlyable()) {
            super.b(f);
        }
    }

    @Override
    public void collide(net.minecraft.server.v1_7_R4.Entity entity) {
        // this method is called by both the entities involved - cancelling
        // it will not stop the NPC from moving.
        super.collide(entity);
        if (npc != null) {
            Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }
    }

    @Override
    public void e(float f, float f1) {
        if (npc == null || !npc.isFlyable()) {
            super.e(f, f1);
        } else {
            NMS.flyingMoveLogic(this, f, f1);
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

    @Override
    public void h() {
        super.h();
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
            i();// TODO
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

    @Override
    public boolean h_() {
        if (npc == null || !npc.isFlyable()) {
            return super.h_();
        } else {
            return false;
        }
    }

    private void initialise(MinecraftServer minecraftServer) {
        Socket socket = new EmptySocket();
        NetworkManager conn = null;
        try {
            conn = new EmptyNetworkManager(false);
            playerConnection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.a(playerConnection);
        } catch (IOException e) {
            // swallow
        }

        NMS.setStepHeight(this, 1); // the default (0) breaks step climbing

        try {
            socket.close();
        } catch (IOException ex) {
            // swallow
        }

        AttributeInstance range = this.getAttributeInstance(GenericAttributes.b);
        if (range == null) {
            range = this.getAttributeMap().b(GenericAttributes.b);
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

    private void moveOnCurrentHeading() {
        NMS.updateAI(this);
        if (bc) {
            if (onGround && jumpTicks == 0) {
                bj();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        bd *= 0.98F;
        be *= 0.98F;
        bf *= 0.9F;
        e(bd, be); // movement method
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

    public void updateAI() {
        controllerMove.c();
        controllerLook.a();
        controllerJump.b();
    }

    private void updatePackets(boolean navigating) {
        if (world.getWorld().getFullTime() % Setting.PACKET_UPDATE_DELAY.asInt() == 0) {
            Location current = getBukkitEntity().getLocation(packetLocationCache);
            Packet[] packets = new Packet[navigating ? 6 : 7];
            if (!navigating) {
                packets[6] = new PacketPlayOutEntityHeadRotation(this,
                        (byte) MathHelper.d(NMS.getHeadYaw(this) * 256.0F / 360.0F));
            }
            for (int i = 0; i < 5; i++) {
                packets[i] = new PacketPlayOutEntityEquipment(getId(), i, getEquipment(i));
            }
            boolean removeFromPlayerList = Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean();
            NMS.addOrRemoveFromPlayerList(getBukkitEntity(),
                    npc.data().get("removefromplayerlist", removeFromPlayerList));
            int useListName = removeFromPlayerList ? 0 : 1;
            if (useListName != this.useListName || this.useListName == -1) {
                this.useListName = useListName;
                // packets[5] = new
                // PacketPlayOutPlayerInfo(getBukkitEntity().getPlayerListName(),
                // !removeFromPlayerList,
                // removeFromPlayerList ? 9999 : ping);
            }
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
