package net.citizensnpcs;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

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
        return "citizens";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        NPC selected = player == null || !player.isOnline() ? selector.getSelected(Bukkit.getConsoleSender())
                : selector.getSelected(player.getPlayer());
        if (selected == null && !params.equals("nearest_npc_id"))
            return null;
        switch (params) {
            case "selected_npc_name":
                return selected == null ? "" : selected.getFullName();
            case "selected_npc_id":
                return selected == null ? "" : Integer.toString(selected.getId());
            case "selected_npc_uuid":
                return selected == null ? "" : selected.getUniqueId().toString();
            case "nearest_npc_id":
                if (player == null || !player.isOnline())
                    return null;
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
