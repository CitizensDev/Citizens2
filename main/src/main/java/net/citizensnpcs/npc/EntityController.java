package net.citizensnpcs.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;

public interface EntityController {
    Entity getBukkitEntity();

    void remove();

    void setEntity(Entity entity);

    void spawn(Location at, NPC npc);
}
