package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.List;

/** A simple circular data structure.
 *
 * @param <T> the element type
 */
class Circle<T> {
    private final List<T> data;
    private int currentPos;

    public Circle (int size) {
	data = new ArrayList<T> (size);
    }

    public void add (T t) {
	data.add (t);
    }

    public T get (int pos) {
	pos %= data.size ();
	return data.get (pos);
    }

    public int getNumElements () {
	return data.size ();
    }

    public void reset () {
	currentPos = 0;
    }

    public T getNext () {
	T ret = data.get (currentPos++);
	currentPos %= data.size ();
	return ret;
    }
}
