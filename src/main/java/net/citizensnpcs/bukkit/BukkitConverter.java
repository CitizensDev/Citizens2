package net.citizensnpcs.bukkit;

import net.citizensnpcs.api.abstraction.ItemStack;
import net.citizensnpcs.api.abstraction.MobType;
import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.Entity;
import net.citizensnpcs.api.abstraction.entity.NPCHolder;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;

public class BukkitConverter {

    public static Entity toEntity(org.bukkit.entity.Entity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    public static WorldVector toWorldVector(Location location) {
        // TODO Auto-generated method stub
        return null;
    }

    public static World toWorld(org.bukkit.World world) {
        // TODO Auto-generated method stub
        return null;
    }

    public static MobType toMobType(EntityType type) {
        // TODO Auto-generated method stub
        return null;
    }

    public static org.bukkit.inventory.ItemStack fromItemStack(ItemStack itemStack) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ItemStack toItemStack(org.bukkit.inventory.ItemStack itemInHand) {
        // TODO Auto-generated method stub
        return null;
    }

    public static NPC toNPC(org.bukkit.entity.Entity entity) {
        net.minecraft.server.Entity handle = ((CraftEntity) entity).getHandle();
        if (handle instanceof NPCHolder) {
            return ((NPCHolder) handle).getNPC();
        }
        return null;
    }

    public static boolean isNPC(org.bukkit.entity.Entity entity) {
        return toNPC(entity) != null;
    }

    public static Player toPlayer(org.bukkit.entity.Player player) {
        return new BukkitPlayer(player);
    }
}
