package net.citizensnpcs.trait;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Messages;

/**
 * Persists a list of {@link Anchor}s.
 */
@TraitName("anchors")
public class Anchors extends Trait {
    private final List<Anchor> anchors = new ArrayList<>();

    public Anchors() {
        super("anchors");
    }

    public boolean addAnchor(String name, Location location) {
        Anchor newAnchor = new Anchor(name, location);
        if (anchors.contains(newAnchor))
            return false;
        anchors.add(newAnchor);
        return true;
    }

    @EventHandler
    private void checkWorld(WorldLoadEvent event) {
        for (Anchor anchor : anchors) {
            if (!anchor.isLoaded()) {
                anchor.load();
            }
        }
    }

    public Anchor getAnchor(String name) {
        for (Anchor anchor : anchors)
            if (anchor.getName().equalsIgnoreCase(name))
                return anchor;
        return null;
    }

    public List<Anchor> getAnchors() {
        return anchors;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        for (DataKey sub : key.getRelative("list").getIntegerSubKeys()) {
            String[] parts = sub.getString("").split(";");
            Location location;
            try {
                location = new Location(Bukkit.getServer().getWorld(parts[1]), Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                anchors.add(new Anchor(parts[0], location));
            } catch (NumberFormatException e) {
                Messaging.logTr(Messages.SKIPPING_INVALID_ANCHOR, sub.name(), e.getMessage());
            } catch (NullPointerException e) {
                // Invalid world/location/etc. Still enough data to build an
                // unloaded anchor
                anchors.add(new Anchor(parts[0], sub.getString("").split(";", 2)[1]));
            }
        }
    }

    public boolean removeAnchor(Anchor anchor) {
        if (anchors.contains(anchor)) {
            anchors.remove(anchor);
            return true;
        }
        return false;
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("list");
        for (int i = 0; i < anchors.size(); i++) {
            key.setString("list." + String.valueOf(i), anchors.get(i).stringValue());
        }
    }

}
