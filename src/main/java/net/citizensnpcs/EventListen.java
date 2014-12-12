package net.citizensnpcs;

import java.util.List;
import java.util.Map;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.EntityTargetNPCEvent;
import net.citizensnpcs.api.event.NPCCombustByBlockEvent;
import net.citizensnpcs.api.event.NPCCombustByEntityEvent;
import net.citizensnpcs.api.event.NPCCombustEvent;
import net.citizensnpcs.api.event.NPCDamageByBlockEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

public class EventListen implements Listener {
    private final NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
    private final Map<String, NPCRegistry> registries;
    private final ListMultimap<ChunkCoord, NPC> toRespawn = ArrayListMultimap.create();

    EventListen(Map<String, NPCRegistry> registries) {
        this.registries = registries;
    }

    private void checkCreationEvent(CommandSenderCreateNPCEvent event) {
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
            if (!event.getNPC().equals(npc) && npc.hasTrait(Owner.class)
                    && npc.getTrait(Owner.class).isOwnedBy(event.getCreator()))
                owned++;
        }
        int wouldOwn = owned + 1;
        if (wouldOwn > limit) {
            event.setCancelled(true);
            event.setCancelReason(Messaging.tr(Messages.OVER_NPC_LIMIT, limit));
        }
    }

    private Iterable<NPC> getAllNPCs() {
        return Iterables.filter(Iterables.<NPC> concat(npcRegistry, Iterables.concat(registries.values())),
                Predicates.notNull());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        respawnAllFromCoord(toCoord(event.getChunk()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkCoord coord = toCoord(event.getChunk());
        Location loc = new Location(null, 0, 0, 0);
        for (NPC npc : getAllNPCs()) {
            if (npc == null || !npc.isSpawned())
                continue;
            loc = npc.getEntity().getLocation(loc);
            boolean sameChunkCoordinates = coord.z == loc.getBlockZ() >> 4 && coord.x == loc.getBlockX() >> 4;
            if (!sameChunkCoordinates || !event.getWorld().equals(loc.getWorld()))
                continue;
            if (!npc.despawn(DespawnReason.CHUNK_UNLOAD)) {
                event.setCancelled(true);
                if (Messaging.isDebugging()) {
                    Messaging.debug("Cancelled chunk unload at [" + coord.x + "," + coord.z + "]");
                }
                respawnAllFromCoord(coord);
                return;
            }
            toRespawn.put(coord, npc);
            if (Messaging.isDebugging()) {
                Messaging
                .debug("Despawned id", npc.getId(), "due to chunk unload at [" + coord.x + "," + coord.z + "]");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandSenderCreateNPC(CommandSenderCreateNPCEvent event) {
        checkCreationEvent(event);
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
            Bukkit.getPluginManager().callEvent(new NPCCombustByEntityEvent((EntityCombustByEntityEvent) event, npc));
        } else if (event instanceof EntityCombustByBlockEvent) {
            Bukkit.getPluginManager().callEvent(new NPCCombustByBlockEvent((EntityCombustByBlockEvent) event, npc));
        } else {
            Bukkit.getPluginManager().callEvent(new NPCCombustEvent(event, npc));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        NPC npc = npcRegistry.getNPC(event.getEntity());
        if (npc == null) {
            if (event instanceof EntityDamageByEntityEvent) {
                npc = npcRegistry.getNPC(((EntityDamageByEntityEvent) event).getDamager());
                if (npc == null)
                    return;
                event.setCancelled(!npc.data().get(NPC.DAMAGE_OTHERS_METADATA, true));
                NPCDamageEntityEvent damageEvent = new NPCDamageEntityEvent(npc, (EntityDamageByEntityEvent) event);
                Bukkit.getPluginManager().callEvent(damageEvent);
            }
            return;
        }
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        if (event instanceof EntityDamageByEntityEvent) {
            NPCDamageByEntityEvent damageEvent = new NPCDamageByEntityEvent(npc, (EntityDamageByEntityEvent) event);
            Bukkit.getPluginManager().callEvent(damageEvent);

            if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player))
                return;
            Player damager = (Player) damageEvent.getDamager();

            // Call left-click event
            NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
            Bukkit.getPluginManager().callEvent(leftClickEvent);
        } else if (event instanceof EntityDamageByBlockEvent) {
            Bukkit.getPluginManager().callEvent(new NPCDamageByBlockEvent(npc, (EntityDamageByBlockEvent) event));
        } else {
            Bukkit.getPluginManager().callEvent(new NPCDamageEvent(npc, event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        final NPC npc = npcRegistry.getNPC(event.getEntity());
        if (npc == null) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new NPCDeathEvent(npc, event));
        final Location location = npc.getEntity().getLocation();
        npc.despawn(DespawnReason.DEATH);

        if (npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1) >= 0) {
            int delay = npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (!npc.isSpawned()) {
                        npc.spawn(location);
                    }
                }
            }, delay + 2);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() && npcRegistry.isNPC(event.getEntity())) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        NPC npc = npcRegistry.getNPC(event.getTarget());
        if (npc == null)
            return;
        event.setCancelled(!npc.data().get(NPC.TARGETABLE_METADATA,
                !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)));
        Bukkit.getPluginManager().callEvent(new EntityTargetNPCEvent(event, npc));
    }

    @EventHandler
    public void onNeedsRespawn(NPCNeedsRespawnEvent event) {
        ChunkCoord coord = toCoord(event.getSpawnLocation());
        if (toRespawn.containsEntry(coord, event.getNPC()))
            return;
        toRespawn.put(coord, event.getNPC());
    }

    @EventHandler
    public void onNPCDespawn(NPCDespawnEvent event) {
        if (event.getReason() == DespawnReason.PLUGIN || event.getReason() == DespawnReason.REMOVAL) {
            if (event.getNPC().getStoredLocation() != null) {
                toRespawn.remove(toCoord(event.getNPC().getStoredLocation()), event.getNPC());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (npcRegistry.getNPC(event.getPlayer()) == null)
            return;
        NMS.removeFromServerPlayerList(event.getPlayer());
        // on teleport, player NPCs are added to the server player list. this is
        // undesirable as player NPCs are not real players and confuse plugins.
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
        checkCreationEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        NPC npc = npcRegistry.getNPC(event.getRightClicked());
        if (npc == null) {
            return;
        }

        Player player = event.getPlayer();
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        Bukkit.getPluginManager().callEvent(rightClickEvent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        for (NPC npc : getAllNPCs()) {
            if (npc.isSpawned() && npc.getEntity().getType() == EntityType.PLAYER) {
                NMS.sendPlayerlistPacket(true, event.getPlayer(), npc);
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (NPC npc : getAllNPCs()) {
                    if (npc.isSpawned() && npc.getEntity().getType() == EntityType.PLAYER) {
                        NMS.sendPlayerlistPacket(false, event.getPlayer(), npc);
                    }
                }
            }
        }, 60);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Editor.leave(event.getPlayer());
        if (event.getPlayer().isInsideVehicle()) {
            NPC npc = npcRegistry.getNPC(event.getPlayer().getVehicle());
            if (npc != null) {
                event.getPlayer().leaveVehicle();
            }
        }
    }

    @EventHandler
    public void onPlayerTeleports(PlayerTeleportEvent event) {
        Location from = roundLocation(event.getFrom());
        Location to = roundLocation(event.getTo());
        if (from.equals(to)) {
            return; // Don't fire on every movement, just full block+.
        }
        int maxRad = 50 * 50; // TODO: Adjust me to perfection
        Location npcPos = new Location(null, 0, 0, 0);
        for (final NPC npc : getAllNPCs()) {
            if (npc.isSpawned() && npc.getEntity().getType() == EntityType.PLAYER) {
                npc.getEntity().getLocation(npcPos);
                if ((to.getWorld() == npcPos.getWorld() && npcPos.distanceSquared(to) < maxRad)
                        && ((from.getWorld() == npcPos.getWorld() && npcPos.distanceSquared(from) > maxRad) || from
                                .getWorld() != to.getWorld())) {
                    NMS.showNPCReset(event.getPlayer(), npc);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerWalks(final PlayerMoveEvent event) {
        Location from = roundLocation(event.getFrom());
        Location to = roundLocation(event.getTo());
        if (from.equals(to)) {
            return;
        }
        if (from.getWorld() != to.getWorld()) {
            return; // Ignore cross-world movement
        }
        int maxRad = 50 * 50; // TODO: Adjust me to perfection
        Location loc = new Location(null, 0, 0, 0);
        for (final NPC npc : getAllNPCs()) {
            if (npc.isSpawned() && npc.getEntity().getType() == EntityType.PLAYER) {
                npc.getEntity().getLocation(loc);
                if (from.getWorld() == loc.getWorld() && loc.distanceSquared(to) < maxRad
                        && loc.distanceSquared(from) > maxRad) {
                    NMS.showNPCReset(event.getPlayer(), npc);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!npcRegistry.isNPC(event.getEntered()))
            return;
        NPC npc = npcRegistry.getNPC(event.getEntered());
        if (npc.getEntity().getType() == EntityType.HORSE && !npc.getTrait(Controllable.class).isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        for (ChunkCoord chunk : toRespawn.keySet()) {
            if (!chunk.worldName.equals(event.getWorld().getName())
                    || !event.getWorld().isChunkLoaded(chunk.x, chunk.z))
                continue;
            respawnAllFromCoord(chunk);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        for (NPC npc : getAllNPCs()) {
            if (npc == null || !npc.isSpawned() || !npc.getEntity().getWorld().equals(event.getWorld()))
                continue;
            boolean despawned = npc.despawn(DespawnReason.WORLD_UNLOAD);
            if (event.isCancelled() || !despawned) {
                for (ChunkCoord coord : toRespawn.keySet()) {
                    if (event.getWorld().getName().equals(coord.worldName)) {
                        respawnAllFromCoord(coord);
                    }
                }
                return;
            }
            storeForRespawn(npc);
            Messaging.debug("Despawned", npc.getId() + "due to world unload at", event.getWorld().getName());
        }
    }

    private void respawnAllFromCoord(ChunkCoord coord) {
        List<NPC> ids = toRespawn.get(coord);
        for (int i = 0; i < ids.size(); i++) {
            NPC npc = ids.get(i);
            boolean success = spawn(npc);
            if (!success) {
                if (Messaging.isDebugging()) {
                    Messaging.debug("Couldn't respawn id", npc.getId(), "during chunk event at [" + coord.x + ","
                            + coord.z + "]");
                }
                continue;
            }
            ids.remove(i--);
            if (Messaging.isDebugging()) {
                Messaging.debug("Spawned id", npc.getId(), "due to chunk event at [" + coord.x + "," + coord.z + "]");
            }
        }
    }

    private Location roundLocation(Location input) {
        return new Location(input.getWorld(), Math.floor(input.getX()),
                Math.floor(input.getY()), Math.floor(input.getZ()));
    }

    private boolean spawn(NPC npc) {
        Location spawn = npc.getTrait(CurrentLocation.class).getLocation();
        if (spawn == null) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Couldn't find a spawn location for despawned NPC id", npc.getId());
            }
            return false;
        }
        return npc.spawn(spawn);
    }

    private void storeForRespawn(NPC npc) {
        toRespawn.put(toCoord(npc.getEntity().getLocation()), npc);
    }

    private ChunkCoord toCoord(Chunk chunk) {
        return new ChunkCoord(chunk);
    }

    private ChunkCoord toCoord(Location loc) {
        return new ChunkCoord(loc.getWorld().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    private static class ChunkCoord {
        private final String worldName;
        private final int x;
        private final int z;

        private ChunkCoord(Chunk chunk) {
            this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        }

        private ChunkCoord(String worldName, int x, int z) {
            this.x = x;
            this.z = z;
            this.worldName = worldName;
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
            if (worldName == null) {
                if (other.worldName != null) {
                    return false;
                }
            } else if (!worldName.equals(other.worldName)) {
                return false;
            }
            return x == other.x && z == other.z;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * (prime * (prime + ((worldName == null) ? 0 : worldName.hashCode())) + x) + z;
        }
    }
}
