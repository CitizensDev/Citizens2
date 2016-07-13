package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

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
            if (NMS.tick(itr.next())) {
                itr.remove();
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

    private static Map<UUID, org.bukkit.entity.Entity> TICKERS = new HashMap<UUID, org.bukkit.entity.Entity>();
    private static List<org.bukkit.entity.Entity> TICKERS_PENDING_ADD = new ArrayList<org.bukkit.entity.Entity>();
    private static List<org.bukkit.entity.Entity> TICKERS_PENDING_REMOVE = new ArrayList<org.bukkit.entity.Entity>();
}
