package org.khelekore.prtree;

/** A minimum bounding rectangle
 */ 
public interface MBR2D {
    /** Get the minimum x value 
     * @return the x min value
     */
    double getMinX ();

    /** Get the minimum y value
     * @return the y min value
     */
    double getMinY ();

    /** Get the maximum x value
     * @return the x max value
     */
    double getMaxX ();

    /** Get the maximum y value
     * @return the y max value
     */
    double getMaxY ();

    /** Return a new MBR that is the union of this mbr and the other 
     * @param mbr the MBR to create a union with
     * @return the new MBR
     */
    MBR2D union (MBR2D mbr);

    /** Check if the other MBR intersects this one
     * @param other the MBR to check against
     * @return true if the given MBR intersects with this MBR
     */
    boolean intersects (MBR2D other);
}