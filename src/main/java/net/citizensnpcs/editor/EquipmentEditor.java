package net.citizensnpcs.editor;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EquipmentEditor extends Editor {
    private final NPC npc;
    private final Player player;

    public EquipmentEditor(Player player, NPC npc) {
        this.player = player;
        this.npc = npc;
    }

    @Override
    public void begin() {
        Messaging.send(player, "<b>Entered the equipment editor!");
        Messaging.send(player, "<e>Right click <a>to equip the NPC!");
    }

    @Override
    public void end() {
        Messaging.send(player, "<a>Exited the equipment editor.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && Editor.hasEditor(event.getPlayer()))
            event.setUseItemInHand(Result.DENY);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().equals(player)
                || !npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            return;

        if (npc instanceof Equipable) {
            ItemStack hand = event.getPlayer().getItemInHand();
            ((Equipable) npc).equip(event.getPlayer(), hand);
            event.getPlayer().setItemInHand(hand.getAmount() > 0 ? hand : null);
        }
    }
}