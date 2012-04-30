package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.citizensnpcs.util.ByIdArray;
import net.citizensnpcs.util.NPCBuilder;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class CitizensNPCManager implements NPCManager {
    private final NPCBuilder npcBuilder = new NPCBuilder();
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final Citizens plugin;
    private final Storage saves;

    public CitizensNPCManager(Citizens plugin, Storage saves) {
        this.plugin = plugin;
        this.saves = saves;
    }

    public NPC createNPC(EntityType type, int id, String name, Character character) {
        CitizensNPC npc = npcBuilder.getByType(type, this, id, name);
        if (npc == null)
            throw new IllegalStateException("could not create npc");
        if (character != null)
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

    public void despawn(NPC npc, boolean keepSelected) {
        if (!keepSelected)
            npc.removeMetadata("selectors", plugin);
        npc.getBukkitEntity().remove();
    }

    private int generateUniqueId() {
        int count = 0;
        while (getNPC(count++) != null)
            ; // TODO: doesn't respect existing save data that might not have
              // been loaded. This causes DBs with NPCs that weren't loaded to
              // have conflicting primary keys.
        return count - 1;
    }

    @Override
    public NPC getNPC(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return null;
        net.minecraft.server.Entity handle = ((CraftEntity) entity).getHandle();
        if (handle instanceof NPCHandle)
            return ((NPCHandle) handle).getNPC();
        return null;
    }

    @Override
    public NPC getNPC(int id) {
        return npcs.get(id);
    }

    @Override
    public Collection<NPC> getNPCs(Class<? extends Character> character) {
        List<NPC> npcs = new ArrayList<NPC>();
        for (NPC npc : this) {
            if (npc.getCharacter() != null && npc.getCharacter().getClass().equals(character))
                npcs.add(npc);
        }
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
        removeData(npc);
    }

    public void removeAll() {
        Iterator<NPC> itr = iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            npc.despawn();
            removeData(npc);
            itr.remove();
        }
    }

    private void removeData(NPC npc) {
        saves.getKey("npc").removeKey(String.valueOf(npc.getId()));
        removeMetadata(npc);
    }

    private void removeMetadata(NPC npc) {
        // Remove metadata from selectors
        if (npc.hasMetadata("selectors")) {
            for (MetadataValue value : npc.getMetadata("selectors"))
                if (Bukkit.getPlayer(value.asString()) != null)
                    Bukkit.getPlayer(value.asString()).removeMetadata("selected", plugin);
            npc.removeMetadata("selectors", plugin);
        }
    }

    public void safeRemove() {
        // Destroy all NPCs everywhere besides storage
        Iterator<NPC> itr = this.iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            itr.remove();
            removeMetadata(npc);
            npc.despawn();
        }
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