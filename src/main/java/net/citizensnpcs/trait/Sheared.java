package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class Sheared extends Trait implements Toggleable {
    private boolean sheared;

    public Sheared() {
        super("sheared");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        sheared = key.getBoolean("");
    }

    @EventHandler
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getEntity())))
            event.setCancelled(true);
    }

    @Override
    public void onSpawn() {
        ((Sheep) npc.getBukkitEntity()).setSheared(sheared);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", sheared);
    }

    @Override
    public boolean toggle() {
        sheared = !sheared;
        if (npc.getBukkitEntity() instanceof Sheep)
            ((Sheep) npc.getBukkitEntity()).setSheared(sheared);
        return sheared;
    }

    @Override
    public String toString() {
        return "Sheared{" + sheared + "}";
    }
}