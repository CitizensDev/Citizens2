package net.citizensnpcs.npc;

import java.lang.reflect.Constructor;
import java.util.Map;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMSReflection;
import net.minecraft.server.Block;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.World;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;

import com.google.common.collect.Maps;

public abstract class CitizensMobNPC extends CitizensNPC {
    private final Constructor<? extends EntityLiving> constructor;
    protected CitizensMobNPC(int id, String name, Class<? extends EntityLiving> clazz) {
        super(id, name);
        this.constructor = getConstructor(clazz);

        NMSReflection.registerEntityClass(clazz);
    }

    private EntityLiving createEntityFromClass(World world) {
        try {
            return constructor.newInstance(world, this);
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
        if (beneath.isBlock()) {
            Block block = Block.byId[beneath.getId()];
            if (block != null && block.material != null) {
                entity.onGround = block.material.isSolid();
            }
        }
        return entity;
    }

    private static final Map<Class<? extends EntityLiving>, Constructor<? extends EntityLiving>> CONSTRUCTOR_CACHE = Maps
            .newHashMap();

    private static Constructor<? extends EntityLiving> getConstructor(Class<? extends EntityLiving> clazz) {
        Constructor<? extends EntityLiving> constructor = CONSTRUCTOR_CACHE.get(clazz);
        if (constructor != null)
            return constructor;
        try {
            return clazz.getConstructor(World.class, NPC.class);
        } catch (Exception ex) {
            throw new IllegalStateException("unable to find an entity constructor");
        }
    }
}