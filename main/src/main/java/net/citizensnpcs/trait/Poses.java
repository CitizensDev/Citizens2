package net.citizensnpcs.trait;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Pose;

/**
 * Persists named {@link Pose}s.
 */
@TraitName("poses")
public class Poses extends Trait {
    @Persist
    private String defaultPose;
    private final Map<String, Pose> poses = Maps.newHashMap();

    public Poses() {
        super("poses");
    }

    /**
     * Add a {@link Pose}
     *
     * @return whether the pose has already been added
     */
    public boolean addPose(String name, Location location) {
        return addPose(name, location, false);
    }

    /**
     * Add a {@link Pose}
     *
     * @return whether the pose has already been added
     */
    public boolean addPose(String name, Location location, boolean isDefault) {
        name = name.toLowerCase();
        Pose newPose = new Pose(name, location.getPitch(), location.getYaw());
        if (poses.containsKey(name))
            return false;
        poses.put(name, newPose);
        if (isDefault) {
            defaultPose = name;
        }
        return true;
    }

    private void assumePose(float yaw, float pitch) {
        if (!npc.isSpawned()) {
            npc.spawn(npc.getStoredLocation(), SpawnReason.COMMAND);
        }
        npc.getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToHave(yaw, pitch);
    }

    /**
     * Sets the yaw/pitch to the supplied {@link Location}.
     */
    public void assumePose(Location location) {
        assumePose(location.getYaw(), location.getPitch());
    }

    /**
     * Sets the yaw/pitch to the stored pose, looked up by name.
     */
    public void assumePose(String flag) {
        if (flag == null)
            return;
        Pose pose = poses.get(flag.toLowerCase());
        assumePose(pose.getYaw(), pose.getPitch());
    }

    public void describe(CommandSender sender, int page) throws CommandException {
        Paginator paginator = new Paginator().header("Pose").console(sender instanceof ConsoleCommandSender);
        paginator.addLine("<green>ID  <yellow>Name  <red>Pitch/Yaw");
        int i = 0;
        for (Pose pose : poses.values()) {
            String line = "<green>" + i + "<yellow>  " + pose.getName() + "<red>  " + pose.getPitch() + " "
                    + pose.getYaw();
            paginator.addLine(line);
            i++;
        }
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
    }

    public Pose getPose(String name) {
        return name == null ? null : poses.get(name.toLowerCase());
    }

    public boolean hasPose(String pose) {
        if (pose == null)
            return false;
        return poses.containsKey(pose.toLowerCase());
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        poses.clear();
        for (DataKey sub : key.getRelative("list").getIntegerSubKeys()) {
            try {
                String[] parts = sub.getString("").split(";");
                poses.put(parts[0].toLowerCase(),
                        new Pose(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
            } catch (NumberFormatException e) {
                Messaging.logTr(Messages.SKIPPING_INVALID_POSE, sub.name(), e.getMessage());
            }
        }
    }

    public boolean removePose(String pose) {
        return pose == null ? false : poses.remove(pose.toLowerCase()) != null;
    }

    @Override
    public void run() {
        if (!hasPose(defaultPose) || npc.getNavigator().isNavigating())
            return;
        if (npc.hasTrait(LookClose.class)) {
            LookClose trait = npc.getOrAddTrait(LookClose.class);
            if (trait.isEnabled() && trait.canSeeTarget())
                return;
        }
        assumePose(defaultPose);
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

    public void setDefaultPose(String pose) {
        defaultPose = pose;
    }
}
