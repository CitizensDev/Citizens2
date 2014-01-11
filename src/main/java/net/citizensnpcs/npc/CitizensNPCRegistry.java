package net.citizensnpcs.npc;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCCreateEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class CitizensNPCRegistry implements NPCRegistry {
    private final NPCCollection npcs = TROVE_EXISTS ? new TroveNPCCollection() : new MapNPCCollection();
    private final NPCDataStore saves;

    public CitizensNPCRegistry(NPCDataStore store) {
        saves = store;
    }

    @Override
    public NPC createNPC(EntityType type, String name) {
        return createNPC(type, UUID.randomUUID(), generateUniqueId(), name);
    }

    @Override
    public NPC createNPC(EntityType type, UUID uuid, int id, String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(type, "type cannot be null");
        CitizensNPC npc = getByType(type, uuid, id, name);
        if (npc == null)
            throw new IllegalStateException("Could not create NPC.");
        npcs.put(npc.getId(), npc);
        Bukkit.getPluginManager().callEvent(new NPCCreateEvent(npc));
        return npc;
    }

    @Override
    public void deregister(NPC npc) {
        npcs.remove(npc.getId());
        if (saves != null) {
            saves.clearData(npc);
        }
        npc.despawn(DespawnReason.REMOVAL);
    }

    @Override
    public void deregisterAll() {
        Iterator<NPC> itr = iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            itr.remove();
            npc.despawn(DespawnReason.REMOVAL);
            for (Trait t : npc.getTraits()) {
                t.onRemove();
            }
            if (saves != null) {
                saves.clearData(npc);
            }
        }
    }

    private int generateUniqueId() {
        return saves.createUniqueNPCId(this);
    }

    @Override
    public NPC getById(int id) {
        if (id < 0)
            throw new IllegalArgumentException("invalid id");
        return npcs.get(id);
    }

    private CitizensNPC getByType(EntityType type, UUID uuid, int id, String name) {
        return new CitizensNPC(uuid, id, name, EntityControllers.createForType(type), this);
    }

    @Override
    public NPC getNPC(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof NPCHolder)
            return ((NPCHolder) entity).getNPC();
        if (!(entity instanceof LivingEntity))
            return null;
        Object handle = NMS.getHandle((LivingEntity) entity);
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

    @Override
    public Iterable<NPC> sorted() {
        return npcs.sorted();
    }

    public static class MapNPCCollection implements NPCCollection {
        private final Map<Integer, NPC> npcs = Maps.newHashMap();

        @Override
        public NPC get(int id) {
            return npcs.get(id);
        }

        @Override
        public Iterator<NPC> iterator() {
            return npcs.values().iterator();
        }

        @Override
        public void put(int id, NPC npc) {
            npcs.put(id, npc);
        }

        @Override
        public void remove(int id) {
            npcs.remove(id);
        }

        @Override
        public Iterable<NPC> sorted() {
            List<NPC> vals = new ArrayList<NPC>(npcs.values());
            Collections.sort(vals, NPC_COMPARATOR);
            return vals;
        }
    }

    public static interface NPCCollection extends Iterable<NPC> {
        public NPC get(int id);

        public void put(int id, NPC npc);

        public void remove(int id);

        public Iterable<NPC> sorted();
    }

    public static class TroveNPCCollection implements NPCCollection {
        private final TIntObjectHashMap<NPC> npcs = new TIntObjectHashMap<NPC>();

        @Override
        public NPC get(int id) {
            return npcs.get(id);
        }

        @Override
        public Iterator<NPC> iterator() {
            return npcs.valueCollection().iterator();
        }

        @Override
        public void put(int id, NPC npc) {
            npcs.put(id, npc);
        }

        @Override
        public void remove(int id) {
            npcs.remove(id);
        }

        @Override
        public Iterable<NPC> sorted() {
            List<NPC> vals = new ArrayList<NPC>(npcs.valueCollection());
            Collections.sort(vals, NPC_COMPARATOR);
            return vals;
        }
    }

    private static final Comparator<NPC> NPC_COMPARATOR = new Comparator<NPC>() {
        @Override
        public int compare(NPC o1, NPC o2) {
            return o1.getId() - o2.getId();
        }
    };
    private static boolean TROVE_EXISTS = false;
    static {
        // allow trove dependency to be optional for debugging purposes
        try {
            Class.forName("gnu.trove.map.hash.TIntObjectHashMap").newInstance();
            TROVE_EXISTS = true;
        } catch (Exception e) {
        }
    }
}