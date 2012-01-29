package net.citizensnpcs.api.npc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.api.npc.trait.trait.Spawned;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class AbstractNPC implements NPC {
    protected final int id;
    protected final Map<Class<? extends Trait>, Trait> traits = new HashMap<Class<? extends Trait>, Trait>();
    protected String name;
    protected Character character;

    protected AbstractNPC(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
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
        Location prev = this.getBukkitEntity().getLocation();
        despawn();
        this.name = name;
        spawn(prev);
    }

    @Override
    public void addTrait(Trait trait) {
        traits.put(trait.getClass(), trait);
    }

    @Override
    public void removeTrait(Class<? extends Trait> trait) {
        traits.remove(trait);
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        Trait t = traits.get(clazz);
        return t != null ? clazz.cast(t) : null;
    }

    @Override
    public Iterable<Trait> getTraits() {
        return Collections.unmodifiableCollection(this.traits.values());
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public Character getCharacter() {
        return character;
    }

    @Override
    public void setCharacter(Character character) {
        this.character = character;
    }

    @Override
    public void chat(String message) {
        String formatted = "<" + getName() + "> " + message;
        for (Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(formatted);
    }

    @Override
    public void save(DataKey root) {
        root.setString("name", getFullName());
        if (!root.keyExists("spawned"))
            root.setBoolean("spawned", true);
        if (root.getBoolean("spawned"))
            root.setBoolean("spawned", getTrait(Spawned.class).isSpawned());

        // Save the character if it exists
        if (getCharacter() != null) {
            root.setString("character", getCharacter().getName());
            getCharacter().save(root.getRelative("characters." + getCharacter().getName()));
        }

        // Save all existing traits
        for (Trait trait : getTraits())
            trait.save(root.getRelative(trait.getName()));
    }

    @Override
    public void load(DataKey root) {
        Character character = CitizensAPI.getCharacterManager().getInstance(root.getString("character"), this);

        // Load the character if it exists, otherwise remove the character
        if (character != null) {
            character.load(root.getRelative("characters." + character.getName()));
            setCharacter(character);
        }

        // Load traits
        for (DataKey traitKey : root.getSubKeys()) {
            Trait trait = CitizensAPI.getTraitManager().getInstance(traitKey.name(), this);
            if (trait == null)
                continue;
            trait.load(traitKey);
            addTrait(trait);
        }

        // Spawn the NPC
        if (root.getBoolean("spawned"))
            spawn(getTrait(SpawnLocation.class).getLocation());
    }
}