package net.citizensnpcs.api.util;

import java.util.regex.Pattern;

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
        text = text.replace("<owner>", npc.getTrait(Owner.class).getOwner());
        text = text.replace("<npc>", npc.getName());
        text = text.replace("<id>", Integer.toString(npc.getId()));
        return text;
    }

    public static String replace(String text, OfflinePlayer player) {
        if (player == null) {
            return setPlaceholderAPIPlaceholders(text, player);
        }
        text = PLAYER_MATCHER.matcher(text).replaceAll(player.getName());
        if (player instanceof Entity && ((Entity) player).isValid()) {
            text = text.replace("<world>", ((Entity) player).getWorld().getName());
        }
        return setPlaceholderAPIPlaceholders(text, player);
    }

    private static String setPlaceholderAPIPlaceholders(String text, OfflinePlayer player) {
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable t) {
            return text;
        }
    }

    private static Pattern PLAYER_MATCHER = Pattern.compile("<player>|<p>");
}
