package net.citizensnpcs.npc;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;

public interface EntityController {
    void create(Location at, NPC npc);

    void die();

    Entity getBukkitEntity();

    void remove();

    void spawn(Location at, Consumer<Boolean> callback);
}
