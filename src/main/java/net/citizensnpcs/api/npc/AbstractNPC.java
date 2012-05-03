package net.citizensnpcs.api.npc;

import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractNPC implements NPC {
    private Character character;
    private final int id;
    private String name;
    protected final List<Runnable> runnables = Lists.newArrayList();
    protected final Map<Class<? extends Trait>, Trait> traits = Maps.newHashMap();

    protected AbstractNPC(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void addTrait(Class<? extends Trait> clazz) {
        addTrait(getTraitFor(clazz));
    }

    @Override
    public void addTrait(Trait trait) {
        // TODO: right now every addTrait call has to be wrapped with
        // TraitManager.getTrait(Class, NPC) -- this is bad, need to fix this.
        if (trait == null) {
            System.err.println("[Citizens] Cannot register a null trait. Was it registered properly?");
            return;
        }

        if (trait instanceof Runnable) {
            runnables.add((Runnable) trait);
            if (traits.containsKey(trait.getClass()))
                runnables.remove(traits.get(trait.getClass()));
        }

        if (trait instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) trait, trait.getPlugin());
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
        Trait trait = traits.get(clazz);
        if (trait == null) {
            trait = getTraitFor(clazz);
            addTrait(trait);
        }
        return trait != null ? clazz.cast(trait) : null;
    }

    protected abstract Trait getTraitFor(Class<? extends Trait> clazz);

    @Override
    public boolean hasMetadata(String key) {
        return METADATA.hasMetadata(this, key);
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public void remove() {
        runnables.clear();
        for (Trait trait : traits.values()) {
            if (trait instanceof Listener) {
                HandlerList.unregisterAll((Listener) trait);
            }
        }
        traits.clear();
    }

    @Override
    public void removeMetadata(String key, Plugin plugin) {
        METADATA.removeMetadata(this, key, plugin);
    }

    @Override
    public void removeTrait(Class<? extends Trait> trait) {
        Trait t = traits.remove(trait);
        if (t != null) {
            if (t instanceof Runnable)
                runnables.remove(t);
            t.onRemove();
        }
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
        for (int i = 0; i < runnables.size(); ++i)
            runnables.get(i).run();
    }

    private static final MetadataStoreBase<NPC> METADATA = new MetadataStoreBase<NPC>() {

        @Override
        protected String disambiguate(NPC subject, String metadataKey) {
            return Integer.toString(subject.getId()) + ":" + subject.getName() + ":" + metadataKey;
        }
    };
}