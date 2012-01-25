package net.citizensnpcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EventListen implements Listener {
    private final List<Integer> toRespawn = new ArrayList<Integer>();
    private final CitizensNPCManager manager;

    public EventListen(CitizensNPCManager manager) {
        this.manager = manager;
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

    /*
     * Entity events
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!manager.isNPC(event.getEntity()))
            return;

        event.setCancelled(true); // TODO implement damage handlers
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if (e.getDamager() instanceof Player) {
                NPC npc = manager.getNPC(event.getEntity());
                if (npc.getCharacter() != null)
                    npc.getCharacter().onLeftClick(npc, (Player) e.getDamager());
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (manager.isNPC(event.getTarget()))
            if (event.isCancelled() || !manager.isNPC(event.getEntity()) || !(event.getTarget() instanceof Player))
                return;

        NPC npc = manager.getNPC(event.getEntity());
        Player player = (Player) event.getTarget();
        if (!manager.hasSelected(player, npc)) {
            if (manager.canSelect(player, npc)) {
                manager.selectNPC(player, npc);
                Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.getString(), npc);
                if (!Setting.QUICK_SELECT.getBoolean())
                    return;
            }
        }
        if (npc.getCharacter() != null)
            npc.getCharacter().onRightClick(npc, player);
    }

    /*
     * Player events
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!manager.isNPC(event.getRightClicked()))
            return;

        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(event.getRightClicked(), event.getPlayer(), TargetReason.CUSTOM));
    }
}