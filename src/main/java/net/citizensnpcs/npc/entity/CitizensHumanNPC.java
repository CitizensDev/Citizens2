package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.util.StringHelper;

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
    protected EntityLiving createHandle(Location loc) {
        WorldServer ws = ((CraftWorld) loc.getWorld()).getHandle();
        EntityHumanNPC handle = new EntityHumanNPC(ws.getServer().getServer(), ws,
                StringHelper.parseColors(getFullName()), new ItemInWorldManager(ws));
        handle.removeFromPlayerMap(getFullName());
        handle.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        return handle;
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) getHandle().getBukkitEntity();
    }

    @Override
    public EntityHumanNPC getHandle() {
        return (EntityHumanNPC) mcEntity;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        super.load(key);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        Location prev = getBukkitEntity().getLocation();
        despawn();
        spawn(prev);
    }

    @Override
    public void update() {
        super.update();
        if (mcEntity == null)
            return;
    }
}