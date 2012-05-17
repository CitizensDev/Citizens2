package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Saddle extends Trait implements Toggleable, Listener {
    private final NPC npc;
    private boolean saddle;

    public Saddle(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (!(npc.getBukkitEntity() instanceof Pig))
            throw new NPCLoadException("NPC must be a pig to have this trait");
        saddle = key.getBoolean("");
    }

    @Override
    public void onNPCSpawn() {
        ((Pig) npc.getBukkitEntity()).setSaddle(saddle);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
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