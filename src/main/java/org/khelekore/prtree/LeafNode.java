package org.khelekore.prtree;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

class LeafNode<T> extends NodeBase<T, T> {

    public LeafNode (Object[] data) {
	super (data);
    }

    public MBR getMBR (T t, MBRConverter<T> converter) {
	return new SimpleMBR (t, converter);
    }

    @Override public MBR computeMBR (MBRConverter<T> converter) {
	MBR ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, getMBR (get (i), converter));
	return ret;
    }

    public void expand (MBR mbr, MBRConverter<T> converter,
			List<T> found, List<Node<T>> nodesToExpand) {
	find (mbr, converter, found);
    }

    public void find (MBR mbr, MBRConverter<T> converter, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (mbr.intersects (t, converter))
		result.add (t);
	}
    }

    public void nnExpand (DistanceCalculator<T> dc,
			  NodeFilter<T> filter,
			  List<DistanceResult<T>> drs,
			  int maxHits,
			  PriorityQueue<Node<T>> queue,
			  MinDistComparator<T, Node<T>> mdc) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (filter.accept (t)) {
		double dist = dc.distanceTo (t, mdc.p);
		int n = drs.size ();
		if (n < maxHits || dist < drs.get (n - 1).getDistance ()) {
		    add (drs, new DistanceResult<T> (t, dist), maxHits);
		}
	    }
	}
    }

    private void add (List<DistanceResult<T>> drs,
		      DistanceResult<T> dr,
		      int maxHits) {
	int n = drs.size ();
	if (n == maxHits)
	    drs.remove (n - 1);
	int pos = Collections.binarySearch (drs, dr, comp);
	if (pos < 0) {
	    // binarySearch return -(pos + 1) for new entries
	    pos = -(pos + 1);
	}
	drs.add (pos, dr);
    }

    private static final Comparator<DistanceResult<?>> comp =
	new Comparator<DistanceResult<?>> () {
	public int compare (DistanceResult<?> d1, DistanceResult<?> d2) {
	    return Double.compare (d1.getDistance (), d2.getDistance ());
	}
    };
}
