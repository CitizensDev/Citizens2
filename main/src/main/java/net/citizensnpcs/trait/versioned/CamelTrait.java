package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Camel;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;

@TraitName("cameltrait")
public class CamelTrait extends Trait {
    @Persist
    private CamelPose pose;

    public CamelTrait() {
        super("cameltrait");
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Camel) {
            Camel camel = (Camel) npc.getEntity();
            if (pose != null) {
                NMS.setCamelPose(npc.getEntity(), pose);
            }
        }
    }

    public void setPose(CamelPose pose) {
        this.pose = pose;
    }

    public enum CamelPose {
        PANIC,
        SITTING,
        STANDING
    }

    @Command(
            aliases = { "npc" },
            usage = "camel (--pose pose)",
            desc = "Sets camel modifiers",
            modifiers = { "camel" },
            min = 1,
            max = 1,
            permission = "citizens.npc.camel")
    @Requirements(selected = true, ownership = true, types = EntityType.CAMEL)
    public static void camel(CommandContext args, CommandSender sender, NPC npc, @Flag("pose") CamelPose pose)
            throws CommandException {
        CamelTrait trait = npc.getOrAddTrait(CamelTrait.class);
        String output = "";

        if (pose != null) {
            trait.setPose(pose);
            output += Messaging.tr(Messages.CAMEL_POSE_SET, pose);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}
