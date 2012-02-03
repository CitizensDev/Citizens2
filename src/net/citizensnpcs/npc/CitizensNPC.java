package net.citizensnpcs.npc;

import java.lang.reflect.Field;
import java.util.Map;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.ai.Navigator;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.api.npc.trait.trait.Spawned;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.Messaging;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;

public class CitizensNPC extends AbstractNPC {
    protected EntityLiving mcEntity;
    protected final CitizensNPCManager manager;

    private Map<Integer, Class<? extends net.minecraft.server.Entity>> intToClass;
    private Map<Class<? extends net.minecraft.server.Entity>, Integer> classToInt;
    private Class<? extends EntityLiving> entityClass;
    private double lookRange = 5;

    protected CitizensNPC(CitizensNPCManager manager, int id, String name, Class<? extends EntityLiving> entityClass) {
        super(id, name);
        this.manager = manager;
        if (intToClass == null || classToInt == null)
            createMaps();
        this.entityClass = entityClass;
        // No need to register entity class for normal human NPC entity
        if (entityClass != null)
            registerEntityClass(entityClass);
    }

    @Override
    public boolean despawn() {
        if (!isSpawned()) {
            Messaging.debug("The NPC with the ID '" + getId() + "' is already despawned.");
            return false;
        }

        Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));

        manager.despawn(this);
        mcEntity = null;

        return true;
    }

    @Override
    public Entity getBukkitEntity() {
        return getHandle().getBukkitEntity();
    }

    public EntityLiving getHandle() {
        return mcEntity;
    }

    @Override
    public Navigator getNavigator() {
        return new CitizensNavigator(this);
    }

    @Override
    public boolean isSpawned() {
        return getHandle() != null;
    }

    @Override
    public void remove() {
        if (isSpawned())
            despawn();
        manager.remove(this);
    }

    @Override
    public boolean spawn(Location loc) {
        if (isSpawned()) {
            Messaging.debug("The NPC with the ID '" + getId() + "' is already spawned.");
            return false;
        }

        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled())
            return false;

        if (entityClass != null) {
            mcEntity = createEntityFromClass(getWorldServer(loc.getWorld()));
            mcEntity.setPosition(loc.getX(), loc.getY(), loc.getZ());
            mcEntity.world.addEntity(mcEntity);
        }

        // Set the location
        addTrait(new SpawnLocation(loc));
        // Set the spawned state
        addTrait(new Spawned(true));
        return true;
    }

    protected WorldServer getWorldServer(World world) {
        return ((CraftWorld) world).getHandle();
    }

    protected MinecraftServer getMinecraftServer(Server server) {
        return ((CraftServer) server).getServer();
    }

    public void tick() {
        if (mcEntity != null)
            if (getTrait(LookClose.class).shouldLookClose()
                    && mcEntity.world.findNearbyPlayer(mcEntity, lookRange) != null)
                faceEntity(mcEntity.world.findNearbyPlayer(mcEntity, lookRange).getBukkitEntity());
    }

    protected void faceEntity(Entity target) {
        if (getBukkitEntity().getWorld() != target.getWorld())
            return;
        Location loc = getBukkitEntity().getLocation();
        Location targetLoc = target.getLocation();

        double xDiff = targetLoc.getX() - loc.getX();
        double yDiff = targetLoc.getY() - loc.getY();
        double zDiff = targetLoc.getZ() - loc.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
        double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        mcEntity.yaw = (float) yaw - 90;
        mcEntity.pitch = (float) pitch;
    }

    private void registerEntityClass(Class<? extends net.minecraft.server.Entity> clazz) {
        if (classToInt == null || intToClass == null)
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && net.minecraft.server.Entity.class.isAssignableFrom(search)) {
            if (!classToInt.containsKey(search))
                continue;
            int code = classToInt.get(search);
            intToClass.put(code, clazz);
            classToInt.put(clazz, code);
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass");
    }

    @SuppressWarnings("unchecked")
    private void createMaps() {
        try {
            Field field = EntityTypes.class.getDeclaredField("d");
            field.setAccessible(true);
            intToClass = (Map<Integer, Class<? extends net.minecraft.server.Entity>>) field.get(null);
            field = EntityTypes.class.getDeclaredField("e");
            field.setAccessible(true);
            classToInt = (Map<Class<? extends net.minecraft.server.Entity>, Integer>) field.get(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private EntityLiving createEntityFromClass(net.minecraft.server.World world) {
        try {
            return entityClass.getConstructor(net.minecraft.server.World.class).newInstance(world);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}