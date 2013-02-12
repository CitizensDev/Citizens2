package org.khelekore.prtree;

/** A minimum bounding box for n dimensions.
 */ 
public interface MBR {
    /**
     * @return the number of dimensions this bounding box has 
     */
    int getDimensions ();

    /** Get the minimum value for the given axis
     * @param axis the axis to use
     * @return the min value
     */
    double getMin (int axis);

    /** Get the maximum value for the given axis
     * @param axis the axis to use
     * @return the x max value
     */
    double getMax (int axis);

    /** Return a new MBR that is the union of this mbr and the other 
     * @param mbr the MBR to create a union with
     * @return the new MBR
     */
    MBR union (MBR mbr);

    /** Check if the other MBR intersects this one
     * @param other the MBR to check against
     * @return true if the given MBR intersects with this MBR
     */
    boolean intersects (MBR other);

    /** Check if this MBR intersects the rectangle given by the object 
     *  and the MBRConverter.
     * @param t a rectangular object
     * @param converter the MBRConverter
     * @return true if the given MBR intersects with the given object
     * @param <T> the object type
     */
    <T> boolean intersects (T t, MBRConverter<T> converter);
}
