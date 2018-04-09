package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCCreateEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.MountTrait;
import net.citizensnpcs.util.NMS;

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
        if (type == EntityType.ARMOR_STAND && !npc.hasTrait(ArmorStandTrait.class)) {
            npc.addTrait(ArmorStandTrait.class);
        }
        if (Setting.DEFAULT_LOOK_CLOSE.asBoolean()) {
            npc.addTrait(LookClose.class);
        }
        npc.addTrait(MountTrait.class);
        return npc;
    }

    @Override
    public void deregister(NPC npc) {
        npc.despawn(DespawnReason.REMOVAL);
        npcs.remove(npc);
        if (saves != null) {
            saves.clearData(npc);
        }
    }

    @Override
    public void deregisterAll() {
        Iterator<NPC> itr = iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            npc.despawn(DespawnReason.REMOVAL);
            for (Trait t : npc.getTraits()) {
                t.onRemove();
            }
            itr.remove();
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
    public NPC getByUniqueId(UUID uuid) {
        return npcs.get(uuid);
    }

    @Override
    public NPC getByUniqueIdGlobal(UUID uuid) {
        NPC npc = getByUniqueId(uuid);
        if (npc != null)
            return npc;
        for (NPCRegistry registry : CitizensAPI.getNPCRegistries()) {
            if (registry != this) {
                NPC other = registry.getByUniqueId(uuid);
                if (other != null) {
                    return other;
                }
            }
        }
        return null;
    }

    @Override
    public NPC getNPC(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof NPCHolder)
            return ((NPCHolder) entity).getNPC();
        return NMS.getNPC(entity);
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
        private final Map<UUID, NPC> uniqueNPCs = Maps.newHashMap();

        @Override
        public NPC get(int id) {
            return npcs.get(id);
        }

        @Override
        public NPC get(UUID uuid) {
            return uniqueNPCs.get(uuid);
        }

        @Override
        public Iterator<NPC> iterator() {
            return npcs.values().iterator();
        }

        @Override
        public void put(int id, NPC npc) {
            npcs.put(id, npc);
            uniqueNPCs.put(npc.getUniqueId(), npc);
        }

        @Override
        public void remove(NPC npc) {
            npcs.remove(npc.getId());
            uniqueNPCs.remove(npc.getUniqueId());
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

        public NPC get(UUID uuid);

        public void put(int id, NPC npc);

        public void remove(NPC npc);

        public Iterable<NPC> sorted();
    }

    public static class TroveNPCCollection implements NPCCollection {
        private final TIntObjectHashMap<NPC> npcs = new TIntObjectHashMap<NPC>();
        private final Map<UUID, NPC> uniqueNPCs = Maps.newHashMap();

        @Override
        public NPC get(int id) {
            return npcs.get(id);
        }

        @Override
        public NPC get(UUID uuid) {
            return uniqueNPCs.get(uuid);
        }

        @Override
        public Iterator<NPC> iterator() {
            return npcs.valueCollection().iterator();
        }

        @Override
        public void put(int id, NPC npc) {
            npcs.put(id, npc);
            uniqueNPCs.put(npc.getUniqueId(), npc);
        }

        @Override
        public void remove(NPC npc) {
            npcs.remove(npc.getId());
            uniqueNPCs.remove(npc.getUniqueId());
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
