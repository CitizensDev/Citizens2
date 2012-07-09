package net.citizensnpcs.api.npc;

import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractNPC implements NPC {
    private final int id;
    private final MetadataStore metadata = new SimpleMetadataStore();
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

        if (trait.getNPC() == null) // link the trait
            trait.setNPC(this);

        if (trait instanceof Runnable) {
            runnables.add((Runnable) trait);
            if (traits.containsKey(trait.getClass()))
                runnables.remove(traits.get(trait.getClass()));
        }

        if (trait instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) trait, CitizensAPI.getPlugin());
        }
        traits.put(trait.getClass(), trait);
    }

    @Override
    public MetadataStore data() {
        return this.metadata;
    }

    @Override
    public void destroy() {
        Bukkit.getPluginManager().callEvent(new NPCRemoveEvent(this));
        runnables.clear();
        for (Trait trait : traits.values()) {
            if (trait instanceof Listener) {
                HandlerList.unregisterAll((Listener) trait);
            }
        }
        traits.clear();
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
        Trait trait = traits.get(clazz);
        if (trait == null) {
            trait = getTraitFor(clazz);
            addTrait(trait);
        }
        return trait != null ? clazz.cast(trait) : null;
    }

    protected abstract Trait getTraitFor(Class<? extends Trait> clazz);

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
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
    public void setName(String name) {
        this.name = name;
    }

    public void update() {
        for (int i = 0; i < runnables.size(); ++i)
            runnables.get(i).run();
    }
}