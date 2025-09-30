package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.CopperGolem.CopperWeatherState;
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
import net.citizensnpcs.util.Util;

@TraitName("coppergolemtrait")
public class CopperGolemTrait extends Trait {
    @Persist
    private CopperWeatherState weather;

    public CopperGolemTrait() {
        super("coppergolemtrait");
    }

    @Override
    public void run() {
        if (weather != null && npc.getEntity() instanceof CopperGolem) {
            CopperGolem golem = (CopperGolem) npc.getEntity();
            golem.setWeatherState(weather);
        }
    }

    public void setWeatherState(CopperGolem.CopperWeatherState weather) {
        this.weather = weather;
    }

    @Command(
            aliases = { "npc" },
            usage = "coppergolem (--weatherstate state)",
            desc = "",
            modifiers = { "coppergolem" },
            min = 1,
            max = 1,
            permission = "citizens.npc.coppergolem")
    @Requirements(selected = true, ownership = true, types = EntityType.COPPER_GOLEM)
    public static void CopperGolem(CommandContext args, CommandSender sender, NPC npc,
            @Flag("weatherstate") CopperGolem.CopperWeatherState state) throws CommandException {
        CopperGolemTrait trait = npc.getOrAddTrait(CopperGolemTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (state == null)
                throw new CommandException(Messages.INVALID_COPPER_WEATHER_STATE,
                        Util.listValuesPretty(CopperGolem.CopperWeatherState.values()));
            trait.setWeatherState(state);
            output += Messaging.tr(Messages.COPPER_WEATHER_STATE_SET, state);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}