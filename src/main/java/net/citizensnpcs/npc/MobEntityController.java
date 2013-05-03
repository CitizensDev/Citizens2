package net.citizensnpcs.npc;

import java.lang.reflect.Constructor;
import java.util.Map;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.World;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Maps;

public abstract class MobEntityController extends AbstractEntityController {
    private final Constructor<?> constructor;

    protected MobEntityController(Class<?> clazz) {
        this.constructor = getConstructor(clazz);
        NMS.registerEntityClass(clazz);
    }

    @Override
    protected LivingEntity createEntity(Location at, NPC npc) {
        EntityLiving entity = createEntityFromClass(((CraftWorld) at.getWorld()).getHandle(), npc);
        entity.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());

        // entity.onGround isn't updated right away - we approximate here so
        // that things like pathfinding still work *immediately* after spawn.
        org.bukkit.Material beneath = at.getBlock().getRelative(BlockFace.DOWN).getType();
        if (beneath.isBlock())
            entity.onGround = true;
        return (LivingEntity) entity.getBukkitEntity();
    }

    private EntityLiving createEntityFromClass(Object... args) {
        try {
            return (EntityLiving) constructor.newInstance(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
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