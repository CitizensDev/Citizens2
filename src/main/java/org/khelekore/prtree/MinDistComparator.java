package org.khelekore.prtree;

import java.util.Comparator;

/** A comparator that uses the MINDIST metrics to sort Nodes
 * @param <T> the data stored in the nodes
 * @param <S> the actual node
 */
class MinDistComparator<T, S extends Node<T>> implements Comparator<S> {
    public final MBRConverter<T> converter;
    public final PointND p;

    public MinDistComparator (MBRConverter<T> converter, PointND p) {
	this.converter = converter;
	this.p = p;
    }

    public int compare (S t1, S t2) {
	MBR mbr1 = t1.getMBR (converter);
	MBR mbr2 = t2.getMBR (converter);
	return Double.compare (MinDist.get (mbr1, p),
			       MinDist.get (mbr2, p));
    }
}
