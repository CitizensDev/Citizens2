package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;

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
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

@TraitName("llamatrait")
public class LlamaTrait extends Trait {
    @Persist
    private Color color = Color.BROWN;
    @Persist
    private int strength = 3;

    public LlamaTrait() {
        super("llamatrait");
    }

    public Color getColor() {
        return color;
    }

    public int getStrength() {
        return strength;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Llama) {
            Llama llama = (Llama) npc.getEntity();
            llama.setColor(color);
            llama.setStrength(strength);
        }
    }

    public void setColor(Llama.Color color) {
        this.color = color;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Command(
            aliases = { "npc" },
            usage = "llama (--color color) (--strength strength)",
            desc = "",
            modifiers = { "llama" },
            min = 1,
            max = 1,
            permission = "citizens.npc.llama")
    @Requirements(selected = true, ownership = true)
    public static void llama(CommandContext args, CommandSender sender, NPC npc,
            @Flag({ "color", "colour" }) Color color, @Flag("strength") Integer strength) throws CommandException {
        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        if (!type.name().contains("LLAMA"))
            throw new CommandException(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE, Util.prettyEnum(type));
        LlamaTrait trait = npc.getOrAddTrait(LlamaTrait.class);
        String output = "";
        if (args.hasAnyValueFlag("color", "colour")) {
            if (color == null) {
                String valid = Util.listValuesPretty(Color.values());
                throw new CommandException(Messages.INVALID_LLAMA_COLOR, valid);
            }
            trait.setColor(color);
            output += Messaging.tr(Messages.LLAMA_COLOR_SET, Util.prettyEnum(color));
        }
        if (strength != null) {
            trait.setStrength(Math.max(1, Math.min(5, strength)));
            output += Messaging.tr(Messages.LLAMA_STRENGTH_SET, trait.getStrength());
        }
        if (args.hasFlag('c')) {
            npc.getOrAddTrait(HorseModifiers.class).setCarryingChest(true);
            output += Messaging.tr(Messages.HORSE_CHEST_SET) + " ";
        } else if (args.hasFlag('b')) {
            npc.getOrAddTrait(HorseModifiers.class).setCarryingChest(false);
            output += Messaging.tr(Messages.HORSE_CHEST_UNSET) + " ";
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}
