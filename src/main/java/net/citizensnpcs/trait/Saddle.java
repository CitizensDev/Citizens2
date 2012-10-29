package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Saddle extends Trait implements Toggleable {
    private boolean pig;
    @Persist("")
    private boolean saddle;

    public Saddle() {
        super("saddle");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (pig && npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            event.setCancelled(true);
    }

    @Override
    public void onSpawn() {
        if (npc.getBukkitEntity() instanceof Pig) {
            ((Pig) npc.getBukkitEntity()).setSaddle(saddle);
            pig = true;
        } else
            pig = false;
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