package net.citizensnpcs.resources.lib;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import net.citizensnpcs.util.Messaging;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;

@SuppressWarnings("unchecked")
public class CraftNPC extends EntityPlayer {

    public CraftNPC(MinecraftServer minecraftServer, World world, String string, ItemInWorldManager itemInWorldManager) {
        super(minecraftServer, world, string, itemInWorldManager);
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
    public Entity getBukkitEntity() {
        if (this.bukkitEntity == null) {
            super.getBukkitEntity();
            removeFromPlayerMap(name);
            // Bukkit uses a map of player names to CraftPlayer instances to
            // solve a reconnect issue, so NPC names will conflict with ordinary
            // player names. Workaround.
        }
        return super.getBukkitEntity();
    }

    public void removeFromPlayerMap(String name) {
        if (players != null)
            players.remove(name);
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
}