package net.citizensnpcs.trait.versioned;

import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot.Type;

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
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

@TraitName("cattrait")
public class CatTrait extends Trait {
    @Persist
    private DyeColor collarColor = null;
    @Persist
    private boolean lying = false;
    @Persist
    private boolean sitting = false;
    @Persist
    private Cat.Type type = Cat.Type.BLACK;

    public CatTrait() {
        super("cattrait");
    }

    public boolean isLyingDown() {
        return lying;
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof Cat))
            return;
        Cat cat = (Cat) npc.getEntity();
        cat.setSitting(sitting);
        cat.setCatType(type);
        if (collarColor != null) {
            cat.setCollarColor(collarColor);
        }
        NMS.setLyingDown(cat, lying);
    }

    public void setCollarColor(DyeColor color) {
        collarColor = color;
    }

    public void setLyingDown(boolean lying) {
        this.lying = lying;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void setType(Cat.Type type) {
        if (type == null) {
            type = Cat.Type.BLACK;
        }
        this.type = type;
    }

    public void setType(Type type2) {
        if (type2 == null) {
            type = Cat.Type.BLACK;
            return;
        }
        switch (type2) {
            case WILD_OCELOT:
                type = Cat.Type.CALICO;
                break;
            case BLACK_CAT:
                type = Cat.Type.BLACK;
                break;
            case RED_CAT:
                type = Cat.Type.RED;
                break;
            case SIAMESE_CAT:
                type = Cat.Type.SIAMESE;
                break;
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "cat (-s/-n/-l) --type type --ccolor collar color",
            desc = "Sets cat modifiers",
            modifiers = { "cat" },
            min = 1,
            max = 1,
            flags = "snl",
            permission = "citizens.npc.cat")
    @Requirements(selected = true, ownership = true, types = EntityType.CAT)
    public static void cat(CommandContext args, CommandSender sender, NPC npc, @Flag("ccolor") DyeColor ccolor,
            @Flag("type") Cat.Type type) throws CommandException {
        CatTrait trait = npc.getOrAddTrait(CatTrait.class);
        String output = "";
        if (args.hasValueFlag("type")) {
            if (type == null)
                throw new CommandUsageException(Messages.INVALID_CAT_TYPE, Util.listValuesPretty(Cat.Type.values()));
            trait.setType(type);
            output += ' ' + Messaging.tr(Messages.CAT_TYPE_SET, args.getFlag("type"));
        }
        if (args.hasValueFlag("ccolor")) {
            if (ccolor == null)
                throw new CommandUsageException(Messages.INVALID_CAT_COLLAR_COLOR,
                        Util.listValuesPretty(DyeColor.values()));
            trait.setCollarColor(ccolor);
            output += ' ' + Messaging.tr(Messages.CAT_COLLAR_COLOR_SET, args.getFlag("ccolor"));
        }
        if (args.hasFlag('s')) {
            trait.setSitting(true);
            output += ' ' + Messaging.tr(Messages.CAT_STARTED_SITTING, npc.getName());
        } else if (args.hasFlag('n')) {
            trait.setSitting(false);
            output += ' ' + Messaging.tr(Messages.CAT_STOPPED_SITTING, npc.getName());
        }
        if (args.hasFlag('l')) {
            trait.setLyingDown(!trait.isLyingDown());
            output += ' ' + Messaging.tr(trait.isLyingDown() ? Messages.CAT_STARTED_LYING : Messages.CAT_STOPPED_LYING,
                    npc.getName());
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
