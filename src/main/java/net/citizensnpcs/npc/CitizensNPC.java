package net.citizensnpcs.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.ai.CitizensAI;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public abstract class CitizensNPC extends AbstractNPC {
    private final CitizensAI ai = new CitizensAI(this);
    private final CitizensNPCManager manager;
    private final CitizensTraitManager traitManager;
    protected EntityLiving mcEntity;

    protected CitizensNPC(CitizensNPCManager manager, int id, String name) {
        super(id, name);
        this.manager = manager;
        traitManager = (CitizensTraitManager) CitizensAPI.getTraitManager();
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
        if (trait instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) trait, trait.getPlugin());

        Map<Class<? extends Trait>, Trait> map = traits.get(trait.getPlugin());
        if (map == null)
            map = new HashMap<Class<? extends Trait>, Trait>();
        map.put(trait.getClass(), trait);
        traits.put(trait.getPlugin(), map);
    }

    @Override
    public void chat(Player player, String message) {
        Messaging.sendWithNPC(player, Setting.CHAT_PREFIX.asString() + message, this);
    }

    @Override
    public void chat(String message) {
        for (Player player : Bukkit.getOnlinePlayers())
            chat(player, message);
    }

    protected abstract EntityLiving createHandle(Location loc);

    @Override
    public boolean despawn() {
        if (!isSpawned()) {
            Messaging.debug("The NPC with the ID '" + getId() + "' is already despawned.");
            return false;
        }

        Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));

        manager.despawn(this, getTrait(Spawned.class).shouldSpawn());
        mcEntity = null;

        return true;
    }

    @Override
    public CitizensAI getAI() {
        return ai;
    }

    @Override
    public LivingEntity getBukkitEntity() {
        return (LivingEntity) getHandle().getBukkitEntity();
    }

    public EntityLiving getHandle() {
        return mcEntity;
    }

    @Override
    public org.bukkit.inventory.Inventory getInventory() {
        Inventory inventory = Bukkit.getServer().createInventory(this, 36, StringHelper.parseColors(getFullName()));
        inventory.setContents(getTrait(net.citizensnpcs.api.trait.trait.Inventory.class).getContents());
        return inventory;
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        Trait trait = null;
        for (Plugin plugin : traits.keySet())
            if (traits.get(plugin).containsKey(clazz))
                trait = traits.get(plugin).get(clazz);
        if (trait == null)
            trait = traitManager.getTrait(clazz, this);

        addTrait(trait);
        return trait != null ? clazz.cast(trait) : null;
    }

    @Override
    public boolean isSpawned() {
        return getHandle() != null;
    }

    @Override
    public void remove() {
        super.remove();
        manager.remove(this);
        if (isSpawned())
            despawn();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public boolean spawn(Location loc) {
        if (isSpawned()) {
            Messaging.debug("The NPC with the ID '" + getId() + "' is already spawned.");
            return false;
        }

        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled())
            return false;

        mcEntity = createHandle(loc);

        mcEntity.world.addEntity(mcEntity);
        mcEntity.world.players.remove(mcEntity);

        // Set the location
        getTrait(CurrentLocation.class).spawn(loc);
        // Set the spawned state
        getTrait(Spawned.class).setSpawned(true);

        // Modify NPC using traits after the entity has been created
        for (Plugin plugin : traits.keySet())
            for (Trait trait : getTraits(plugin))
                trait.onNPCSpawn();
        return true;
    }

    @Override
    public void update() {
        super.update();
        ai.update();
    }

    public void load(DataKey root) throws NPCLoadException {
        Character character = CitizensAPI.getCharacterManager().getCharacter(root.getString("character"));

        // Load the character if it exists
        if (character != null) {
            character.load(root.getRelative("characters." + character.getName()));
            setCharacter(character);
        }

        // Load traits
        for (DataKey traitKey : root.getRelative("traits").getSubKeys()) {
            Trait trait = traitManager.getTrait(traitKey.name(), this);
            if (trait == null)
                throw new NPCLoadException("No trait with the name '" + traitKey.name()
                        + "' exists. Was it registered properly?");
            addTrait(trait);
            try {
                getTrait(trait.getClass()).load(traitKey);
            } catch (Exception ex) {
                Bukkit.getLogger().log(
                        Level.SEVERE,
                        "[Citizens] The trait '" + traitKey.name()
                                + "' failed to load properly for the NPC with the ID '" + getId() + "'. "
                                + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Spawn the NPC
        if (getTrait(Spawned.class).shouldSpawn())
            spawn(getTrait(CurrentLocation.class).getLocation());
    }

    public void save(DataKey root) {
        root.setString("name", getFullName());

        // Save the character if it exists
        if (getCharacter() != null) {
            root.setString("character", getCharacter().getName());
            getCharacter().save(root.getRelative("characters." + getCharacter().getName()));
        }

        // Save all existing traits
        for (Plugin plugin : traits.keySet())
            for (Trait trait : getTraits(plugin))
                trait.save(root.getRelative("traits." + trait.getName()));
    }
}