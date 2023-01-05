package net.citizensnpcs.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensDeserialiseMetaEvent;
import net.citizensnpcs.api.event.CitizensSerialiseMetaEvent;

public class ItemStorage {
    private static void deserialiseBanner(DataKey root, Banner meta) {
        if (root.keyExists("banner.basecolor")) {
            meta.setBaseColor(DyeColor.valueOf(root.getString("banner.basecolor")));
        }
        if (root.keyExists("banner.patterns")) {
            for (DataKey sub : root.getRelative("banner.patterns").getIntegerSubKeys()) {
                Pattern pattern = new Pattern(DyeColor.valueOf(sub.getString("color")),
                        PatternType.getByIdentifier(sub.getString("type")));
                meta.addPattern(pattern);
            }
        }
    }

    private static Iterable<Color> deserialiseColors(DataKey key) {
        List<Color> colors = Lists.newArrayList();
        for (DataKey sub : key.getIntegerSubKeys()) {
            colors.add(Color.fromRGB(sub.getInt("rgb")));
        }
        return colors;
    }

    private static Enchantment deserialiseEnchantment(String string) {
        Enchantment enchantment = null;
        if (SpigotUtil.isUsing1_13API()) {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(string.toLowerCase()));
        }
        if (enchantment == null) {
            enchantment = Enchantment.getByName(string);
        }
        return enchantment;
    }

    private static Map<Enchantment, Integer> deserialiseEnchantments(DataKey root, ItemStack res) {
        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
        for (DataKey subKey : root.getSubKeys()) {
            Enchantment enchantment = deserialiseEnchantment(subKey.name());
            if (enchantment != null) {
                enchantments.put(enchantment, subKey.getInt(""));
            }
        }
        return enchantments;
    }

    private static FireworkEffect deserialiseFireworkEffect(DataKey key) {
        FireworkEffect.Builder effect = FireworkEffect.builder();
        effect.flicker(key.getBoolean("flicker"));
        effect.trail(key.getBoolean("trail"));
        effect.with(FireworkEffect.Type.valueOf(key.getString("type")));
        effect.withColor(deserialiseColors(key.getRelative("colors")));
        effect.withFade(deserialiseColors(key.getRelative("fadecolors")));
        return effect.build();
    }

    private static void deserialiseMeta(DataKey root, ItemStack res) {
        if (root.keyExists("encoded-meta")) {
            byte[] raw = BaseEncoding.base64().decode(root.getString("encoded-meta"));
            try {
                BukkitObjectInputStream inp = new BukkitObjectInputStream(new ByteArrayInputStream(raw));
                ItemMeta meta = (ItemMeta) inp.readObject();
                res.setItemMeta(meta);
                Bukkit.getPluginManager().callEvent(new CitizensDeserialiseMetaEvent(root, res));

                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (SUPPORTS_CUSTOM_MODEL_DATA) {
            try {
                if (root.keyExists("custommodel")) {
                    ItemMeta meta = ensureMeta(res);
                    meta.setCustomModelData(root.getInt("custommodel"));
                    res.setItemMeta(meta);
                }
            } catch (Throwable t) {
                SUPPORTS_CUSTOM_MODEL_DATA = false;
            }
        }
        if (root.keyExists("flags")) {
            ItemMeta meta = ensureMeta(res);
            for (DataKey key : root.getRelative("flags").getIntegerSubKeys()) {
                meta.addItemFlags(ItemFlag.valueOf(key.getString("")));
            }
            res.setItemMeta(meta);
        }
        if (root.keyExists("lore")) {
            ItemMeta meta = ensureMeta(res);
            List<String> lore = Lists.newArrayList();
            for (DataKey key : root.getRelative("lore").getIntegerSubKeys())
                lore.add(key.getString(""));
            meta.setLore(lore);
            res.setItemMeta(meta);
        }
        if (root.keyExists("displayname")) {
            ItemMeta meta = ensureMeta(res);
            meta.setDisplayName(root.getString("displayname"));
            res.setItemMeta(meta);
        }
        if (root.keyExists("firework")) {
            FireworkMeta meta = ensureMeta(res);
            for (DataKey sub : root.getRelative("firework.effects").getIntegerSubKeys()) {
                meta.addEffect(deserialiseFireworkEffect(sub));
            }
            meta.setPower(root.getInt("firework.power"));
            res.setItemMeta(meta);
        }
        if (root.keyExists("book")) {
            BookMeta meta = ensureMeta(res);
            for (DataKey sub : root.getRelative("book.pages").getIntegerSubKeys()) {
                meta.addPage(sub.getString(""));
            }
            meta.setTitle(root.getString("book.title"));
            meta.setAuthor(root.getString("book.author"));
            res.setItemMeta(meta);
        }
        if (root.keyExists("armor")) {
            LeatherArmorMeta meta = ensureMeta(res);
            meta.setColor(Color.fromRGB(root.getInt("armor.color")));
            res.setItemMeta(meta);
        }
        if (root.keyExists("map")) {
            MapMeta meta = ensureMeta(res);
            meta.setScaling(root.getBoolean("map.scaling"));
            res.setItemMeta(meta);
        }
        if (root.keyExists("blockstate")) {
            BlockStateMeta meta = ensureMeta(res);
            if (root.keyExists("blockstate.banner")) {
                Banner banner = (Banner) meta.getBlockState();
                deserialiseBanner(root.getRelative("blockstate"), banner);
                banner.update(true);
                meta.setBlockState(banner);
            }
            res.setItemMeta(meta);
        }

        if (root.keyExists("enchantmentstorage")) {
            EnchantmentStorageMeta meta = ensureMeta(res);
            for (DataKey key : root.getRelative("enchantmentstorage").getSubKeys()) {
                meta.addStoredEnchant(deserialiseEnchantment(key.name()), key.getInt(""), true);
            }
            res.setItemMeta(meta);
        }

        if (root.keyExists("skull")) {
            SkullMeta meta = ensureMeta(res);
            if (SUPPORT_OWNING_PLAYER) {
                try {
                    meta.getOwningPlayer();
                } catch (Throwable t) {
                    SUPPORT_OWNING_PLAYER = false;
                }
            }

            if (root.keyExists("skull.uuid") && SUPPORT_OWNING_PLAYER) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(root.getString("skull.uuid")));
                meta.setOwningPlayer(offlinePlayer);
            } else if (root.keyExists("skull.owner") && !root.getString("skull.owner").isEmpty()) {
                meta.setOwner(root.getString("skull.owner", ""));
            }

            if (root.keyExists("skull.texture") && !root.getString("skull.texture").isEmpty()) {
                CitizensAPI.getNMSHelper().setTexture(root.getString("skull.texture", ""), meta);
            }

            res.setItemMeta(meta);
        }

        if (root.keyExists("banner")) {
            BannerMeta meta = ensureMeta(res);
            if (root.keyExists("banner.basecolor")) {
                meta.setBaseColor(DyeColor.valueOf(root.getString("banner.basecolor")));
            }
            if (root.keyExists("banner.patterns")) {
                for (DataKey sub : root.getRelative("banner.patterns").getIntegerSubKeys()) {
                    Pattern pattern = new Pattern(DyeColor.valueOf(sub.getString("color")),
                            PatternType.getByIdentifier(sub.getString("type")));
                    meta.addPattern(pattern);
                }
            }
            res.setItemMeta(meta);
        }

        if (root.keyExists("potion")) {
            PotionMeta meta = ensureMeta(res);
            try {
                PotionData data = new PotionData(PotionType.valueOf(root.getString("potion.data.type")),
                        root.getBoolean("potion.data.extended"), root.getBoolean("potion.data.upgraded"));
                meta.setBasePotionData(data);
            } catch (Throwable t) {
            }
            for (DataKey sub : root.getRelative("potion.effects").getIntegerSubKeys()) {
                int duration = sub.getInt("duration");
                int amplifier = sub.getInt("amplifier");
                PotionEffectType type = PotionEffectType.getByName(sub.getString("type"));
                boolean ambient = sub.getBoolean("ambient");
                meta.addCustomEffect(new PotionEffect(type, duration, amplifier, ambient), true);
            }
            res.setItemMeta(meta);
        }

        if (root.keyExists("crossbow") && SUPPORTS_1_14_API) {
            CrossbowMeta meta = null;
            try {
                meta = ensureMeta(res);
            } catch (Throwable t) {
                SUPPORTS_1_14_API = false;
                // old MC version
            }
            if (meta != null) {
                for (DataKey key : root.getRelative("crossbow.projectiles").getSubKeys()) {
                    meta.addChargedProjectile(ItemStorage.loadItemStack(key));
                }
                res.setItemMeta(meta);
            }
        }

        if (root.keyExists("repaircost") && res.getItemMeta() instanceof Repairable) {
            ItemMeta meta = ensureMeta(res);
            ((Repairable) meta).setRepairCost(root.getInt("repaircost"));
            res.setItemMeta(meta);
        }

        if (root.keyExists("attributes") && SUPPORTS_ATTRIBUTES) {
            ItemMeta meta = ensureMeta(res);
            try {
                for (DataKey attr : root.getRelative("attributes").getSubKeys()) {
                    Attribute attribute = Attribute.valueOf(attr.name());
                    for (DataKey modifier : attr.getIntegerSubKeys()) {
                        UUID uuid = UUID.fromString(modifier.getString("uuid"));
                        String name = modifier.getString("name");
                        double amount = modifier.getDouble("amount");
                        Operation operation = Operation.valueOf(modifier.getString("operation"));
                        EquipmentSlot slot = modifier.keyExists("slot")
                                ? EquipmentSlot.valueOf(modifier.getString("slot"))
                                : null;
                        meta.addAttributeModifier(attribute,
                                new AttributeModifier(uuid, name, amount, operation, slot));
                    }
                }
            } catch (Throwable e) {
                SUPPORTS_ATTRIBUTES = false;
            }
            res.setItemMeta(meta);
        }

        ItemMeta meta = res.getItemMeta();
        if (meta != null) {
            try {
                meta.setUnbreakable(root.getBoolean("unbreakable", false));
            } catch (Throwable t) {
                // probably backwards-compat issue, don't log
            }
            res.setItemMeta(meta);
        }

        Bukkit.getPluginManager().callEvent(new CitizensDeserialiseMetaEvent(root, res));
    }

    @SuppressWarnings("unchecked")
    private static <T extends ItemMeta> T ensureMeta(ItemStack stack) {
        if (!stack.hasItemMeta()) {
            stack.setItemMeta(Bukkit.getServer().getItemFactory().getItemMeta(stack.getType()));
        }
        return (T) stack.getItemMeta();
    }

    public static ItemStack loadItemStack(DataKey root) {
        Material material = null;
        if (root.keyExists("type_key") && SpigotUtil.isUsing1_13API()) {
            NamespacedKey key = new NamespacedKey(root.getString("type_namespace", "minecraft"),
                    root.getString("type_key"));
            material = Material.getMaterial(key.getKey().toUpperCase(), false);
        } else {
            String raw = root.getString("type", root.getString("id"));
            if (raw == null || raw.length() == 0) {
                return null;
            }
            material = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(raw, true) : Material.matchMaterial(raw);
        }
        if (material == null || material == Material.AIR) {
            return null;
        }
        ItemStack res = new ItemStack(material, root.getInt("amount"),
                (short) (root.getInt("durability", root.getInt("data", 0))));
        if (root.keyExists("mdata") && res.getData() != null) {
            res.getData().setData((byte) root.getInt("mdata")); // TODO: what to migrate to?
        }

        if (root.keyExists("enchantments")) {
            Map<Enchantment, Integer> enchantments = deserialiseEnchantments(root.getRelative("enchantments"), res);
            res.addUnsafeEnchantments(enchantments);
        }
        deserialiseMeta(root.getRelative("meta"), res);
        return res;
    }

    private static void migrateForSave(DataKey key) {
        key.removeKey("data");
        key.removeKey("id");
    }

    public static void saveItem(DataKey key, ItemStack item) {
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        migrateForSave(key);
        if (SpigotUtil.isUsing1_13API()) {
            if (!item.getType().getKey().getNamespace().equals("minecraft")) {
                key.setString("type_namespace", item.getType().getKey().getNamespace());
            } else {
                key.removeKey("type_namespace");
            }
            key.setString("type_key", item.getType().getKey().getKey());
        } else {
            key.setString("type", item.getType().name());
        }
        key.setInt("amount", item.getAmount());
        if (item.getDurability() != 0) {
            key.setInt("durability", item.getDurability());
        } else {
            key.removeKey("durability");
        }

        if (!SpigotUtil.isUsing1_13API() && item.getData() != null) {
            key.setInt("mdata", item.getData().getData());
        } else {
            key.removeKey("mdata");
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            serialiseMeta(key.getRelative("meta"), meta);
        } else {
            key.removeKey("meta");
        }
        serialiseEnchantments(key.getRelative("enchantments"), item.getEnchantments());
    }

    private static void serialiseEnchantments(DataKey key, Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
            key.setInt(SpigotUtil.isUsing1_13API() ? enchantment.getKey().getKey().getKey()
                    : enchantment.getKey().getName(), enchantment.getValue());
        }
    }

    private static void serialiseMeta(DataKey key, ItemMeta meta) {
        key.removeKey("encoded-meta");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BukkitObjectOutputStream bukkitOut;
        try {
            bukkitOut = new BukkitObjectOutputStream(out);
            bukkitOut.writeObject(meta);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // spigot bug
            Messaging.severe("Spigot error when saving item meta: upgrade spigot");
            e.printStackTrace();
            return;
        }
        String encoded = BaseEncoding.base64().encode(out.toByteArray());
        key.setString("encoded-meta", encoded);
        Bukkit.getPluginManager().callEvent(new CitizensSerialiseMetaEvent(key, meta));
        return;
    }

    private static boolean SUPPORT_OWNING_PLAYER = true;
    private static boolean SUPPORTS_1_14_API = true;
    private static boolean SUPPORTS_ATTRIBUTES = true;
    private static boolean SUPPORTS_CUSTOM_MODEL_DATA = true;
}
