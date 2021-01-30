package net.citizensnpcs.editor;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.gui.InventoryMenu;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

public class EquipmentEditor extends Editor {
    private InventoryMenu menu;
    private final NPC npc;
    private final Player player;

    public EquipmentEditor(Player player, NPC npc) {
        this.player = player;
        this.npc = npc;
    }

    @Override
    public void begin() {
        if (!EQUIPPERS.containsKey(npc.getEntity().getType())) {
            Map<String, Object> ctx = Maps.newHashMap();
            ctx.put("npc", npc);
            menu = InventoryMenu.createWithContext(EquipmentGUI.class, ctx);
            menu.present(player);
            return;
        }
        Messaging.sendTr(player, Messages.EQUIPMENT_EDITOR_BEGIN);
    }

    @Override
    public void end() {
        if (menu != null) {
            menu.close();
            menu = null;
            return;
        }
        Messaging.sendTr(player, Messages.EQUIPMENT_EDITOR_END);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (menu != null && event.getWhoClicked().equals(player)) {
            menu.onInventoryClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (menu != null && event.getPlayer().equals(player)) {
            menu.onInventoryClose(event);
            Editor.leave((Player) event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player))
            return;
        EquipmentSlot slot = Util.matchEnum(EquipmentSlot.values(), event.getMessage());
        if (slot == null) {
            return;
        }
        if (!event.getPlayer().hasPermission("citizens.npc.edit.equip." + slot.name().toLowerCase().replace(" ", ""))
                && (slot != EquipmentSlot.HELMET
                        || !event.getPlayer().hasPermission("citizens.npc.edit.equip.any-helmet"))) {
            return;
        }
        final EquipmentSlot finalSlot = slot;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isValid())
                    return;
                ItemStack hand = event.getPlayer().getInventory().getItemInHand();
                if (hand.getType() == Material.AIR || hand.getAmount() <= 0) {
                    return;
                }
                ItemStack old = npc.getOrAddTrait(Equipment.class).get(finalSlot);
                if (old != null && old.getType() != Material.AIR) {
                    event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), old);
                }
                ItemStack newStack = hand.clone();
                newStack.setAmount(1);
                npc.getOrAddTrait(Equipment.class).set(finalSlot, newStack);
                hand.setAmount(hand.getAmount() - 1);
                event.getPlayer().getInventory().setItemInHand(hand);
            }
        });
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().equals(player)) {
            event.setUseItemInHand(Result.DENY);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!npc.isSpawned() || !event.getPlayer().equals(player) || Util.isOffHand(event)
                || !npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            return;
        Equipper equipper = EQUIPPERS.get(npc.getEntity().getType());
        if (equipper == null) {
            equipper = new GenericEquipper();
        }
        equipper.equip(event.getPlayer(), npc);
        event.setCancelled(true);
    }

    private static final Map<EntityType, Equipper> EQUIPPERS = Maps.newEnumMap(EntityType.class);

    static {
        EQUIPPERS.put(EntityType.PIG, new PigEquipper());
        EQUIPPERS.put(EntityType.SHEEP, new SheepEquipper());
        EQUIPPERS.put(EntityType.ENDERMAN, new EndermanEquipper());
        EQUIPPERS.put(EntityType.HORSE, new HorseEquipper());
        for (EntityType type : Util.optionalEntitySet("ZOMBIE_HORSE", "LLAMA", "TRADER_LLAMA", "DONKEY", "MULE",
                "SKELETON_HORSE")) {
            EQUIPPERS.put(type, new HorseEquipper());
        }
    }
}