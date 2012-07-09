package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Saddle extends Trait implements Toggleable, Listener {
    private boolean pig;
    private boolean saddle;

    public Saddle() {
        super("saddle");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        saddle = key.getBoolean("");
    }

    @Override
    public void onNPCSpawn() {
        if (npc.getBukkitEntity() instanceof Pig) {
            ((Pig) npc.getBukkitEntity()).setSaddle(saddle);
            pig = true;
        } else
            pig = false;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (pig && npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            event.setCancelled(true);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", saddle);
    }

    @Override
    public boolean toggle() {
        saddle = !saddle;
        if (pig)
            ((Pig) npc.getBukkitEntity()).setSaddle(saddle);
        return saddle;
    }

    @Override
    public String toString() {
        return "Saddle{" + saddle + "}";
    }
}