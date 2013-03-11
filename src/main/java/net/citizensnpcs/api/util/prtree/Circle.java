package net.citizensnpcs.api.util.prtree;

import java.util.ArrayList;
import java.util.List;

/** A simple circular data structure.
 *
 * @param <T> the element type
 */
class Circle<T> {
    private int currentPos;
    private final List<T> data;

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

    public T getNext () {
	T ret = data.get (currentPos++);
	currentPos %= data.size ();
	return ret;
    }

    public int getNumElements () {
	return data.size ();
    }

    public void reset () {
	currentPos = 0;
    }
}
