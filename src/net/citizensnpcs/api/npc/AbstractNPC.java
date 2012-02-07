package net.citizensnpcs.api.npc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.api.npc.trait.trait.Spawned;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

public abstract class AbstractNPC implements NPC {
    protected final int id;
    protected final List<Runnable> runnables = Lists.newArrayList();
    protected final Map<Class<? extends Trait>, Trait> traits = new HashMap<Class<? extends Trait>, Trait>();
    protected String name;
    protected Character character;

    protected AbstractNPC(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void addTrait(Trait trait) {
        if (trait instanceof Runnable) {
            runnables.add((Runnable) trait);
            if (traits.containsKey(trait.getClass()))
                runnables.remove(traits.get(trait.getClass()));
        }
        traits.put(trait.getClass(), trait);
    }

    @Override
    public void chat(String message) {
        String formatted = "<" + getName() + "> " + message;
        for (Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(formatted);
    }

    @Override
    public Character getCharacter() {
        return character;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        String parsed = name;
        for (ChatColor color : ChatColor.values())
            if (parsed.contains("<" + color.getChar() + ">"))
                parsed = parsed.replace("<" + color.getChar() + ">", "");
        return parsed;
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        Trait t = traits.get(clazz);
        if (t == null) {
            t = CitizensAPI.getTraitManager().getInstance(clazz.getAnnotation(SaveId.class).value(), this);
            addTrait(t);
        }

        return t != null ? clazz.cast(t) : null;
    }

    @Override
    public Iterable<Trait> getTraits() {
        return Collections.unmodifiableCollection(traits.values());
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public void load(DataKey root) throws NPCLoadException {
        Character character = CitizensAPI.getCharacterManager().getInstance(root.getString("character"), this);

        // Load the character if it exists, otherwise remove the character
        if (character != null) {
            if (!character.getClass().isAnnotationPresent(SaveId.class))
                throw new NPCLoadException("Could not load character '" + root.getString("character")
                        + "'. SaveId annotation is missing.");
            character.load(root.getRelative("characters." + character.getClass().getAnnotation(SaveId.class).value()));
            setCharacter(character);
        }

        // Load traits
        for (DataKey traitKey : root.getRelative("traits").getSubKeys()) {
            Trait trait = CitizensAPI.getTraitManager().getInstance(traitKey.name(), this);
            if (trait == null)
                continue;
            if (!trait.getClass().isAnnotationPresent(SaveId.class))
                throw new NPCLoadException("Could not load trait '" + traitKey.name()
                        + "'. SaveId annotation is missing.");
            try {
                trait.load(traitKey);
            } catch (Exception ex) {
                Bukkit.getLogger().log(
                        Level.SEVERE,
                        "[Citizens] The trait '" + traitKey.name()
                                + "' failed to load properly for the NPC with the ID '" + getId() + "'. "
                                + ex.getMessage());
            }
            addTrait(trait);
        }

        // Spawn the NPC
        if (getTrait(Spawned.class).shouldSpawn())
            spawn(getTrait(SpawnLocation.class).getLocation());
    }

    @Override
    public void removeTrait(Class<? extends Trait> trait) {
        if (traits.containsKey(trait) && traits.get(trait) instanceof Runnable) {
            runnables.remove(traits.get(trait));
        }
        traits.remove(trait);
    }

    @Override
    public void save(DataKey root) {
        root.setString("name", getFullName());

        // Save the character if it exists
        if (getCharacter() != null) {
            root.setString("character", getCharacter().getClass().getAnnotation(SaveId.class).value());
            getCharacter().save(
                    root.getRelative("characters." + getCharacter().getClass().getAnnotation(SaveId.class).value()));
        }

        // Save all existing traits
        for (Trait trait : getTraits())
            trait.save(root.getRelative("traits." + trait.getClass().getAnnotation(SaveId.class).value()));
    }

    @Override
    public void setCharacter(Character character) {
        this.character = character;
    }

    @Override
    public void update() {
        for (Runnable runnable : runnables)
            runnable.run();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}