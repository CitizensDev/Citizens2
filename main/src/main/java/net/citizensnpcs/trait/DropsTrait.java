package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.gui.InventoryMenu;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.gui.PercentageSlotHandler;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.Util;

@TraitName("dropstrait")
public class DropsTrait extends Trait {
    @Persist(reify = true)
    private List<ItemDrop> drops = Lists.newArrayList();

    public DropsTrait() {
        super("dropstrait");
    }

    public void displayEditor(Player sender) {
        InventoryMenu.createSelfRegistered(new DropsGUI(this)).present(sender);
    }

    @EventHandler
    public void onNPCDeath(NPCDeathEvent event) {
        if (!event.getNPC().equals(npc))
            return;

        Random random = Util.getFastRandom();
        for (ItemDrop drop : drops) {
            if (random.nextDouble() < drop.chance) {
                event.getDrops().add(drop.drop.clone());
            }
        }
    }

    @Menu(title = "Add items for drops", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class DropsGUI extends InventoryMenuPage {
        private final Map<Integer, Double> chances = Maps.newHashMap();
        private Inventory inventory;
        private DropsTrait trait;

        private DropsGUI() {
            throw new UnsupportedOperationException();
        }

        public DropsGUI(DropsTrait trait) {
            this.trait = trait;
        }

        @Override
        public void initialise(MenuContext ctx) {
            inventory = ctx.getInventory();
            int k = 0;
            for (int i = 1; i < 5; i += 2) {
                for (int j = 0; j < 9; j++) {
                    int islot = (i - 1) * 9 + j;

                    ItemDrop drop;
                    int chance = 100;
                    if (k < trait.drops.size()) {
                        drop = trait.drops.get(k++);
                        chance = (int) Math.floor(drop.chance * 100.0);
                        chances.put(islot, drop.chance);
                        ctx.getInventory().setItem(islot, drop.drop.clone());
                    }
                    InventoryMenuSlot slot = ctx.getSlot(i * 9 + j);
                    slot.setItemStack(new ItemStack(Util.getFallbackMaterial("BARRIER", "FIRE")),
                            "Drop chance <e>" + chance + "%");
                    slot.setClickHandler(new PercentageSlotHandler(pct -> {
                        if (chances.containsKey(islot)) {
                            chances.put(islot, pct / 100.0);
                        }
                        return "Drop chance <e>" + pct + "%";
                    }, chance));
                }
            }
        }

        @Override
        public void onClick(InventoryMenuSlot slot, InventoryClickEvent event) {
            if (slot.getCurrentItem() != null && slot.getCurrentItem().getType().name().equals("BARRIER"))
                return;
            event.setCancelled(false);
            if (event.getAction().name().contains("PICKUP")) {
                chances.remove(event.getSlot());
            } else if (event.getAction().name().contains("PLACE")) {
                chances.putIfAbsent(event.getSlot(), 1.0);
            }
        }

        @Override
        public void onClose(HumanEntity player) {
            List<ItemDrop> drops = Lists.newArrayList();
            for (int i = 0; i < 5; i += 2) {
                for (int j = 0; j < 9; j++) {
                    int slot = i * 9 + j;
                    ItemStack stack = inventory.getItem(slot);
                    if (stack == null || stack.getType() == Material.AIR) {
                        continue;
                    }
                    drops.add(new ItemDrop(stack.clone(), chances.getOrDefault(slot, 1.0)));
                }
            }
            trait.drops = drops;
        }
    }

    private static class ItemDrop {
        @Persist
        double chance;
        @Persist
        ItemStack drop;

        public ItemDrop() {
        }

        public ItemDrop(ItemStack drop, double chance) {
            this.drop = drop;
            this.chance = chance;
        }
    }
}