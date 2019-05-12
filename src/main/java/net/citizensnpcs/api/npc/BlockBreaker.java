package net.citizensnpcs.api.npc;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;

/**
 * A {@link Runnable} task that will break a block over time just as a normal Minecraft {@link Player} would. Should be
 * run every tick until completed.
 *
 * This class also implements the {@link Behavior} interface for ease of use.
 *
 * Due to NMS constraints, this is currently implemented inside Citizens2.
 */
public abstract class BlockBreaker extends BehaviorGoalAdapter {
    public static class BlockBreakerConfiguration {
        private Runnable callback;
        private org.bukkit.inventory.ItemStack itemStack;
        private float modifier = 1;
        private double radius = 0;

        public float blockStrengthModifier() {
            return modifier;
        }

        /**
         * @param modifier
         *            The block strength modifier
         */
        public BlockBreakerConfiguration blockStrengthModifier(float modifier) {
            this.modifier = modifier;
            return this;
        }

        public Runnable callback() {
            return callback;
        }

        /**
         * @param callback
         *            A callback that is run on completion
         */
        public BlockBreakerConfiguration callback(Runnable callback) {
            this.callback = callback;
            return this;
        }

        public org.bukkit.inventory.ItemStack item() {
            return itemStack;
        }

        /**
         *
         * @param stack
         *            The item to simulate the NPC using to break the block (e.g. an axe for wood)
         */
        public BlockBreakerConfiguration item(org.bukkit.inventory.ItemStack stack) {
            itemStack = stack;
            return this;
        }

        /**
         * @param radius
         *            The maximum radius to be from the target block. The NPC will attempt to pathfind towards the
         *            target block if this is specified and it is outside of the radius.
         */
        public BlockBreakerConfiguration radius(double radius) {
            this.radius = radius;
            return this;
        }

        public double radiusSquared() {
            return Math.pow(radius, 2);
        }
    }

    public static final BlockBreakerConfiguration EMPTY_CONFIG = new BlockBreakerConfiguration();
}
