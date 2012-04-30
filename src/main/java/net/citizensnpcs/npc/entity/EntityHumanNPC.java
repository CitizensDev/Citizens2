package net.citizensnpcs.npc.entity;

import java.io.IOException;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.citizensnpcs.npc.network.NPCNetHandler;
import net.citizensnpcs.npc.network.NPCNetworkManager;
import net.citizensnpcs.npc.network.NPCSocket;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

public class EntityHumanNPC extends EntityPlayer implements NPCHandle {
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
    public void F_() {
        super.F_();
        npc.update();
    }

    public void moveOnCurrentHeading() {
        getControllerMove().c();
        getControllerLook().a();
        getControllerJump().b();
        if (this.aZ) {
            if (aT()) {
                this.motY += 0.03999999910593033D;
            } else if (aU()) {
                this.motY += 0.03999999910593033D;
            } else if (this.onGround && this.q == 0) {
                this.motY = 0.5;
                this.q = 10;
            }
        } else {
            this.q = 0;
        }

        aX *= 0.98F;
        this.a(aW, aX);
        X = yaw; // TODO: this looks jerky
    }

    @Override
    public NPC getNPC() {
        return this.npc;
    }
}