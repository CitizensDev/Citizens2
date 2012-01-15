package net.citizensnpcs.resources.lib;

import java.io.IOException;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}