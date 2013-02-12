package org.khelekore.prtree;

/** An implementation of MBRND that keeps a double array with the max
 *  and min values
 *
 * <p>Please note that you should not normally use this class when PRTree
 * wants a MBR since this will actually use a lot of extra memory.
 */
public class SimpleMBR implements MBR {
    private final double values[];

    private SimpleMBR (int dimensions) {
	values = new double[dimensions * 2];	
    }

    /** Create a new SimpleMBR using the given double values for max and min.
     *  Note that the values should be stored as min, max, min, max.
     * @param values the min and max values for each dimension.
     */
    public SimpleMBR (double... values) {
	this.values = values.clone ();
    }

    /** Create a new SimpleMBR from a given object and a MBRConverter
     * @param t the object to create the bounding box for
     * @param converter the actual MBRConverter to use
     */
    public <T> SimpleMBR (T t, MBRConverter<T> converter) {
	int dims = converter.getDimensions ();
	values = new double[dims * 2];
	int p = 0;
	for (int i = 0; i < dims; i++) {
	    values[p++] = converter.getMin (i, t);
	    values[p++] = converter.getMax (i, t);
	}
    }

    public int getDimensions () {
	return values.length / 2;
    }

    public double getMin (int axis) {
	return values[axis * 2];
    }

    public double getMax (int axis) {
	return values[axis * 2 + 1];
    }

    public MBR union (MBR mbr) {
	int dims = getDimensions ();
	SimpleMBR n = new SimpleMBR (dims);
	int p = 0;
	for (int i = 0; i < dims; i++) {
	    n.values[p] = Math.min (getMin (i), mbr.getMin (i));
	    p++;
	    n.values[p] = Math.max (getMax (i), mbr.getMax (i));
	    p++;
	}
	return n;
    }

    public boolean intersects (MBR other) {
	for (int i = 0; i < getDimensions (); i++) {
	    if (other.getMax (i) < getMin (i) || other.getMin (i) > getMax (i))
		return false;
	}
	return true;
    }

    public <T> boolean intersects (T t, MBRConverter<T> converter) {
	for (int i = 0; i < getDimensions (); i++) {
	    if (converter.getMax (i, t) < getMin (i) ||
		converter.getMin (i, t) > getMax (i))
		return false;
	}
	return true;
    }

    @Override public String toString () {
	return getClass ().getSimpleName () +
	    "{values: " + java.util.Arrays.toString (values) + "}";
    }
}