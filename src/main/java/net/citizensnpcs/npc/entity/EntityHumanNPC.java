package net.citizensnpcs.npc.entity;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.network.EmptyNetHandler;
import net.citizensnpcs.npc.network.EmptyNetworkManager;
import net.citizensnpcs.npc.network.EmptySocket;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_5_R3.Connection;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.EnumGamemode;
import net.minecraft.server.v1_5_R3.MathHelper;
import net.minecraft.server.v1_5_R3.MinecraftServer;
import net.minecraft.server.v1_5_R3.Navigation;
import net.minecraft.server.v1_5_R3.NetworkManager;
import net.minecraft.server.v1_5_R3.Packet;
import net.minecraft.server.v1_5_R3.Packet35EntityHeadRotation;
import net.minecraft.server.v1_5_R3.Packet5EntityEquipment;
import net.minecraft.server.v1_5_R3.PlayerInteractManager;
import net.minecraft.server.v1_5_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.CraftServer;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private boolean gravity = true;
    private final CitizensNPC npc;
    private final Location packetLocationCache = new Location(null, 0, 0, 0);
    private int packetUpdateCount;

    public EntityHumanNPC(MinecraftServer minecraftServer, World world, String string,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, string, playerInteractManager);
        playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);

        this.npc = (CitizensNPC) npc;
        if (npc != null)
            initialise(minecraftServer);
    }

    @Override
    public float bE() {
        return NMS.modifiedSpeed(super.bE(), npc);
    }

    @Override
    public void collide(net.minecraft.server.v1_5_R3.Entity entity) {
        // this method is called by both the entities involved - cancelling
        // it will not stop the NPC from moving.
        super.collide(entity);
        if (npc != null)
            Util.callCollisionEvent(npc, entity.getBukkitEntity());
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

    @Override
    public NPC getNPC() {
        return npc;
    }

    private void initialise(MinecraftServer minecraftServer) {
        Socket socket = new EmptySocket();
        NetworkManager conn = null;
        try {
            conn = new EmptyNetworkManager(server.getLogger(), socket, "npc mgr", new Connection() {
                @Override
                public boolean a() {
                    return false;
                }
            }, server.F().getPrivate());
            playerConnection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.a(playerConnection);
        } catch (IOException e) {
            // swallow
        }

        getNavigation().e(true);
        Y = 1F; // stepHeight - must not stay as the default 0 (breaks steps).
                // Check the EntityPlayer constructor for the new name.

        try {
            socket.close();
        } catch (IOException ex) {
            // swallow
        }
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
        if (!npc.data().get("removefromplayerlist", true)) {
            g();
        }
        if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON)
            motX = motY = motZ = 0;

        NMS.updateSenses(this);
        if (navigating) {
            Navigation navigation = getNavigation();
            if (!navigation.f())
                navigation.e();
            moveOnCurrentHeading();
        } else if (motX != 0 || motZ != 0 || motY != 0) {
            e(0, 0); // is this necessary? it does controllable but sometimes
                     // players sink into the ground
        }

        if (noDamageTicks > 0)
            --noDamageTicks;
        npc.update();
    }

    private void moveOnCurrentHeading() {
        NMS.updateAI(this);
        // taken from EntityLiving update method
        if (bG) {
            /* boolean inLiquid = G() || I();
             if (inLiquid) {
                 motY += 0.04;
             } else //(handled elsewhere)*/
            if (onGround && bX == 0) {
                bl();
                bX = 10;
            }
        } else
            bX = 0;

        bD *= 0.98F;
        bE *= 0.98F;
        bF *= 0.9F;

        float prev = aO;
        aO *= bE();
        e(bD, bE); // movement method
        aO = prev;
        NMS.setHeadYaw(this, yaw);
    }

    private void updatePackets(boolean navigating) {
        if (++packetUpdateCount >= 30) {
            Location current = getBukkitEntity().getLocation(packetLocationCache);
            Packet[] packets = new Packet[navigating ? 5 : 6];
            if (!navigating) {
                packets[5] = new Packet35EntityHeadRotation(id,
                        (byte) MathHelper.d(NMS.getHeadYaw(this) * 256.0F / 360.0F));
            }
            for (int i = 0; i < 5; i++) {
                packets[i] = new Packet5EntityEquipment(id, i, getEquipment(i));
            }
            NMS.sendPacketsNearby(current, packets);
            packetUpdateCount = 0;
        }
    }

    public static class PlayerNPC extends CraftPlayer implements NPCHolder {
        private final CitizensNPC npc;

        private PlayerNPC(EntityHumanNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public List<MetadataValue> getMetadata(String metadataKey) {
            return server.getEntityMetadata().getMetadata(this, metadataKey);
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean hasMetadata(String metadataKey) {
            return server.getEntityMetadata().hasMetadata(this, metadataKey);
        }

        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {
            server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
        }

        public void setGravityEnabled(boolean enabled) {
            ((EntityHumanNPC) getHandle()).gravity = enabled;
        }

        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
            server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
        }
    }

    private static final float EPSILON = 0.005F;

    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}