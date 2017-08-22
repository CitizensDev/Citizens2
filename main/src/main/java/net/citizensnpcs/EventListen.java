package net.citizensnpcs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Minecart;
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
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.CitizensDeserialiseMetaEvent;
import net.citizensnpcs.api.event.CitizensPreReloadEvent;
import net.citizensnpcs.api.event.CitizensSerialiseMetaEvent;
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
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.skin.SkinUpdateTracker;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;

public class EventListen implements Listener {
    private final NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
    private final Map<String, NPCRegistry> registries;
    private final SkinUpdateTracker skinUpdateTracker;
    private final ListMultimap<ChunkCoord, NPC> toRespawn = ArrayListMultimap.create();

    EventListen(Map<String, NPCRegistry> registries) {
        this.registries = registries;
        this.skinUpdateTracker = new SkinUpdateTracker(npcRegistry, registries);
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
                    && npc.getTrait(Owner.class).isOwnedBy(event.getCreator())) {
                owned++;
            }
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
                Messaging.debug("Despawned id", npc.getId(),
                        "due to chunk unload at [" + coord.x + "," + coord.z + "]");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCitizensReload(CitizensPreReloadEvent event) {
        skinUpdateTracker.reset();
        toRespawn.clear();
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

        if (!npc.data().get(NPC.DROPS_ITEMS_METADATA, false)) {
            event.getDrops().clear();
        }

        final Location location = npc.getEntity().getLocation();
        Bukkit.getPluginManager().callEvent(new NPCDeathEvent(npc, event));
        npc.despawn(DespawnReason.DEATH);

        if (npc.data().has(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA)) {
            String teamName = npc.data().get(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
            if (team != null) {
                team.unregister();
            }

            npc.data().remove(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
        }

        if (npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1) >= 0) {
            int delay = npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (!npc.isSpawned() && npc.getOwningRegistry().getByUniqueId(npc.getUniqueId()) == npc) {
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
        event.setCancelled(
                !npc.data().get(NPC.TARGETABLE_METADATA, !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)));
        Bukkit.getPluginManager().callEvent(new EntityTargetNPCEvent(event, npc));
    }

    @EventHandler
    public void onMetaDeserialise(CitizensDeserialiseMetaEvent event) {
        if (event.getKey().keyExists("skull")) {
            String owner = event.getKey().getString("skull.owner", "");
            UUID uuid = event.getKey().keyExists("skull.uuid") ? UUID.fromString(event.getKey().getString("skull.uuid"))
                    : null;
            if (owner.isEmpty() && uuid == null) {
                return;
            }
            GameProfile profile = new GameProfile(uuid, owner);
            for (DataKey sub : event.getKey().getRelative("skull.properties").getSubKeys()) {
                String propertyName = sub.name();
                for (DataKey property : sub.getIntegerSubKeys()) {
                    profile.getProperties().put(propertyName,
                            new Property(property.getString("name"), property.getString("value"),
                                    property.keyExists("signature") ? property.getString("signature") : null));
                }
            }
            SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
            NMS.setProfile(meta, profile);
            event.getItemStack().setItemMeta(meta);
        }
    }

    @EventHandler
    public void onMetaSerialise(CitizensSerialiseMetaEvent event) {
        if (!(event.getMeta() instanceof SkullMeta))
            return;
        SkullMeta meta = (SkullMeta) event.getMeta();
        GameProfile profile = NMS.getProfile(meta);
        if (profile == null)
            return;
        if (profile.getName() != null) {
            event.getKey().setString("skull.owner", profile.getName());
        }
        if (profile.getId() != null) {
            event.getKey().setString("skull.uuid", profile.getId().toString());
        }
        if (profile.getProperties() != null) {
            for (Entry<String, Collection<Property>> entry : profile.getProperties().asMap().entrySet()) {
                DataKey relative = event.getKey().getRelative("skull.properties." + entry.getKey());
                int i = 0;
                for (Property value : entry.getValue()) {
                    relative.getRelative(i).setString("name", value.getName());
                    if (value.getSignature() != null) {
                        relative.getRelative(i).setString("signature", value.getSignature());
                    }
                    relative.getRelative(i).setString("value", value.getValue());
                    i++;
                }
            }
        }
    }

    @EventHandler
    public void onNavigationBegin(NavigationBeginEvent event) {
        skinUpdateTracker.onNPCNavigationBegin(event.getNPC());
    }

    @EventHandler
    public void onNavigationComplete(NavigationCompleteEvent event) {
        skinUpdateTracker.onNPCNavigationComplete(event.getNPC());
    }

    @EventHandler
    public void onNeedsRespawn(NPCNeedsRespawnEvent event) {
        ChunkCoord coord = toCoord(event.getSpawnLocation());
        if (toRespawn.containsEntry(coord, event.getNPC()))
            return;
        Messaging.debug("Stored", event.getNPC().getId(), "for respawn from NPCNeedsRespawnEvent");
        toRespawn.put(coord, event.getNPC());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCDespawn(NPCDespawnEvent event) {
        if (event.getReason() == DespawnReason.PLUGIN || event.getReason() == DespawnReason.REMOVAL
                || event.getReason() == DespawnReason.RELOAD) {
            Messaging.debug("Preventing further respawns of " + event.getNPC().getId() + " due to DespawnReason."
                    + event.getReason().name());
            if (event.getNPC().getStoredLocation() != null) {
                toRespawn.remove(toCoord(event.getNPC().getStoredLocation()), event.getNPC());
            }
        } else {
            Messaging.debug("Removing " + event.getNPC().getId() + " from skin tracker due to DespawnReason."
                    + event.getReason().name());
        }
        skinUpdateTracker.onNPCDespawn(event.getNPC());
    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCSpawn(NPCSpawnEvent event) {
        skinUpdateTracker.onNPCSpawn(event.getNPC());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (npcRegistry.getNPC(event.getPlayer()) == null)
            return;
        NMS.removeFromServerPlayerList(event.getPlayer());
        // on teleport, player NPCs are added to the server player list. this is
        // undesirable as player NPCs are not real players and confuse plugins.
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        skinUpdateTracker.updatePlayer(event.getPlayer(), 20, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
        checkCreationEvent(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (npcRegistry.isNPC(event.getCaught()) && npcRegistry.getNPC(event.getCaught()).isProtected()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        NPC npc = npcRegistry.getNPC(event.getRightClicked());
        if (npc == null || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        Player player = event.getPlayer();
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        Bukkit.getPluginManager().callEvent(rightClickEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        skinUpdateTracker.updatePlayer(event.getPlayer(), 6 * 20, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        NPC npc = npcRegistry.getNPC(event.getEntity());
        if (npc == null) {
            return;
        }
        if (npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)) {
            event.setCancelled(true);
        }
    }

    // recalculate player NPCs the first time a player moves and every time
    // a player moves a certain distance from their last position.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        skinUpdateTracker.onPlayerMove(event.getPlayer());
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
        skinUpdateTracker.removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        skinUpdateTracker.updatePlayer(event.getPlayer(), 15, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.PLUGIN && !event.getPlayer().hasMetadata("citizens-force-teleporting")
                && npcRegistry.getNPC(event.getPlayer()) != null && Setting.TELEPORT_DELAY.asInt() > 0) {
            event.setCancelled(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().setMetadata("citizens-force-teleporting",
                            new FixedMetadataValue(CitizensAPI.getPlugin(), true));
                    event.getPlayer().teleport(event.getTo());
                    event.getPlayer().removeMetadata("citizens-force-teleporting", CitizensAPI.getPlugin());
                }
            }, Setting.TELEPORT_DELAY.asInt());
        }
        skinUpdateTracker.updatePlayer(event.getPlayer(), 15, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof FishHook))
            return;
        NMS.removeHookIfNecessary(npcRegistry, (FishHook) event.getEntity());
        new BukkitRunnable() {
            int n = 0;

            @Override
            public void run() {
                if (n++ > 5) {
                    cancel();
                }
                NMS.removeHookIfNecessary(npcRegistry, (FishHook) event.getEntity());
            }
        }.runTaskTimer(CitizensAPI.getPlugin(), 0, 1);
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        NPC npc = npcRegistry.getNPC(event.getVehicle());
        if (npc == null) {
            return;
        }
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!npcRegistry.isNPC(event.getEntered()))
            return;
        NPC npc = npcRegistry.getNPC(event.getEntered());
        if ((npc.getEntity() instanceof AbstractHorse || npc.getEntity().getType() == EntityType.BOAT
                || npc.getEntity() instanceof Minecart) && !npc.getTrait(Controllable.class).isEnabled()) {
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
                event.setCancelled(true);
                return;
            }
            if (npc.isSpawned()) {
                storeForRespawn(npc);
                Messaging.debug("Despawned", npc.getId() + "due to world unload at", event.getWorld().getName());
            }
        }
    }

    private void respawnAllFromCoord(ChunkCoord coord) {
        List<NPC> ids = toRespawn.get(coord);
        for (int i = 0; i < ids.size(); i++) {
            NPC npc = ids.get(i);
            boolean success = spawn(npc);
            if (!success) {
                if (Messaging.isDebugging()) {
                    Messaging.debug("Couldn't respawn id", npc.getId(),
                            "during chunk event at [" + coord.x + "," + coord.z + "]");
                }
                continue;
            }
            try {
                ids.remove(i--);
            } catch (IndexOutOfBoundsException ex) {
                // something caused toRespawn to get modified?
                Messaging.debug("Some strange chunk loading happened while spawning", npc.getId(),
                        " - check all your NPCs in chunk [" + coord.x + "," + coord.z + "] are spawned");
                break;
            }
            if (Messaging.isDebugging()) {
                Messaging.debug("Spawned id", npc.getId(), "due to chunk event at [" + coord.x + "," + coord.z + "]");
            }
        }
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
