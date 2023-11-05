package net.citizensnpcs.trait;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("pausepathfinding")
public class PausePathfindingTrait extends Trait {
    @Persist("pauseticks")
    private int pauseTicks;
    @Persist("playerrange")
    private double playerRange = -1;
    @Persist("rightclick")
    private boolean rightclick;
    private int unpauseTaskId = -1;

    public PausePathfindingTrait() {
        super("pausepathfinding");
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(NPCRightClickEvent event) {
        if (!rightclick || event.getNPC() != npc)
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
        unpauseTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                () -> npc.getNavigator().setPaused(false), pauseTicks <= 0 ? 20 : pauseTicks);
    }

    @Override
    public void run() {
        if (playerRange == -1 || !npc.isSpawned() || unpauseTaskId == -1 && !npc.getNavigator().isNavigating())
            return;
        if (CitizensAPI.getLocationLookup().getNearbyPlayers(npc.getStoredLocation(), playerRange).iterator()
                .hasNext()) {
            pause();
        }
    }

    public void setPauseTicks(int pauseTicks) {
        this.pauseTicks = pauseTicks;
    }

    public void setPlayerRangeBlocks(double range) {
        playerRange = range;
    }

    public void setRightClick(boolean rightclick) {
        this.rightclick = rightclick;
    }
}