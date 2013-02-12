package org.khelekore.prtree;

import java.util.List;

/** A class that can get the next available node.
 * @param <N> the type of the node
 */
interface NodeGetter<N> {
    /** Get the next node. 
     * @param maxObjects use at most this many objects
     * @return the next node
     */
    N getNextNode (int maxObjects);

    /** Check if we can get more nodes from this NodeGetter.
     * @return true if there are more nodes, false otherwise
     */
    boolean hasMoreNodes ();

    /** Check if there is unused data in this NodeGetter.
     * @return true if there is unused data, false otherwise
     */
    boolean hasMoreData ();

    /** Split this NodeGetter into the low and high parts.
     * @param lowId the id of the low part
     * @param highId the id of the high part
     * @return a list with  the two sublists
     */
    List<? extends NodeGetter<N>> split (int lowId, int highId);
}
