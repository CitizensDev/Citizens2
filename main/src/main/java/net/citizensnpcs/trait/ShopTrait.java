package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Shop trait for NPC GUI shops.
 */
@TraitName("shop")
public class ShopTrait extends Trait {
    public ShopTrait() {
        super("shop");
    }

    public NPCShop getDefaultShop() {
        return NPC_SHOPS.computeIfAbsent(npc.getUniqueId().toString(), NPCShop::new);
    }

    public NPCShop getShop(String name) {
        return SHOPS.computeIfAbsent(name, NPCShop::new);
    }

    public static class NPCShop {
        @Persist
        private final String name;
        @Persist(reify = true)
        private final List<NPCShopPage> pages = Lists.newArrayList();
        @Persist
        private String title;

        private NPCShop(String name) {
            this.name = name;
        }

        public void display(Player sender) {
        }

        public void displayEditor(Player sender) {
        }

        public String getName() {
            return name;
        }
    }

    public static class NPCShopItem {
        @Persist
        private int cost;
        @Persist
        private ItemStack display;
    }

    public static class NPCShopPage {
        @Persist("")
        private int index;
        @Persist(reify = true)
        private final Map<Integer, NPCShopItem> items = Maps.newHashMap();
        @Persist
        private String title;
    }

    @Persist(value = "npcShops", namespace = "shopstrait")
    private static Map<String, NPCShop> NPC_SHOPS = Maps.newHashMap();
    @Persist(value = "namedShops", namespace = "shopstrait")
    private static Map<String, NPCShop> SHOPS = Maps.newHashMap();
}