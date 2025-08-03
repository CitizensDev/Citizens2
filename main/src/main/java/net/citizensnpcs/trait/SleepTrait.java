package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.trait.EntityPoseTrait.EntityPose;
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
        npc.getOrAddTrait(EntityPoseTrait.class).setPose(null);
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
        if (sleeping)
            return;
        if (npc.getEntity() instanceof Player) {
            Player player = (Player) npc.getEntity();
            if (SUPPORT_BLOCKDATA) {
                try {
                    if (at.getBlock().getBlockData() instanceof Bed || at.getBlock().getState() instanceof Bed) {
                        player.sleep(at, true);
                    } else {
                        NMS.sleep(player, true);
                    }
                } catch (Throwable t) {
                    SUPPORT_BLOCKDATA = false;
                    NMS.sleep(player, true);
                }
            } else {
                NMS.sleep(player, true);
            }
            npc.getOrAddTrait(EntityPoseTrait.class).setPose(EntityPose.SLEEPING);
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
        npc.getOrAddTrait(EntityPoseTrait.class).setPose(null);
        if (npc.getEntity() instanceof Player) {
            NMS.sleep((Player) npc.getEntity(), false);
        } else if (npc.getEntity() instanceof Villager) {
            ((Villager) npc.getEntity()).wakeup();
        }
        sleeping = false;
    }

    private static Boolean SUPPORT_BLOCKDATA = null;
    static {
        try {
            Block.class.getMethod("getBlockData");
            SUPPORT_BLOCKDATA = true;
        } catch (NoSuchMethodException | SecurityException e) {
            SUPPORT_BLOCKDATA = false;
        }
    }
}
