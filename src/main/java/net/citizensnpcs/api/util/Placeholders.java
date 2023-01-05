package net.citizensnpcs.api.util;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;

public class Placeholders {
    private static OfflinePlayer getPlayer(BlockCommandSender sender) {
        return CitizensAPI.getNMSHelper().getPlayer(sender);
    }

    public static String replace(String text, CommandSender sender, NPC npc) {
        text = replace(text, sender instanceof OfflinePlayer ? (OfflinePlayer) sender
                : sender instanceof BlockCommandSender ? getPlayer((BlockCommandSender) sender) : null);
        if (npc == null || text == null) {
            return text;
        }
        StringBuffer out = new StringBuffer();
        Matcher matcher = PLACEHOLDER_MATCHER.matcher(text);
        while (matcher.find()) {
            String replacement = "";
            String group = matcher.group(1);
            if (group.equals("id")) {
                replacement = Integer.toString(npc.getId());
            } else if (group.equals("npc")) {
                replacement = npc.getName();
            } else if (group.equals("owner")) {
                replacement = npc.getOrAddTrait(Owner.class).getOwner();
            }
            matcher.appendReplacement(out, "");
            out.append(replacement);
        }
        matcher.appendTail(out);
        return out.toString();
    }

    public static String replace(String text, OfflinePlayer player) {
        if (player == null || (!player.isOnline() && !player.hasPlayedBefore())) {
            return setPlaceholderAPIPlaceholders(text, player);
        }
        if (text == null) {
            return text;
        }
        if (player.getPlayer() != null) {
            StringBuffer out = new StringBuffer();
            Matcher matcher = PLAYER_PLACEHOLDER_MATCHER.matcher(text);
            while (matcher.find()) {
                String replacement = "";
                String group = matcher.group(1);
                if (PLAYER_VARIABLES.contains(group)) {
                    replacement = player.getName();
                } else if (group.equals("<random_player>")) {
                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
                    Player possible = Iterables.get(players, new Random().nextInt(players.size()), null);
                    if (possible != null) {
                        replacement = possible.getName();
                    }
                } else if (group.equals("<random_npc>")) {
                    List<NPC> all = Lists.newArrayList(CitizensAPI.getNPCRegistry());
                    if (all.size() > 0) {
                        replacement = all.get(new Random().nextInt(all.size())).getName();
                    }
                } else if (group.equals("<random_npc_id>")) {
                    List<NPC> all = Lists.newArrayList(CitizensAPI.getNPCRegistry());
                    if (all.size() > 0) {
                        replacement = Integer.toString(all.get(new Random().nextInt(all.size())).getId());
                    }
                } else if (group.equals("<nearest_player>")) {
                    double min = Double.MAX_VALUE;
                    Player closest = null;
                    for (Player entity : CitizensAPI.getLocationLookup()
                            .getNearbyPlayers(player.getPlayer().getLocation(), 25)) {
                        if (entity == player || CitizensAPI.getNPCRegistry().isNPC(entity))
                            continue;
                        Location location = entity.getLocation();
                        double dist = location.distanceSquared(player.getPlayer().getLocation());
                        if (dist > min)
                            continue;
                        min = dist;
                        closest = entity;
                    }
                    if (closest != null) {
                        replacement = closest.getName();
                    }
                } else if (group.equals("<world>")) {
                    replacement = player.getPlayer().getWorld().getName();
                }
                matcher.appendReplacement(out, "");
                out.append(replacement);
            }
            matcher.appendTail(out);
            text = out.toString();
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

    private static final Pattern PLACEHOLDER_MATCHER = Pattern.compile("<(id|npc|owner)>");
    private static boolean PLACEHOLDERAPI_ENABLED = true;
    private static final Pattern PLAYER_PLACEHOLDER_MATCHER = Pattern.compile(
            "(<player>|<p>|@p|%player%|<random_player>|<random_npc>|<random_npc_id>|<nearest_player>|<world>)");
    private static final String[] PLAYER_PLACEHOLDERS = { "<player>", "<p>", "@p", "%player%" };
    private static final Collection<String> PLAYER_VARIABLES = ImmutableList.of("<player>", "<p>", "@p", "%player%");
}
