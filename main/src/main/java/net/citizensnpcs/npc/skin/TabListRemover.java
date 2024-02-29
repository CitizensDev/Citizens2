package net.citizensnpcs.npc.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;

/**
 * Sends remove packets in batch per player.
 *
 * <p>
 * Collects entities to remove and sends them all to the player in a single packet.
 * </p>
 */
public class TabListRemover {
    private final Map<UUID, PlayerEntry> pending = new HashMap<>(
            Math.max(128, Math.min(1024, Bukkit.getMaxPlayers() / 2)));

    TabListRemover() {
        Bukkit.getScheduler().runTaskTimer(CitizensAPI.getPlugin(), new Sender(), 2, 2);
    }

    /**
     * Cancel packets pending to be sent to the specified player.
     *
     * @param player
     *            The player.
     */
    public void cancelPackets(Player player) {
        Preconditions.checkNotNull(player);

        PlayerEntry entry = pending.remove(player.getUniqueId());
        if (entry == null)
            return;

        for (SkinnableEntity entity : entry.toRemove) {
            entity.getSkinTracker().notifyRemovePacketCancelled(player.getUniqueId());
        }
    }

    /**
     * Cancel packets pending to be sent to the specified player for the specified skinnable entity.
     *
     * @param player
     *            The player.
     * @param skinnable
     *            The skinnable entity.
     */
    public void cancelPackets(Player player, SkinnableEntity skinnable) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(skinnable);

        PlayerEntry entry = pending.get(player.getUniqueId());
        if (entry == null)
            return;

        if (entry.toRemove.remove(skinnable)) {
            skinnable.getSkinTracker().notifyRemovePacketCancelled(player.getUniqueId());
        }
        if (entry.toRemove.isEmpty()) {
            pending.remove(player.getUniqueId());
        }
    }

    private PlayerEntry getEntry(Player player) {
        PlayerEntry entry = pending.get(player.getUniqueId());
        if (entry == null) {
            entry = new PlayerEntry(player);
            pending.put(player.getUniqueId(), entry);
        }
        return entry;
    }

    /**
     * Send a remove packet to the specified player for the specified skinnable entity.
     *
     * @param player
     *            The player to send the packet to.
     * @param entity
     *            The entity to remove.
     */
    public void sendPacket(Player player, SkinnableEntity entity) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(entity);

        PlayerEntry entry = getEntry(player);

        entry.toRemove.add(entity);
    }

    private static class PlayerEntry {
        Player player;
        Set<SkinnableEntity> toRemove = new HashSet<>(20);

        PlayerEntry(Player player) {
            this.player = player;
        }
    }

    private class Sender implements Runnable {
        @Override
        public void run() {
            int maxPacketEntries = 15;

            Iterator<Map.Entry<UUID, PlayerEntry>> entryIterator = pending.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<UUID, PlayerEntry> mapEntry = entryIterator.next();
                PlayerEntry entry = mapEntry.getValue();
                if (!entry.player.isOnline()) {
                    entryIterator.remove();
                    continue;
                }
                int listSize = Math.min(maxPacketEntries, entry.toRemove.size());

                List<Player> skinnableList = new ArrayList<>(listSize);

                int i = 0;
                for (Iterator<SkinnableEntity> skinIterator = entry.toRemove.iterator(); skinIterator.hasNext();) {
                    if (i >= maxPacketEntries)
                        break;

                    SkinnableEntity next = skinIterator.next();
                    skinnableList.add(next.getBukkitEntity());
                    next.getSkinTracker().notifyRemovePacketSent(entry.player.getUniqueId());
                    skinIterator.remove();
                    i++;
                }
                NMS.sendTabListRemove(entry.player, skinnableList);
                if (entry.toRemove.isEmpty()) {
                    entryIterator.remove();
                }
            }
        }
    }
}
