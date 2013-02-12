package org.khelekore.prtree;

import java.util.Comparator;

interface NodeComparators<T> {
    /** Get a comparator for the given axis
     * @param axis the axis that the comparator will use
     * @return the comparator
     */
    Comparator<T> getMinComparator (int axis);

    /** Get a comparator for the given axis
     * @param axis the axis that the comparator will use
     * @return the comparator
     */
    Comparator<T> getMaxComparator (int axis);
}
