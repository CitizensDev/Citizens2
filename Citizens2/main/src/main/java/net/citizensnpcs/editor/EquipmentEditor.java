package net.citizensnpcs.editor;

import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.gui.InventoryMenu;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.npc.NPC;
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
        if (EQUIPPER_GUIS.containsKey(npc.getEntity().getType()) || !EQUIPPERS.containsKey(npc.getEntity().getType())) {
            Map<String, Object> ctx = Maps.newHashMap();
            ctx.put("npc", npc);
            menu = InventoryMenu.createWithContext(
                    EQUIPPER_GUIS.getOrDefault(npc.getEntity().getType(), GenericEquipperGUI.class), ctx);
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
    public void onInventoryClick(InventoryDragEvent event) {
        if (menu != null && event.getWhoClicked().equals(player)) {
            menu.onInventoryDrag(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (menu != null && event.getPlayer().equals(player)) {
            menu.onInventoryClose(event);
            Editor.leave((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().equals(player)) {
            event.setUseItemInHand(Result.DENY);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!npc.isSpawned() || menu != null || !event.getPlayer().equals(player) || Util.isOffHand(event)
                || !npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            return;
        Equipper equipper = EQUIPPERS.get(npc.getEntity().getType());
        equipper.equip(event.getPlayer(), npc);
        event.setCancelled(true);
    }

    private static final Map<EntityType, Class<? extends InventoryMenuPage>> EQUIPPER_GUIS = Maps
            .newEnumMap(EntityType.class);
    private static final Map<EntityType, Equipper> EQUIPPERS = Maps.newEnumMap(EntityType.class);

    static {
        EQUIPPER_GUIS.put(EntityType.PIG, SteerableEquipperGUI.class);
        try {
            EQUIPPER_GUIS.put(EntityType.valueOf("STRIDER"), SteerableEquipperGUI.class);
        } catch (IllegalArgumentException ex) {
        }
        EQUIPPER_GUIS.put(EntityType.ENDERMAN, EndermanEquipperGUI.class);
        EQUIPPERS.put(EntityType.SHEEP, new SheepEquipper());
        EQUIPPERS.put(EntityType.HORSE, new HorseEquipper());
        EQUIPPERS.put(EntityType.WOLF, new WolfEquipper());
        for (EntityType type : Util.optionalEntitySet("ZOMBIE_HORSE", "LLAMA", "TRADER_LLAMA", "DONKEY", "MULE",
                "SKELETON_HORSE", "CAMEL")) {
            EQUIPPERS.put(type, new HorseEquipper());
        }
    }
}