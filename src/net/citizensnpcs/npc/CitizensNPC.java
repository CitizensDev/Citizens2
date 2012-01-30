package net.citizensnpcs.npc;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.ai.Navigator;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.api.npc.trait.trait.Spawned;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.resource.lib.CraftNPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CitizensNPC extends AbstractNPC {
    private CraftNPC mcEntity;
    private final CitizensNPCManager manager;

    public CitizensNPC(CitizensNPCManager manager, int id, String name) {
        super(id, name);
        this.manager = manager;
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
        getTrait(Spawned.class).setSpawned(false);

        return true;
    }

    @Override
    public Player getBukkitEntity() {
        return getHandle().getBukkitEntity();
    }

    public CraftNPC getHandle() {
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

        mcEntity = manager.spawn(this, loc);

        // Set the location
        // TODO: do this automatically (default traits?)
        // TODO: is spawned as a trait needed? Takes up memory
        if (!hasTrait(SpawnLocation.class)) {
            addTrait(new SpawnLocation(loc));
        } else {
            getTrait(SpawnLocation.class).setLocation(loc);
        }

        if (!hasTrait(Spawned.class)) {
            addTrait(new Spawned(true));
        } else {
            getTrait(Spawned.class).setSpawned(true);
        }
        return true;
    }
}