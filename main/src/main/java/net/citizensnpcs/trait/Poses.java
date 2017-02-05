package net.citizensnpcs.trait;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Pose;
import net.citizensnpcs.util.Util;

@TraitName("poses")
public class Poses extends Trait {
    private final Map<String, Pose> poses = Maps.newHashMap();

    public Poses() {
        super("poses");
    }

    public boolean addPose(String name, Location location) {
        name = name.toLowerCase();
        Pose newPose = new Pose(name, location.getPitch(), location.getYaw());
        if (poses.containsValue(newPose) || poses.containsKey(name))
            return false;
        poses.put(name, newPose);
        return true;
    }

    private void assumePose(float yaw, float pitch) {
        if (!npc.isSpawned()) {
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        }
        Util.assumePose(npc.getEntity(), yaw, pitch);
    }

    public void assumePose(Location location) {
        assumePose(location.getYaw(), location.getPitch());
    }

    public void assumePose(String flag) {
        Pose pose = poses.get(flag.toLowerCase());
        assumePose(pose.getYaw(), pose.getPitch());
    }

    public void describe(CommandSender sender, int page) throws CommandException {
        Paginator paginator = new Paginator().header("Pose");
        paginator.addLine("<e>Key: <a>ID  <b>Name  <c>Pitch/Yaw");
        int i = 0;
        for (Pose pose : poses.values()) {
            String line = "<a>" + i + "<b>  " + pose.getName() + "<c>  " + pose.getPitch() + "/" + pose.getYaw();
            paginator.addLine(line);
            i++;
        }

        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING);
    }

    public Pose getPose(String name) {
        for (Pose pose : poses.values()) {
            if (pose.getName().equalsIgnoreCase(name)) {
                return pose;
            }
        }
        return null;
    }

    public boolean hasPose(String pose) {
        return poses.containsKey(pose.toLowerCase());
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        poses.clear();
        for (DataKey sub : key.getRelative("list").getIntegerSubKeys())
            try {
                String[] parts = sub.getString("").split(";");
                poses.put(parts[0], new Pose(parts[0], Float.valueOf(parts[1]), Float.valueOf(parts[2])));
            } catch (NumberFormatException e) {
                Messaging.logTr(Messages.SKIPPING_INVALID_POSE, sub.name(), e.getMessage());
            }
    }

    public boolean removePose(String pose) {
        return poses.remove(pose.toLowerCase()) != null;
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("list");
        int i = 0;
        for (Pose pose : poses.values()) {
            key.setString("list." + i, pose.stringValue());
            i++;
        }
    }
}
