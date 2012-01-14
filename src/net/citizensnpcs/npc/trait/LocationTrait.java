package net.citizensnpcs.npc.trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.npc.trait.Trait;

public class LocationTrait implements Trait {
	private Location loc;

	public LocationTrait(Location loc) {
		this.loc = loc;
	}

	@Override
	public String getName() {
		return "location";
	}

	@Override
	public void load(ConfigurationSection cs) {
		loc = new Location(Bukkit.getWorld(cs.getString("location.world")), cs.getDouble("location.x"),
				cs.getDouble("location.y"), cs.getDouble("location.z"), (float) cs.getDouble("location.pitch"),
				(float) cs.getDouble("location.yaw"));
	}

	@Override
	public void save(ConfigurationSection cs) {
		cs.set("location.world", loc.getWorld());
		cs.set("location.x", loc.getX());
		cs.set("location.y", loc.getY());
		cs.set("location.z", loc.getZ());
		cs.set("location.pitch", loc.getPitch());
		cs.set("location.yaw", loc.getYaw());
	}

	public Location getLocation() {
		return loc;
	}
}