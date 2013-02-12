package org.khelekore.prtree;

/** A description of an N-dimensional point
 */
public interface PointND {
    /**
     * @return the number of dimensions this point has
     */
    int getDimensions ();
    
    /**
     * @param axis the axis to get the value for
     * @return the ordinate value for the given axis
     */
    double getOrd (int axis);
}
