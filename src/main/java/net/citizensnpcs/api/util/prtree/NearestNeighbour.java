package net.citizensnpcs.api.util.prtree;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.util.prtree.DistanceResult;
import net.citizensnpcs.api.util.prtree.NodeFilter;

class NearestNeighbour<T> {

    private final MBRConverter<T> converter;
    private final DistanceCalculator<T> dc;
    private final NodeFilter<T> filter;
    private final int maxHits;
    private final PointND p;
    private final Node<T> root;

    public NearestNeighbour (MBRConverter<T> converter,
			     NodeFilter<T> filter,
			     int maxHits,
			     Node<T> root,
			     DistanceCalculator<T> dc,
			     PointND p) {
	this.converter = converter;
	this.filter = filter;
	this.maxHits = maxHits;
	this.root = root;
	this.dc = dc;
	this.p = p;
    }

    /**
     * @return the nearest neighbour
     */
    public List<DistanceResult<T>> find () {
	List<DistanceResult<T>> ret =
	    new ArrayList<DistanceResult<T>> (maxHits);
	MinDistComparator<T, Node<T>> nc =
	    new MinDistComparator<T, Node<T>> (converter, p);
	PriorityQueue<Node<T>> queue = new PriorityQueue<Node<T>> (20, nc);
	queue.add (root);
	while (!queue.isEmpty ()) {
	    Node<T> n = queue.remove ();
	    n.nnExpand (dc, filter, ret, maxHits, queue, nc);
	}
	return ret;
    }
}
