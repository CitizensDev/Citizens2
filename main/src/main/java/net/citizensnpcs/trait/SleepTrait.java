package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.block.Bed;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("sleeptrait")
public class SleepTrait extends Trait {
    @Persist
    private Location at;
    private boolean sleeping;

    public SleepTrait() {
        super("sleeptrait");
    }

    @Override
    public void onDespawn() {
        sleeping = false;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;

        if (at == null) {
            if (sleeping) {
                wakeup();
            }
            return;
        }
        if (SUPPORT_BLOCKDATA == null) {
            try {
                SUPPORT_BLOCKDATA = true;
                at.getBlock().getBlockData();
            } catch (NoSuchMethodError e) {
                SUPPORT_BLOCKDATA = false;
            }
        }
        if (npc.getEntity() instanceof Player) {
            Player player = (Player) npc.getEntity();
            if (!SUPPORT_BLOCKSTATE) {
                NMS.sleep(player, true);
            } else {
                try {
                    if (SUPPORT_BLOCKDATA && at.getBlock().getBlockData() instanceof Bed
                            || at.getBlock().getState() instanceof Bed) {
                        player.sleep(at, true);
                    } else {
                        NMS.sleep(player, true);
                    }
                } catch (Throwable t) {
                    SUPPORT_BLOCKSTATE = false;
                    NMS.sleep(player, true);
                }
            }
            sleeping = true;
        } else if (npc.getEntity() instanceof Villager) {
            sleeping = ((Villager) npc.getEntity()).sleep(at);
        }
    }

    public void setSleeping(Location at) {
        this.at = at != null ? at.clone() : null;
        wakeup();
    }

    private void wakeup() {
        if (npc.getEntity() instanceof Player) {
            NMS.sleep((Player) npc.getEntity(), false);
        } else if (npc.getEntity() instanceof Villager) {
            ((Villager) npc.getEntity()).wakeup();
        }
        sleeping = false;
    }

    private static Boolean SUPPORT_BLOCKDATA = null;
    private static boolean SUPPORT_BLOCKSTATE = true;
}
