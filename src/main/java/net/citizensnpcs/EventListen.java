package net.citizensnpcs;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.trait.text.Text;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.internal.Pair;

public class EventListen implements Listener {
    private volatile CitizensNPCManager npcManager;
    private final ListMultimap<Pair<Integer, Integer>, Integer> toRespawn = ArrayListMultimap.create();

    public EventListen(CitizensNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    /*
     * Chunk events
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Pair<Integer, Integer> coord = toIntPair(event.getChunk());
        if (!toRespawn.containsKey(coord))
            return;
        for (int id : toRespawn.get(coord)) {
            NPC npc = npcManager.getNPC(id);
            npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
        }
        toRespawn.removeAll(coord);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.isCancelled())
            return;

        Pair<Integer, Integer> coord = toIntPair(event.getChunk());
        for (NPC npc : npcManager) {
            if (!npc.isSpawned())
                continue;
            Location loc = npc.getBukkitEntity().getLocation();
            if (event.getWorld().equals(loc.getWorld()) && event.getChunk().getX() == loc.getChunk().getX()
                    && event.getChunk().getZ() == loc.getChunk().getZ()) {
                npc.getTrait(SpawnLocation.class).setLocation(loc);
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
        if (!npcManager.isNPC(event.getEntity()))
            return;

        event.setCancelled(true);
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if (e.getDamager() instanceof Player) {
                Player damager = (Player) e.getDamager();
                NPC npc = npcManager.getNPC(event.getEntity());

                // Call left-click event
                NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
                Bukkit.getPluginManager().callEvent(leftClickEvent);
                if (leftClickEvent.isCancelled())
                    return;

                if (npc.getCharacter() != null)
                    npc.getCharacter().onLeftClick(npc, damager);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled() || !npcManager.isNPC(event.getEntity()) || !(event.getTarget() instanceof Player))
            return;

        NPC npc = npcManager.getNPC(event.getEntity());
        Player player = (Player) event.getTarget();

        // Call right-click event
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        Bukkit.getPluginManager().callEvent(rightClickEvent);
        if (rightClickEvent.isCancelled())
            return;

        if (!player.hasMetadata("selected") || player.getMetadata("selected").size() == 0
                || player.getMetadata("selected").get(0).asInt() != npc.getId()) {
            if (player.getItemInHand().getTypeId() == Setting.SELECTION_ITEM.asInt()
                    && (npc.getTrait(Owner.class).getOwner().equals(player.getName()) || player
                            .hasPermission("citizens.admin"))) {
                npcManager.selectNPC(player, npc);
                Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), npc);
                if (!Setting.QUICK_SELECT.asBoolean())
                    return;
            }
        }
        // If the NPC isn't a close talker
        if (player.getItemInHand().getTypeId() == Setting.TALK_ITEM.asInt()
                && !npc.getTrait(Text.class).shouldTalkClose())
            npc.getTrait(Text.class).sendText(player);

        if (npc.getCharacter() != null)
            npc.getCharacter().onRightClick(npc, player);
    }

    /*
     * Player events
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!(((CraftPlayer) event.getPlayer()).getHandle() instanceof EntityHumanNPC))
            return;

        ((CraftServer) Bukkit.getServer()).getHandle().players.remove(((CraftPlayer) event.getPlayer()).getHandle());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!npcManager.isNPC(event.getRightClicked()))
            return;

        // Call target event for NPCs
        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(event.getRightClicked(), event.getPlayer(), TargetReason.CUSTOM));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Editor.leave(event.getPlayer());
    }

    /*
     * World events
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        for (Pair<Integer, Integer> chunk : toRespawn.keySet()) {
            if (!event.getWorld().isChunkLoaded(chunk.first, chunk.second))
                continue;
            for (int id : toRespawn.get(chunk)) {
                NPC npc = npcManager.getNPC(id);
                npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
            }
            toRespawn.removeAll(chunk);
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
            toRespawn.put(toIntPair(loc.getChunk()), npc.getId());
        }
    }

    private Pair<Integer, Integer> toIntPair(Chunk chunk) {
        return new Pair<Integer, Integer>(chunk.getX(), chunk.getZ());
    }
}