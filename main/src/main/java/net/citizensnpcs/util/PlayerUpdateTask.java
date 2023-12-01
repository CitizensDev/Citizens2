package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

public class PlayerUpdateTask extends BukkitRunnable {
    private final List<PlayerTick> players = Lists.newArrayList();
    private final Set<UUID> uuids = Sets.newHashSet();

    @Override
    public void cancel() {
        super.cancel();
        uuids.clear();
        players.clear();
    }

    @Override
    public void run() {
        if (PLAYERS_PENDING_REMOVE.size() > 0) {
            players.removeIf(pt -> PLAYERS_PENDING_REMOVE.contains(pt.entity));
            for (Entity entity : PLAYERS_PENDING_REMOVE) {
                uuids.remove(entity.getUniqueId());
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
                Messaging.severe(old == next ? "Player registered twice"
                        : "Player registered twice with different NPC instances", rm.entity.getUniqueId());
                rm.entity.remove();
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
            tick.run();
        }
    }

    public static void deregisterPlayer(org.bukkit.entity.Entity entity) {
        PLAYERS_PENDING_ADD.remove(entity);
        PLAYERS_PENDING_REMOVE.add(entity);
    }

    public static void registerPlayer(org.bukkit.entity.Entity entity) {
        PLAYERS_PENDING_REMOVE.remove(entity);
        PLAYERS_PENDING_ADD.add(entity);
    }

    private static List<Entity> PLAYERS_PENDING_ADD = new ArrayList<>();
    private static List<Entity> PLAYERS_PENDING_REMOVE = new ArrayList<>();
}
