package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowman;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

@TraitName("snowmantrait")
public class SnowmanTrait extends Trait {
    @Persist("derp")
    private boolean derp;
    @Persist
    private boolean formSnow;

    public SnowmanTrait() {
        super("snowmantrait");
    }

    public boolean isDerp() {
        return derp;
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Snowman) {
            ((Snowman) npc.getEntity()).setDerp(derp);
        }
    }

    public void setDerp(boolean derp) {
        this.derp = derp;
    }

    public void setFormSnow(boolean snow) {
        formSnow = snow;
    }

    public boolean shouldFormSnow() {
        return formSnow;
    }

    public boolean toggleDerp() {
        return derp = !derp;
    }

    @Command(
            aliases = { "npc" },
            usage = "snowman (-d[erp]) (-f[orm snow])",
            desc = "",
            modifiers = { "snowman" },
            min = 1,
            max = 1,
            flags = "df",
            permission = "citizens.npc.snowman")
    @Requirements(selected = true, ownership = true, types = { EntityType.SNOWMAN })
    public static void snowman(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        SnowmanTrait trait = npc.getOrAddTrait(SnowmanTrait.class);
        boolean hasArg = false;
        if (args.hasFlag('d')) {
            boolean isDerp = trait.toggleDerp();
            Messaging.sendTr(sender, isDerp ? Messages.SNOWMAN_DERP_SET : Messages.SNOWMAN_DERP_STOPPED, npc.getName());
            hasArg = true;
        }
        if (args.hasFlag('f')) {
            trait.setFormSnow(!trait.shouldFormSnow());
            Messaging.sendTr(sender,
                    trait.shouldFormSnow() ? Messages.SNOWMAN_FORM_SNOW_SET : Messages.SNOWMAN_FORM_SNOW_STOPPED,
                    npc.getName());
            hasArg = true;
        }
        if (!hasArg)
            throw new CommandUsageException();
    }
}
