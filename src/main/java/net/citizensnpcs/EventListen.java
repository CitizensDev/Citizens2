package net.citizensnpcs;

import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.EntityTargetNPCEvent;
import net.citizensnpcs.api.event.NPCCombustByBlockEvent;
import net.citizensnpcs.api.event.NPCCombustByEntityEvent;
import net.citizensnpcs.api.event.NPCCombustEvent;
import net.citizensnpcs.api.event.NPCDamageByBlockEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Owner;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
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
        List<Integer> ids = toRespawn.get(coord);
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
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
    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        NPC npc = npcRegistry.getNPC(event.getEntity());
        if (npc == null)
            return;
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        if (event instanceof EntityCombustByEntityEvent) {
            Bukkit.getPluginManager().callEvent(
                    new NPCCombustByEntityEvent((EntityCombustByEntityEvent) event, npc));
        } else if (event instanceof EntityCombustByBlockEvent) {
            Bukkit.getPluginManager().callEvent(
                    new NPCCombustByBlockEvent((EntityCombustByBlockEvent) event, npc));
        } else {
            Bukkit.getPluginManager().callEvent(new NPCCombustEvent(event, npc));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!npcRegistry.isNPC(event.getEntity()))
            return;

        NPC npc = npcRegistry.getNPC(event.getEntity());
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
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
        } else if (event instanceof EntityDamageByBlockEvent) {
            Bukkit.getPluginManager().callEvent(
                    new NPCDamageByBlockEvent(npc, (EntityDamageByBlockEvent) event));
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() && npcRegistry.isNPC(event.getEntity()))
            event.setCancelled(false);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (npcRegistry.isNPC(event.getTarget())) {
            NPC npc = npcRegistry.getNPC(event.getTarget());

            event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
            Bukkit.getPluginManager().callEvent(new EntityTargetNPCEvent(event, npc));
        }
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
    public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
        if (event.getCreator().hasPermission("citizens.admin.avoid-limits"))
            return;
        int limit = Setting.DEFAULT_NPC_LIMIT.asInt();
        int maxChecks = Setting.MAX_NPC_LIMIT_CHECKS.asInt();
        for (int i = maxChecks; i >= 0; i--) {
            if (!event.getCreator().hasPermission("citizens.npc.limit." + i))
                continue;
            limit = i;
            break;
        }
        if (limit < 0)
            return;
        int owned = 0;
        for (NPC npc : npcRegistry) {
            if (npc.getTrait(Owner.class).isOwnedBy(event.getCreator()))
                owned++;
        }
        if (limit >= owned + 1 || limit == 0) {
            event.setCancelled(true);
            event.setCancelReason(String.format("Over the NPC limit of %d.", limit));
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        NPC npc = npcRegistry.getNPC(event.getRightClicked());
        if (npc == null)
            return;

        Player player = event.getPlayer();

        // Call right-click event
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        Bukkit.getPluginManager().callEvent(rightClickEvent);
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
            List<Integer> ids = toRespawn.get(chunk);
            for (int i = 0; i < ids.size(); i++) {
                int id = ids.get(i);
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
            storeForRespawn(npc);
            npc.despawn();
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

        private ChunkCoord(Chunk chunk) {
            this(chunk.getX(), chunk.getZ());
        }

        private ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * (prime + x) + z;
        }
    }
}