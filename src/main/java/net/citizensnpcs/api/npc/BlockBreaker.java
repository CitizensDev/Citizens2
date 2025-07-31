package net.citizensnpcs.api.npc;

import java.util.function.BiConsumer;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;

/**
 * A {@link Runnable} task that will break a block over time just as a normal Minecraft {@link Player} would. Should be
 * run every tick until completed.
 * <p>
 * This class also implements the {@link Behavior} interface for ease of use.
 * <p>
 * Due to NMS constraints, this is currently implemented inside Citizens2.
 */
public abstract class BlockBreaker extends BehaviorGoalAdapter {
    public static class BlockBreakerConfiguration {
        private BiConsumer<Block, ItemStack> blockBreaker = Block::breakNaturally;
        private Runnable callback;
        private org.bukkit.inventory.ItemStack itemStack;
        private float modifier = 1;
        private double radius = 0;

        public BiConsumer<Block, ItemStack> blockBreaker() {
            return blockBreaker;
        }

        /**
         * @param breaker
         *            The function that actually breaks the block. By default, this will call
         *            {@link Block#breakNaturally(ItemStack)}
         */
        public void blockBreaker(BiConsumer<Block, ItemStack> breaker) {
            blockBreaker = breaker;
        }

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

        public double radius() {
            return radius;
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
    }

    public static final BlockBreakerConfiguration EMPTY_CONFIG = new BlockBreakerConfiguration();
}
