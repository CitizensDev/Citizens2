package net.citizensnpcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EventListen implements Listener {
    private final List<Integer> toRespawn = new ArrayList<Integer>();
    private final NPCManager manager;

    public EventListen(NPCManager manager) {
        this.manager = manager;
    }

    /*
     * Entity events
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!manager.isNPC(event.getEntity()))
            return;

        event.setCancelled(true); // TODO: implement damage handlers
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if (e.getDamager() instanceof Player) {
                NPC npc = manager.getNPC(event.getEntity());
                if (npc.getCharacter() != null) {
                    npc.getCharacter().onLeftClick(npc, (Player) e.getDamager());
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled() || !manager.isNPC(event.getEntity()) || !(event.getTarget() instanceof Player))
            return;

        NPC npc = manager.getNPC(event.getEntity());
        if (npc.getCharacter() != null)
            npc.getCharacter().onRightClick(npc, (Player) event.getTarget());
    }

    /*
     * World events
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Iterator<Integer> itr = toRespawn.iterator();
        while (itr.hasNext()) {
            NPC npc = manager.getNPC(itr.next());
            npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
            itr.remove();
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.isCancelled())
            return;

        for (NPC npc : manager.getSpawnedNPCs()) {
            Location loc = npc.getBukkitEntity().getLocation();
            if (event.getWorld().equals(loc.getWorld()) && event.getChunk().getX() == loc.getChunk().getX()
                    && event.getChunk().getZ() == loc.getChunk().getZ()) {
                toRespawn.add(npc.getId());
                npc.getTrait(SpawnLocation.class).setLocation(loc);
                npc.despawn();
            }
        }
    }
}