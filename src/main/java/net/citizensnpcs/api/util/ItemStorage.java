package net.citizensnpcs.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.BaseEncoding;

import net.citizensnpcs.api.event.CitizensDeserialiseMetaEvent;
import net.citizensnpcs.api.event.CitizensSerialiseMetaEvent;

public class ItemStorage {
    private static void deserialiseMeta(DataKey root, ItemStack res, List<String> lore, String displayName) {
        if (root.keyExists("encoded-meta")) {
            root = root.getRelative("encoded-meta");
        }
        byte[] raw = BaseEncoding.base64().decode(root.getString(""));
        try {
            BukkitObjectInputStream inp = new BukkitObjectInputStream(new ByteArrayInputStream(raw));
            ItemMeta meta = (ItemMeta) inp.readObject();
            if (lore != null) {
                meta.setLore(lore);
            }
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            res.setItemMeta(meta);
            Bukkit.getPluginManager().callEvent(new CitizensDeserialiseMetaEvent(root, res));

            return;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack loadItemStack(DataKey root) {
        Material material = null;
        if (root.keyExists("type_key") && SpigotUtil.isUsing1_13API()) {
            NamespacedKey key = new NamespacedKey(root.getString("type_namespace", "minecraft"),
                    root.getString("type_key").toLowerCase(Locale.ROOT));
            material = Material.getMaterial(key.getKey().toUpperCase(Locale.ROOT), false);
        } else {
            String raw = root.getString("type", root.getString("id"));
            if (raw == null || raw.length() == 0)
                return null;
            if (SUPPORT_REGISTRY) {
                material = Registry.MATERIAL.get(SpigotUtil.getKey(raw));
            } else {
                material = SpigotUtil.isUsing1_13API() ? Material.getMaterial(raw, false) : Material.matchMaterial(raw);
            }
        }
        if (material == null || material == Material.AIR)
            return null;
        ItemStack res = new ItemStack(material, root.getInt("amount"),
                (short) root.getInt("durability", root.getInt("data", 0)));
        if (root.keyExists("mdata") && res.getData() != null) {
            res.getData().setData((byte) root.getInt("mdata"));
        }
        if (root.keyExists("meta")) {
            List<String> lore = null;
            if (root.keyExists("lore")) {
                lore = Splitter.on(CHAT_NEWLINE).splitToStream(root.getString("lore"))
                        .map(s -> Messaging.parseComponents(s)).collect(Collectors.toList());
            }
            String displayName = root.getString("displayname", null);
            deserialiseMeta(root.getRelative("meta"), res, lore, displayName);
        }
        return res;
    }

    public static void saveItem(DataKey key, ItemStack item) {
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        if (SpigotUtil.isUsing1_13API()) {
            key.setString("type",
                    item.getType().getKey().getNamespace().equals("minecraft") ? item.getType().getKey().getKey()
                            : item.getType().getKey().toString());
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
            if (meta.hasDisplayName()) {
                key.setString("displayname", meta.getDisplayName());
            } else {
                key.removeKey("displayname");
            }
            if (meta.hasLore()) {
                key.setString("lore", Joiner.on("<br>").join(meta.getLore()));
            } else {
                key.removeKey("lore");
            }
            serialiseMeta(key.getRelative("meta"), item.getType(), meta);
        } else {
            key.removeKey("meta");
        }
        key.removeKey("enchantments");
    }

    private static void serialiseMeta(DataKey key, Material material, ItemMeta meta) {
        key.removeKey("");
        ByteArrayOutputStream defOut = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream bukkitOut = new BukkitObjectOutputStream(out);
            bukkitOut.writeObject(meta);
            bukkitOut = new BukkitObjectOutputStream(defOut);
            bukkitOut.writeObject(Bukkit.getItemFactory().getItemMeta(material));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // spigot bug
            Messaging.severe("Spigot error when saving item meta: upgrade spigot");
            e.printStackTrace();
            return;
        }
        String defEncoded = BaseEncoding.base64().encode(defOut.toByteArray());
        String encoded = BaseEncoding.base64().encode(out.toByteArray());
        if (defEncoded.equals(encoded))
            return;
        key.setString("", encoded);
        Bukkit.getPluginManager().callEvent(new CitizensSerialiseMetaEvent(key, meta));
        return;
    }

    private static final Pattern CHAT_NEWLINE = Pattern.compile("<br>|\\n", Pattern.MULTILINE);

    private static boolean SUPPORT_REGISTRY = true;
    static {
        try {
            Class.forName("org.bukkit.Registry").getField("MATERIAL");
        } catch (Throwable e) {
            SUPPORT_REGISTRY = false;
        }
    }
}
