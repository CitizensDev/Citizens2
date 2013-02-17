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
import net.minecraft.server.v1_4_R1.Connection;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.EnumGamemode;
import net.minecraft.server.v1_4_R1.MinecraftServer;
import net.minecraft.server.v1_4_R1.Navigation;
import net.minecraft.server.v1_4_R1.NetworkManager;
import net.minecraft.server.v1_4_R1.Packet5EntityEquipment;
import net.minecraft.server.v1_4_R1.PlayerInteractManager;
import net.minecraft.server.v1_4_R1.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private final CitizensNPC npc;
    private final net.minecraft.server.v1_4_R1.ItemStack[] previousEquipment = { null, null, null, null, null };

    public EntityHumanNPC(MinecraftServer minecraftServer, World world, String string,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, string, playerInteractManager);
        playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);

        this.npc = (CitizensNPC) npc;
        if (npc != null)
            initialise(minecraftServer);
    }

    @Override
    public float bB() {
        return super.bB() * npc.getNavigator().getLocalParameters().speed();
    }

    @Override
    public void collide(net.minecraft.server.v1_4_R1.Entity entity) {
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
            conn = new EmptyNetworkManager(socket, "npc mgr", new Connection() {
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
        X = 1F; // stepHeight - must not stay as the default 0 (breaks steps).
                // Check the EntityPlayer constructor for the new name.

        try {
            socket.close();
        } catch (IOException ex) {
            // swallow
        }
    }

    @Override
    public void j_() {
        super.j_();
        if (npc == null)
            return;

        if (getBukkitEntity() != null && Util.isLoaded(getBukkitEntity().getLocation(LOADED_LOCATION))) {
            if (!npc.getNavigator().isNavigating() && !NMS.inWater(getBukkitEntity()))
                move(0, -0.2, 0);
            // gravity. also works around an entity.onGround not updating issue
            // (onGround is normally updated by the client)
        }

        updateEquipment();
        if (!npc.data().get("removefromplayerlist", true))
            g();
        if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON)
            motX = motY = motZ = 0;

        NMS.updateSenses(this);
        if (npc.getNavigator().isNavigating()) {
            Navigation navigation = getNavigation();
            if (!navigation.f())
                navigation.e();
            moveOnCurrentHeading();
        } else if (motX != 0 || motZ != 0 || motY != 0)
            e(0, 0); // is this necessary? it does controllable but sometimes
                     // players sink into the ground

        if (noDamageTicks > 0)
            --noDamageTicks;
        npc.update();
    }

    private void moveOnCurrentHeading() {
        NMS.updateAI(this);
        // taken from EntityLiving update method
        if (bF) {
            /* boolean inLiquid = H() || J();
             if (inLiquid) {
                 motY += 0.04;
             } else //(handled elsewhere)*/
            if (onGround && bV == 0) {
                bi();
                bV = 10;
            }
        } else
            bV = 0;

        bC *= 0.98F;
        bD *= 0.98F;
        bE *= 0.9F;

        float prev = aN;
        aN *= bB();
        e(bC, bD); // movement method
        aN = prev;
        NMS.setHeadYaw(this, yaw);
    }

    private void updateEquipment() {
        for (int i = 0; i < previousEquipment.length; i++) {
            net.minecraft.server.v1_4_R1.ItemStack previous = previousEquipment[i];
            net.minecraft.server.v1_4_R1.ItemStack current = getEquipment(i);
            if (previous != current) {
                NMS.sendPacketNearby(getBukkitEntity().getLocation(), new Packet5EntityEquipment(id, i, current));
                previousEquipment[i] = current;
            }
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

        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
            server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
        }
    }

    private static final float EPSILON = 0.005F;

    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}