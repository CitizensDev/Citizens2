package net.citizensnpcs.api.npc;

public class MemoryNPCDataStore implements NPCDataStore {
    private int lastID;

    @Override
    public void clearData(NPC npc) {
    }

    @Override
    public int createUniqueNPCId(NPCRegistry registry) {
        return lastID++;
    }

    @Override
    public void loadInto(NPCRegistry registry) {
    }

    @Override
    public void saveToDisk() {
    }

    @Override
    public void saveToDiskImmediate() {
    }

    @Override
    public void store(NPC npc) {
    }

    @Override
    public void storeAll(NPCRegistry registry) {
    }

    @Override
    public void reloadFromSource() {
    }
}
