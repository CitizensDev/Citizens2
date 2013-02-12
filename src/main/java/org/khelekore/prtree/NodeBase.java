package org.khelekore.prtree;

/**
 * @param <N> the type of the child entries
 * @param <T> the type of the data entries
 */
abstract class NodeBase<N, T> implements Node<T> {
    private MBR mbr;
    private Object[] data;

    public NodeBase (Object[] data) {
	this.data = data;
    }

    public int size () {
	return data.length;
    }

    @SuppressWarnings("unchecked")
    public N get (int i) {
	return (N)data[i];
    }
    
    public MBR getMBR (MBRConverter<T> converter) {
	if (mbr == null)
	    mbr = computeMBR (converter);
	return mbr;
    }
    
    public abstract MBR computeMBR (MBRConverter<T> converter);
    
    public MBR getUnion (MBR m1, MBR m2) {
	if (m1 == null)
	    return m2;
	return m1.union (m2);
    }
}
