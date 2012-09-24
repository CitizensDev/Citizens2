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
        if (npc == null)
            return super.getBukkitEntity();
        if (bukkitEntity != null)
            return (CraftPlayer) bukkitEntity;
        return (CraftPlayer) (bukkitEntity = new CraftPlayer(((CraftServer) Bukkit.getServer()), this) {
            @Override
            public List<MetadataValue> getMetadata(String metadataKey) {
                return server.getEntityMetadata().getMetadata(this, metadataKey);
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
        });
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    @Override
    public void h_() {
        super.h_();
        if (npc == null)
            return;
        Navigation navigation = getNavigation();
        if (!navigation.f()) {
            navigation.e();
            moveOnCurrentHeading();
        } else if (!npc.getNavigator().isNavigating() && (motX != 0 || motZ != 0 || motY != 0)) {
            if (Math.abs(motX) < EPSILON && Math.abs(motY) < EPSILON && Math.abs(motZ) < EPSILON) {
                motX = motY = motZ = 0;
            } else
                e(0, 0); // is this necessary? it does gravity/controllable but
            // sometimes players sink into the ground
        }
        if (noDamageTicks > 0)
            --noDamageTicks;
        npc.update();
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
            }, server.E().getPrivate());
            netServerHandler = new EmptyNetHandler(minecraftServer, netMgr, this);
            netMgr.a(netServerHandler);
        } catch (IOException e) {
            // swallow
        }

        W = STEP_HEIGHT; // fix moving up slabs and steps
        getNavigation().e(true);

        try {
            socket.close();
        } catch (IOException ex) {
            // swallow
        }
    }

    private void moveOnCurrentHeading() {
        getControllerMove().c();
        getControllerLook().a();
        getControllerJump().b();
        e(npc.getNavigator().getDefaultParameters().speed());

        // taken from EntityLiving update method
        if (bu) {
            boolean inLiquid = H() || J();
            if (inLiquid) {
                motY += 0.04;
            } else if (onGround && bE == 0) {
                // this.aZ(); - this doesn't jump high enough
                motY = 0.6;
                bE = 10;
            }
        } else {
            bE = 0;
        }
        br *= 0.98F;
        bs *= 0.98F;
        bt *= 0.9F;

        float prev = aG;
        aG *= bs() * npc.getNavigator().getDefaultParameters().speed();
        e(br, bs); // movement method
        aG = prev;
        as = yaw; // update head yaw to match entity yaw
    }

    private static final float EPSILON = 0.001F;

    private static final float STEP_HEIGHT = 1F;
}