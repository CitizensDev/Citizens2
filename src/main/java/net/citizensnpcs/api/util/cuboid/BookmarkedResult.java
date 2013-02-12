package net.citizensnpcs.api.util.cuboid;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BookmarkedResult implements Iterable<QuadCuboid> {
    final QuadNode bookmark;
    private final List<QuadCuboid> results;

    BookmarkedResult(QuadNode node, List<QuadCuboid> cuboids) {
        bookmark = node;
        results = Collections.unmodifiableList(cuboids);
    }

    public Collection<QuadCuboid> getResults() {
        return results;
    }

    @Override
    public Iterator<QuadCuboid> iterator() {
        return results.iterator();
    }

    @SuppressWarnings("unchecked")
    public static final BookmarkedResult EMPTY = new BookmarkedResult(null, (Collections.EMPTY_LIST));
}
