package net.citizensnpcs.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
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
        if (bukkitEntity instanceof Player) {
            NMS.removeFromWorld(bukkitEntity);
            SkinnableEntity npc = bukkitEntity instanceof SkinnableEntity ? (SkinnableEntity) bukkitEntity : null;
            npc.getSkinTracker().onRemoveNPC();
            NMS.remove(bukkitEntity);
            setEntity(null);
        } else {
            bukkitEntity.remove();
            bukkitEntity = null;
        }
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