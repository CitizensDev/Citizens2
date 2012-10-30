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
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EnumGamemode;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Navigation;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private final CitizensNPC npc;

    public EntityHumanNPC(MinecraftServer minecraftServer, World world, String string,
            ItemInWorldManager itemInWorldManager, NPC npc) {
        super(minecraftServer, world, string, itemInWorldManager);
        itemInWorldManager.setGameMode(EnumGamemode.SURVIVAL);

        this.npc = (CitizensNPC) npc;
        if (npc != null)
            initialise(minecraftServer);
    }

    @Override
    public void collide(net.minecraft.server.Entity entity) {
        // this method is called by both the entities involved - cancelling
        // it will not stop the NPC from moving.
        super.collide(entity);
        if (npc != null)
            Util.callCollisionEvent(npc, entity);
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
        NetworkManager netMgr = null;
        try {
            netMgr = new EmptyNetworkManager(socket, "npc mgr", new NetHandler() {
                @Override
                public boolean a() {
                    return false;
                }
            }, server.F().getPrivate());
            netServerHandler = new EmptyNetHandler(minecraftServer, netMgr, this);
            netMgr.a(netServerHandler);
        } catch (IOException e) {
            // swallow
        }

        getNavigation().e(true);

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

        NMS.updateAI(this);
        Navigation navigation = getNavigation();
        if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON)
            motX = motY = motZ = 0;

        if (!navigation.f()) {
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
        // taken from EntityLiving update method
        if (bG) {
            /* boolean inLiquid = H() || J();
             if (inLiquid) {
                 motY += 0.04;
             } else //(handled elsewhere)*/
            if (onGround && bW == 0) {
                // bf(); // jump
                motY = 0.5F;
                bW = 10;
            }
        } else
            bW = 0;

        bD *= 0.98F;
        bE *= 0.98F;
        bF *= 0.9F;
        float speed = npc.getNavigator().getDefaultParameters().speed();
        e(speed);
        float prev = aM;
        aM *= by() * speed;
        e(bD, bE); // movement method
        aM = prev;
        NMS.setHeadYaw(this, yaw);
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
}