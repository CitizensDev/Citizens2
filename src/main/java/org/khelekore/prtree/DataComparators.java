package org.khelekore.prtree;

import java.util.Comparator;

class DataComparators<T> implements NodeComparators<T> {
    private final MBRConverter<T> converter;

    public DataComparators (MBRConverter<T> converter) {
	this.converter = converter;
    }

    public Comparator<T> getMinComparator (final int axis) {
	return new Comparator<T> () {
	    public int compare (T t1, T t2) {
		double d1 = converter.getMin (axis, t1);
		double d2 = converter.getMin (axis, t2);
		return Double.compare (d1, d2);
	    }
	};
    }

    public Comparator<T> getMaxComparator (final int axis) {
	return new Comparator<T> () {
	    public int compare (T t1, T t2) {
		double d1 = converter.getMax (axis, t1);
		double d2 = converter.getMax (axis, t2);
		return Double.compare (d1, d2);
	    }
	};
    }
}