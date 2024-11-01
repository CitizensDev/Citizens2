package net.citizensnpcs.api.event;

import com.google.common.base.Preconditions;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NPCMoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final NPC npc;
    private Location from;
    private Location to;
    private boolean cancelled;

    public NPCMoveEvent(NPC npc, Location from, Location to) {
        this.npc = npc;
        this.from = from;
        this.to = to;
    }

    public NPC getNPC() {
        return npc;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public void setFrom(Location from) {
        validateLocation(from);
        this.from = from;
    }

    public void setTo(Location to) {
        validateLocation(to);
        this.to = to;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    private void validateLocation(Location loc) {
        Preconditions.checkArgument(loc != null, "Cannot use null location!");
        Preconditions.checkArgument(loc.getWorld() != null, "Cannot use null location with null world!");
    }

    @Override
    public String toString() {
        return "NPCMoveEvent{" +
                "npc=" + npc +
                ", from=" + from +
                ", to=" + to +
                ", cancelled=" + cancelled +
                '}';
    }
}
