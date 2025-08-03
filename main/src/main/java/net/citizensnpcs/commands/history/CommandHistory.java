package net.citizensnpcs.commands.history;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.citizensnpcs.npc.NPCSelector;

public class CommandHistory {
    private final ListMultimap<UUID, CommandHistoryItem> history = ArrayListMultimap.create();
    private final NPCSelector selector;

    public CommandHistory(NPCSelector selector) {
        this.selector = selector;
    }

    public void add(CommandSender sender, CommandHistoryItem item) {
        history.put(sender instanceof Entity ? ((Entity) sender).getUniqueId() : null, item);
    }

    public boolean undo(CommandSender sender) {
        UUID uuid = sender instanceof Entity ? ((Entity) sender).getUniqueId() : null;
        List<CommandHistoryItem> hist = history.get(uuid);
        if (hist.size() == 0)
            return false;
        CommandHistoryItem item = hist.remove(hist.size() - 1);
        item.undo(sender, selector);
        return true;
    }
}
