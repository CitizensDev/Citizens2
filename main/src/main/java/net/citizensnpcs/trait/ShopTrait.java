package net.citizensnpcs.trait;

import java.util.Map;

import org.bukkit.entity.Player;

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
        return NPC_SHOPS.get(npc.getUniqueId().toString());
    }

    public NPCShop getShop(String name) {
        return SHOPS.get(name);
    }

    public static class NPCShop {
        @Persist
        String name;

        private NPCShop(String name) {
            this.name = name;
        }

        public void display(Player sender) {
        }
    }

    @Persist(value = "npcShops", namespace = "shopstrait")
    private static Map<String, NPCShop> NPC_SHOPS = Maps.newHashMap();
    @Persist(value = "namedShops", namespace = "shopstrait")
    private static Map<String, NPCShop> SHOPS = Maps.newHashMap();
}