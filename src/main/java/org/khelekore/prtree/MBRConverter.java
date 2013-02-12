package org.khelekore.prtree;

/** A class that given a T can tell the minimum and maximum 
 *  ordinates for that object.
 * @param <T> the data type stored in the PRTree
 */
public interface MBRConverter<T> {
    /**
     * @return the number of dimensions this converter cares about
     */
    int getDimensions ();

    /** Get the minimum coordinate value for the given t.
     * @param axis the axis to get the min value for
     * @param t the object to get the mbr ordinate for
     * @return the min value for the given axis
     */
    double getMin (int axis, T t);

    /** Get the maximum coordinate value for the given t
     * @param axis the axis to get the max value for
     * @param t the object to get the mbr ordinate for
     * @return the max value for the given axis
     */
    double getMax (int axis, T t);
}
