package net.citizensnpcs.commands.history;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;

public class RemoveNPCHistoryItem implements CommandHistoryItem {
    private final int id;
    private final DataKey key;
    private final EntityType type;
    private final UUID uuid;

    public RemoveNPCHistoryItem(NPC from) {
        key = new MemoryDataKey();
        from.save(key);
        type = from.getOrAddTrait(MobType.class).getType();
        uuid = from.getUniqueId();
        id = from.getId();
    }

    @Override
    public void undo(CommandSender sender, NPCSelector selector) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(type, uuid, id, key.getString("name"));
        npc.load(key);
        selector.select(sender, npc);
    }
}
