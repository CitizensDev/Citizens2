package net.citizensnpcs.api.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
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

import com.google.common.collect.Lists;

import net.citizensnpcs.api.event.CitizensDeserialiseMetaEvent;
import net.citizensnpcs.api.event.CitizensSerialiseMetaEvent;

public class ItemStorage {
    private static void deserialiseBanner(DataKey root, Banner meta) {
        meta.setBaseColor(DyeColor.valueOf(root.getString("banner.basecolor")));
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

    private static Map<Enchantment, Integer> deserialiseEnchantments(DataKey root, ItemStack res) {
        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
        for (DataKey subKey : root.getSubKeys()) {
            Enchantment enchantment = null;
            try {
                enchantment = Enchantment.getById(Integer.parseInt(subKey.name()));
            } catch (NumberFormatException ex) {
                enchantment = Enchantment.getByName(subKey.name());
            }
            if (enchantment != null && enchantment.canEnchantItem(res)) {
                int level = Math.min(subKey.getInt(""), enchantment.getMaxLevel());
                enchantments.put(enchantment, level);
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
        if (root.keyExists("flags")) {
            ItemMeta meta = ensureMeta(res);
            for (DataKey key : root.getRelative("flags").getIntegerSubKeys()) {
                meta.addItemFlags(ItemFlag.valueOf(key.getString("")));
            }
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
                meta.addStoredEnchant(Enchantment.getByName(key.name()), key.getInt(""), true);
            }
            res.setItemMeta(meta);
        }

        if (root.keyExists("skull")) {
            SkullMeta meta = ensureMeta(res);
            if (root.keyExists("skull.owner") && !root.getString("skull.owner").isEmpty()) {
                meta.setOwner(root.getString("skull.owner", ""));
            }
            res.setItemMeta(meta);
        }

        if (root.keyExists("banner")) {
            BannerMeta meta = ensureMeta(res);
            meta.setBaseColor(DyeColor.valueOf(root.getString("banner.basecolor")));
            if (root.keyExists("banner.patterns")) {
                for (DataKey sub : root.getRelative("banner.patterns").getIntegerSubKeys()) {
                    Pattern pattern = new Pattern(DyeColor.valueOf(sub.getString("color")),
                            PatternType.getByIdentifier(sub.getString("type")));
                    meta.addPattern(pattern);
                }
            }
        }

        if (root.keyExists("potion")) {
            PotionMeta meta = ensureMeta(res);
            PotionData data = new PotionData(PotionType.valueOf(root.getString("potion.data.type")),
                    root.getBoolean("potion.data.extended"), root.getBoolean("potion.data.upgraded"));
            meta.setBasePotionData(data);
            for (DataKey sub : root.getRelative("potion.effects").getIntegerSubKeys()) {
                int duration = sub.getInt("duration");
                int amplifier = sub.getInt("amplifier");
                PotionEffectType type = PotionEffectType.getByName(sub.getString("type"));
                boolean ambient = sub.getBoolean("ambient");
                meta.addCustomEffect(new PotionEffect(type, duration, amplifier, ambient), true);
            }
            res.setItemMeta(meta);
        }

        if (root.keyExists("repaircost") && res.getItemMeta() instanceof Repairable) {
            ((Repairable) res.getItemMeta()).setRepairCost(root.getInt("repaircost"));
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
        String raw = root.getString("type", root.getString("id"));
        if (raw == null || raw.length() == 0) {
            return null;
        }
        Material material = Material.matchMaterial(raw);
        if (material == null || material == Material.AIR) {
            return null;
        }
        ItemStack res = new ItemStack(material, root.getInt("amount"),
                (short) (root.getInt("durability", root.getInt("data", 0))));
        if (root.keyExists("mdata") && res.getData() != null) {
            res.getData().setData((byte) root.getInt("mdata"));
        }

        if (root.keyExists("enchantments")) {
            Map<Enchantment, Integer> enchantments = deserialiseEnchantments(root.getRelative("enchantments"), res);
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
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        migrateForSave(key);
        key.setString("type", item.getType().name());
        key.setInt("amount", item.getAmount());
        key.setInt("durability", item.getDurability());
        if (item.getData() != null) {
            key.setInt("mdata", item.getData().getData());
        }

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            serialiseMeta(key.getRelative("meta"), meta);
        } else {
            key.removeKey("meta");
        }
        serialiseEnchantments(key.getRelative("enchantments"), item.getEnchantments());
    }

    private static void serialiseBanner(DataKey root, Banner banner) {
        root.setString("basecolor", banner.getBaseColor().name());
        List<org.bukkit.block.banner.Pattern> patterns = banner.getPatterns();
        root.removeKey("patterns");
        for (int i = 0; i < patterns.size(); i++) {
            org.bukkit.block.banner.Pattern pattern = patterns.get(i);
            DataKey sub = root.getRelative("patterns." + i);
            sub.setString("color", pattern.getColor().name());
            sub.setString("type", pattern.getPattern().getIdentifier());
        }
    }

    private static void serialiseColors(DataKey key, List<Color> colors) {
        for (int i = 0; i < colors.size(); i++) {
            Color color = colors.get(i);
            key.getRelative(i).setInt("rgb", color.asRGB());
        }
    }

    private static void serialiseEnchantments(DataKey key, Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
            key.setInt(enchantment.getKey().getName(), enchantment.getValue());
        }
    }

    private static void serialiseFireworkEffect(DataKey key, FireworkEffect effect) {
        key.setBoolean("trail", effect.hasTrail());
        key.setBoolean("flicker", effect.hasFlicker());
        key.setString("type", effect.getType().name());
        serialiseColors(key.getRelative("colors"), effect.getColors());
        serialiseColors(key.getRelative("fadecolors"), effect.getFadeColors());
    }

    private static void serialiseMeta(DataKey key, ItemMeta meta) {
        key.removeKey("flags");
        int j = 0;
        for (ItemFlag flag : ItemFlag.values()) {
            if (meta.hasItemFlag(flag)) {
                key.setString("flags." + j++, flag.name());
            }
        }
        if (meta instanceof Repairable) {
            Repairable rep = (Repairable) meta;
            key.setInt("repaircost", rep.getRepairCost());
        } else {
            key.removeKey("repaircost");
        }
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            DataKey root = key.getRelative("lore");
            for (int i = 0; i < lore.size(); i++) {
                root.setString(Integer.toString(i), lore.get(i));
            }
        } else {
            key.removeKey("lore");
        }

        if (meta.hasDisplayName()) {
            key.setString("displayname", meta.getDisplayName());
        } else {
            key.removeKey("displayname");
        }

        if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            DataKey pages = key.getRelative("book.pages");
            for (int i = 1; i <= book.getPageCount(); i++) {
                pages.setString(Integer.toString(i), book.getPage(i));
            }
            key.setString("book.title", book.getTitle());
            key.setString("book.author", book.getAuthor());
            serialiseEnchantments(key.getRelative("book.enchantments"), book.getEnchants());
        } else {
            key.removeKey("book");
        }

        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            key.setString("skull.owner", skull.getOwner());
        } else {
            key.removeKey("skull");
        }

