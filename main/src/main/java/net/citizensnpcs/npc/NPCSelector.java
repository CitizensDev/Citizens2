package net.citizensnpcs.npc;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Util;

public class NPCSelector implements Listener, net.citizensnpcs.api.npc.NPCSelector {
    private UUID consoleSelectedNPC;
    private final Plugin plugin;

    public NPCSelector(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public NPC getSelected(CommandSender sender) {
        if (sender instanceof Player) {
            return getSelectedFromMetadatable((Player) sender);
        } else if (sender instanceof BlockCommandSender) {
            return getSelectedFromMetadatable(((BlockCommandSender) sender).getBlock());
        } else if (sender instanceof ConsoleCommandSender) {
            if (consoleSelectedNPC == null)
                return null;
            return CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(consoleSelectedNPC);
        }
        return null;
    }

    private NPC getSelectedFromMetadatable(Metadatable sender) {
        List<MetadataValue> metadata = sender.getMetadata("selected");
        if (metadata.size() == 0)
            return null;
        if (metadata.get(0).value() == null) {
            sender.removeMetadata("selected", plugin);
            return null;
        }
        return CitizensAPI.getNPCRegistry().getByUniqueIdGlobal((UUID) metadata.get(0).value());
    }

    @EventHandler
    public void onNPCRemove(NPCRemoveEvent event) {
        NPC npc = event.getNPC();
        List<String> selectors = npc.data().get("selectors");
        if (selectors == null)
            return;
        for (String value : selectors) {
            if (value.equals("console")) {
                consoleSelectedNPC = null;
            } else if (value.startsWith("@")) {
                String[] parts = value.substring(1, value.length()).split(":");
                World world = Bukkit.getWorld(parts[0]);
                if (world != null) {
                    Block block = world.getBlockAt(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]));
                    removeMetadata(block);
                }
            } else {
                Player search = Bukkit.getPlayerExact(value);
                removeMetadata(search);
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
            if (Util.matchesItemInHand(player, Setting.SELECTION_ITEM.asString())
                    && npc.getTrait(Owner.class).isOwnedBy(player)) {
                player.removeMetadata("selected", plugin);
                select(player, npc);
                Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), npc);
                if (!Setting.QUICK_SELECT.asBoolean())
                    return;
            }
        }
    }

    private void removeMetadata(Metadatable metadatable) {
        if (metadatable != null) {
            metadatable.removeMetadata("selected", plugin);
        }
    }

    public void select(CommandSender sender, NPC npc) {
        // Remove existing selection if any
        List<String> selectors = npc.data().get("selectors");
        if (selectors == null) {
            selectors = Lists.newArrayList();
            npc.data().set("selectors", selectors);
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            setMetadata(npc, player);
            selectors.add(sender.getName());

            // Remove editor if the player has one
            Editor.leave(player);
        } else if (sender instanceof BlockCommandSender) {
            Block block = ((BlockCommandSender) sender).getBlock();
            setMetadata(npc, block);
            selectors.add(toName(block));
        } else if (sender instanceof ConsoleCommandSender) {
            consoleSelectedNPC = npc.getUniqueId();
            selectors.add("console");
        }

        Bukkit.getPluginManager().callEvent(new NPCSelectEvent(npc, sender));
    }

    private void setMetadata(NPC npc, Metadatable metadatable) {
        if (metadatable.hasMetadata("selected")) {
            metadatable.removeMetadata("selected", plugin);
        }
        metadatable.setMetadata("selected", new FixedMetadataValue(plugin, npc.getUniqueId()));
    }

    private String toName(Block block) {
        return '@' + block.getWorld().getName() + ":" + Integer.toString(block.getX()) + ":"
                + Integer.toString(block.getY()) + ":" + Integer.toString(block.getZ());
    }
}
