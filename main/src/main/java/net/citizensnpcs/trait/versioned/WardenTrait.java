package net.citizensnpcs.trait.versioned;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Warden;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.command.Arg;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;

@TraitName("wardentrait")
public class WardenTrait extends Trait {
    private final Map<UUID, Integer> anger = Maps.newHashMap();

    public WardenTrait() {
        super("wardentrait");
    }

    private void addAnger(Entity entity, int anger) {
        this.anger.put(entity.getUniqueId(), anger);
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Warden) {
            Warden warden = (Warden) npc.getEntity();
            for (Map.Entry<UUID, Integer> entry : anger.entrySet()) {
                warden.setAnger(Bukkit.getEntity(entry.getKey()), entry.getValue());
            }
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "warden dig|emerge|roar|anger [entity uuid/player name] [anger]",
            desc = "Sets warden modifiers",
            modifiers = { "warden" },
            min = 1,
            max = 4,
            permission = "citizens.npc.warden")
    @Requirements(selected = true, ownership = true, types = EntityType.WARDEN)
    public static void warden(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = { "anger", "dig", "emerge", "roar" }) String command, @Arg(2) String player,
            @Arg(3) Integer anger) throws CommandException {
        WardenTrait trait = npc.getOrAddTrait(WardenTrait.class);
        String output = "";
        if (command.equalsIgnoreCase("anger")) {
            if (anger == null)
                throw new CommandUsageException();
            Entity entity = null;
            try {
                UUID uuid = UUID.fromString(player);
                entity = Bukkit.getEntity(uuid);
            } catch (IllegalArgumentException iae) {
                entity = Bukkit.getOfflinePlayer(player).getPlayer();
            }
            if (entity != null) {
                trait.addAnger(entity, anger);
                output = Messaging.tr(Messages.WARDEN_ANGER_ADDED, entity, anger);
            }
        } else if (command.equalsIgnoreCase("dig")) {
            NMS.setWardenPose(npc.getEntity(), Pose.DIGGING);
            output = Messaging.tr(Messages.WARDEN_POSE_SET, npc.getName(), "dig");
        } else if (command.equalsIgnoreCase("emerge")) {
            NMS.setWardenPose(npc.getEntity(), Pose.EMERGING);
            output = Messaging.tr(Messages.WARDEN_POSE_SET, npc.getName(), "emerge");
        } else if (command.equalsIgnoreCase("roar")) {
            NMS.setWardenPose(npc.getEntity(), Pose.ROARING);
            output = Messaging.tr(Messages.WARDEN_POSE_SET, npc.getName(), "roar");
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
