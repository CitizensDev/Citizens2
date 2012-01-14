package net.citizensnpcs.npc;

import java.util.HashSet;
import java.util.Set;

import net.citizensnpcs.api.Citizens;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.pathfinding.Navigator;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.npc.trait.LocationTrait;
import net.citizensnpcs.resources.lib.CraftNPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class CitizensNPC implements NPC {
	private final int id;
	private final Set<Trait> traits = new HashSet<Trait>();
	private Character character = null;
	private CraftNPC mcEntity;

	protected CitizensNPC(Character character, Trait... traits) {
		this.character = character;
		for (Trait trait : traits) {
			this.traits.add(trait);
		}
		this.id = ((CitizensNPCManager) Citizens.getNPCManager()).getUniqueID();
	}

	@Override
	public void addTrait(Trait trait) {
		if (!hasTrait(trait))
			traits.add(trait);
		else
			throw new IllegalArgumentException("The NPC already has the trait '" + trait.getName() + "'.");
	}

	@Override
	public void addTrait(String name) {
		addTrait(Citizens.getTraitManager().getTrait(name));
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
		return hasTrait(Citizens.getTraitManager().getTrait(name));
	}

	@Override
	public void removeTrait(Trait trait) {
		traits.remove(trait);
	}

	@Override
	public void removeTrait(String name) {
		removeTrait(Citizens.getTraitManager().getTrait(name));
	}

	@Override
	public void setCharacter(Character character) {
		if (this.character.equals(character))
			throw new IllegalArgumentException("The NPC already has the character '" + character.getName() + "'.");
		this.character = character;
	}

	@Override
	public void spawn(Location loc) {
		NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
		Bukkit.getPluginManager().callEvent(spawnEvent);
		if (spawnEvent.isCancelled()) {
			return;
		}
		addTrait(new LocationTrait(loc));
		mcEntity = ((CitizensNPCManager) Citizens.getNPCManager()).spawn(this);
	}

	@Override
	public void despawn() {
		mcEntity.die();
		((CitizensNPCManager) Citizens.getNPCManager()).despawn(this);
	}

	public CraftNPC getHandle() {
		return mcEntity;
	}
}