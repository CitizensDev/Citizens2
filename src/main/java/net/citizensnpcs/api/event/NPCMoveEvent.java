package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(from.getWorld(), "from.getWorld() cannot be null");
        this.from = from;
    }

    public void setTo(Location to) {
        Objects.requireNonNull(to, "to cannot be null");
        Objects.requireNonNull(to.getWorld(), "to.getWorld() cannot be null");
        this.to = to;
    }

    public boolean hasChangedPosition() {
        return hasExplicitlyChangedPosition() || !from.getWorld().equals(to.getWorld());
    }

    public boolean hasExplicitlyChangedPosition() {
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }

    public boolean hasChangedBlock() {
        return hasExplicitlyChangedBlock() || !from.getWorld().equals(to.getWorld());
    }

    public boolean hasExplicitlyChangedBlock() {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    public boolean hasChangedRotation() {
        return from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw();
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