        if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            int i = 0;
            for (FireworkEffect effect : firework.getEffects()) {
                serialiseFireworkEffect(key.getRelative("firework.effects." + i), effect);
                i++;
            }
            key.setInt("firework.power", firework.getPower());
        } else {
            key.removeKey("firework");
        }

        if (meta instanceof MapMeta) {
            MapMeta map = (MapMeta) meta;
            key.setBoolean("map.scaling", map.isScaling());
        } else {
            key.removeKey("map");
        }

        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta armor = (LeatherArmorMeta) meta;
            Color color = armor.getColor();
            key.setInt("armor.color", color.asRGB());
        } else {
            key.removeKey("armor");
        }

        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta ench = (EnchantmentStorageMeta) meta;
            for (Map.Entry<Enchantment, Integer> e : ench.getStoredEnchants().entrySet()) {
                key.getRelative("enchantmentstorage").setInt(e.getKey().getName(), e.getValue());
            }
        } else {
            key.removeKey("enchantmentstorage");
        }

        if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;
            PotionData data = potion.getBasePotionData();
            List<PotionEffect> effects = potion.getCustomEffects();
            key.setBoolean("potion.data.extended", data.isExtended());
            key.setBoolean("potion.data.upgraded", data.isUpgraded());
            key.setString("potion.data.type", data.getType().name());
            key.removeKey("potion.effects");
            DataKey effectKey = key.getRelative("potion.effects");
            for (int i = 0; i < effects.size(); i++) {
                PotionEffect effect = effects.get(i);
                DataKey sub = effectKey.getRelative(Integer.toString(i));
                sub.setBoolean("ambient", effect.isAmbient());
                sub.setInt("amplifier", effect.getAmplifier());
                sub.setInt("duration", effect.getDuration());
                sub.setString("type", effect.getType().getName());
            }
        } else {
            key.removeKey("potion");
        }

        key.removeKey("blockstate");
        if (meta instanceof BlockStateMeta) {
            BlockStateMeta state = (BlockStateMeta) meta;
            if (state.hasBlockState()) {
                DataKey root = key.getRelative("blockstate");
                BlockState blockstate = state.getBlockState();
                if (blockstate instanceof Banner) {
                    Banner banner = (Banner) blockstate;
                    serialiseBanner(root.getRelative("banner"), banner);
                } else {
                    root.removeKey("banner");
                }
            }
        }

        if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            DataKey root = key.getRelative("banner");
            root.setString("basecolor", banner.getBaseColor().name());
            List<org.bukkit.block.banner.Pattern> patterns = banner.getPatterns();
            root.removeKey("patterns");
            for (int i = 0; i < patterns.size(); i++) {
                org.bukkit.block.banner.Pattern pattern = patterns.get(i);
                DataKey sub = root.getRelative("patterns." + i);
                sub.setString("color", pattern.getColor().name());
                sub.setString("type", pattern.getPattern().getIdentifier());
            }
        } else {
            key.removeKey("banner");
        }

        Bukkit.getPluginManager().callEvent(new CitizensSerialiseMetaEvent(key, meta));
    }
}