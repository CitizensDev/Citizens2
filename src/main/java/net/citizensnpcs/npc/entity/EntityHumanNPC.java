package net.citizensnpcs.npc.entity;

import java.io.IOException;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.network.NPCNetHandler;
import net.citizensnpcs.npc.network.NPCNetworkManager;
import net.citizensnpcs.npc.network.NPCSocket;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Navigation;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
    private CitizensNPC npc;

    public EntityHumanNPC(MinecraftServer minecraftServer, World world, String string,
            ItemInWorldManager itemInWorldManager, NPC npc) {
        super(minecraftServer, world, string, itemInWorldManager);
        this.npc = (CitizensNPC) npc;
        itemInWorldManager.setGameMode(0);

        NPCSocket socket = new NPCSocket();
        NetworkManager netMgr = new NPCNetworkManager(socket, "npc mgr", new NetHandler() {
            @Override
            public boolean c() {
                return false;
            }
        });
        netServerHandler = new NPCNetHandler(minecraftServer, netMgr, this);
        netMgr.a(netServerHandler);

        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void b_(double x, double y, double z) {
        // when another entity collides, b_ is called to push the NPC
        // so we prevent b_ from doing anything.
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

    @Override
    public NPC getNPC() {
        return npc;
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
}