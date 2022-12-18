package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Camel;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandMessages;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.MobType;
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
            usage = "camel (--pose pose) (--strength strength)",
            desc = "Sets camel modifiers",
            modifiers = { "camel" },
            min = 1,
            max = 1,
            permission = "citizens.npc.camel")
    @Requirements(selected = true, ownership = true)
    public static void camel(CommandContext args, CommandSender sender, NPC npc, @Flag("pose") CamelPose pose)
            throws CommandException {
        if (npc.getOrAddTrait(MobType.class).getType().name().equals("CAMEL"))
            throw new CommandException(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE);
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
