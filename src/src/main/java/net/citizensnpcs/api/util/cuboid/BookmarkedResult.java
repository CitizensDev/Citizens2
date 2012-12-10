package net.citizensnpcs.api.util.cuboid;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BookmarkedResult implements Iterable<Cuboid> {
    final QuadNode bookmark;
    private final List<Cuboid> results;

    BookmarkedResult(QuadNode node, List<Cuboid> cuboids) {
        bookmark = node;
        results = Collections.unmodifiableList(cuboids);
    }

    public Collection<Cuboid> getResults() {
        return results;
    }

    @Override
    public Iterator<Cuboid> iterator() {
        return results.iterator();
    }

    @SuppressWarnings("unchecked")
    public static final BookmarkedResult EMPTY = new BookmarkedResult(null, (Collections.EMPTY_LIST));
}
