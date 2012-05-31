package net.citizensnpcs.npc;

import java.util.Iterator;

import net.citizensnpcs.api.abstraction.MobType;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.util.ByIdArray;

public class CitizensNPCRegistry implements NPCRegistry {
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final Storage saves;

    public CitizensNPCRegistry(Storage saves) {
        this.saves = saves;
    }

    @Override
    public NPC getById(int id) {
        if (id < 0)
            throw new IllegalArgumentException("invalid id");
        return npcs.get(id);
    }

    @Override
    public Iterator<NPC> iterator() {
        return npcs.iterator();
    }

    @Override
    public void deregister(NPC npc) {
        npcs.remove(npc.getId());
        saves.getKey("npc").removeKey(String.valueOf(npc.getId()));
        npc.despawn();
    }

    @Override
    public NPC createAndSpawn(String name, WorldVector at, MobType type) {
        CitizensNPC npc = new CitizensNPC(this, name);
        npc.setEntityController(null); // TODO;
        npc.spawn(at);
        return npc;
    }

    @Override
    public int register(NPC npc) {
        return npcs.add(npc);
    }
}