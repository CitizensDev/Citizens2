package net.citizensnpcs.api.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;

public class NavigatorParameters implements Cloneable {
    private int attackDelayTicks = 20;
    private double attackRange;
    private AttackStrategy attackStrategy;
    private boolean avoidWater;
    private float baseSpeed = 1F;
    private List<NavigatorCallback> callbacks = Lists.newArrayList();
    private boolean debug;
    private AttackStrategy defaultStrategy;
    private double destinationTeleportMargin = -1;
    private double distanceMargin = 2F;
    private List<BlockExaminer> examiners = Lists.newArrayList();
    private int fallDistance = -1;
    private Function<Navigator, Location> lookAtFunction;
    private Function<Entity, Location> mapper;
    private double pathDistanceMargin = 1F;
    private PathfinderType pathfinderType;
    private float range;
    private List<Runnable> runCallbacks = Lists.newArrayList();
    private float speedModifier = 1F;
    private int stationaryTicks = -1;
    private float straightLineTargetingDistance;
    private StuckAction stuckAction;
    private int updatePathRate;

    /**
     * Adds a {@link Runnable} callback that will be called every tick while the path is running.
     *
     * @param callback
     *            The callback to add
     */
    public NavigatorParameters addRunCallback(Runnable callback) {
        runCallbacks.add(callback);
        return this;
    }

    /**
     * Adds a {@link NavigatorCallback} that will be removed <em>immediately</em> after being called.
     *
     * @param callback
     *            The callback
     */
    public NavigatorParameters addSingleUseCallback(NavigatorCallback callback) {
        callbacks.add(callback);
        return this;
    }

    /**
     * @see #attackDelayTicks(int)
     * @return The number of ticks to wait between attacks
     */
    public int attackDelayTicks() {
        return attackDelayTicks;
    }

    /**
     * Sets the delay in ticks between attacks. When attacking a target using an aggressive target strategy, the NPC
     * waits for a certain number of ticks between attacks to avoid repeatedly damaging the target.
     *
     * @param ticks
     *            The new number of ticks to wait between attacks
     */
    public NavigatorParameters attackDelayTicks(int ticks) {
        attackDelayTicks = ticks;
        return this;
    }

    /**
     * @see #attackRange(double)
     * @return The attack range, in blocks
     */
    public double attackRange() {
        return attackRange;
    }

    /**
     * When using aggressive NPC navigation, the NPC will wait until close enough to the target before attempting to use
     * the {@link #attackStrategy()}. This parameter determines the range in blocks before the target will be valid to
     * attack.
     *
     * @param range
     *            The new attack range, in blocks
     */
    public NavigatorParameters attackRange(double range) {
        this.attackRange = range;
        return this;
    }

