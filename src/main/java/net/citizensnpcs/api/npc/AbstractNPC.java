package net.citizensnpcs.api.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public abstract class AbstractNPC implements NPC {
    private Character character;
    private final int id;
    private String name;
    private final List<Runnable> runnables = new ArrayList<Runnable>();
    private final Map<Class<? extends Trait>, Trait> traits = new HashMap<Class<? extends Trait>, Trait>();

    protected AbstractNPC(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void addTrait(Trait trait) {
        if (trait == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot register a null trait. Was it registered properly?");
            return;
        }
        if (trait instanceof Runnable) {
            runnables.add((Runnable) trait);
            if (traits.containsKey(trait.getClass()))
                runnables.remove(traits.get(trait.getClass()));
        }
        traits.put(trait.getClass(), trait);
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
    public List<MetadataValue> getMetadata(String key) {
        return METADATA.getMetadata(this, key);
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
        if (t == null)
            addTrait(CitizensAPI.getTraitManager().getTrait(clazz, this));

        return traits.get(clazz) != null ? clazz.cast(traits.get(clazz)) : null;
    }

    @Override
    public Iterable<Trait> getTraits() {
        return Collections.unmodifiableCollection(traits.values());
    }

    @Override
    public boolean hasMetadata(String key) {
        return METADATA.hasMetadata(this, key);
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public void load(DataKey root) throws NPCLoadException {
        Character character = CitizensAPI.getCharacterManager().getCharacter(root.getString("character"));

        // Load the character if it exists
        if (character != null) {
            character.load(root.getRelative("characters." + character.getName()));
            setCharacter(character);
        }

        // Load traits
        for (DataKey traitKey : root.getRelative("traits").getSubKeys()) {
            Trait trait = CitizensAPI.getTraitManager().getTrait(traitKey.name(), this);
            if (trait == null)
                throw new NPCLoadException("No trait with the name '" + traitKey.name()
                        + "' exists. Was it registered properly?");
            try {
                trait.load(traitKey);
            } catch (Exception ex) {
                Bukkit.getLogger().log(
                        Level.SEVERE,
                        "[Citizens] The trait '" + traitKey.name()
                                + "' failed to load properly for the NPC with the ID '" + getId() + "'. "
                                + ex.getMessage());
                ex.printStackTrace();
            }
            addTrait(trait);
        }

        // Spawn the NPC
        if (getTrait(Spawned.class).shouldSpawn())
            spawn(getTrait(SpawnLocation.class).getLocation());
    }

    @Override
    public void removeMetadata(String key, Plugin plugin) {
        METADATA.removeMetadata(this, key, plugin);
    }

    @Override
    public void removeTrait(Class<? extends Trait> trait) {
        if (traits.containsKey(trait)) {
            Trait t = traits.get(trait);
            if (t instanceof Runnable)
                runnables.remove(t);
            t.onRemove(this);
        }
        traits.remove(trait);
    }

    @Override
    public void save(DataKey root) {
        root.setString("name", getFullName());

        // Save the character if it exists
        if (getCharacter() != null) {
            root.setString("character", getCharacter().getName());
            getCharacter().save(root.getRelative("characters." + getCharacter().getName()));
        }

        // Save all existing traits
        for (Trait trait : getTraits())
            trait.save(root.getRelative("traits." + trait.getName()));
    }

    @Override
    public void setCharacter(Character character) {
        // If there was an old character, remove it
        if (this.character != null) {
            if (this.character instanceof Runnable)
                runnables.remove(this.character);
            this.character.onRemove(this);
        }
        // Set the new character
        this.character = character;
        if (character != null) {
            if (character instanceof Runnable)
                runnables.add((Runnable) character);
            character.onSet(this);
        }
    }

    @Override
    public void setMetadata(String key, MetadataValue value) {
        METADATA.setMetadata(this, key, value);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void update() {
        for (Runnable runnable : runnables)
            runnable.run();
    }

    private static final MetadataStoreBase<NPC> METADATA = new MetadataStoreBase<NPC>() {

        @Override
        protected String disambiguate(NPC subject, String metadataKey) {
            return Integer.toString(subject.getId()) + ":" + subject.getName() + ":" + metadataKey;
        }
    };
}