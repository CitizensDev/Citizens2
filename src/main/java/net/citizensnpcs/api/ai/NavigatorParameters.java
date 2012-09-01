package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.ai.event.CancelReason;

public class NavigatorParameters implements Cloneable {
    private boolean avoidWater;
    private float range;
    private float speed;
    private float speedModifier = 1F;
    private int stationaryTicks = -1;
    private StuckAction stuckAction = TeleportStuckAction.INSTANCE;

    /**
     * @return Whether to avoid water while pathfinding
     */
    public boolean avoidWater() {
        return avoidWater;
    }

    /**
     * Sets whether to avoid water while pathfinding
     * 
     * @param avoidWater
     *            Whether to avoid water
     */
    public NavigatorParameters avoidWater(boolean avoidWater) {
        this.avoidWater = avoidWater;
        return this;
    }

    /**
     * @return The base movement speed
     */
    public float baseSpeed() {
        return speed;
    }

    @Override
    public NavigatorParameters clone() {
        try {
            return (NavigatorParameters) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * @return The pathfinding range of the navigator in blocks.
     * @see #range(float)
     */
    public float range() {
        return range;
    }

    /**
     * Sets the pathfinding range in blocks. The pathfinding range determines
     * how far away the {@link Navigator} will attempt to pathfind before giving
     * up to save computations.
     * 
     * @param range
     *            The new range
     */
    public NavigatorParameters range(float range) {
        this.range = range;
        return this;
    }

    /**
     * @return The modified movement speed as given by {@link #baseSpeed()}
     *         multiplied by {@link #speedModifier()}
     */
    public float speed() {
        return speed * speedModifier;
    }

    /**
     * Sets the base movement speed of the {@link Navigator}. Note that this is
     * mob-specific and may not always be sane. Using {@link #speedModifier()}
     * is preferred.
     * 
     * @see #speedModifier()
     * @param speed
     *            The new movement speed
     */
    public NavigatorParameters speed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * @return The movement speed modifier
     * @see #speedModifier(float)
     */
    public float speedModifier() {
        return speedModifier;
    }

    /**
     * Sets the movement speed modifier of the {@link Navigator}. This is a
     * percentage modifier that alters the movement speed returned in
     * {@link #speed()}.
     * 
     * @param percent
     *            The new speed modifier
     */
    public NavigatorParameters speedModifier(float percent) {
        speedModifier = percent;
        return this;
    }

    /**
     * @return The number of stationary ticks
     * @see #stationaryTicks(int)
     */
    public int stationaryTicks() {
        return stationaryTicks;
    }

    /**
     * Sets the number of stationary ticks before navigation is cancelled with a
     * {@link CancelReason} of STUCK.
     * 
     * @param ticks
     *            The new number of stationary ticks
     */
    public NavigatorParameters stationaryTicks(int ticks) {
        stationaryTicks = ticks;
        return this;
    }

    public StuckAction stuckAction() {
        return stuckAction;
    }

    public NavigatorParameters stuckAction(StuckAction action) {
        stuckAction = action;
        return this;
    }
}
