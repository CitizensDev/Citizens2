package net.citizensnpcs.api.util.cuboid;

import java.util.Collections;
import java.util.List;

public class BookmarkedResult {
    final QuadNode bookmark;
    public final List<PrimitiveCuboid> results;

    BookmarkedResult(QuadNode node, List<PrimitiveCuboid> c) {
        bookmark = node;
        results = Collections.unmodifiableList(c);
    }

    @SuppressWarnings("unchecked")
    public static final BookmarkedResult EMPTY = new BookmarkedResult(null, (Collections.EMPTY_LIST));
}
