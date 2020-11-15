package net.citizensnpcs.api.util;

import java.util.regex.Matcher;
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
        Matcher matcher = CITIZENS_PLACEHOLDERS.matcher(text);
        StringBuffer sb = null;
        while (matcher.find()) {
            if (sb == null) {
                sb = new StringBuffer(text.length());
            }
            String match = matcher.group(1);
            String replacement = null;
            if (match.equals("owner")) {
                replacement = npc.getOrAddTrait(Owner.class).getOwner();
            } else if (match.equals("npc")) {
                replacement = npc.getName();
            } else if (match.equals("id")) {
                replacement = Integer.toString(npc.getId());
            }
            matcher.appendReplacement(sb, replacement);
        }
        return sb == null ? text : matcher.appendTail(sb).toString();
    }

    public static String replace(String text, OfflinePlayer player) {
        if (player == null) {
            return setPlaceholderAPIPlaceholders(text, player);
        }
        text = PLAYER_MATCHER.matcher(text).replaceAll(player.getName());
        if (player instanceof Entity && ((Entity) player).isValid()) {
            text = WORLD_MATCHER.matcher(text).replaceAll(((Entity) player).getWorld().getName());
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

    private static Pattern CITIZENS_PLACEHOLDERS = Pattern.compile("<(owner|id|npc)>");
    private static boolean PLACEHOLDERAPI_ENABLED = true;
    private static Pattern PLAYER_MATCHER = Pattern.compile("<player>|<p>");
    private static Pattern WORLD_MATCHER = Pattern.compile("<world>");
}
