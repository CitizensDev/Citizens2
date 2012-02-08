package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.storage.Storage;
import net.citizensnpcs.util.ByIdArray;
import net.citizensnpcs.util.NPCBuilder;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class CitizensNPCManager implements NPCManager {
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final SetMultimap<Integer, String> selected = HashMultimap.create();
    private final Storage saves;
    private final NPCBuilder npcBuilder = new NPCBuilder();

    public CitizensNPCManager(Storage saves) {
        this.saves = saves;
    }

    public NPC createNPC(CreatureType type, int id, String name, Character character) {
        if (npcs.contains(id))
            throw new IllegalArgumentException("An NPC already has the ID '" + id + "'.");

        CitizensNPC npc = npcBuilder.getByType(type, this, id, name);
        npc.setCharacter(character);
        npcs.put(npc.getId(), npc);
        return npc;
    }

    @Override
    public NPC createNPC(CreatureType type, String name) {
        return createNPC(type, name, null);
    }

    @Override
    public NPC createNPC(CreatureType type, String name, Character character) {
        return createNPC(type, generateUniqueId(), name, character);
    }

    public void despawn(NPC npc, boolean deselect) {
        npc.getTrait(SpawnLocation.class).setLocation(npc.getBukkitEntity().getLocation());
        if (deselect)
            selected.removeAll(npc.getId());
        npc.getBukkitEntity().remove();
    }

    private int generateUniqueId() {
        int count = 0;
        while (getNPC(count++) != null)
            ;
        return count - 1;
    }

    @Override
    public NPC getNPC(Entity entity) {
        for (NPC npc : npcs)
            if (npc.isSpawned() && npc.getBukkitEntity().getEntityId() == entity.getEntityId())
                return npc;
        return null;
    }

    @Override
    public NPC getNPC(int id) {
        return npcs.get(id);
    }

    @Override
    public Collection<NPC> getNPCs(Class<? extends Character> character) {
        List<NPC> npcs = new ArrayList<NPC>();
        for (NPC npc : this)
            if (npc.getCharacter() != null && npc.getCharacter().getClass().equals(character))
                npcs.add(npc);
        return npcs;
    }

    public NPC getSelectedNPC(Player player) {
        for (int id : selected.keySet()) {
            if (selected.get(id).contains(player.getName()))
                return getNPC(id);
        }
        return null;
    }

    @Override
    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    @Override
    public Iterator<NPC> iterator() {
        return npcs.iterator();
    }

    public boolean npcIsSelectedByPlayer(Player player, NPC npc) {
        if (!selected.containsKey(npc.getId()))
            return false;
        return selected.get(npc.getId()).contains(player.getName());
    }

    public void remove(NPC npc) {
        if (npc.isSpawned())
            despawn(npc, true);
        npcs.remove(npc.getId());
        saves.getKey("npc").removeKey("" + npc.getId());
        selected.removeAll(npc.getId());
    }

    public void selectNPC(Player player, NPC npc) {
        // Remove existing selection if any
        NPC existing = getSelectedNPC(player);
        if (existing != null)
            selected.get(existing.getId()).remove(player.getName());
        selected.put(npc.getId(), player.getName());
    }
}