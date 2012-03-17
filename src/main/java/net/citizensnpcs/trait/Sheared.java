package net.citizensnpcs.trait;

import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class Sheared extends Trait implements Toggleable, Listener {
    private boolean sheared;
    private final NPC npc;

    public Sheared(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        sheared = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", sheared);
    }

    @Override
    public void onNPCSpawn() {
        // TODO Get rid of instanceof's, add
        // TraitFactory.withTypes(EntityType...) or similar
        if (npc.getBukkitEntity() instanceof Sheep)
            ((Sheep) npc.getBukkitEntity()).setSheared(sheared);
    }

    @Override
    public boolean toggle() {
        sheared = !sheared;
        ((Sheep) npc.getBukkitEntity()).setSheared(sheared);
        return sheared;
    }

    @EventHandler
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        // TODO: Complete trait/plugin system
        if (CitizensAPI.getNPCManager().isNPC(event.getEntity()))
            event.setCancelled(true);
    }

    @Override
    public String toString() {
        return "Sheared{" + sheared + "}";
    }
}