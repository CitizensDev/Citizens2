package net.citizensnpcs.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;

public abstract class AbstractEntityController implements EntityController {
    private Entity bukkitEntity;

    public AbstractEntityController() {
    }

    public AbstractEntityController(Class<?> clazz) {
        NMS.registerEntityClass(clazz);
    }

    protected abstract Entity createEntity(Location at, NPC npc);

    @Override
    public Entity getBukkitEntity() {
        return bukkitEntity;
    }

    @Override
    public void remove() {
        if (bukkitEntity == null)
            return;
        bukkitEntity.remove();
        bukkitEntity = null;
    }

    @Override
    public void setEntity(Entity entity) {
        this.bukkitEntity = entity;
    }

    @Override
    public void spawn(Location at, NPC npc) {
        bukkitEntity = createEntity(at, npc);
    }
}