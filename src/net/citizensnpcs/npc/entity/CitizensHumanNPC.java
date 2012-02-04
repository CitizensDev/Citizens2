package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.EntityHumanNPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
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

    @Override
    public void update() {
        super.update();
        if (mcEntity == null)
            return;
        if (mcEntity.noDamageTicks > 0)
            mcEntity.noDamageTicks--;
        if (mcEntity.attackTicks > 0)
            mcEntity.attackTicks--;
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