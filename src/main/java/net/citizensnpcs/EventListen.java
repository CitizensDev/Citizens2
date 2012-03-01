package net.citizensnpcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.EntityHumanNPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class EventListen implements Listener {
    private final Map<Chunk, List<Integer>> toRespawn = new HashMap<Chunk, List<Integer>>();
    private volatile CitizensNPCManager npcManager;

    public EventListen(CitizensNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    /*
     * World events
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!toRespawn.containsKey(event.getChunk()))
            return;
        for (int id : toRespawn.get(event.getChunk())) {
            NPC npc = npcManager.getNPC(id);
            npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
        }
        toRespawn.remove(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.isCancelled())
            return;

        List<Integer> respawn = new ArrayList<Integer>();
        for (NPC npc : npcManager) {
            if (!npc.isSpawned())
                continue;
            Location loc = npc.getBukkitEntity().getLocation();
            if (event.getWorld().equals(loc.getWorld()) && event.getChunk().getX() == loc.getChunk().getX()
                    && event.getChunk().getZ() == loc.getChunk().getZ()) {
                npc.getTrait(SpawnLocation.class).setLocation(loc);
                npc.despawn();
                respawn.add(npc.getId());
            }
        }
        if (respawn.size() > 0)
            toRespawn.put(event.getChunk(), respawn);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        for (Chunk chunk : toRespawn.keySet()) {
            if (event.getWorld().isChunkLoaded(chunk)) {
                for (int id : toRespawn.get(chunk)) {
                    NPC npc = npcManager.getNPC(id);
                    npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
                }
                toRespawn.remove(chunk);
            }
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        if (event.isCancelled())
            return;

        for (NPC npc : npcManager) {
            if (!npc.isSpawned() || !npc.getBukkitEntity().getWorld().equals(event.getWorld()))
                continue;
            Location loc = npc.getBukkitEntity().getLocation();
            npc.getTrait(SpawnLocation.class).setLocation(loc);
            npc.despawn();
            if (toRespawn.containsKey(loc.getChunk()))
                toRespawn.get(loc.getChunk()).add(npc.getId());
            else {
                List<Integer> respawn = new ArrayList<Integer>();
                respawn.add(npc.getId());
                toRespawn.put(loc.getChunk(), respawn);
            }
        }
    }

    /*
     * Entity events
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!npcManager.isNPC(event.getEntity()))
            return;

        event.setCancelled(true);
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if (e.getDamager() instanceof Player) {
                NPC npc = npcManager.getNPC(event.getEntity());
                if (npc.getCharacter() != null)
                    npc.getCharacter().onLeftClick(npc, (Player) e.getDamager());
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled() || !npcManager.isNPC(event.getEntity()) || !(event.getTarget() instanceof Player))
            return;

        NPC npc = npcManager.getNPC(event.getEntity());
        Player player = (Player) event.getTarget();
        if (player.getMetadata("selected").size() == 0 || player.getMetadata("selected").get(0).asInt() != npc.getId()) {
            if (player.getItemInHand().getTypeId() == Setting.SELECTION_ITEM.asInt()
                    && (npc.getTrait(Owner.class).getOwner().equals(player.getName()) || player
                            .hasPermission("citizens.admin"))) {
                npcManager.selectNPC(player, npc);
                Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), npc);
                if (!Setting.QUICK_SELECT.asBoolean())
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
    public void onPlayerQuit(PlayerQuitEvent event) {
        Editor.leave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!npcManager.isNPC(event.getRightClicked()))
            return;

        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(event.getRightClicked(), event.getPlayer(), TargetReason.CUSTOM));
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!(((CraftPlayer) event.getPlayer()).getHandle() instanceof EntityHumanNPC))
            return;

        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(((CraftPlayer) event.getPlayer()).getHandle());
    }
}