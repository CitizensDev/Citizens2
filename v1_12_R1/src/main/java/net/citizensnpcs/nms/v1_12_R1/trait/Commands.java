package net.citizensnpcs.nms.v1_12_R1.trait;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama.Color;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

public class Commands {
    @Command(
            aliases = { "npc" },
            usage = "llama (--color color) (--strength strength)",
            desc = "Sets llama modifiers",
            modifiers = { "llama" },
            min = 1,
            max = 1,
            permission = "citizens.npc.llama")
    @Requirements(selected = true, ownership = true, types = EntityType.LLAMA)
    public void llama(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        LlamaTrait trait = npc.getTrait(LlamaTrait.class);
        String output = "";
        if (args.hasValueFlag("color") || args.hasValueFlag("colour")) {
            String colorRaw = args.getFlag("color", args.getFlag("colour"));
            Color color = Util.matchEnum(Color.values(), colorRaw);
            if (color == null) {
                String valid = Util.listValuesPretty(Color.values());
                throw new CommandException(Messages.INVALID_LLAMA_COLOR, valid);
            }
            trait.setColor(color);
            output += Messaging.tr(Messages.LLAMA_COLOR_SET, Util.prettyEnum(color));
        }
        if (args.hasValueFlag("strength")) {
            trait.setStrength(Math.max(1, Math.min(5, args.getFlagInteger("strength"))));
            output += Messaging.tr(Messages.LLAMA_STRENGTH_SET, args.getFlagInteger("strength"));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}
