package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCCreateEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.util.RemoveReason;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.LookClose;

public class CitizensNPCRegistry implements NPCRegistry {
    private final String name;
    private final Int2ObjectOpenHashMap<NPC> npcs = new Int2ObjectOpenHashMap<>();
    private final NPCDataStore saves;
    private final Map<UUID, NPC> uniqueNPCs = Maps.newHashMap();

    public CitizensNPCRegistry(NPCDataStore store) {
        this(store, "");
    }

    public CitizensNPCRegistry(NPCDataStore store, String registryName) {
        saves = store;
        name = registryName;
    }

    @Override
    public NPC createNPC(EntityType type, String name) {
        return createNPC(type, UUID.randomUUID(), generateIntegerId(), name);
    }

    @Override
    public NPC createNPC(EntityType type, String name, Location loc) {
        NPC npc = createNPC(type, name);
        npc.spawn(loc, SpawnReason.PLUGIN);
        return npc;
    }

    @Override
    public NPC createNPC(EntityType type, UUID uuid, int id, String name) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        CitizensNPC npc = new CitizensNPC(uuid, id, name, EntityControllers.createForType(type), this);
        npc.getOrAddTrait(MobType.class).setType(type);
        npcs.put(id, npc);
        uniqueNPCs.put(npc.getUniqueId(), npc);
        Bukkit.getPluginManager().callEvent(new NPCCreateEvent(npc));
        if (type == EntityType.ARMOR_STAND && !npc.hasTrait(ArmorStandTrait.class)) {
            npc.addTrait(ArmorStandTrait.class);
        }
        if (Setting.DEFAULT_LOOK_CLOSE.asBoolean()) {
            npc.addTrait(LookClose.class);
        }
        return npc;
    }

    @Override
    public NPC createNPCUsingItem(EntityType type, String name, ItemStack item) {
        NPC npc = createNPC(type, name);
        if (type.name().equals("OMINOUS_ITEM_SPAWNER") || type.name().equals("DROPPED_ITEM")
                || type.name().contains("MINECART") || type.name().equals("ITEM") || type == EntityType.FALLING_BLOCK
                || type == EntityType.ITEM_FRAME || type.name().equals("GLOW_ITEM_FRAME")
                || type.name().equals("ITEM_DISPLAY") || type.name().equals("BLOCK_DISPLAY")) {
            npc.data().set(NPC.Metadata.ITEM_AMOUNT, item.getAmount());
            npc.data().set(NPC.Metadata.ITEM_ID, item.getType().name());
            npc.data().set(NPC.Metadata.ITEM_DATA, item.getData().getData());
            npc.setItemProvider(() -> item.clone());
        } else
            throw new UnsupportedOperationException("Not an item entity type");
        return npc;
    }

    @Override
    public void deregister(NPC npc) {
        npc.despawn(DespawnReason.REMOVAL);
        npcs.remove(npc.getId());
        uniqueNPCs.remove(npc.getUniqueId());
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
                HandlerList.unregisterAll(t);
                t.onRemove(RemoveReason.REMOVAL);
            }
            itr.remove();
            if (saves != null) {
                saves.clearData(npc);
            }
        }
    }

    @Override
    public void despawnNPCs(DespawnReason reason) {
        Iterator<NPC> itr = iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            try {
                npc.despawn(reason);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            itr.remove();
        }
    }

    private int generateIntegerId() {
        return saves.createUniqueNPCId(this);
    }

    @Override
    public NPC getById(int id) {
        if (id < 0)
            throw new IllegalArgumentException("invalid id");
        return npcs.get(id);
    }

    @Override
    public NPC getByUniqueId(UUID uuid) {
        if (uuid.version() == 2) {
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000002000L;
            msb |= 0x0000000000004000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
        }
        return uniqueNPCs.get(uuid);
    }

    @Override
    public NPC getByUniqueIdGlobal(UUID uuid) {
        if (uuid.version() == 2) {
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000002000L;
            msb |= 0x0000000000004000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
        }
        NPC npc = getByUniqueId(uuid);
        if (npc != null)
            return npc;
        for (NPCRegistry registry : CitizensAPI.getNPCRegistries()) {
            if (registry == this) {
                continue;
            }
            NPC other = registry.getByUniqueId(uuid);
            if (other != null)
                return other;
        }
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NPC getNPC(Entity entity) {
        return entity instanceof NPCHolder ? ((NPCHolder) entity).getNPC() : null;
    }

    @Override
    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    @Override
    public Iterator<NPC> iterator() {
        return new Iterator<NPC>() {
            Iterator<NPC> itr = npcs.values().iterator();
            UUID lastUUID;

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public NPC next() {
                NPC npc = itr.next();
                if (npc != null && npc.getUniqueId() != null) {
                    lastUUID = npc.getUniqueId();
                }
                return npc;
            }

            @Override
            public void remove() {
                itr.remove();
                if (lastUUID != null) {
                    uniqueNPCs.remove(lastUUID);
                    lastUUID = null;
                }
            }
        };
    }

    @Override
    public void saveToStore() {
        saves.storeAll(this);
        saves.saveToDiskImmediate();
    }

    @Override
    public Iterable<NPC> sorted() {
        List<NPC> vals = new ArrayList<>(npcs.values());
        vals.sort(Comparator.comparing(NPC::getId));
        return vals;
    }
}
