package net.citizensnpcs.api.ai.tree;

/**
 * A marker interface for {@link Behavior}s that indicates to any parent nodes that the behavior can be run in
 * <em>parallel</em> along with other behaviors.
 *
 * Parallel behaviors will not affect the success or failure status of any composite nodes; the return
 * {@link BehaviorStatus} will only act as a terminal status or an indication to remove the parallel node.
 */
public interface ParallelBehavior {
}
