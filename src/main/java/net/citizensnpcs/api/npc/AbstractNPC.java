package net.citizensnpcs.api.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.Trait;

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
    protected final Map<Class<? extends Trait>, Trait> traits = new HashMap<Class<? extends Trait>, Trait>();

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