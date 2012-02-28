package net.citizensnpcs.editor;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EquipmentEditor extends Editor {
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
        Messaging.send(player, "<2>Entered the equipment editor!");
        Messaging.send(player, "<e>Right click <a>to equip armor and items.");
        Messaging.send(player, "<e>Right click <a>while <e>crouching <a>to equip armor in the NPC's hand.");
        Messaging.send(player, "<e>Right click <a>with an <e>empty hand <a>to remove all armor and items.");
    }

    @Override
    public void end() {
        Messaging.send(player, "<a>Exited equipment editor.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && Editor.hasEditor(event.getPlayer()))
            event.setUseItemInHand(Result.DENY);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getNPCManager().isNPC(event.getRightClicked())
                || !plugin.getNPCManager().getNPC(event.getRightClicked()).equals(npc)
                || !event.getPlayer().equals(player))
            return;

        ItemStack hand = player.getItemInHand();
        Equipment trait = npc.getTrait(Equipment.class);
        int slot = 0;
        // First, determine the slot to edit
        switch (hand.getType()) {
        case PUMPKIN:
        case JACK_O_LANTERN:
        case LEATHER_HELMET:
        case CHAINMAIL_HELMET:
        case GOLD_HELMET:
        case IRON_HELMET:
        case DIAMOND_HELMET:
            if (!player.isSneaking())
                slot = 1;
            break;
        case LEATHER_CHESTPLATE:
        case CHAINMAIL_CHESTPLATE:
        case GOLD_CHESTPLATE:
        case IRON_CHESTPLATE:
        case DIAMOND_CHESTPLATE:
            if (!player.isSneaking())
                slot = 2;
            break;
        case LEATHER_LEGGINGS:
        case CHAINMAIL_LEGGINGS:
        case GOLD_LEGGINGS:
        case IRON_LEGGINGS:
        case DIAMOND_LEGGINGS:
            if (!player.isSneaking())
                slot = 3;
            break;
        case LEATHER_BOOTS:
        case CHAINMAIL_BOOTS:
        case GOLD_BOOTS:
        case IRON_BOOTS:
        case DIAMOND_BOOTS:
            if (!player.isSneaking())
                slot = 4;
            break;
        case AIR:
            for (int i = 0; i < 4; i++) {
                if (trait.get(i) != null && trait.get(i).getType() != Material.AIR) {
                    player.getWorld().dropItemNaturally(npc.getBukkitEntity().getLocation(), trait.get(i));
                    trait.set(i, null);
                }
            }
            Messaging.send(player, "<e>" + npc.getName() + " <a>had all of its items removed.");
        }
        // Now edit the equipment based on the slot
        if (trait.get(slot) != null && trait.get(slot).getType() != Material.AIR)
            player.getWorld().dropItemNaturally(npc.getBukkitEntity().getLocation(), trait.get(slot));

        ItemStack set = hand;
        if (set != null && set.getType() != Material.AIR) {
            if (hand.getAmount() > 1)
                hand.setAmount(hand.getAmount() - 1);
            else
                hand = null;
            player.setItemInHand(hand);
            set.setAmount(1);
        }
        trait.set(slot, set);
    }
}