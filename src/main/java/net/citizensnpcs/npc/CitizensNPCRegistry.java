package net.citizensnpcs.npc;

import java.util.Iterator;

import net.citizensnpcs.NPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.ByIdArray;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class CitizensNPCRegistry implements NPCRegistry {
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final NPCDataStore saves;

    public CitizensNPCRegistry(NPCDataStore store) {
        saves = store;
    }

    public NPC createNPC(EntityType type, int id, String name) {
        CitizensNPC npc = getByType(type, id, name);
        if (npc == null)
            throw new IllegalStateException("Could not create NPC.");
        npcs.put(npc.getId(), npc);
        return npc;
    }

    @Override
    public NPC createNPC(EntityType type, String name) {
        return createNPC(type, generateUniqueId(), name);
    }

    @Override
    public void deregister(NPC npc) {
        npcs.remove(npc.getId());
        saves.remove(npc);
        npc.despawn();
    }

    @Override
    public void deregisterAll() {
        Iterator<NPC> itr = iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            itr.remove();
            npc.despawn();
            for (Trait t : npc.getTraits())
                t.onRemove();
            saves.remove(npc);
        }
    }

    private int generateUniqueId() {
        int count = 0;
        while (getById(count++) != null)
            ; // TODO: doesn't respect existing save data that might not have
              // been loaded. This causes DBs with NPCs that weren't loaded to
              // have conflicting primary keys.
        return count - 1;
    }

    @Override
    public NPC getById(int id) {
        if (id < 0)
            throw new IllegalArgumentException("invalid id");
        return npcs.get(id);
    }

    private CitizensNPC getByType(EntityType type, int id, String name) {
        return new CitizensNPC(id, name, EntityControllers.createForType(type));
    }

    @Override
    public NPC getNPC(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof NPCHolder)
            return ((NPCHolder) entity).getNPC();
        net.minecraft.server.v1_4_5.Entity handle = ((CraftEntity) entity).getHandle();
        return handle instanceof NPCHolder ? ((NPCHolder) handle).getNPC() : null;
    }

    @Override
    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    @Override
    public Iterator<NPC> iterator() {
        return npcs.iterator();
    }
}