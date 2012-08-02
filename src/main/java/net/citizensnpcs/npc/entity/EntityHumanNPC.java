package net.citizensnpcs.npc.entity;

import java.io.IOException;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.network.EmptyNetHandler;
import net.citizensnpcs.npc.network.EmptyNetworkManager;
import net.citizensnpcs.npc.network.NPCSocket;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EnumGamemode;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Navigation;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

import org.bukkit.util.Vector;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private final CitizensNPC npc;

    public EntityHumanNPC(MinecraftServer minecraftServer, World world, String string,
            ItemInWorldManager itemInWorldManager, NPC npc) {
        super(minecraftServer, world, string, itemInWorldManager);
        this.npc = (CitizensNPC) npc;
        itemInWorldManager.setGameMode(EnumGamemode.SURVIVAL);

        NPCSocket socket = new NPCSocket();
        NetworkManager netMgr = new EmptyNetworkManager(socket, "npc mgr", new NetHandler() {
            @Override
            public boolean a() {
                return false;
            }
        }, server.E().getPrivate());
        netServerHandler = new EmptyNetHandler(minecraftServer, netMgr, this);
        netMgr.a(netServerHandler);

        try {
            socket.close();
        } catch (IOException ex) {
            // swallow
        }
    }

    @Override
    public void collide(net.minecraft.server.Entity entity) {
        // this method is called by both the entities involved - cancelling
        // it will not stop the NPC from moving.
        super.collide(entity);
        Util.callCollisionEvent(npc, entity);
    }

    @Override
    public void g(double x, double y, double z) {
        if (npc == null) {
            super.g(x, y, z);
            return;
        }
        if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0)
            return;
        NPCPushEvent event = Util.callPushEvent(npc, new Vector(x, y, z));
        if (!event.isCancelled())
            super.g(x, y, z);
        // when another entity collides, this method is called to push the NPC
        // so we prevent it from doing anything if the event is cancelled.
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    @Override
    public void h_() {
        super.h_();
        Navigation navigation = getNavigation();
        if (!navigation.f()) {
            navigation.e();
            moveOnCurrentHeading();
        } else if (motX != 0 || motZ != 0 || motY != 0) {
            // a(0, 0);
        }
        if (noDamageTicks > 0)
            --noDamageTicks;
        npc.update();
    }

    private void moveOnCurrentHeading() {
        getControllerMove().c();
        getControllerLook().a();
        getControllerJump().b();

        // taken from EntityLiving
        if (bu) {
            boolean inLiquid = G() || H();
            if (inLiquid) {
                motY += 0.04;
            } else if (onGround && bE == 0) {
                // this.ac(); - this doesn't jump high enough
                motY = 0.6;
                bE = 10;
            }
        } else {
            bE = 0;
        }

        aX *= 0.98F;
        a(aW, aX);
        as = yaw;
    }

}