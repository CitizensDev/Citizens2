package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.citizensnpcs.api.util.schedulers.SchedulerRunnable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.PacketNPC;

public class PlayerUpdateTask extends SchedulerRunnable {
    private final java.util.Queue<PlayerTick> players = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final Set<UUID> uuids = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @Override
    public void cancel() {
        super.cancel();
        uuids.clear();
        players.clear();
        PLAYERS_PENDING_ADD.clear();
        PLAYERS_PENDING_REMOVE.clear();
    }

    @Override
    public void run() {
        if (PLAYERS_PENDING_REMOVE.size() > 0) {
            players.removeIf(pt -> PLAYERS_PENDING_REMOVE.contains(pt.entity.getUniqueId()));
            for (UUID uuid : PLAYERS_PENDING_REMOVE) {
                uuids.remove(uuid);
            }
            PLAYERS_PENDING_REMOVE.clear();
        }
        for (Entity entity : PLAYERS_PENDING_ADD) {
            NPC next = ((NPCHolder) entity).getNPC();
            if (uuids.contains(entity.getUniqueId())) {
                // XXX: how often does this case get hit? can simplify implementation
                PlayerTick rm = null;
                for (Iterator<PlayerTick> itr = players.iterator(); itr.hasNext();) {
                    PlayerTick pt = itr.next();
                    if (pt.entity.getUniqueId().equals(entity.getUniqueId())) {
                        rm = pt;
                        itr.remove();
                        uuids.remove(pt.entity.getUniqueId());
                        break;
                    }
                }
                NPC old = ((NPCHolder) rm.entity).getNPC();
                if (old != next) {
                    Messaging.severe("Player registered twice with different NPC instances", rm.entity.getUniqueId());
                }
                if (rm.entity instanceof Player) {
                    ((Player) rm.entity).kickPlayer(null);
                } else {
                    rm.entity.remove();
                }
            }
            if (next.hasTrait(PacketNPC.class)) {
                players.add(new PlayerTick(entity, () -> ((AbstractNPC) next).update()));
            } else {
                players.add(new PlayerTick(entity, NMS.playerTicker((Player) entity)));
            }
            uuids.add(entity.getUniqueId());
        }
        PLAYERS_PENDING_ADD.clear();

        for (PlayerTick player : players) {
            player.run();
        }
    }

    private static class PlayerTick implements Runnable {
        private final Entity entity;
        private final Runnable tick;

        public PlayerTick(Entity entity, Runnable tick) {
            this.entity = entity;
            this.tick = tick;
        }

        @Override
        public void run() {
            net.citizensnpcs.api.CitizensAPI.getScheduler().runEntityTask(entity, tick);
        }
    }

    public static void deregister(org.bukkit.entity.Entity entity) {
        PLAYERS_PENDING_ADD.remove(entity);
        PLAYERS_PENDING_REMOVE.add(entity.getUniqueId());
    }

    public static void register(org.bukkit.entity.Entity entity) {
        PLAYERS_PENDING_REMOVE.remove(entity.getUniqueId());
        PLAYERS_PENDING_ADD.add(entity);
    }

    private static final java.util.Queue<Entity> PLAYERS_PENDING_ADD = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private static final java.util.Queue<UUID> PLAYERS_PENDING_REMOVE = new java.util.concurrent.ConcurrentLinkedQueue<>();
}
