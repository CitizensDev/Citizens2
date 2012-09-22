package net.citizensnpcs.npc;

import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

public class CitizensNPCSelector implements Listener, NPCSelector {
    private int consoleSelectedNPC = -1;
    private final Plugin plugin;

    public CitizensNPCSelector(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public NPC getSelected(CommandSender sender) {
        if (sender instanceof Player) {
            List<MetadataValue> metadata = ((Player) sender).getMetadata("selected");
            if (metadata.size() == 0)
                return null;
            return CitizensAPI.getNPCRegistry().getById(metadata.get(0).asInt());
        } else {
            if (consoleSelectedNPC == -1)
                return null;
            return CitizensAPI.getNPCRegistry().getById(consoleSelectedNPC);
        }
    }

    @EventHandler
    public void onNPCRemove(NPCRemoveEvent event) {
        NPC npc = event.getNPC();
        List<String> selectors = npc.data().get("selectors");
        if (selectors == null)
            return;
        for (String value : selectors) {
            if (value.equals("console")) {
                consoleSelectedNPC = -1;
            } else {
                Player search = Bukkit.getPlayerExact(value);
                if (search != null)
                    search.removeMetadata("selected", plugin);
            }
        }
        npc.data().remove("selectors");
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        List<MetadataValue> selected = player.getMetadata("selected");
        if (selected == null || selected.size() == 0 || selected.get(0).asInt() != npc.getId()) {
            if (Util.isSettingFulfilled(player, Setting.SELECTION_ITEM)
                    && (npc.getTrait(Owner.class).isOwnedBy(player))) {
                player.removeMetadata("selected", plugin);
                select(player, npc);
                Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), npc);
                if (!Setting.QUICK_SELECT.asBoolean())
                    return;
            }
        }
    }

    @Override
    public void select(CommandSender sender, NPC npc) {
        // Remove existing selection if any
        List<Object> selectors = npc.data().get("selectors", Lists.newArrayList());
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasMetadata("selected"))
                player.removeMetadata("selected", plugin);

            player.setMetadata("selected", new FixedMetadataValue(plugin, npc.getId()));
            selectors.add(player.getName());

            // Remove editor if the player has one
            Editor.leave(player);
        } else {
            consoleSelectedNPC = npc.getId();
            selectors.add("console");
        }

        Bukkit.getPluginManager().callEvent(new NPCSelectEvent(npc, sender));
    }
}
