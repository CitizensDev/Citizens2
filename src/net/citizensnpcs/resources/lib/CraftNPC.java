package net.citizensnpcs.resources.lib;

import java.io.IOException;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.World;

public class CraftNPC extends EntityPlayer {

	public CraftNPC(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager) {
		super(minecraftserver, world, s, iteminworldmanager);
		iteminworldmanager.setGameMode(0);

		NPCSocket socket = new NPCSocket();

		NetworkManager netMgr = new NPCNetworkManager(socket, "npc mgr", new NetHandler() {
			@Override
			public boolean c() {
				return false;
			}
		});
		this.netServerHandler = new NPCNetHandler(minecraftserver, netMgr, this);
		netMgr.a(this.netServerHandler);

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}