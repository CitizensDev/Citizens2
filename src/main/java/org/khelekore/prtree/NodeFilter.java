package org.khelekore.prtree;

/** A node object filterer.
 * @param <T> the node type
 */
public interface NodeFilter<T> {
    /** Check if the given node object is accepted
     * @param t the node user data
     * @return True if the node is accepted, false otherwise
     */
    boolean accept (T t);
}