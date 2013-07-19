package net.citizensnpcs.api.util.prtree;

/** Class to hold object and distance to it
 * @param <T> The node type
 */
public class DistanceResult<T> {
    private final double dist;
    private final T t;
    
    /** Create a new DistanceResult with a given object and distance
     * @param t the object we are measuring the distance to
     * @param dist the actual distance to the object
     */
    public DistanceResult (T t, double dist) {
	this.t = t;
	this.dist = dist;
    }

    /** Get the object
     * @return The node object
     */
    public T get () {
	return t;
    }

    /** Get the distance
     * @return The distance to the node object
     */
    public double getDistance () {
	return dist;
    }
}

