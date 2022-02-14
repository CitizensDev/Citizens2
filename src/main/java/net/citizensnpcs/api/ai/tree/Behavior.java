package net.citizensnpcs.api.ai.tree;

import net.citizensnpcs.api.ai.Goal;

/**
 * The base class for the second iteration of the {@link Goal} API, which can be made backwards compatible by extending
 * {@link BehaviorGoalAdapter}.
 *
 * A behavior is a common term for the parts of a <em>behavior tree</em>, which is a simple directed acyclic graph (DAG)
 * for AI. It is a simple state machine using {@link BehaviorStatus}.
 *
 * Nodes are executed in a top-down fashion through the tree. For legacy reasons, the tree is executed as a number of
 * <em>executing nodes</em> which are transitioned between using the {@link BehaviorStatus} they return.
 *
 * New child nodes are selected to become <em>executing nodes</em> based on {@link Behavior#shouldExecute()}. The
 * selection behavior can vary, e.g. running a list of nodes using {@link Sequence} or choosing from children nodes
 * using {@link Selector}. The executing nodes are repeatedly {@link Behavior#run()} until the return result changes
 * from {@link BehaviorStatus#RUNNING}.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Behavior_tree_(artificial_intelligence,_robotics_and_control)">https://en.wikipedia.org/wiki/Behavior_tree_(artificial_intelligence,_robotics_and_control)</a>
 */
public interface Behavior {
    /**
     * Resets the behavior and any state it is holding.
     */
    void reset();

    /**
     * Runs the behavior for one 'tick', optionally changing the state that it is in.
     *
     * @return The new state
     */
    BehaviorStatus run();

    /**
     * Returns whether the behavior is ready to run. Note this is called <em>once</em> when deciding whether to start
     * execution of a leaf node. The actual execution status is determined by the return value of {@link Behavior#run()}
     * which is repeatedly called by the executing node.
     */
    boolean shouldExecute();
}
