package net.citizensnpcs;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.Chunk;
import net.citizensnpcs.api.abstraction.EventHandler;
import net.citizensnpcs.api.abstraction.Listener;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.bukkit.BukkitConverter;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class EventListen implements Listener {
    private final NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
    private final ListMultimap<ChunkCoord, Integer> toRespawn = ArrayListMultimap.create();

    /*
     * Chunk events
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkCoord coord = new ChunkCoord(event.getChunk().getX(), event.getChunk().getZ());
        if (!toRespawn.containsKey(coord))
            return;
        for (int id : toRespawn.get(coord)) {
            NPC npc = npcRegistry.getById(id);
            npc.spawn(npc.getAttachment(CurrentLocation.class).getLocation());
        }
        toRespawn.removeAll(coord);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.isCancelled())
            return;

        ChunkCoord coord = new ChunkCoord(event.getChunk().getX(), event.getChunk().getZ());
        for (NPC npc : npcRegistry) {
            if (!npc.isSpawned())
                continue;
            WorldVector loc = npc.getEntity().getLocation();
            if (loc.getWorld().equals(event.getWorld()) && loc.getChunk().equals(event.getChunk())) {
                npc.despawn();
                toRespawn.put(coord, npc.getId());
            }
        }
    }

    /*
     * Entity events
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        NPC npc = BukkitConverter.toNPC(event.getEntity());
        if (npc == null)
            return;

        NPCDamageEvent damageEvent;
        if (event instanceof EntityDamageByEntityEvent) {
            NPCDamageByEntityEvent damageByEntityEvent = new NPCDamageByEntityEvent(npc, event.getDamage(),
                    BukkitConverter.toEntity(((EntityDamageByEntityEvent) event).getDamager()));
            damageEvent = damageByEntityEvent;
            CitizensAPI.getServer().callEvent(damageEvent);

            if (!damageEvent.isCancelled() || !(damageByEntityEvent.getDamager() instanceof Player)) {
                Player damager = (Player) damageByEntityEvent.getDamager();
                CitizensAPI.getServer().callEvent(new NPCLeftClickEvent(npc, damager));
            }
            event.setDamage(damageEvent.getDamage());
            event.setCancelled(damageEvent.isCancelled());
        } else {
            damageEvent = new NPCDamageEvent(npc, event.getDamage());
            CitizensAPI.getServer().callEvent(damageEvent);
        }
        event.setDamage(damageEvent.getDamage());
        event.setCancelled(damageEvent.isCancelled());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        NPC npc = BukkitConverter.toNPC(event.getEntity());
        if (npc == null)
            return;

        npc.despawn();
    }

    /*
     * Player events
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!BukkitConverter.isNPC(event.getPlayer()))
            return;

        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(((CraftPlayer) event.getPlayer()).getHandle());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled())
            return;

        NPC npc = BukkitConverter.toNPC(event.getRightClicked());
        if (npc == null)
            return;

        Player player = BukkitConverter.toPlayer(event.getPlayer());

        // Call right-click event
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        CitizensAPI.getServer().callEvent(rightClickEvent);
        if (rightClickEvent.isCancelled())
            return;
        // If the NPC isn't a close talker
        // TODO: move this into text.class
        if (Util.isSettingFulfilled(player, Setting.TALK_ITEM) && !npc.getAttachment(Text.class).shouldTalkClose())
            npc.getAttachment(Text.class).sendText(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Editor.leave(event.getPlayer().getName());
    }

    /*
     * World events
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        for (ChunkCoord chunk : toRespawn.keySet()) {
            if (!event.getWorld().isChunkLoaded(chunk.x, chunk.z))
                continue;
            for (int id : toRespawn.get(chunk)) {
                NPC npc = npcRegistry.getById(id);
                npc.spawn(npc.getAttachment(CurrentLocation.class).getLocation());
            }
            toRespawn.removeAll(chunk);
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        if (event.isCancelled())
            return;

        for (NPC npc : npcRegistry) {
            if (!npc.isSpawned() || !npc.getEntity().getWorld().equals(event.getWorld()))
                continue;

            npc.despawn();
            Chunk chunk = npc.getEntity().getLocation().getChunk();
            toRespawn.put(new ChunkCoord(chunk.getX(), chunk.getZ()), npc.getId());
        }
    }

    private static class ChunkCoord {
        private final int x;
        private final int z;

        private ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode() {
            return 31 * (31 + x) + z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ChunkCoord other = (ChunkCoord) obj;
            return x == other.x && z == other.z;
        }
    }
}
