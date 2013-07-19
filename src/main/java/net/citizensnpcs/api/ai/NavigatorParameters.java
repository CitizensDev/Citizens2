package net.citizensnpcs.api.ai;

import java.util.List;

import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;

import com.google.common.collect.Lists;

public class NavigatorParameters implements Cloneable {
    private AttackStrategy attackStrategy;
    private boolean avoidWater;
    private float baseSpeed;
    private final List<NavigatorCallback> callbacks = Lists.newArrayListWithExpectedSize(3);
    private double distanceMargin = 3F;
    private final List<BlockExaminer> examiners = Lists.newArrayList();
    private float range;
    private float speedModifier = 1F;
    private int stationaryTicks = -1;
    private StuckAction stuckAction;

    /**
     * Adds a {@link NavigatorCallback} that will be removed
     * <em>immediately</em> after being called.
     * 
     * @param callback
     *            The callback
     */
    public NavigatorParameters addSingleUseCallback(NavigatorCallback callback) {
        callbacks.add(callback);
        return this;
    }

    public void addSingleUseCallback(Object callback) {
    }

    /**
     * @return The {@link AttackStrategy} currently in use (may be null)
     */
    public AttackStrategy attackStrategy() {
        return attackStrategy;
    }

    /**
     * Sets the {@link AttackStrategy} for use when attacking entity targets.
     * 
     * @param strategy
     *            The strategy to use
     */
    public void attackStrategy(AttackStrategy strategy) {
        attackStrategy = strategy;
    }

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
        return baseSpeed;
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
    public NavigatorParameters baseSpeed(float speed) {
        this.baseSpeed = speed;
        return this;
    }

    /**
     * @return All callbacks currently registered
     */
    public Iterable<NavigatorCallback> callbacks() {
        return callbacks;
    }

    /**
     * Clears all current {@link BlockExaminer}s.
     */
    public NavigatorParameters clearExaminers() {
        examiners.clear();
        return this;
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
     * Returns the distance margin that the {@link Navigator} will be able to
     * stop from the target. The margin will be measured against the block
     * distance squared.
     * 
     * @return The distance margin
     */
    public double distanceMargin() {
        return distanceMargin;
    }

    /**
     * Sets the distance margin.
     * 
     * @see #distanceMargin()
     * @param newMargin
     *            The new distance margin
     */
    public NavigatorParameters distanceMargin(double newMargin) {
        distanceMargin = newMargin;
        return this;
    }

    /**
     * Adds the given {@link BlockExaminer}.
     * 
     * @param examiner
     *            The BlockExaminer to add
     */
    public NavigatorParameters examiner(BlockExaminer examiner) {
        examiners.add(examiner);
        return this;
    }

    /**
     * Gets a copy of all current {@link BlockExaminer}s.
     * 
     * @return An array of all current examiners
     */
    public BlockExaminer[] examiners() {
        return examiners.toArray(new BlockExaminer[examiners.size()]);
    }

    /**
     * Modifieds the given speed value based on the current parameters.
     * 
     * @param toModify
     *            The speed value to modify
     * @return The modified speed
     */
    public float modifiedSpeed(float toModify) {
        return toModify * speedModifier();
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
     * up to save computation.
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
        return modifiedSpeed(baseSpeed);
    }

    /**
     * Sets the base movement speed of the {@link Navigator}. Note that this is
     * mob-specific and may not always be sane. Using {@link #speedModifier()}
     * is preferred.
     * 
     * @see #speedModifier()
     * @param speed
     *            The new movement speed
     * @deprecated @see {@link #baseSpeed(float)}
     */
    @Deprecated
    public NavigatorParameters speed(float speed) {
        this.baseSpeed = speed;
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

    /**
     * Gets the {@link StuckAction} of these parameters. This will be run when
     * the navigation is stuck and must either be fixed up or cancelled.
     * 
     * @return The current stuck action
     */
    public StuckAction stuckAction() {
        return stuckAction;
    }

    /**
     * Sets the {@link StuckAction} of the parameters.
     * 
     * @param action
     *            The new stuck action
     * @see #stuckAction()
     */
    public NavigatorParameters stuckAction(StuckAction action) {
        stuckAction = action;
        return this;
    }
}
