package net.citizensnpcs.commands.gui;

import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.gui.CitizensInventoryClickEvent;
import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.Util;

@Menu(title = "Configure NPC", type = InventoryType.CHEST, dimensions = { 5, 9 })
public class NPCConfigurator extends InventoryMenuPage {
    private NPC npc;

    private NPCConfigurator() {
        throw new UnsupportedOperationException();
    }

    public NPCConfigurator(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void initialise(MenuContext ctx) {
        for (Map.Entry<Integer, ConfiguratorInfo> entry : SLOT_MAP.entrySet()) {
            ConfiguratorInfo info = entry.getValue();
            InventoryMenuSlot slot = ctx.getSlot(entry.getKey());
            slot.setItemStack(new ItemStack(info.material, 1));
            slot.setClickHandler(evt -> info.clickHandler.accept(new ConfiguratorEvent(ctx, npc, slot, evt)));
            info.clickHandler.accept(new ConfiguratorEvent(ctx, npc, slot, null));
        }
    }

    private static class ConfiguratorEvent {
        private final MenuContext ctx;
        private final CitizensInventoryClickEvent event;
        private final NPC npc;
        private final InventoryMenuSlot slot;

        public ConfiguratorEvent(MenuContext ctx, NPC npc, InventoryMenuSlot slot, CitizensInventoryClickEvent evt) {
            this.ctx = ctx;
            this.npc = npc;
            this.slot = slot;
            event = evt;
        }
    }

    private static class ConfiguratorInfo {
        private final Consumer<ConfiguratorEvent> clickHandler;
        private final Material material;

        public ConfiguratorInfo(Material mat, Consumer<ConfiguratorEvent> con) {
            material = mat;
            clickHandler = con;
        }
    }

    private static final Map<Integer, ConfiguratorInfo> SLOT_MAP = Maps.newHashMap();
    static {
        SLOT_MAP.put(0, new ConfiguratorInfo(Util.getFallbackMaterial("OAK_SIGN", "SIGN"), evt -> {
            evt.slot.setDescription("Edit NPC name\n" + evt.npc.getName());
            if (evt.event != null) {
                evt.ctx.getMenu()
                        .transition(InputMenus.stringSetter(() -> evt.npc.getName(), input -> evt.npc.setName(input)));
            }
        }));
    }
}
