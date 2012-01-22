package net.citizensnpcs.npc;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.pathfinding.Navigator;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.resources.lib.CraftNPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CitizensNPC implements NPC {
    private final int id;
    private Character character;
    private final Map<Class<? extends Trait>, Trait> traits = new HashMap<Class<? extends Trait>, Trait>();
    private String name;
    private CraftNPC mcEntity;
    private boolean spawned;
    private final CitizensNPCManager manager;

    public CitizensNPC(String name, Character character) {
        this.name = name;
        this.character = character;
        manager = (CitizensNPCManager) CitizensAPI.getNPCManager();
        id = manager.getUniqueID();
    }

    public CitizensNPC(int id, String name, Character character) {
        this.name = name;
        this.character = character;
        manager = (CitizensNPCManager) CitizensAPI.getNPCManager();
        this.id = id;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public String getName() {
        return ChatColor.stripColor(name);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addTrait(Trait trait) {
        if (!hasTrait(trait.getClass()))
            traits.put(trait.getClass(), trait);
        else
            Messaging.debug("The NPC already has the trait '" + getTrait(trait.getClass()).getName() + "'.");
    }

    @Override
    public Character getCharacter() {
        return character;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Navigator getNavigator() {
        // TODO add default navigator
        return null;
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        Trait t = traits.get(clazz);
        return t != null ? clazz.cast(t) : null;
    }

    @Override
    public Iterable<Trait> getTraits() {
        return traits.values();
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public void removeTrait(Class<? extends Trait> trait) {
        if (!hasTrait(trait)) {
            Messaging.debug("The NPC does not have a trait with the name of '" + trait.getName() + ".");
            return;
        }
        traits.remove(trait);
    }

    @Override
    public void setCharacter(Character character) {
        if (this.character.equals(character)) {
            Messaging.debug("The NPC already has the character '" + character.getName() + "'.");
            return;
        }
        this.character = character;
    }

    @Override
    public boolean isSpawned() {
        return spawned;
    }

    @Override
    public void spawn(Location loc) {
        if (isSpawned()) {
            Messaging.debug("The NPC is already spawned.");
            return;
        }

        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled())
            return;

        if (mcEntity == null)
            mcEntity = manager.spawn(this, loc);
        else
            manager.spawn(this, loc);

        // Set the location
        addTrait(new SpawnLocation(loc));

        spawned = true;
    }

    @Override
    public void despawn() {
        if (!isSpawned()) {
            Messaging.debug("The NPC is already despawned.");
            return;
        }

        Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));

        manager.despawn(this);
        mcEntity.die();
        spawned = false;
    }

    @Override
    public void remove() {
        if (isSpawned())
            despawn();
        manager.remove(this);
    }

    @Override
    public Entity getBukkitEntity() {
        return mcEntity.getBukkitEntity();
    }

    public CraftNPC getHandle() {
        return mcEntity;
    }
}