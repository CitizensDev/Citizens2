package net.citizensnpcs.npc;

import java.security.acl.Owner;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.CommandSender;
import net.citizensnpcs.api.abstraction.EventHandler;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Util;

import org.mozilla.javascript.ContextFactory.Listener;

public class NPCSelector implements Listener {
    private int consoleSelectedNPC = -1;

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

    @EventHandler
    public void onNPCRemove(NPCRemoveEvent event) {
        NPC npc = event.getNPC();
        for (MetadataValue value : npc.getMetadata("selectors")) {
            if (value.asString().equals("console")) {
                consoleSelectedNPC = -1;
            } else {
                Player search = Bukkit.getPlayerExact(value.asString());
                if (search != null)
                    search.removeMetadata("selected", plugin);
            }
        }
        npc.removeMetadata("selectors", plugin);
    }

    public void select(CommandSender sender, NPC npc) {
        // Remove existing selection if any
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasMetadata("selected"))
                player.removeMetadata("selected", plugin);

            player.setMetadata("selected", new FixedMetadataValue(plugin, npc.getId()));
            npc.setMetadata("selectors", new FixedMetadataValue(plugin, player.getName()));

            // Remove editor if the player has one
            Editor.leave(player);
        } else {
            consoleSelectedNPC = npc.getId();
            npc.setMetadata("selectors", new FixedMetadataValue(plugin, "console"));
        }

        Bukkit.getPluginManager().callEvent(new NPCSelectEvent(npc, sender));
    }

    public NPC getSelected(CommandSender sender) {
        if (sender instanceof Player) {
            List<MetadataValue> metadata = ((Player) sender).getMetadata("selected");
            if (metadata.size() == 0)
                return null;
            return CitizensAPI.getNPCRegistry().getNPC(metadata.get(0).asInt());
        } else {
            if (consoleSelectedNPC == -1)
                return null;
            return CitizensAPI.getNPCRegistry().getNPC(consoleSelectedNPC);
        }
    }
}
