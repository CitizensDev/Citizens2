package net.citizensnpcs.api.npc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.ChatColor;
import org.bukkit.Location;

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
}