package net.citizensnpcs.api.util;

public interface Storage {
    /**
     * Returns a {@link DataKey} starting from the given root.
     * 
     * @param root
     *            The root to start at
     * @return the created key
     */
    public DataKey getKey(String root);

    /**
     * Loads data from a file or other location.
     * 
     * @return Whether the load was successful
     */
    public boolean load();

    /**
     * Saves the in-memory aspects of the storage to disk.
     */
    public void save();
}