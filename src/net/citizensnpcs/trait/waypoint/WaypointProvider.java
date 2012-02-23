package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.ai.NavigationCallback;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.entity.Player;

public interface WaypointProvider {
    public Editor createEditor(Player player);

    public void load(DataKey key);

    public void save(DataKey key);

    public NavigationCallback getCallback();
}