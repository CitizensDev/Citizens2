package org.khelekore.prtree;

import java.util.List;
import java.util.PriorityQueue;

/** A node in a Priority R-Tree
 * @param <T> the data type of the elements stored in this node
 */
interface Node<T> {
    /** Get the size of the node, that is how many data elements it holds
     * @return the number of elements (leafs or child nodes) that this node has
     */
    int size ();

    /** Get the MBR of this node
     * @param converter the MBR converter to use for the actual objects
     * @return the MBR for this node
     */
    MBR getMBR (MBRConverter<T> converter);

    /** Visit this node and add the leafs to the found list and add
     *  any child nodes to the list of nodes to expand.
     * @param mbr the query rectangle
     * @param converter the MBR converter to use for the actual objects
     * @param found the List of leaf nodes
     * @param nodesToExpand the List of nodes that needs to be expanded
    */
    void expand (MBR mbr, MBRConverter<T> converter,
		 List<T> found, List<Node<T>> nodesToExpand);

    /** Find nodes that intersect with the given MBR.
     * @param mbr the query rectangle
     * @param converter the MBR converter to use for the actual objects
     * @param result the List to add the found nodes to
     */
    void find (MBR mbr, MBRConverter<T> converter, List<T> result);

    /** Expand the nearest neighbour search
     * @param dc the DistanceCalculator to use when calculating distances
     * @param filter the NodeFilter to use when getting node objects
     * @param currentFinds the currently found results, this list will
     *        be populated
     * @param maxHits the maximum number of entries to store
     * @param queue where to store child nodes that needs expansion
     * @param mdc the MinDistComparator to use
     */
    void nnExpand (DistanceCalculator<T> dc,
		   NodeFilter<T> filter,
		   List<DistanceResult<T>> currentFinds,
		   int maxHits,
		   PriorityQueue<Node<T>> queue,
		   MinDistComparator<T, Node<T>> mdc);

}
