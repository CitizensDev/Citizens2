package net.citizensnpcs.resources.lib;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.World;

public class CraftNPC extends EntityPlayer {

	public CraftNPC(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager) {
		super(minecraftserver, world, s, iteminworldmanager);
	}
}