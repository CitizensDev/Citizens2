package net.citizensnpcs.npc;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public abstract class AbstractEntityController implements EntityController {
    private LivingEntity bukkitEntity;

    protected abstract LivingEntity createEntity(Location at, NPC npc);

    @Override
    public LivingEntity getBukkitEntity() {
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
    public void spawn(Location at, NPC npc) {
        bukkitEntity = createEntity(at, npc);
    }
}