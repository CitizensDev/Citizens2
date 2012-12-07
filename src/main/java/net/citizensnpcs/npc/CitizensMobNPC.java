package net.citizensnpcs.npc;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_4_5.EntityLiving;
import net.minecraft.server.v1_4_5.World;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_4_5.CraftWorld;

import com.google.common.collect.Maps;

public abstract class CitizensMobNPC extends CitizensNPC {
    private final Constructor<?> constructor;

    protected CitizensMobNPC(int id, String name, Class<?> clazz) {
        super(id, name);
        this.constructor = getConstructor(clazz);
        NMS.registerEntityClass(clazz);
    }

    private EntityLiving createEntityFromClass(Object... args) {
        try {
            Object[] newArgs = Arrays.copyOf(args, args.length + 1);
            newArgs[args.length] = this;
            return (EntityLiving) constructor.newInstance(newArgs);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected EntityLiving createHandle(Location loc) {
        EntityLiving entity = createEntityFromClass(((CraftWorld) loc.getWorld()).getHandle());
        entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        // entity.onGround isn't updated right away - we approximate here so
        // that things like pathfinding still work *immediately* after spawn.
        org.bukkit.Material beneath = loc.getBlock().getRelative(BlockFace.DOWN).getType();
        if (beneath.isBlock())
            entity.onGround = true;
        return entity;
    }

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = Maps.newHashMap();

    private static Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?> constructor = CONSTRUCTOR_CACHE.get(clazz);
        if (constructor != null)
            return constructor;
        try {
            return clazz.getConstructor(World.class, NPC.class);
        } catch (Exception ex) {
            throw new IllegalStateException("unable to find an entity constructor");
        }
    }
}