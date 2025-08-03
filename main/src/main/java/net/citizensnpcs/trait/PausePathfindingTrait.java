package net.citizensnpcs.trait;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitEventHandler;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("pausepathfinding")
public class PausePathfindingTrait extends Trait {
    @Persist("lockoutduration")
    private int lockoutDuration = -1;
    @Persist("pauseticks")
    private int pauseTicks;
    @Persist("playerrange")
    private double playerRange = -1;
    @Persist("rightclick")
    private boolean rightclick;
    private int t;
    private int unpauseTaskId = -1;

    public PausePathfindingTrait() {
        super("pausepathfinding");
    }

    public int getLockoutDuration() {
        return lockoutDuration;
    }

    public int getPauseDuration() {
        return pauseTicks;
    }

    public double getPlayerRangeInBlocks() {
        return playerRange;
    }

    @TraitEventHandler(@EventHandler(ignoreCancelled = true))
    public void onInteract(NPCRightClickEvent event) {
        if (lockoutDuration > t || !rightclick)
            return;
        pause();
        event.setDelayedCancellation(true);
    }

    private void pause() {
        if (unpauseTaskId != -1) {
            Bukkit.getScheduler().cancelTask(unpauseTaskId);
        }
        npc.getNavigator().cancelNavigation();
        npc.getNavigator().setPaused(true);
        unpauseTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            NMS.setPitch(npc.getEntity(), 0);
            npc.getNavigator().setPaused(false);
        }, pauseTicks <= 0 ? 20 : pauseTicks);
        t = 0;
    }

    public boolean pauseOnRightClick() {
        return rightclick;
    }

    @Override
    public void run() {
        if (lockoutDuration > t++ || playerRange == -1 || !npc.isSpawned()
                || unpauseTaskId == -1 && !npc.getNavigator().isNavigating())
            return;

        if (CitizensAPI.getLocationLookup()
                .getNearbyVisiblePlayers(npc.getEntity(), npc.getStoredLocation(), playerRange).iterator().hasNext()) {
            pause();
        }
    }

    public void setLockoutDuration(int ticks) {
        this.lockoutDuration = ticks;
    }

    public void setPauseDuration(int ticks) {
        this.pauseTicks = ticks;
    }

    public void setPauseOnRightClick(boolean rightclick) {
        this.rightclick = rightclick;
    }

    public void setPlayerRange(double blockRange) {
        playerRange = blockRange;
    }
}