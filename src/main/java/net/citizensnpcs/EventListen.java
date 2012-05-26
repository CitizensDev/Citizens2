package net.citizensnpcs;

import javassist.compiler.ast.Pair;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
    private final ListMultimap<Pair<Integer, Integer>, Integer> toRespawn = ArrayListMultimap.create();

    /*
     * Chunk events
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Pair<Integer, Integer> coord = toIntPair(event.getChunk());
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

        Pair<Integer, Integer> coord = toIntPair(event.getChunk());
        for (NPC npc : npcRegistry) {
            if (!npc.isSpawned())
                continue;
            WorldVector loc = npc.getEntity().getLocation();
            if (event.getWorld().equals(loc.getWorld()) && event.getChunk().equals(loc.getChunk())) {
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
        if (!npcRegistry.isNPC(event.getEntity()))
            return;

        NPC npc = npcRegistry.getNPC(event.getEntity());
        if (event instanceof EntityDamageByEntityEvent) {
            NPCDamageByEntityEvent damageEvent = new NPCDamageByEntityEvent(npc, (EntityDamageByEntityEvent) event);
            CitizensAPI.getServer().callEvent(damageEvent);

            if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player))
                return;
            Player damager = (Player) damageEvent.getDamager();

            CitizensAPI.getServer().callEvent(new NPCLeftClickEvent(npc, damager));
        } else {
            CitizensAPI.getServer().callEvent(new NPCDamageEvent(npc, event));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!npcRegistry.isNPC(event.getEntity()))
            return;
        NPC npc = npcRegistry.getNPC(event.getEntity());
        npc.despawn();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled() || !npcRegistry.isNPC(event.getEntity()) || !(event.getTarget() instanceof Player))
            return;

        NPC npc = npcRegistry.getNPC(event.getEntity());
        Player player = (Player) event.getTarget();

        // Call right-click event
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        Bukkit.getPluginManager().callEvent(rightClickEvent);
        if (rightClickEvent.isCancelled())
            return;
        // If the NPC isn't a close talker
        // TODO: move this into text.class
        if (Util.isSettingFulfilled(player, Setting.TALK_ITEM) && !npc.getTrait(Text.class).shouldTalkClose())
            npc.getTrait(Text.class).sendText(player);
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
        if (!npcRegistry.isNPC(event.getRightClicked()))
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
                NPC npc = npcRegistry.getNPC(id);
                npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
            }
            toRespawn.removeAll(chunk);
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        if (event.isCancelled())
            return;

        for (NPC npc : npcRegistry) {
            if (!npc.isSpawned() || !npc.getBukkitEntity().getWorld().equals(event.getWorld()))
                continue;

            npc.despawn();
            toRespawn.put(toIntPair(npc.getBukkitEntity().getLocation().getChunk()), npc.getId());
        }
    }

    private Pair<Integer, Integer> toIntPair(Chunk chunk) {
        return new Pair<Integer, Integer>(chunk.getX(), chunk.getZ());
    }
}