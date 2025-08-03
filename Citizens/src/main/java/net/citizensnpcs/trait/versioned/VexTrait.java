package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vex;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

@TraitName("vextrait")
public class VexTrait extends Trait {
    @Persist("charging")
    private Boolean charging;

    public VexTrait() {
        super("vextrait");
    }

    @Override
    public void run() {
        if (charging != null && npc.getEntity() instanceof Vex) {
            ((Vex) npc.getEntity()).setCharging(charging);
        }
    }

    private void setCharging(Boolean charging) {
        this.charging = charging;
    }

    @Command(
            aliases = { "npc" },
            usage = "vex (--charging [charging])",
            desc = "",
            modifiers = { "vex" },
            min = 1,
            max = 1,
            permission = "citizens.npc.vex")
    @Requirements(selected = true, ownership = true, types = { EntityType.VEX })
    public static void shulker(CommandContext args, CommandSender sender, NPC npc, @Flag("charging") Boolean charging)
            throws CommandException {
        VexTrait trait = npc.getOrAddTrait(VexTrait.class);
        boolean hasArg = false;
        if (charging != null) {
            trait.setCharging(charging);
            Messaging.sendTr(sender, Messages.VEX_CHARGING_SET, npc.getName(), charging);
            hasArg = true;
        }
        if (!hasArg)
            throw new CommandUsageException();
    }
}
