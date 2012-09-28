package net.citizensnpcs.trait;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Pose;
import net.citizensnpcs.util.Util;

public class Poses extends Trait {
	private final List<Pose> poses = new ArrayList<Pose>();

	Pose currentPosition = null;

	public Poses() {
		super("poses");
	}

	@Override
	public void load(DataKey key) throws NPCLoadException {
		for (DataKey sub : key.getRelative("list").getIntegerSubKeys())
			try {
				poses.add(new Pose(sub.getString("").split(";")[0], Float.valueOf(sub.getString("").split(";")[1]), Float.valueOf(sub.getString("").split(";")[2]))) ;
			} catch(Exception e) { /* Perhaps remove the entry if bad? Warn console? */ }
	}

	@Override
	public void save(DataKey key) {
		key.removeKey("list");
		for (int i = 0; i < poses.size(); i++)
			key.setString("list." + String.valueOf(i), poses.get(i).stringValue());
	}

	public List<Pose> getPoses() {
		return poses;
	}

	public boolean addPose(String name, Location location) {
		Pose newPose = new Pose(name, location.getPitch(), location.getYaw());

		if (poses.contains(newPose)) return false;
		poses.add(newPose);
		return true;
	}

	public boolean removePose(Pose pose) {
		if (poses.contains(pose)) {
			poses.remove(pose);
			return true;
		}
		else return false;
	}

	public Pose getPose(String name) {
		for (Pose pose : poses) 
			if (pose.getName().equalsIgnoreCase(name)) return pose;

		return null;
	}

	public void assumePose(Pose pose) {

		if (!npc.isSpawned())
			npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());

		Util.assumePose(npc.getBukkitEntity(), pose);
	}


}
