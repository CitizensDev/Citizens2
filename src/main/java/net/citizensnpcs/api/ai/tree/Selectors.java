package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class Selectors {
    private Selectors() {
    }

    private static final Comparator<Behavior> BEHAVIOR_COMPARATOR = new Comparator<Behavior>() {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(Behavior o1, Behavior o2) {
            return ((Comparable<Behavior>) o1).compareTo(o2);
        }
    };

    public static Function<List<Behavior>, Behavior> prioritySelectionFunction() {
        return prioritySelectionFunction0(BEHAVIOR_COMPARATOR);
    }

    private static Function<List<Behavior>, Behavior> prioritySelectionFunction0(final Comparator<Behavior> comparator) {
        return new Function<List<Behavior>, Behavior>() {
            @Override
            public Behavior apply(@Nullable List<Behavior> input) {
                Collections.sort(input, comparator);
                return input.get(input.size() - 1);
            }
        };
    }

    public static Selector.Builder prioritySelector(Comparator<Behavior> comparator, Behavior... behaviors) {
        return prioritySelector(comparator, Arrays.asList(behaviors));
    }

    public static Selector.Builder prioritySelector(final Comparator<Behavior> comparator,
            Collection<Behavior> behaviors) {
        Preconditions.checkArgument(behaviors.size() > 0, "must have at least one behavior for comparison");
        return Selector.selecting(behaviors).selectionFunction(prioritySelectionFunction0(comparator));
    }
}
