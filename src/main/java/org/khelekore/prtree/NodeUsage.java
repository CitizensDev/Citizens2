package org.khelekore.prtree;

/** A node used while building the leaf sets, holds the actual data
 *  and an identifier for the partition this node belongs to.
 *
 * @param <T> the data type of the stored item
 */
class NodeUsage<T> {
    private final T data;
    private int id;

    public NodeUsage (T data, int id) {
	this.data = data;
	this.id = id;
    }

    public T getData () {
	return data;
    }

    public int getOwner () {
	return id;
    }

    public void changeOwner (int id) {
	this.id = id;
    }

    public void use () {
	if (id >= 0)
	    id = -id;
	else 
	    throw new RuntimeException ("Trying to use already used node");
    }

    public boolean isUsed () {
	return id < 0;
    }

    @Override public String toString () {
	return getClass ().getSimpleName () + "{data: " + data +
	    ", id: " + id + "}";
    }
}
