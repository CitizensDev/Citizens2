package net.citizensnpcs.npc;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.ai.Navigator;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.resources.lib.CraftNPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CitizensNPC extends AbstractNPC {
    private CraftNPC mcEntity;
    private boolean spawned;
    private final CitizensNPCManager manager;

    public CitizensNPC(CitizensNPCManager manager, int id, String name) {
        super(id, name);
        this.manager = manager;
    }

    @Override
    public boolean despawn() {
        if (!isSpawned()) {
            Messaging.debug("The NPC with the ID '" + getId() + "' is already despawned.");
            return false;
        }

        Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));

        manager.despawn(this);
        getHandle().die();

        spawned = false;
        save();
        return true;
    }

    @Override
    public Entity getBukkitEntity() {
        return getHandle().getBukkitEntity();
    }

    public CraftNPC getHandle() {
        return mcEntity;
    }

    @Override
    public Navigator getNavigator() {
        return new CitizensNavigator(this);
    }

    @Override
    public boolean isSpawned() {
        return spawned;
    }

    @Override
    public void remove() {
        if (isSpawned())
            despawn();
        manager.remove(this);
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

        if (mcEntity == null)
            mcEntity = manager.spawn(this, loc);
        else
            manager.spawn(this, loc);

        // Set the location
        addTrait(new SpawnLocation(loc));

        spawned = true;
        save();
        return true;
    }

    @Override
    public void chat(String message) {
        String formatted = "<" + getName() + "> " + message;
        for (Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(formatted);
        if (Setting.PRINT_CHAT_TO_CONSOLE.getBoolean())
            Messaging.log(formatted);
    }

    public void save() {
        DataKey key = Citizens.getNPCStorage().getKey("npc." + getId());
        key.setString("name", getFullName());
        if (!key.keyExists("spawned"))
            key.setBoolean("spawned", true);
        if (key.getBoolean("spawned"))
            key.setBoolean("spawned", !getBukkitEntity().isDead());

        // Save the character if it exists
        if (getCharacter() != null) {
            key.setString("character", getCharacter().getName());
            getCharacter().save(key.getRelative("characters." + getCharacter().getName()));
        }

        // Save all existing traits
        for (Trait trait : getTraits())
            trait.save(key.getRelative(trait.getName()));
    }
}