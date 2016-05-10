package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.v1_9_R2.CrashReport;
import net.minecraft.server.v1_9_R2.CrashReportSystemDetails;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.ReportedException;

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
            org.bukkit.entity.Entity ent = TICKERS_PENDING_REMOVE.get(i);
            TICKERS.remove(ent.getUniqueId());
        }
        TICKERS_PENDING_ADD.clear();
        TICKERS_PENDING_REMOVE.clear();
        Iterator<org.bukkit.entity.Entity> itr = TICKERS.values().iterator();
        while (itr.hasNext()) {
            Entity entity = NMS.getHandle(itr.next());
            Entity entity1 = entity.bz();
            if (entity1 != null) {
                if ((entity1.dead) || (!entity1.w(entity))) {
                    entity.stopRiding();
                }
            } else {
                if (!entity.dead) {
                    try {
                        entity.world.g(entity);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.a(throwable, "Ticking player");
                        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Player being ticked");

                        entity.appendEntityCrashDetails(crashreportsystemdetails);
                        throw new ReportedException(crashreport);
                    }
                }
                boolean removeFromPlayerList = ((NPCHolder) entity).getNPC().data().get("removefromplayerlist",
                        Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
                if (entity.dead) {
                    entity.world.removeEntity(entity);
                    itr.remove();
                } else if (!removeFromPlayerList) {
                    itr.remove();
                    if (!entity.world.players.contains(entity)) {
                        entity.world.players.add((EntityHuman) entity);
                    }
                } else {
                    entity.world.players.remove(entity);
                }
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
