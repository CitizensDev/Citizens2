package net.citizensnpcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.trait.CurrentLocation;
import net.minecraft.server.EntityPlayer;

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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
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
    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkCoord coord = toCoord(event.getChunk());
        if (!toRespawn.containsKey(coord))
            return;
        for (int id : toRespawn.get(coord)) {
            NPC npc = npcRegistry.getById(id);
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        }
        toRespawn.removeAll(coord);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkCoord coord = toCoord(event.getChunk());
        for (NPC npc : npcRegistry) {
            if (!npc.isSpawned())
                continue;
            Location loc = npc.getBukkitEntity().getLocation();
            Chunk chunk = loc.getChunk();
            if (event.getWorld().equals(loc.getWorld()) && event.getChunk().getX() == chunk.getX()
                    && event.getChunk().getZ() == chunk.getZ()) {
                npc.despawn();
                toRespawn.put(coord, npc.getId());
            }
        }
    }

    /*
     * Entity events
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!npcRegistry.isNPC(event.getEntity()))
            return;

        NPC npc = npcRegistry.getNPC(event.getEntity());
        if (event instanceof EntityDamageByEntityEvent) {
            NPCDamageByEntityEvent damageEvent = new NPCDamageByEntityEvent(npc,
                    (EntityDamageByEntityEvent) event);
            Bukkit.getPluginManager().callEvent(damageEvent);

            if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player))
                return;
            Player damager = (Player) damageEvent.getDamager();

            // Call left-click event
            NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
            Bukkit.getPluginManager().callEvent(leftClickEvent);
        } else {
            Bukkit.getPluginManager().callEvent(new NPCDamageEvent(npc, event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!npcRegistry.isNPC(event.getEntity()))
            return;
        NPC npc = npcRegistry.getNPC(event.getEntity());
        npc.despawn();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!npcRegistry.isNPC(event.getEntity()) || !(event.getTarget() instanceof Player))
            return;

        NPC npc = npcRegistry.getNPC(event.getEntity());
        Player player = (Player) event.getTarget();

        // Call right-click event
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        Bukkit.getPluginManager().callEvent(rightClickEvent);
    }

    /*
     * Player events
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        EntityPlayer handle = ((CraftPlayer) event.getPlayer()).getHandle();
        if (!(handle instanceof EntityHumanNPC))
            return;

        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(handle);
        // on teleport, player NPCs are added to the server player list. this is
        // undesirable as player NPCs are not real players and confuse plugins.
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!npcRegistry.isNPC(event.getRightClicked()))
            return;

        // Call target event for NPCs
        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(event.getRightClicked(), event.getPlayer(), TargetReason.CUSTOM));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Editor.leave(event.getPlayer());
    }

    /*
     * World events
     */
    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        for (ChunkCoord chunk : toRespawn.keySet()) {
            if (!event.getWorld().isChunkLoaded(chunk.x, chunk.z))
                continue;
            for (int id : toRespawn.get(chunk)) {
                NPC npc = npcRegistry.getById(id);
                npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
            }
            toRespawn.removeAll(chunk);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        for (NPC npc : npcRegistry) {
            if (!npc.isSpawned() || !npc.getBukkitEntity().getWorld().equals(event.getWorld()))
                continue;

            npc.despawn();
            storeForRespawn(npc);
        }
    }

    private void storeForRespawn(NPC npc) {
        toRespawn.put(toCoord(npc.getBukkitEntity().getLocation().getChunk()), npc.getId());
    }

    private ChunkCoord toCoord(Chunk chunk) {
        return new ChunkCoord(chunk);
    }

    private static class ChunkCoord {
        private final int x;
        private final int z;

        private ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }

        private ChunkCoord(Chunk chunk) {
            this(chunk.getX(), chunk.getZ());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * (prime + x) + z;
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