package org.khelekore.prtree;

/** One implementatoin of a point
 */
public class SimplePointND implements PointND {
    private final double[] ords;

    /** Create a new SimplePointND using the given ordinates.
     * @param ords the ordinates
     */
    public SimplePointND (double... ords) {
	this.ords = ords;
    }

    public int getDimensions () {
	return ords.length;
    }

    public double getOrd (int axis) {
	return ords[axis];
    }
}
