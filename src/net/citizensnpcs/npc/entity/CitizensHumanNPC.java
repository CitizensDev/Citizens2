package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityHumanNPC;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;

public class CitizensHumanNPC extends CitizensNPC {

    public CitizensHumanNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, null);
    }

    @Override
    public boolean spawn(Location loc) {
        if (super.spawn(loc)) {
            WorldServer ws = getWorldServer(loc.getWorld());
            mcEntity = new EntityHumanNPC(getMinecraftServer(ws.getServer()), ws, getFullName(),
                    new ItemInWorldManager(ws));
            ((EntityHumanNPC) mcEntity).removeFromPlayerMap(getFullName());
            mcEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            ws.addEntity(mcEntity);
            ws.players.remove(mcEntity);
            return true;
        }
        return false;
    }
}