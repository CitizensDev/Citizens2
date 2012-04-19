package net.citizensnpcs.npc.entity;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.citizensnpcs.npc.network.NPCNetHandler;
import net.citizensnpcs.npc.network.NPCNetworkManager;
import net.citizensnpcs.npc.network.NPCSocket;
import net.citizensnpcs.util.Messaging;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;

@SuppressWarnings("unchecked")
public class EntityHumanNPC extends EntityPlayer implements NPCHandle {
    private CitizensNPC npc;

    public EntityHumanNPC(MinecraftServer minecraftServer, World world, String string,
            ItemInWorldManager itemInWorldManager, CitizensNPC npc) {
        super(minecraftServer, world, string, itemInWorldManager);
        this.npc = npc;
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
    public void d_() {
        super.d_();
        npc.update();
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (bukkitEntity == null) {
            super.getBukkitEntity();
            removeFromPlayerMap(name);
            // Bukkit uses a map of player names to CraftPlayer instances to
            // solve a reconnect issue, so NPC names will conflict with ordinary
            // player names. Workaround.
        }
        return super.getBukkitEntity();
    }

    public void moveOnCurrentHeading() {
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

    public void removeFromPlayerMap(String name) {
        if (players != null) {
            players.remove(name);
        }
    }

    private static Map<String, CraftPlayer> players;

    static {
        try {
            Field f = CraftEntity.class.getDeclaredField("players");
            f.setAccessible(true);
            players = (Map<String, CraftPlayer>) f.get(null);
        } catch (Exception ex) {
            Messaging.log("Unable to fetch player map from CraftEntity: " + ex.getMessage());
        }
    }

    @Override
    public NPC getNPC() {
        return this.npc;
    }
}