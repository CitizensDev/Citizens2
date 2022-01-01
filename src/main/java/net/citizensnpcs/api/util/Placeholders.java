package net.citizensnpcs.api.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;

public class Placeholders {
    public static String replace(String text, CommandSender sender, NPC npc) {
        text = replace(text, sender instanceof OfflinePlayer ? (OfflinePlayer) sender : null);
        if (npc == null) {
            return text;
        }
        for (int i = 0; i < CITIZENS_PLACEHOLDERS.length; i++) {
            text = text.replace(CITIZENS_PLACEHOLDERS[i], i == 0 ? Integer.toString(npc.getId())
                    : i == 1 ? npc.getName() : npc.getOrAddTrait(Owner.class).getOwner());
        }
        return text;
    }

    public static String replace(String text, OfflinePlayer player) {
        if (player == null) {
            return setPlaceholderAPIPlaceholders(text, player);
        }
        if (player instanceof Entity && ((Entity) player).isValid()) {
            for (int i = 0; i < PLAYER_WORLD_PLACEHOLDERS.length; i++) {
                text = text.replace(PLAYER_WORLD_PLACEHOLDERS[i],
                        i < 4 ? player.getName() : ((Entity) player).getWorld().getName());
            }
        } else {
            for (int i = 0; i < PLAYER_PLACEHOLDERS.length; i++) {
                text = text.replace(PLAYER_PLACEHOLDERS[i], player.getName());
            }
        }
        return setPlaceholderAPIPlaceholders(text, player);
    }

    private static String setPlaceholderAPIPlaceholders(String text, OfflinePlayer player) {
        if (!PLACEHOLDERAPI_ENABLED) {
            return text;
        }
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable t) {
            PLACEHOLDERAPI_ENABLED = false;
            return text;
        }
    }

    private static final String[] CITIZENS_PLACEHOLDERS = { "<id>", "<npc>", "<owner>" };
    private static boolean PLACEHOLDERAPI_ENABLED = true;
    private static final String[] PLAYER_PLACEHOLDERS = { "<player>", "<p>", "@p", "%player%" };
    private static final String[] PLAYER_WORLD_PLACEHOLDERS = { "<player>", "<p>", "@p", "%player%", "<world>" };
}
