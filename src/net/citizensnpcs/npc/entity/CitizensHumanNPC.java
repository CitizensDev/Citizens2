package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.EntityHumanNPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

public class CitizensHumanNPC extends CitizensNPC {
    public CitizensHumanNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name);
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) getHandle().getBukkitEntity();
    }

    protected static MinecraftServer getMinecraftServer(Server server) {
        return ((CraftServer) server).getServer();
    }

    @Override
    protected EntityLiving createHandle(Location loc) {
        WorldServer ws = ((CraftWorld) loc.getWorld()).getHandle();
        EntityHumanNPC handle = new EntityHumanNPC(ws.getServer().getServer(), ws, getFullName(),
                new ItemInWorldManager(ws));
        handle.removeFromPlayerMap(getFullName());
        handle.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        return handle;
    }
}