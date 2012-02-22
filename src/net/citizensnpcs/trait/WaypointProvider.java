package net.citizensnpcs.trait;

import net.citizensnpcs.api.npc.ai.NavigationCallback;
import net.citizensnpcs.editor.Editor;

import org.bukkit.entity.Player;

public interface WaypointProvider {
    public Editor createEditor(Player player);

    public void addWaypoint(Waypoint waypoint);

    public NavigationCallback getCallback();
}
