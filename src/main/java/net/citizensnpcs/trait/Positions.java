package net.citizensnpcs.trait;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Position;
import net.citizensnpcs.util.Util;

public class Positions extends Trait {
	private final List<Position> positions = new ArrayList<Position>();

	Position currentPosition = null;

	public Positions() {
		super("positions");
	}

	@Override
	public void load(DataKey key) throws NPCLoadException {
		for (DataKey sub : key.getRelative("list").getIntegerSubKeys())
			try {
				positions.add(new Position(sub.getString("").split(";")[0], Float.valueOf(sub.getString("").split(";")[1]), Float.valueOf(sub.getString("").split(";")[2]))) ;
			} catch(Exception e) { /* Perhaps remove the entry if bad? Warn console? */ }
	}

	@Override
	public void save(DataKey key) {
		for (int i = 0; i < 100; i++)
			key.removeKey(String.valueOf(i));
		key.removeKey("list");

		for (int i = 0; i < positions.size(); i++)
			key.setString("list." + String.valueOf(i), positions.get(i).stringValue());
	}

	public List<Position> getPositions() {
		return positions;
	}

	public boolean addPosition(String name, Location location) {
		Position newPosition = new Position(name, location.getPitch(), location.getYaw());

		if (positions.contains(newPosition)) return false;
		positions.add(newPosition);
		return true;
	}

	public boolean removePosition(Position position) {
		if (positions.contains(position)) {
			positions.remove(position);
			return true;
		}
		else return false;
	}

	public Position getPosition(String name) {
		for (Position position : positions) 
			if (position.name.equalsIgnoreCase(name)) return position;

		return null;
	}

	public void assumePosition(Position position) {

		if (!npc.isSpawned())
			npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());

		Util.assumePosition(npc.getBukkitEntity(), position);
	}


}
