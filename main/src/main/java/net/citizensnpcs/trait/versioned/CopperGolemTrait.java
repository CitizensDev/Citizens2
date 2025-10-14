package net.citizensnpcs.trait.versioned;

import java.lang.invoke.MethodHandle;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.CopperGolem;
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

@TraitName("coppergolemtrait")
public class CopperGolemTrait extends Trait {
    @Persist
    private Object weather;

    public CopperGolemTrait() {
        super("coppergolemtrait");
    }

    private void _setWeatherState(Object weather) {
        try {
            if (PAPER_SET_WEATHER_STATE != null) {
                PAPER_SET_WEATHER_STATE.invoke(npc.getEntity(), weather);
            } else {
                SPIGOT_SET_WEATHER_STATE.invoke(npc.getEntity(), weather);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (weather != null && npc.getEntity() instanceof CopperGolem) {
            _setWeatherState(weather);
        }
    }

    public void setWeatherState(Object weather) {
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
    public static void copperGolem(CommandContext args, CommandSender sender, NPC npc,
            @Flag("weatherstate") String state) throws CommandException {
        CopperGolemTrait trait = npc.getOrAddTrait(CopperGolemTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (state == null)
                throw new CommandException(Messages.INVALID_COPPER_WEATHER_STATE);
            try {
                trait.setWeatherState(Enum.valueOf(WEATHER_STATE_CLASS, state.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new CommandException(Messages.INVALID_COPPER_WEATHER_STATE);
            }
            output += Messaging.tr(Messages.COPPER_WEATHER_STATE_SET, state);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }

    private static MethodHandle PAPER_SET_WEATHER_STATE;
    private static MethodHandle SPIGOT_SET_WEATHER_STATE;
    private static Class WEATHER_STATE_CLASS;
    static {
        try {
            WEATHER_STATE_CLASS = Class.forName("org.bukkit.entity.CopperGolem$CopperWeatherState");
            SPIGOT_SET_WEATHER_STATE = NMS.getMethodHandle(CopperGolem.class, "setWeatherState", false,
                    WEATHER_STATE_CLASS);
        } catch (ClassNotFoundException e) {
            try {
                WEATHER_STATE_CLASS = Class.forName("io.papermc.paper.world.WeatheringCopperState");
                PAPER_SET_WEATHER_STATE = NMS.getMethodHandle(CopperGolem.class, "setWeatheringState", true,
                        WEATHER_STATE_CLASS);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }
}