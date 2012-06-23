package net.citizensnpcs.npc.entity;

import java.io.IOException;

import net.citizensnpcs.api.abstraction.entity.NPCHolder;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.network.EmptyNetHandler;
import net.citizensnpcs.npc.network.EmptyNetworkManager;
import net.citizensnpcs.npc.network.EmptySocket;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Navigation;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private CitizensNPC npc;

    public EntityHumanNPC(MinecraftServer minecraftServer, net.minecraft.server.World world, String string,
            ItemInWorldManager itemInWorldManager, NPC npc) {
        super(minecraftServer, world, string, itemInWorldManager);
        this.npc = (CitizensNPC) npc;
        itemInWorldManager.setGameMode(0);

        EmptySocket socket = new EmptySocket();
        NetworkManager netMgr = new EmptyNetworkManager(socket, "npc mgr", new NetHandler() {
            @Override
            public boolean c() {
                return false;
            }
        });
        netServerHandler = new EmptyNetHandler(minecraftServer, netMgr, this);
        netMgr.a(netServerHandler);

        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void F_() {
        super.F_();
        Navigation navigation = al();
        if (!navigation.e()) {
            navigation.d();
            moveOnCurrentHeading();
        } else if (motX != 0 || motZ != 0 || motY != 0) {
            a(0, 0);
        }
        if (noDamageTicks > 0)
            --noDamageTicks;
        npc.update();
    }

    private void moveOnCurrentHeading() {
        getControllerMove().c();
        getControllerLook().a();
        getControllerJump().b();
        if (aZ) {
            if (aT()) {
                motY += 0.04;
            } else if (onGround && q == 0) {
                motY = 0.6;
                q = 10;
            }
        } else {
            q = 0;
        }

        aX *= 0.98F;
        a(aW, aX);
        X = yaw; // TODO: this looks jerky
    }

    @Override
    public NPC getNPC() {
        return npc;
    }
}