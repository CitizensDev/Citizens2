package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    }

    @Override
    public void run() {
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
        if (remove) {
            TICKERS.remove(entity);
        } else {
            TICKERS.add(entity);
        }
    }

    private static List<org.bukkit.entity.Entity> TICKERS = new ArrayList<org.bukkit.entity.Entity>();
}
