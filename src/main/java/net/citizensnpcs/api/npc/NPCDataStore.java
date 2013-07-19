package net.citizensnpcs.api.npc;

public interface NPCDataStore {
    /**
     * Clears all data about the given {@link NPC} from storage. Called when the
     * NPC is removed.
     * 
     * @param npc
     *            The NPC to clear data from
     */
    void clearData(NPC npc);

    /**
     * Loads NPCs from disk into the given {@link NPCRegistry}. The registry
     * should be cleared before this is called.
     * 
     * @param registry
     *            The NPCRegistry to load NPCs into
     */
    void loadInto(NPCRegistry registry);

    /**
     * Notifies the data store to save all stored data to disk. May be
     * asynchronous.
     */
    void saveToDisk();

    /**
     * Notifies the data store to save all stored data to disk
     * <em>immediately</em>. Must not be asynchronous.
     */
    void saveToDiskImmediate();

    /**
     * Stores the given {@link NPC} into memory or to a disk representation.
     * 
     * @param npc
     *            The NPC to store
     */
    void store(NPC npc);

    /**
     * Stores all {@link NPC}s in the given {@link NPCRegistry} to disk.
     * 
     * @param registry
     *            The registry to store NPCs from
     */
    void storeAll(NPCRegistry registry);
}