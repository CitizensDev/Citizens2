package net.citizensnpcs.npc;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface EntityController {
    LivingEntity getBukkitEntity();

    void remove();

    void spawn(Location at, NPC npc);
}