    /**
     * @return The {@link AttackStrategy} currently in use or the {@link #defaultAttackStrategy()} if not configured
     *         (may return null)
     */
    public AttackStrategy attackStrategy() {
        return attackStrategy == null ? defaultStrategy : attackStrategy;
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
     * Sets the base movement speed of the {@link Navigator}. Note that this is mob-specific and may not always be sane.
     * Using {@link #speedModifier()} is preferred.
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
    @SuppressWarnings("unchecked")
    public NavigatorParameters clone() {
        try {
            NavigatorParameters clone = (NavigatorParameters) super.clone();
            if (callbacks instanceof ArrayList) {
                clone.callbacks = (List<NavigatorCallback>) ((ArrayList<NavigatorCallback>) callbacks).clone();
            }
            if (examiners instanceof ArrayList) {
                clone.examiners = (List<BlockExaminer>) ((ArrayList<BlockExaminer>) examiners).clone();
            }
            if (runCallbacks instanceof ArrayList) {
                clone.runCallbacks = (List<Runnable>) ((ArrayList<Runnable>) runCallbacks).clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns whether this path will be debugged. Path debugging happens by repeatedly setting the next destination
     * block to a client-sided flower.
     *
     * @return Whether the path is debugging
     */
    public boolean debug() {
        return this.debug;
    }

    /**
     * Sets whether the path should be debugged.
     *
     * @see #debug()
     */
    public NavigatorParameters debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Returns the configured <em>default</em> attack strategy, which tries to perform the most Minecraft-like attack on
     * the target.
     *
     * @return The default strategy
     */
    public AttackStrategy defaultAttackStrategy() {
        return this.defaultStrategy;
    }

    /**
     * Sets the default {@link AttackStrategy}.
     *
     * @param defaultStrategy
     *            The new default strategy
     * @see #defaultAttackStrategy()
     */
    public NavigatorParameters defaultAttackStrategy(AttackStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
        return this;
    }

    /**
     * @see #destinationTeleportMargin(double)
     */
    public double destinationTeleportMargin() {
        return destinationTeleportMargin;
    }

    /**
     * Sets the distance (in blocks) after which the NPC will directly teleport to the destination or -1 if disabled.
     * For example, if the destination teleport margin was 1.5 and the NPC reached 1.5 blocks from the target it would
     * instantly teleport to the target location.
     *
     * @param margin
     *            Distance teleport margin
     */
    public NavigatorParameters destinationTeleportMargin(double margin) {
        destinationTeleportMargin = margin;
        return this;
    }

    /**
     * Returns the distance margin or leeway that the {@link Navigator} will be able to stop from the target
     * destination. The margin will be measured against the block distance.
     * <p>
     * For example: if the distance margin were 2, then the {@link Navigator} could stop moving towards the target when
     * it is 2 blocks away from it.
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
     * Gets the target location mapper. This is a function that maps from a target entity to the location the NPC should
     * pathfind to. The default mapper returns the location using {@link Entity#getLocation(Location)}.
     */
    public Function<Entity, Location> entityTargetLocationMapper() {
        return mapper != null ? mapper : DEFAULT_MAPPER;
    }

    /**
     * Set the target location mapper.
     *
     * @param mapper
     *            The new mapper
     * @see #entityTargetLocationMapper(Function)
     */
    public NavigatorParameters entityTargetLocationMapper(Function<Entity, Location> mapper) {
        this.mapper = mapper;
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

    public int fallDistance() {
        return fallDistance;
    }

    public NavigatorParameters fallDistance(int distance) {
        this.fallDistance = distance;
        return this;
    }

    public boolean hasExaminer(Class<? extends BlockExaminer> clazz) {
        return Arrays.asList(examiners).stream().anyMatch(e -> clazz.isAssignableFrom(e.getClass()));
    }

    /**
     * @see #lookAtFunction(Function)
     */
    public Function<Navigator, Location> lookAtFunction() {
        return this.lookAtFunction;
    }

    /**
     * Sets the position to look at during pathfinding, overriding the default 'look at target' behaviour.
     *
     * @param lookAt
     *            Where to look
     */
    public NavigatorParameters lookAtFunction(Function<Navigator, Location> lookAt) {
        this.lookAtFunction = lookAt;
        return this;
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
     * Gets the path distance margin.
     *
     * @see #pathDistanceMargin(double)
     */
    public double pathDistanceMargin() {
        return pathDistanceMargin;
    }

    /**
     * Sets the path distance margin. This is how close the pathfinder should pathfind to the target in blocks. If you
     * need to set the cutoff distance before the NPC considers their path completed, use
     * {@link #distanceMargin(double)}.
     *
     * @param distance
     *            The distance margin
     */
    public NavigatorParameters pathDistanceMargin(double distance) {
        this.pathDistanceMargin = distance;
        return this;
    }

    public PathfinderType pathfinderType() {
        return pathfinderType;
    }

    /**
     * Sets whether to use an A* pathfinder defined in {@link AStarMachine} for pathfinding.
     * <p>
     * If this is set to MINECRAFT, then the Minecraft pathfinder will be used, which may or may not be more consistent.
     * <p>
     * Note that certain API features will not be possible if this is set to MINECRAFT - for example,
     * {@link #examiner(BlockExaminer)}.
     *
     * @param type
     *            The new pathfinder type
     */
    public NavigatorParameters pathfinderType(PathfinderType type) {
        pathfinderType = type;
        return this;
    }

    /**
     * @return The pathfinding range of the navigator in blocks.
     * @see #range(float)
     */
    public float range() {
        return range;
    }

    /**
     * Sets the pathfinding range in blocks. The pathfinding range determines how far away the {@link Navigator} will
     * attempt to pathfind before giving up to save computation.
     *
     * @param range
     *            The new range
     */
    public NavigatorParameters range(float range) {
        this.range = range;
        return this;
    }

    /**
     * Removes a previously added run callback.
     *
     * @see #addRunCallback(Runnable)
     * @param runnable
     *            The run callback to remove
     */
    public NavigatorParameters removeRunCallback(Runnable runnable) {
        runCallbacks.remove(runnable);
        return this;
    }

    /**
     * FOR INTERNAL USE ONLY: ticks all {@link Runnable} callbacks.
     */
    public void run() {
        for (int i = 0; i < runCallbacks.size(); i++) {
            runCallbacks.get(i).run();
        }
    }

    /**
     * @return The modified movement speed as given by {@link #baseSpeed()} multiplied by {@link #speedModifier()}
     */
    public float speed() {
        return modifiedSpeed(baseSpeed);
    }

    /**
     * Sets the base movement speed of the {@link Navigator}. Note that this is mob-specific and may not always be sane.
     * Using {@link #speedModifier()} is preferred.
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
     * Sets the movement speed modifier of the {@link Navigator}. This is a percentage modifier that alters the movement
     * speed returned in {@link #speed()}.
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
     * Sets the number of stationary ticks before navigation is cancelled with a {@link CancelReason} of STUCK.
     *
     * @param ticks
     *            The new number of stationary ticks
     */
    public NavigatorParameters stationaryTicks(int ticks) {
        stationaryTicks = ticks;
        return this;
    }

    /**
     * @see #straightLineTargetingDistance(float)
     * @return The distance
     */
    public float straightLineTargetingDistance() {
        return straightLineTargetingDistance;
    }

    /**
     * Sets the distance (in blocks) at which the entity targeter will switch to simply following a straight line to the
     * target instead of pathfinding.
     *
     * @param distance
     *            The distance (in blocks)
     */
    public NavigatorParameters straightLineTargetingDistance(float distance) {
        straightLineTargetingDistance = distance;
        return this;
    }

    /**
     * Gets the {@link StuckAction} of these parameters. This will be run when the navigation is stuck and must either
     * be fixed up or cancelled.
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

    /**
     * @see #updatePathRate(int)
     * @return The current path rate
     */
    public int updatePathRate() {
        return updatePathRate;
    }

    /**
     * Sets the update path rate, in ticks (default 20). Mainly used for target following at this point - the NPC will
     * recalculate its path to the target every {@code rate} ticks.
     *
     * @param rate
     *            The new rate in ticks to use
     */
    public NavigatorParameters updatePathRate(int rate) {
        updatePathRate = rate;
        return this;
    }

    /**
     * @see #useNewPathfinder(boolean)
     * @return Whether to use the new pathfinder
     */
    @Deprecated
    public boolean useNewPathfinder() {
        return pathfinderType() == PathfinderType.CITIZENS;
    }

    /**
     * Sets whether to use an A* pathfinder defined in {@link AStarMachine} for pathfinding.
     * <p>
     * If this is set to false, then the Minecraft pathfinder will be used, which may or may not be more consistent.
     * <p>
     * Note that certain API features will not be possible if this is set to false - this includes
     * {@link #examiner(BlockExaminer)} and {@link #distanceMargin(double)}.
     *
     * @param use
     *            Whether to use the A* pathfinder
     */
    @Deprecated
    public NavigatorParameters useNewPathfinder(boolean use) {
        if (use) {
            pathfinderType(PathfinderType.CITIZENS);
        } else {
            pathfinderType(PathfinderType.MINECRAFT);
        }
        return this;
    }

    private static final Function<org.bukkit.entity.Entity, Location> DEFAULT_MAPPER = Entity::getLocation;
}
