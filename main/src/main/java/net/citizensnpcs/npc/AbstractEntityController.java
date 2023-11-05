package net.citizensnpcs.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public abstract class AbstractEntityController implements EntityController {
    private Entity bukkitEntity;

    public AbstractEntityController() {
    }

    public AbstractEntityController(Class<?> clazz) {
        NMS.registerEntityClass(clazz);
    }

    @Override
    public void create(Location at, NPC npc) {
        bukkitEntity = createEntity(at, npc);
    }

    protected abstract Entity createEntity(Location at, NPC npc);

    @Override
    public void die() {
        bukkitEntity = null;
    }

    @Override
    public Entity getBukkitEntity() {
        return bukkitEntity;
    }

    @Override
    public void remove() {
        if (bukkitEntity == null)
            return;
        if (bukkitEntity instanceof Player) {
            NMS.removeFromWorld(bukkitEntity);
            NMS.remove(bukkitEntity);
        } else {
            bukkitEntity.remove();
        }
        bukkitEntity = null;
    }

    @Override
    public boolean spawn(Location at) {
        return !Util.isLoaded(at) ? false : NMS.addEntityToWorld(bukkitEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}