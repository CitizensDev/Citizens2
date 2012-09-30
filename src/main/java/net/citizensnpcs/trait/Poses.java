package net.citizensnpcs.trait;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Pose;
import net.citizensnpcs.util.Util;

import org.bukkit.Location;

public class Poses extends Trait {
    private final List<Pose> poses = new ArrayList<Pose>();

    public Poses() {
        super("poses");
    }

    public boolean addPose(String name, Location location) {
        Pose newPose = new Pose(name, location.getPitch(), location.getYaw());
        if (poses.contains(newPose))
            return false;
        poses.add(newPose);
        return true;
    }

    public void assumePose(Pose pose) {
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());

        Util.assumePose(npc.getBukkitEntity(), pose);
    }

    public Pose getPose(String name) {
        for (Pose pose : poses)
            if (pose.getName().equalsIgnoreCase(name))
                return pose;
        return null;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        for (DataKey sub : key.getRelative("list").getIntegerSubKeys())
            try {
                String[] parts = sub.getString("").split(";");
                poses.add(new Pose(parts[0], Float.valueOf(parts[1]), Float.valueOf(parts[2])));
            } catch (NumberFormatException e) {
                Messaging.logTr(Messages.SKIPPING_INVALID_POSE, sub.name(), e.getMessage());
            }
    }

    public boolean removePose(Pose pose) {
        if (poses.contains(pose)) {
            poses.remove(pose);
            return true;
        }
        return false;
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("list");
        for (int i = 0; i < poses.size(); i++)
            key.setString("list." + String.valueOf(i), poses.get(i).stringValue());
    }
}
