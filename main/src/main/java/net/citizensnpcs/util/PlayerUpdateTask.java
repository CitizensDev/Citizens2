package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.PacketNPC;

public class PlayerUpdateTask extends BukkitRunnable {
    @Override
    public void cancel() {
        super.cancel();
        PLAYERS.clear();
    }

    @Override
    public void run() {
        for (Entity entity : PLAYERS_PENDING_REMOVE) {
            PLAYERS.remove(entity.getUniqueId());
        }
        for (Entity entity : PLAYERS_PENDING_ADD) {
            PlayerTick rm = PLAYERS.remove(entity.getUniqueId());
            NPC next = ((NPCHolder) entity).getNPC();
            if (rm != null) {
                NPC old = ((NPCHolder) rm.entity).getNPC();
                Messaging.severe(old == next ? "Player registered twice"
                        : "Player registered twice with different NPC instances", rm.entity.getUniqueId());
                rm.entity.remove();
            }
            if (next.hasTrait(PacketNPC.class)) {
                PLAYERS.put(entity.getUniqueId(), new PlayerTick(entity, () -> ((CitizensNPC) next).update()));
            } else {
                PLAYERS.put(entity.getUniqueId(), new PlayerTick((Player) entity));
            }
        }
        PLAYERS_PENDING_ADD.clear();
        PLAYERS_PENDING_REMOVE.clear();

        PLAYERS.values().forEach(Runnable::run);
    }

    private static class PlayerTick implements Runnable {
        private final Entity entity;
        private final Runnable tick;

        public PlayerTick(Entity entity, Runnable tick) {
            this.entity = entity;
            this.tick = tick;
        }

        public PlayerTick(Player player) {
            this(player, NMS.playerTicker(player));
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

    private static Map<UUID, PlayerTick> PLAYERS = new HashMap<>();
    private static List<Entity> PLAYERS_PENDING_ADD = new ArrayList<>();
    private static List<Entity> PLAYERS_PENDING_REMOVE = new ArrayList<>();
}
