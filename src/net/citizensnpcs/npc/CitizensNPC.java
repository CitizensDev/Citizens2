package net.citizensnpcs.npc;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.trait.trait.Inventory;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.api.npc.trait.trait.Spawned;
import net.citizensnpcs.npc.ai.CitizensAI;
import net.citizensnpcs.util.Messaging;

import net.minecraft.server.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class CitizensNPC extends AbstractNPC {
    protected final CitizensNPCManager manager;
    protected final CitizensAI ai = new CitizensAI(this);
    protected EntityLiving mcEntity;
    protected final NPCInventory inventory;

    protected CitizensNPC(CitizensNPCManager manager, int id, String name) {
        super(id, name);
        this.manager = manager;
        inventory = new NPCInventory(this);
    }

    @Override
    public void chat(String message) {
        for (Player player : Bukkit.getOnlinePlayers())
            chat(player, message);
    }

    @Override
    public void chat(Player player, String message) {
        Messaging.sendWithNPC(player, Setting.CHAT_PREFIX.asString() + message, this);
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
    public LivingEntity getBukkitEntity() {
        return (LivingEntity) getHandle().getBukkitEntity();
    }

    public EntityLiving getHandle() {
        return mcEntity;
    }

    @Override
    public CitizensAI getAI() {
        return ai;
    }

    @Override
    public boolean isSpawned() {
        return getHandle() != null;
    }

    @Override
    public void remove() {
        if (isSpawned())
            despawn();
        manager.remove(this);
        ((Citizens) Bukkit.getServer().getPluginManager().getPlugin("Citizens")).getStorage().getKey("npc").removeKey(
                String.valueOf(getId()));
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
        addTrait(new SpawnLocation(loc));
        // Set the spawned state
        addTrait(new Spawned(true));
        return true;
    }

    @Override
    public org.bukkit.inventory.Inventory getInventory() {
        return inventory.asInventory();
    }

    @Override
    public boolean openInventory(Player player) {
        if (!isSpawned())
            return false;
        getInventory().setContents(getTrait(Inventory.class).getContents());
        inventory.show(player);
        return true;
    }

    @Override
    public void update() {
        super.update();
        ai.update();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        inventory.setName(name);
    }
}