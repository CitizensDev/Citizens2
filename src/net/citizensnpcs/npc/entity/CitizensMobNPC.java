package net.citizensnpcs.npc.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityTypes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.LivingEntity;

public abstract class CitizensMobNPC extends CitizensNPC {
    private final Constructor<? extends EntityLiving> constructor;

    protected CitizensMobNPC(CitizensNPCManager manager, int id, String name, Class<? extends EntityLiving> clazz) {
        super(manager, id, name);
        try {
            this.constructor = clazz.getConstructor(World.class);
        } catch (Exception ex) {
            throw new IllegalStateException("unable to find an entity constructor");
        }
        if (!classToInt.containsKey(clazz))
            registerEntityClass(clazz);
    }

    private EntityLiving createEntityFromClass(net.minecraft.server.World world) {
        try {
            return constructor.newInstance(world);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected EntityLiving createHandle(Location loc) {
        EntityLiving entity = createEntityFromClass(((CraftWorld) loc.getWorld()).getHandle());
        mcEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        return entity;
    }

    @Override
    public LivingEntity getBukkitEntity() {
        return (LivingEntity) getHandle().getBukkitEntity();
    }

    private static Map<Class<? extends Entity>, Integer> classToInt;
    private static Map<Integer, Class<? extends Entity>> intToClass;

    private static void registerEntityClass(Class<? extends Entity> clazz) {
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            if (!classToInt.containsKey(search))
                continue;
            int code = classToInt.get(search);
            intToClass.put(code, clazz);
            classToInt.put(clazz, code);
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass");
    }

    static {
        try {
            Field field = EntityTypes.class.getDeclaredField("d");
            field.setAccessible(true);
            intToClass = (Map<Integer, Class<? extends Entity>>) field.get(null);
            field = EntityTypes.class.getDeclaredField("e");
            field.setAccessible(true);
            classToInt = (Map<Class<? extends Entity>, Integer>) field.get(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException(
                    "Unable to fetch entity class mapping - is Citizens updated for this version of CraftBukkit?");
        }
    }
}
