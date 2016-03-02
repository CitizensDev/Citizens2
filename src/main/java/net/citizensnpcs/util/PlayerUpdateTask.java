package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_9_R1.CrashReport;
import net.minecraft.server.v1_9_R1.CrashReportSystemDetails;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.ReportedException;

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
        TICKERS.removeAll(TICKERS_PENDING_REMOVE);
        TICKERS.addAll(TICKERS_PENDING_ADD);
        TICKERS_PENDING_ADD.clear();
        TICKERS_PENDING_REMOVE.clear();
        Iterator<org.bukkit.entity.Entity> itr = TICKERS.iterator();
        while (itr.hasNext()) {
            Entity entity = NMS.getHandle(itr.next());
            Entity entity1 = entity.by();
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
                if (entity.dead) {
                    entity.world.removeEntity(entity);
                    itr.remove();
                }
            }
        }
    }

    public static void addOrRemove(org.bukkit.entity.Entity entity, boolean remove) {
        if (!remove) {
            TICKERS_PENDING_REMOVE.add(entity);
        } else {
            TICKERS_PENDING_ADD.add(entity);
        }
    }

    private static Set<org.bukkit.entity.Entity> TICKERS = new HashSet<org.bukkit.entity.Entity>();
    private static List<org.bukkit.entity.Entity> TICKERS_PENDING_ADD = new ArrayList<org.bukkit.entity.Entity>();
    private static List<org.bukkit.entity.Entity> TICKERS_PENDING_REMOVE = new ArrayList<org.bukkit.entity.Entity>();
}
