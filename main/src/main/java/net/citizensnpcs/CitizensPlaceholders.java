package net.citizensnpcs;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.citizensnpcs.npc.NPCSelector;

public class CitizensPlaceholders extends PlaceholderExpansion {
    private final NPCSelector selector;

    public CitizensPlaceholders(NPCSelector selector) {
        this.selector = selector;
    }

    @Override
    public String getAuthor() {
        return "fullwall";
    }

    @Override
    public String getIdentifier() {
        return "citizensplaceholder";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.isOnline())
            return null;

        if (params.equalsIgnoreCase("citizens_selected_npc_name")) {
            return selector.getSelected((CommandSender) player).getFullName();
        }

        if (params.equalsIgnoreCase("citizens_selected_npc_id")) {
            return Integer.toString(selector.getSelected((CommandSender) player).getId());
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }
}