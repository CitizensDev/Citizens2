package net.citizensnpcs.npc;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.npc.ai.CitizensAI;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class CitizensNPC extends AbstractNPC {
    protected final CitizensAI ai = new CitizensAI(this);
    protected final CitizensNPCManager manager;
    protected EntityLiving mcEntity;

    protected CitizensNPC(CitizensNPCManager manager, int id, String name) {
        super(id, name);
        this.manager = manager;
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
    public boolean isSpawned() {
        return getHandle() != null;
    }

    @Override
    public void remove() {
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
        addTrait(new SpawnLocation(loc));
        // Set the spawned state
        addTrait(new Spawned(true));
        return true;
    }

    @Override
    public void update() {
        super.update();
        ai.update();
    }
}