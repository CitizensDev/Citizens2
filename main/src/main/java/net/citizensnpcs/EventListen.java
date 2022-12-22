package net.citizensnpcs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
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
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
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
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
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
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.NPCVehicleDamageEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.skin.SkinUpdateTracker;
import net.citizensnpcs.trait.ClickRedirectTrait;
import net.citizensnpcs.trait.CommandTrait;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.trait.ShopTrait;
import net.citizensnpcs.trait.SitTrait;
import net.citizensnpcs.util.ChunkCoord;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;

public class EventListen implements Listener {
    private EventListenChunk chunkEventListener;
    private final Map<String, NPCRegistry> registries;
    private final SkinUpdateTracker skinUpdateTracker;
    private final ListMultimap<ChunkCoord, NPC> toRespawn = ArrayListMultimap.create(64, 4);

    EventListen(Map<String, NPCRegistry> registries) {
        this.registries = registries;
        this.skinUpdateTracker = new SkinUpdateTracker(registries);
        try {
            this.chunkEventListener = new EventListenChunk(this);
            Bukkit.getPluginManager().registerEvents(chunkEventListener, CitizensAPI.getPlugin());
        } catch (Throwable ex) {
            this.chunkEventListener = null;
        }
        try {
            Class.forName("org.bukkit.event.entity.EntityTransformEvent");
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onEntityTransform(EntityTransformEvent event) {
                    NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
                    if (npc == null)
                        return;
                    if (npc.isProtected()) {
                        event.setCancelled(true);
                    }
                }
            }, CitizensAPI.getPlugin());
        } catch (Throwable ex) {
        }
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
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!event.getNPC().equals(npc) && npc.hasTrait(Owner.class)
                    && npc.getTraitNullable(Owner.class).isOwnedBy(event.getCreator())) {
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
        return Iterables.filter(
                Iterables.<NPC> concat(CitizensAPI.getNPCRegistry(), Iterables.concat(registries.values())),
                Predicates.notNull());
    }

    void loadNPCs(ChunkEvent event) {
        ChunkCoord coord = new ChunkCoord(event.getChunk());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                respawnAllFromCoord(coord, event);
            }
        };
        if (Messaging.isDebugging() && Setting.DEBUG_CHUNK_LOADS.asBoolean() && toRespawn.containsKey(coord)) {
            new Exception("CITIZENS CHUNK LOAD DEBUG " + coord).printStackTrace();
        }
        if (event instanceof Cancellable) {
            runnable.run();
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), runnable);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (chunkEventListener != null)
            return;
        loadNPCs(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(final ChunkUnloadEvent event) {
        if (chunkEventListener != null)
            return;
        unloadNPCs(event, Arrays.asList(event.getChunk().getEntities()));
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
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
        if (npc == null)
            return;
        event.setCancelled(npc.isProtected());
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
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
        if (npc == null) {
            if (event instanceof EntityDamageByEntityEvent) {
                npc = CitizensAPI.getNPCRegistry().getNPC(((EntityDamageByEntityEvent) event).getDamager());
                if (npc == null)
                    return;
                event.setCancelled(!npc.data().get(NPC.DAMAGE_OTHERS_METADATA, true));
                NPCDamageEntityEvent damageEvent = new NPCDamageEntityEvent(npc, (EntityDamageByEntityEvent) event);
                Bukkit.getPluginManager().callEvent(damageEvent);
            }
            return;
        }
        event.setCancelled(npc.isProtected());
        if (event instanceof EntityDamageByEntityEvent) {
            NPCDamageByEntityEvent damageEvent = new NPCDamageByEntityEvent(npc, (EntityDamageByEntityEvent) event);
            Bukkit.getPluginManager().callEvent(damageEvent);

            if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player))
                return;

            Player damager = (Player) damageEvent.getDamager();

            if (npc.hasTrait(ClickRedirectTrait.class)) {
                npc = npc.getTraitNullable(ClickRedirectTrait.class).getRedirectNPC();
                if (npc == null)
                    return;
            }

            NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
            Bukkit.getPluginManager().callEvent(leftClickEvent);
            if (npc.hasTrait(CommandTrait.class)) {
                npc.getTraitNullable(CommandTrait.class).dispatch(damager, CommandTrait.Hand.LEFT);
            }
        } else if (event instanceof EntityDamageByBlockEvent) {
            Bukkit.getPluginManager().callEvent(new NPCDamageByBlockEvent(npc, (EntityDamageByBlockEvent) event));
        } else {
            Bukkit.getPluginManager().callEvent(new NPCDamageEvent(npc, event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        final NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
        if (npc == null) {
            return;
        }

        if (!npc.data().get(NPC.DROPS_ITEMS_METADATA, false)) {
            event.getDrops().clear();
        }

        final Location location = npc.getStoredLocation();
        Bukkit.getPluginManager().callEvent(new NPCDeathEvent(npc, event));
        npc.despawn(DespawnReason.DEATH);

        int delay = npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1);
        if (delay < 0)
            return;
        int deathAnimationTicks = event.getEntity() instanceof LivingEntity ? 20 : 2;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (!npc.isSpawned() && npc.getOwningRegistry().getByUniqueId(npc.getUniqueId()) == npc) {
                    npc.spawn(location, SpawnReason.TIMED_RESPAWN);
                }
            }
        }, delay + deathAnimationTicks);
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
        if (npc == null || npc.getEntity().getType() != EntityType.PLAYER)
            return;
        event.setCancelled(true);
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        event.getTo().getChunk();
        npc.spawn(event.getTo(), SpawnReason.RESPAWN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getTarget());
        if (npc == null)
            return;
        event.setCancelled(!npc.data().get(NPC.TARGETABLE_METADATA, !npc.isProtected()));
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
            Material mat = SpigotUtil.isUsing1_13API() ? Material.SKELETON_SKULL : Material.valueOf("SKULL_ITEM");
            SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(mat);
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
        ChunkCoord coord = new ChunkCoord(event.getSpawnLocation());
        if (toRespawn.containsEntry(coord, event.getNPC()))
            return;
        Messaging.debug("Stored", event.getNPC(), "for respawn from NPCNeedsRespawnEvent");
        toRespawn.put(coord, event.getNPC());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCDespawn(NPCDespawnEvent event) {
        if (event.getReason() == DespawnReason.PLUGIN || event.getReason() == DespawnReason.REMOVAL
                || event.getReason() == DespawnReason.RELOAD) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Preventing further respawns of", event.getNPC(),
                        "due to DespawnReason." + event.getReason());
            }
            toRespawn.values().remove(event.getNPC());
        } else if (Messaging.isDebugging()) {
            Messaging.debug("Removing", event.getNPC(),
                    "from skin tracker due to DespawnReason." + event.getReason().name());
        }
        skinUpdateTracker.onNPCDespawn(event.getNPC());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCRemove(NPCRemoveEvent event) {
        toRespawn.values().remove(event.getNPC());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCSpawn(NPCSpawnEvent event) {
        skinUpdateTracker.onNPCSpawn(event.getNPC());
        if (Messaging.isDebugging()) {
            Messaging.debug("Removing respawns of", event.getNPC(), "due to SpawnReason." + event.getReason());
        }
        toRespawn.values().remove(event.getNPC());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (CitizensAPI.getNPCRegistry().getNPC(event.getPlayer()) == null)
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
        if (CitizensAPI.getNPCRegistry().isNPC(event.getCaught())
                && CitizensAPI.getNPCRegistry().getNPC(event.getCaught()).isProtected()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked());
        if (npc == null || Util.isOffHand(event)) {
            return;
        }
        if (npc.hasTrait(ClickRedirectTrait.class)) {
            npc = npc.getTraitNullable(ClickRedirectTrait.class).getRedirectNPC();
            if (npc == null)
                return;
        }
        Player player = event.getPlayer();
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        if (event.getHand() == EquipmentSlot.HAND && event.getPlayer().getItemInHand().getType() == Material.NAME_TAG) {
            rightClickEvent.setCancelled(npc.isProtected());
        }
        Bukkit.getPluginManager().callEvent(rightClickEvent);
        if (rightClickEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        if (SUPPORT_STOP_USE_ITEM) {
            try {
                PlayerAnimation.STOP_USE_ITEM.play(player);
            } catch (UnsupportedOperationException e) {
                SUPPORT_STOP_USE_ITEM = false;
            }
        }
        if (npc.hasTrait(CommandTrait.class)) {
            npc.getTraitNullable(CommandTrait.class).dispatch(player, CommandTrait.Hand.RIGHT);
        }
        if (npc.hasTrait(ShopTrait.class)) {
            npc.getTraitNullable(ShopTrait.class).onRightClick(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        skinUpdateTracker.updatePlayer(event.getPlayer(), Setting.INITIAL_PLAYER_JOIN_SKIN_PACKET_DELAY_TICKS.asInt(),
                true);

        ScoreboardTrait.onPlayerJoin(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
        if (npc == null) {
            return;
        }
        boolean leashProtected = npc.isProtected();
        if (npc.data().get(NPC.LEASH_PROTECTED_METADATA, leashProtected)) {
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
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getPlayer().getVehicle());
            if (npc != null) {
                event.getPlayer().leaveVehicle();
            }
        }
        skinUpdateTracker.removePlayer(event.getPlayer().getUniqueId());
        ScoreboardTrait.onPlayerQuit(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        skinUpdateTracker.updatePlayer(event.getPlayer(), 15, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getPlayer());
        if (event.getCause() == TeleportCause.PLUGIN && npc != null) {
            if (npc.hasTrait(SitTrait.class)) {
                npc.getOrAddTrait(SitTrait.class).setSitting(event.getTo());
            }
        }
        if (event.getCause() == TeleportCause.PLUGIN && !event.getPlayer().hasMetadata("citizens-force-teleporting")
                && npc != null && Setting.PLAYER_TELEPORT_DELAY.asInt() > 0) {
            event.setCancelled(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().setMetadata("citizens-force-teleporting",
                            new FixedMetadataValue(CitizensAPI.getPlugin(), true));
                    event.getPlayer().teleport(event.getTo());
                    event.getPlayer().removeMetadata("citizens-force-teleporting", CitizensAPI.getPlugin());
                }
            }, Setting.PLAYER_TELEPORT_DELAY.asInt());
        }
        skinUpdateTracker.updatePlayer(event.getPlayer(), 15, true);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        // hack: Spigot now unloads plugin classes on disable in reverse order so prefer unloading at the start of
        // plugin disable cycle
        PluginDescriptionFile file = event.getPlugin().getDescription();
        for (String plugin : Iterables.concat(file.getDepend(), file.getSoftDepend())) {
            if (plugin.equalsIgnoreCase("citizens") && CitizensAPI.hasImplementation()) {
                ((Citizens) CitizensAPI.getPlugin()).onDependentPluginDisable();
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplashEvent(PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (npc == null)
                continue;
            if (npc.isProtected()) {
                event.setIntensity(entity, 0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof FishHook))
            return;
        NMS.removeHookIfNecessary(CitizensAPI.getNPCRegistry(), (FishHook) event.getEntity());
        new BukkitRunnable() {
            int n = 0;

            @Override
            public void run() {
                if (n++ > 5) {
                    cancel();
                }
                NMS.removeHookIfNecessary(CitizensAPI.getNPCRegistry(), (FishHook) event.getEntity());
            }
        }.runTaskTimer(CitizensAPI.getPlugin(), 0, 1);
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getVehicle());
        if (npc == null) {
            return;
        }
        event.setCancelled(npc.isProtected());

        NPCVehicleDamageEvent damageEvent = new NPCVehicleDamageEvent(npc, event);
        Bukkit.getPluginManager().callEvent(damageEvent);

        if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player))
            return;
        Player damager = (Player) damageEvent.getDamager();

        NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
        Bukkit.getPluginManager().callEvent(leftClickEvent);
        if (npc.hasTrait(CommandTrait.class)) {
            npc.getTraitNullable(CommandTrait.class).dispatch(damager, CommandTrait.Hand.LEFT);
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getVehicle());
        if (npc == null) {
            return;
        }
        event.setCancelled(npc.isProtected());
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getVehicle());
        NPC rider = CitizensAPI.getNPCRegistry().getNPC(event.getEntered());
        if (npc == null) {
            if (rider != null && rider.isProtected() && (event.getVehicle().getType().name().contains("BOAT")
                    || event.getVehicle() instanceof Minecart)) {
                event.setCancelled(true);
            }
            return;
        }
        if (npc.getEntity() instanceof Vehicle
                && (!npc.hasTrait(Controllable.class) || !npc.getTraitNullable(Controllable.class).isEnabled())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        for (ChunkCoord chunk : toRespawn.keySet()) {
            if (!chunk.worldUUID.equals(event.getWorld().getUID()) || !event.getWorld().isChunkLoaded(chunk.x, chunk.z))
                continue;
            respawnAllFromCoord(chunk, event);
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
                    if (event.getWorld().getUID().equals(coord.worldUUID)) {
                        respawnAllFromCoord(coord, event);
                    }
                }
                event.setCancelled(true);
                return;
            }
            if (npc.isSpawned()) {
                toRespawn.put(new ChunkCoord(npc.getEntity().getLocation()), npc);
                Messaging.debug("Despawned", npc, "due to world unload at", event.getWorld().getName());
            }
        }
    }

    private void respawnAllFromCoord(ChunkCoord coord, Event event) {
        List<NPC> ids = Lists.newArrayList(toRespawn.get(coord));
        if (ids.size() > 0) {
            Messaging.debug("Respawning all NPCs at", coord, "due to", event);
        }
        for (int i = 0; i < ids.size(); i++) {
            NPC npc = ids.get(i);
            if (npc.getOwningRegistry().getById(npc.getId()) != npc) {
                if (Messaging.isDebugging()) {
                    Messaging.debug("Prevented deregistered NPC from respawning", npc);
                }
                continue;
            }
            if (npc.isSpawned()) {
                if (Messaging.isDebugging()) {
                    Messaging.debug("Can't respawn NPC", npc, ": already spawned");
                }
                continue;
            }
            boolean success = spawn(npc);
            if (!success) {
                ids.remove(i--);
                if (Messaging.isDebugging()) {
                    Messaging.debug("Couldn't respawn", npc, "during", event, "at", coord);
                }
                continue;
            }
            if (Messaging.isDebugging()) {
                Messaging.debug("Spawned", npc, "during", event, "at", coord);
            }
        }
        for (NPC npc : ids) {
            toRespawn.remove(coord, npc);
        }
    }

    private boolean spawn(NPC npc) {
        Location spawn = npc.getOrAddTrait(CurrentLocation.class).getLocation();
        if (spawn == null) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Couldn't find a spawn location for despawned NPC", npc);
            }
            return false;
        }
        return npc.spawn(spawn, SpawnReason.CHUNK_LOAD);
    }

    void unloadNPCs(ChunkEvent event, List<Entity> entities) {
        final List<NPC> toDespawn = Lists.newArrayList();
        for (Entity entity : entities) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            // XXX : npc#isSpawned() checks entity valid status which is now inconsistent on chunk unload between
            // different server software (e.g. Paper and Spigot), so check for npc.getEntity() == null instead.
            if (npc == null || npc.getEntity() == null)
                continue;
            toDespawn.add(npc);
        }
        if (toDespawn.isEmpty())
            return;
        ChunkCoord coord = new ChunkCoord(event.getChunk());
        boolean loadChunk = false;
        for (NPC npc : toDespawn) {
            if (!npc.despawn(DespawnReason.CHUNK_UNLOAD)) {
                if (!(event instanceof Cancellable)) {
                    if (Messaging.isDebugging()) {
                        Messaging.debug("Reloading chunk because", npc, "couldn't despawn");
                    }
                    loadChunk = true;
                    toRespawn.put(coord, npc);
                    continue;
                }
                ((Cancellable) event).setCancelled(true);
                Messaging.debug("Cancelled chunk unload at", coord);
                respawnAllFromCoord(coord, event);
                return;
            }
            toRespawn.put(coord, npc);
            if (Messaging.isDebugging()) {
                Messaging.debug("Despawned", npc, "due to chunk unload at", coord);
            }
        }
        if (Messaging.isDebugging() && Setting.DEBUG_CHUNK_LOADS.asBoolean()) {
            new Exception("CITIZENS CHUNK UNLOAD DEBUG " + coord).printStackTrace();
        }
        if (loadChunk) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Loading chunk in 10 ticks due to forced chunk load at", coord);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (!event.getChunk().isLoaded()) {
                        event.getChunk().load();
                    }
                }
            }, 10);
        }
    }

    private static boolean SUPPORT_STOP_USE_ITEM = true;
}