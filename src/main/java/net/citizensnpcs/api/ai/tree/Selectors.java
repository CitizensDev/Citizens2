package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;

public class Selectors {
    private Selectors() {
    }

    public static Selector.Builder prioritySelector(Comparator<Behavior> comparator, Behavior... behaviors) {
        return prioritySelector(comparator, Arrays.asList(behaviors));
    }

    public static Selector.Builder prioritySelector(final Comparator<Behavior> comparator,
            Collection<Behavior> behaviors) {
        return Selector.selecting(behaviors).selectionFunction(new Function<List<Behavior>, Behavior>() {
            @Override
            public Behavior apply(@Nullable List<Behavior> input) {
                Collections.sort(input, comparator);
                return input.get(input.size() - 1);
            }
        });
    }
}
