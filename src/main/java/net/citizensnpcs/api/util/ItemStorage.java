package net.citizensnpcs.api.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

public class ItemStorage {
    private static void deserialiseMeta(DataKey root, ItemStack res) {
        if (root.keyExists("lore")) {
            ensureMeta(res);
            List<String> lore = Lists.newArrayList();
            for (DataKey key : root.getRelative("lore").getIntegerSubKeys())
                lore.add(key.getString(""));
            res.getItemMeta().setLore(lore);
        }
        if (root.keyExists("displayname")) {
            ensureMeta(res);
            res.getItemMeta().setDisplayName(root.getString("displayname"));
        }
        if (root.keyExists("book")) {
            BookMeta meta = ensureMeta(res);
            int i = 0;
            for (DataKey sub : root.getRelative("book.pages").getIntegerSubKeys()) {
                meta.setPage(i, sub.getString(""));
            }
            meta.setTitle(root.getString("book.title"));
            meta.setAuthor(root.getString("book.author"));
        }
        if (root.keyExists("armor")) {
            LeatherArmorMeta meta = ensureMeta(res);
            meta.setColor(Color.fromRGB(root.getInt("armor.color")));
        }
        if (root.keyExists("map")) {
            MapMeta meta = ensureMeta(res);
            meta.setScaling(root.getBoolean("map.scaling"));
        }
        if (root.keyExists("skull")) {
            SkullMeta meta = ensureMeta(res);
            meta.setOwner(root.getString("skull.owner"));
        }
        if (root.keyExists("potion")) {
            PotionMeta meta = ensureMeta(res);
            for (DataKey sub : root.getRelative("potion.effects").getIntegerSubKeys()) {
                int duration = sub.getInt("duration");
                int amplifier = sub.getInt("amplifier");
                PotionEffectType type = PotionEffectType.getByName(sub.getString("type"));
                boolean ambient = sub.getBoolean("ambient");
                meta.addCustomEffect(new PotionEffect(type, duration, amplifier, ambient), true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends ItemMeta> T ensureMeta(ItemStack stack) {
        if (!stack.hasItemMeta())
            stack.setItemMeta(Bukkit.getServer().getItemFactory().getItemMeta(stack.getType()));
        return (T) stack.getItemMeta();
    }

    public static ItemStack loadItemStack(DataKey root) {
        Material matched = Material.matchMaterial(root.getString("type", root.getString("id")));
        if (matched == null)
            return null;
        ItemStack res = new ItemStack(matched, root.getInt("amount"), (short) (root.getInt("durability",
                root.getInt("data", 0))));
        if (root.keyExists("mdata") && res.getData() != null) {
            res.getData().setData((byte) root.getInt("mdata"));
        }

        if (root.keyExists("enchantments")) {
            Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
            for (DataKey subKey : root.getRelative("enchantments").getSubKeys()) {
                Enchantment enchantment = Enchantment.getById(Integer.parseInt(subKey.name()));
                if (enchantment != null && enchantment.canEnchantItem(res)) {
                    int level = Math.min(subKey.getInt(""), enchantment.getMaxLevel());
                    enchantments.put(enchantment, level);
                }
            }
            res.addEnchantments(enchantments);
        }
        deserialiseMeta(root.getRelative("meta"), res);
        return res;
    }

    private static void migrateForSave(DataKey key) {
        key.removeKey("data");
        key.removeKey("id");
    }

    public static void saveItem(DataKey key, ItemStack item) {
        if (item == null)
            return;
        migrateForSave(key);
        String type = item.getType() == null ? Material.AIR.name() : item.getType().name();
        key.setString("type", type);
        key.setInt("amount", item.getAmount());
        key.setInt("durability", item.getDurability());
        if (item.getData() != null) {
            key.setInt("mdata", item.getData().getData());
        }

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            serialiseMeta(key.getRelative("meta"), meta);
        } else
            key.removeKey("meta");

        key = key.getRelative("enchantments");
        for (Enchantment enchantment : item.getEnchantments().keySet())
            key.setInt(Integer.toString(enchantment.getId()), item.getEnchantmentLevel(enchantment));
    }

    private static void serialiseMeta(DataKey key, ItemMeta meta) {
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            DataKey root = key.getRelative("lore");
            for (int i = 0; i < lore.size(); i++) {
                root.setString(Integer.toString(i), lore.get(i));
            }
        } else
            key.removeKey("lore");

        if (meta.hasDisplayName()) {
            key.setString("displayname", meta.getDisplayName());
        } else
            key.removeKey("displayname");
        if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            DataKey pages = key.getRelative("book.pages");
            for (int i = 0; i < book.getPageCount(); i++) {
                pages.setString(Integer.toString(i), book.getPage(i));
            }
            key.setString("book.title", book.getTitle());
            key.setString("book.author", book.getAuthor());
        } else
            key.removeKey("book");

        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            key.setString("skull.owner", skull.getOwner());
        } else
            key.removeKey("skull");

        if (meta instanceof MapMeta) {
            MapMeta map = (MapMeta) meta;
            key.setBoolean("map.scaling", map.isScaling());
        } else
            key.removeKey("map");

        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta armor = (LeatherArmorMeta) meta;
            Color color = armor.getColor();
            key.setInt("armor.color", color.asRGB());
        } else
            key.removeKey("armor");

        if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;
            List<PotionEffect> effects = potion.getCustomEffects();
            DataKey effectKey = key.getRelative("potion.effects");
            for (int i = 0; i < effects.size(); i++) {
                PotionEffect effect = effects.get(i);
                DataKey sub = effectKey.getRelative(Integer.toString(i));
                sub.setBoolean("ambient", effect.isAmbient());
                sub.setInt("amplifier", effect.getAmplifier());
                sub.setInt("duration", effect.getDuration());
                sub.setString("type", effect.getType().getName());
            }
        } else
            key.removeKey("potion");
    }
}