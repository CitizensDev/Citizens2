package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;

/**
 * Static helper class for creating common {@link Selector}s.
 */
public class Selectors {
    private Selectors() {
    }

    public static final class PrioritySelection implements Function<List<Behavior>, Behavior> {
        private final Comparator<Behavior> comparator;

        private PrioritySelection(Comparator<Behavior> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Behavior apply(List<Behavior> input) {
            input.sort(comparator);
            return input.get(input.size() - 1);
        }
    }

    /**
     * Returns a default priority selection function that assumes the input {@link Behavior}s implement
     * {@link Comparable}.
     */
    public static Function<List<Behavior>, Behavior> prioritySelectionFunction() {
        return new PrioritySelection(BEHAVIOR_COMPARATOR);
    }

    /**
     * @see #prioritySelector(Comparator, Collection)
     */
    public static Selector.Builder prioritySelector(Comparator<Behavior> comparator, Behavior... behaviors) {
        return prioritySelector(comparator, Arrays.asList(behaviors));
    }

    /**
     * Builds a {@link Selector} that <i>prioritises</i> certain {@link Behavior}s based on a comparison function.
     *
     * @param comparator
     *            The comparison function
     * @param behaviors
     *            The behaviors to select from
     */
    public static Selector.Builder prioritySelector(final Comparator<Behavior> comparator,
            Collection<Behavior> behaviors) {
        if (behaviors.size() > 0)
            throw new IllegalArgumentException("must have at least one behavior for comparison");
        return Selector.selecting(behaviors).selectionFunction(new PrioritySelection(comparator));
    }

    private static final Comparator<Behavior> BEHAVIOR_COMPARATOR = (o1, o2) -> ((Comparable<Behavior>) o1)
            .compareTo(o2);
}
