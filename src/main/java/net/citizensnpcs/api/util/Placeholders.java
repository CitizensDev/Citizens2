package net.citizensnpcs.api.util;

import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;

public class Placeholders {
    public static String replace(String text, CommandSender sender, NPC npc) {
        if (sender instanceof Player) {
            text = replace(text, (Player) sender);
        }
        if (npc == null) {
            return text;
        }
        text = text.replace("<owner>", npc.getTrait(Owner.class).getOwner());
        text = text.replace("<npc>", npc.getName());
        text = text.replace("<id>", Integer.toString(npc.getId()));
        return text;
    }

    public static String replace(String text, Player player) {
        if (player == null) {
            return text;
        }
        text = PLAYER_MATCHER.matcher(text).replaceAll(player.getName());
        text = text.replace("<world>", player.getWorld().getName());
        try {
            PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable t) {
        }
        return text;
    }

    private static Pattern PLAYER_MATCHER = Pattern.compile("<player>|<p>");
}
