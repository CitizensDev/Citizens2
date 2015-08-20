package net.citizensnpcs.npc;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

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
    public boolean spawn(Location at, NPC npc) {
        bukkitEntity = createEntity(at, npc);
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity)bukkitEntity).getHandle();
        return couldSpawn(at, npc, entity);
    }

    protected boolean couldSpawn(Location at, NPC npc, net.minecraft.server.v1_8_R3.Entity entity) {
        return Util.isLoaded(at) && entity.world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}