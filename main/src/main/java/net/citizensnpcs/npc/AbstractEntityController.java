package net.citizensnpcs.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public abstract class AbstractEntityController implements EntityController {
    private Entity bukkitEntity;

    public AbstractEntityController() {
    }

    @Override
    public void create(Location at, NPC npc) {
        bukkitEntity = createEntity(at, npc);
        if (npc != null) {
            bukkitEntity.setMetadata("NPC", new FixedMetadataValue(CitizensAPI.getPlugin(), true));
            bukkitEntity.setMetadata("NPC-ID", new FixedMetadataValue(CitizensAPI.getPlugin(), npc.getId()));
        }
    }

    protected abstract Entity createEntity(Location at, NPC npc);

    @Override
    public void die() {
        if (bukkitEntity == null)
            return;
        bukkitEntity.removeMetadata("NPC", CitizensAPI.getPlugin());
        bukkitEntity.removeMetadata("NPC-ID", CitizensAPI.getPlugin());
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
        bukkitEntity.removeMetadata("NPC", CitizensAPI.getPlugin());
        bukkitEntity.removeMetadata("NPC-ID", CitizensAPI.getPlugin());
        bukkitEntity = null;
    }

    @Override
    public boolean spawn(Location at) {
        return !Util.isLoaded(at) ? false : NMS.addEntityToWorld(bukkitEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}