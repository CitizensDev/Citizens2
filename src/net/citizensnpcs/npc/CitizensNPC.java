package net.citizensnpcs.npc;

import java.util.HashSet;
import java.util.Set;

import net.citizensnpcs.api.Citizens;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.pathfinding.Navigator;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.LocationTrait;
import net.citizensnpcs.resources.lib.CraftNPC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class CitizensNPC implements NPC {
	private final int id;
	private Character character = null;
	private final Set<Trait> traits = new HashSet<Trait>();
	private String name;
	private CraftNPC mcEntity;
	private boolean spawned;
	private final CitizensNPCManager manager;

	protected CitizensNPC(String name, Character character, Trait... traits) {
		this.name = name;
		this.character = character;
		for (Trait trait : traits) {
			this.traits.add(trait);
		}
		manager = (CitizensNPCManager) Citizens.getNPCManager();
		id = manager.getUniqueID();
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
		if (!hasTrait(trait))
			traits.add(trait);
		else
			System.out.println("The NPC already has the trait '" + trait.getName() + "'.");
	}

	@Override
	public void addTrait(String name) {
		addTrait(getTrait(name));
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
	public Trait getTrait(String name) {
		for (Trait trait : traits) {
			if (trait.getName().equals(name)) {
				return trait;
			}
		}
		return null;
	}

	@Override
	public Iterable<Trait> getTraits() {
		return traits;
	}

	@Override
	public boolean hasTrait(Trait trait) {
		return traits.contains(trait);
	}

	@Override
	public boolean hasTrait(String name) {
		return hasTrait(getTrait(name));
	}

	@Override
	public void removeTrait(Trait trait) {
		if (!hasTrait(trait)) {
			System.out.println("The NPC does not have a trait with the name of '" + trait.getName() + ".");
			return;
		}
		traits.remove(trait);
	}

	@Override
	public void removeTrait(String name) {
		removeTrait(getTrait(name));
	}

	@Override
	public void setCharacter(Character character) {
		if (this.character.equals(character)) {
			System.out.println("The NPC already has the character '" + character.getName() + "'.");
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
		if (spawned) {
			System.out.println("The NPC is already spawned.");
			return;
		}

		NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
		Bukkit.getPluginManager().callEvent(spawnEvent);
		if (spawnEvent.isCancelled()) {
			return;
		}

		spawned = true;
		addTrait(new LocationTrait(loc));
		mcEntity = manager.spawn(this, loc);
	}

	@Override
	public void despawn() {
		if (!spawned) {
			System.out.println("The NPC is already despawned.");
			return;
		}

		Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));

		spawned = false;
		mcEntity.die();
		manager.despawn(this);
	}

	@Override
	public void remove() {
		if (spawned) {
			despawn();
		}
		manager.remove(this);
	}

	public CraftNPC getHandle() {
		return mcEntity;
	}
}