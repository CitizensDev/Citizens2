package net.citizensnpcs;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
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

        NPC selected = selector.getSelected((CommandSender) player);
        switch (params) {
            case "citizens_selected_npc_name":
                return selected == null ? "" : selected.getFullName();
            case "citizens_selected_npc_id":
                return selected == null ? "" : Integer.toString(selected.getId());
            case "citizens_selected_npc_uuid":
                return selected == null ? "" : selected.getUniqueId().toString();
            case "citizens_nearest_npc_id":
                Location location = player.getPlayer().getLocation();

                Optional<NPC> closestNPC = player.getPlayer().getNearbyEntities(25, 25, 25).stream()
                        .map(CitizensAPI.getNPCRegistry()::getNPC).filter(e -> e != null && e.getEntity() != player)
                        .min((a, b) -> Double.compare(a.getEntity().getLocation().distanceSquared(location),
                                b.getEntity().getLocation().distanceSquared(location)));
                return closestNPC.isPresent() ? Integer.toString(closestNPC.get().getId()) : "";
        }
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }
}