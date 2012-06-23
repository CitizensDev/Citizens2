package net.citizensnpcs.trait;

import net.citizensnpcs.api.abstraction.EventHandler;
import net.citizensnpcs.api.abstraction.Listener;
import net.citizensnpcs.api.abstraction.entity.Sheep;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.bukkit.BukkitConverter;

import org.bukkit.event.player.PlayerShearEntityEvent;

public class Sheared extends Attachment implements Toggleable, Listener {
    private final NPC npc;
    private boolean sheared;

    public Sheared(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        sheared = key.getBoolean("");
    }

    @Override
    public void onSpawn() {
        ((Sheep) npc.getEntity()).setSheared(sheared);
    }

    @EventHandler
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (npc.equals(BukkitConverter.toNPC(event.getEntity())))
            event.setCancelled(true);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", sheared);
    }

    @Override
    public boolean toggle() {
        sheared = !sheared;
        if (npc.getEntity() instanceof Sheep)
            ((Sheep) npc.getEntity()).setSheared(sheared);
        return sheared;
    }

    @Override
    public String toString() {
        return "Sheared{" + sheared + "}";
    }
}