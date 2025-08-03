package net.citizensnpcs.trait.versioned;

import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;

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
import net.citizensnpcs.util.Util;

@TraitName("tropicalfishtrait")
public class TropicalFishTrait extends Trait {
    @Persist
    private DyeColor bodyColor = DyeColor.BLUE;
    @Persist
    private Pattern pattern = Pattern.BRINELY;
    @Persist
    private DyeColor patternColor = DyeColor.BLUE;

    public TropicalFishTrait() {
        super("tropicalfishtrait");
    }

    public DyeColor getBodyColor() {
        return bodyColor;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public DyeColor getPatternColor() {
        return patternColor;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof TropicalFish) {
            TropicalFish fish = (TropicalFish) npc.getEntity();
            fish.setBodyColor(bodyColor);
            fish.setPatternColor(patternColor);
            fish.setPattern(pattern);
        }
    }

    public void setBodyColor(DyeColor color) {
        bodyColor = color;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setPatternColor(DyeColor color) {
        patternColor = color;
    }

    @Command(
            aliases = { "npc" },
            usage = "tropicalfish (--body color) (--pattern pattern) (--patterncolor color)",
            desc = "",
            modifiers = { "tropicalfish" },
            min = 1,
            max = 1,
            permission = "citizens.npc.tropicalfish")
    @Requirements(selected = true, ownership = true, types = EntityType.TROPICAL_FISH)
    public static void tropicalfish(CommandContext args, CommandSender sender, NPC npc, @Flag("body") DyeColor body,
            @Flag("patterncolor") DyeColor patterncolor, @Flag("pattern") Pattern pattern) throws CommandException {
        TropicalFishTrait trait = npc.getOrAddTrait(TropicalFishTrait.class);
        String output = "";
        if (args.hasValueFlag("body")) {
            if (body == null)
                throw new CommandException(Messages.INVALID_TROPICALFISH_COLOR,
                        Util.listValuesPretty(DyeColor.values()));
            trait.setBodyColor(body);
            output += Messaging.tr(Messages.TROPICALFISH_BODY_COLOR_SET, Util.prettyEnum(body));
        }
        if (args.hasValueFlag("patterncolor")) {
            if (patterncolor == null)
                throw new CommandException(Messages.INVALID_TROPICALFISH_COLOR,
                        Util.listValuesPretty(DyeColor.values()));
            trait.setPatternColor(patterncolor);
            output += Messaging.tr(Messages.TROPICALFISH_PATTERN_COLOR_SET, Util.prettyEnum(patterncolor));
        }
        if (args.hasValueFlag("pattern")) {
            if (pattern == null)
                throw new CommandException(Messages.INVALID_TROPICALFISH_PATTERN,
                        Util.listValuesPretty(Pattern.values()));
            trait.setPattern(pattern);
            output += Messaging.tr(Messages.TROPICALFISH_PATTERN_SET, Util.prettyEnum(pattern));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        } else
            throw new CommandUsageException();
    }
}
