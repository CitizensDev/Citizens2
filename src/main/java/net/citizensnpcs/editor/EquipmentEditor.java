package net.citizensnpcs.editor;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EquipmentEditor implements Editor {
    private final Citizens plugin;
    private final Player player;
    private final NPC npc;

    public EquipmentEditor(Citizens plugin, Player player, NPC npc) {
        this.plugin = plugin;
        this.player = player;
        this.npc = npc;
    }

    @Override
    public void begin() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Messaging.send(player, "<a>Entered the equipment editor!");
        Messaging.send(player, "<a>Right click to equip armor and items.");
        Messaging.send(player, "<a>Right click while crouching to equip armor in the NPC's hand.");
        Messaging.send(player, "<a>Left click to remove all armor and items.");
    }

    @Override
    public void end() {
        HandlerList.unregisterAll(this);
        Messaging.send(player, "<a>Exited equipment editor.");
    }

    @Override
    public String getName() {
        return "equip";
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().getName().equals(player.getName()))
            event.setUseItemInHand(Result.DENY);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (plugin.getNPCManager().isNPC(event.getRightClicked())
                && plugin.getNPCManager().getNPC(event.getRightClicked()).equals(npc)
                && event.getPlayer().getName().equals(player.getName())) {
            npc.chat("You clicked me!");
        }
    }
}