package org.khelekore.prtree;

import java.util.List;
import java.util.PriorityQueue;

class InternalNode<T> extends NodeBase<Node<T>, T> {
    public InternalNode (Object[] data) {
	super (data);
    }

    @Override public MBR computeMBR (MBRConverter<T> converter) {
	MBR ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, get (i).getMBR (converter));
	return ret;
    }

    public void expand (MBR mbr, MBRConverter<T> converter, List<T> found,
			List<Node<T>> nodesToExpand) {
	for (int i = 0, s = size (); i < s; i++) {
	    Node<T> n = get (i);
	    if (mbr.intersects (n.getMBR (converter)))
		nodesToExpand.add (n);
	}
    }

    public void find (MBR mbr, MBRConverter<T> converter, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    Node<T> n = get (i);
	    if (mbr.intersects (n.getMBR (converter)))
		n.find (mbr, converter, result);
	}
    }

    public void nnExpand (DistanceCalculator<T> dc,
			  NodeFilter<T> filter,
			  List<DistanceResult<T>> drs,
			  int maxHits,
			  PriorityQueue<Node<T>> queue,
			  MinDistComparator<T, Node<T>> mdc) {
	int s = size ();
	for (int i = 0; i < s; i++) {
	    Node<T> n = get (i);
	    MBR mbr = n.getMBR (mdc.converter);
	    double minDist = MinDist.get (mbr, mdc.p);
	    int t = drs.size ();
	    // drs is sorted so we can check only the last entry
	    if (t < maxHits || minDist <= drs.get (t - 1).getDistance ())
		queue.add (n);
	}
    }
}
