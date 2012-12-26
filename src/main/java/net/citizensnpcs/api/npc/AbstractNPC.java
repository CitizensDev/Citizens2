package net.citizensnpcs.api.npc;

import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.SimpleGoalController;
import net.citizensnpcs.api.ai.speech.SimpleSpeechController;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractNPC implements NPC {
    private final GoalController goalController = new SimpleGoalController();
    private final SpeechController speechController = new SimpleSpeechController(this);
    private final int id;
    protected final MetadataStore metadata = new SimpleMetadataStore();
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
        if (trait == null) {
            System.err.println("[Citizens] Cannot register a null trait. Was it registered properly?");
            return;
        }

        if (trait.getNPC() == null)
            trait.linkToNPC(this);

        // if an existing trait is being replaced, we need to remove the
        // currently registered runnable to avoid conflicts
        Trait replaced = traits.get(trait.getClass());

        Bukkit.getPluginManager().registerEvents(trait, CitizensAPI.getPlugin());
        traits.put(trait.getClass(), trait);
        if (isSpawned())
            trait.onSpawn();

        if (trait.isRunImplemented()) {
            if (replaced != null)
                runnables.remove(replaced);
            runnables.add(trait);
        }
    }

    @Override
    public MetadataStore data() {
        return this.metadata;
    }

    @Override
    public boolean despawn() {
        return despawn(DespawnReason.PLUGIN);
    }

    @Override
    public void destroy() {
        Bukkit.getPluginManager().callEvent(new NPCRemoveEvent(this));
        runnables.clear();
        for (Trait trait : traits.values()) {
            HandlerList.unregisterAll(trait);
            trait.onRemove();
        }
        traits.clear();
        CitizensAPI.getNPCRegistry().deregister(this);
    }

    @Override
    public GoalController getDefaultGoalController() {
        return goalController;
    }

    @Override
    public SpeechController getDefaultSpeechController() {
        return speechController;
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

    protected Trait getTraitFor(Class<? extends Trait> clazz) {
        return CitizensAPI.getTraitFactory().getTrait(clazz);
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
    public void removeTrait(Class<? extends Trait> traitClass) {
        Trait trait = traits.remove(traitClass);
        if (trait != null) {
            if (trait.isRunImplemented())
                runnables.remove(trait);
            HandlerList.unregisterAll(trait);
            trait.onRemove();
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void update() {
        for (int i = 0; i < runnables.size(); ++i)
            runnables.get(i).run();
        if (isSpawned())
            goalController.run();
    }
}