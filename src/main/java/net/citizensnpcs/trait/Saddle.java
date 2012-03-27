package net.citizensnpcs.trait;

import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class Saddle extends Trait implements Toggleable, Listener {
    private final NPC npc;
    private boolean saddle;

    public Saddle(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        saddle = key.getBoolean("");
    }

    @Override
    public void onNPCSpawn() {
        if (npc.getBukkitEntity() instanceof Pig)
            ((Pig) npc.getBukkitEntity()).setSaddle(saddle);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (CitizensAPI.getNPCManager().isNPC(event.getRightClicked()))
            event.setCancelled(true);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", saddle);
    }

    @Override
    public boolean toggle() {
        saddle = !saddle;
        ((Pig) npc.getBukkitEntity()).setSaddle(saddle);
        return saddle;
    }

    @Override
    public String toString() {
        return "Saddle{" + saddle + "}";
    }
}