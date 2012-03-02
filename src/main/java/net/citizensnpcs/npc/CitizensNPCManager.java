package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.trait.Character;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.ByIdArray;
import net.citizensnpcs.util.NPCBuilder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class CitizensNPCManager implements NPCManager {
    private final NPCBuilder npcBuilder = new NPCBuilder();
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final Citizens plugin;
    private final Storage saves;

    public CitizensNPCManager(Storage saves) {
        plugin = (Citizens) Bukkit.getPluginManager().getPlugin("Citizens");
        this.saves = saves;
    }

    public NPC createNPC(EntityType type, int id, String name, Character character) {
        CitizensNPC npc = npcBuilder.getByType(type, this, id, name);
        npc.setCharacter(character);
        npcs.put(npc.getId(), npc);
        return npc;
    }

    @Override
    public NPC createNPC(EntityType type, String name) {
        return createNPC(type, name, null);
    }

    @Override
    public NPC createNPC(EntityType type, String name, Character character) {
        return createNPC(type, generateUniqueId(), name, character);
    }

    public void despawn(NPC npc, boolean deselect) {
        npc.getTrait(SpawnLocation.class).setLocation(npc.getBukkitEntity().getLocation());
        if (!deselect)
            npc.removeMetadata("selectors", plugin);
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

    @Override
    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    @Override
    public Iterator<NPC> iterator() {
        return npcs.iterator();
    }

    public void remove(NPC npc) {
        npcs.remove(npc.getId());
        saves.getKey("npc").removeKey(String.valueOf(npc.getId()));

        // Remove metadata from selectors
        if (npc.hasMetadata("selectors")) {
            for (MetadataValue value : npc.getMetadata("selectors"))
                if (Bukkit.getPlayer(value.asString()) != null)
                    Bukkit.getPlayer(value.asString()).removeMetadata("selected", plugin);
            npc.removeMetadata("selectors", plugin);
        }
    }

    public void removeAll() {
        while (iterator().hasNext())
            iterator().next().remove();
    }

    public void selectNPC(Player player, NPC npc) {
        // Remove existing selection if any
        if (player.hasMetadata("selected"))
            player.removeMetadata("selected", plugin);

        player.setMetadata("selected", new FixedMetadataValue(plugin, npc.getId()));
        npc.setMetadata("selectors", new FixedMetadataValue(plugin, player.getName()));

        // Remove editor if the player has one
        Editor.leave(player);

        // Call selection event
        player.getServer().getPluginManager().callEvent(new NPCSelectEvent(npc, player));
    }
}