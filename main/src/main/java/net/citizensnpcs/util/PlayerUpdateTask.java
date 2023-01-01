package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.NPCHolder;

public class PlayerUpdateTask extends BukkitRunnable {
    @Override
    public void cancel() {
        super.cancel();
        TICKERS.clear();
        TICKERS_PENDING_ADD.clear();
        TICKERS_PENDING_REMOVE.clear();
    }

    @Override
    public void run() {
        for (int i = 0; i < TICKERS_PENDING_ADD.size(); i++) {
            org.bukkit.entity.Entity ent = TICKERS_PENDING_ADD.get(i);
            TICKERS.put(ent.getUniqueId(), ent);
        }
        for (int i = 0; i < TICKERS_PENDING_REMOVE.size(); i++) {
            TICKERS.remove(TICKERS_PENDING_REMOVE.get(i).getUniqueId());
        }
        TICKERS_PENDING_ADD.clear();
        TICKERS_PENDING_REMOVE.clear();
        Iterator<org.bukkit.entity.Entity> itr = TICKERS.values().iterator();
        while (itr.hasNext()) {
            Entity entity = itr.next();
            if (NMS.tick(entity)) {
                itr.remove();
            }
        }

        for (Entity entity : PLAYERS_PENDING_REMOVE) {
            PLAYERS.remove(entity.getUniqueId());
        }
        for (Entity entity : PLAYERS_PENDING_ADD) {
            PlayerTick rm = PLAYERS.remove(entity.getUniqueId());
            if (rm != null) {
                NPC old = ((NPCHolder) rm).getNPC();
                NPC next = ((NPCHolder) entity).getNPC();
                Messaging.severe(old == next ? "Player registered twice"
                        : "Player registered twice with different NPC instances", rm.entity.getUniqueId());
                rm.entity.remove();
            }
            PLAYERS.put(entity.getUniqueId(), new PlayerTick((Player) entity));
        }
        PLAYERS_PENDING_ADD.clear();
        PLAYERS_PENDING_REMOVE.clear();

        PLAYERS.values().forEach(Runnable::run);
    }

    private static class PlayerTick implements Runnable {
        Player entity;
        Runnable tick;

        public PlayerTick(Player player) {
            entity = player;
            tick = NMS.playerTicker(player);
        }

        @Override
        public void run() {
            if (entity.isValid()) {
                tick.run();
            }
        }
    }

    public static void addOrRemove(org.bukkit.entity.Entity entity, boolean remove) {
        boolean contains = TICKERS.containsKey(entity.getUniqueId());
        if (!remove) {
            if (contains) {
                TICKERS_PENDING_REMOVE.add(entity);
            }
        } else if (!contains) {
            TICKERS_PENDING_ADD.add(entity);
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

    private static Map<UUID, PlayerTick> PLAYERS = new HashMap<>();
    private static List<Entity> PLAYERS_PENDING_ADD = new ArrayList<>();
    private static List<Entity> PLAYERS_PENDING_REMOVE = new ArrayList<>();
    private static Map<UUID, Entity> TICKERS = new HashMap<>();
    private static List<Entity> TICKERS_PENDING_ADD = new ArrayList<>();
    private static List<Entity> TICKERS_PENDING_REMOVE = new ArrayList<>();
}
