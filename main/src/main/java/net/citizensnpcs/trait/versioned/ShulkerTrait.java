package net.citizensnpcs.trait.versioned;

import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;

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

@TraitName("shulkertrait")
public class ShulkerTrait extends Trait {
    @Persist("color")
    private DyeColor color = DyeColor.PURPLE;
    private int lastPeekSet = 0;
    @Persist("peek")
    private int peek = 0;

    public ShulkerTrait() {
        super("shulkertrait");
    }

    public DyeColor getColor() {
        return color;
    }

    public int getPeek() {
        return peek;
    }

    @Override
    public void onSpawn() {
        setPeek(peek);
    }

    @Override
    public void run() {
        if (color == null) {
            color = DyeColor.PURPLE;
        }
        if (npc.getEntity() instanceof Shulker) {
            if (peek != lastPeekSet) {
                NMS.setPeekShulker(npc.getEntity(), peek);
                lastPeekSet = peek;
            }
            ((Shulker) npc.getEntity()).setColor(color);
        }
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public void setPeek(int peek) {
        this.peek = peek;
        lastPeekSet = -1;
    }

    @Command(
            aliases = { "npc" },
            usage = "shulker (--peek [peek] --color [color])",
            desc = "Sets shulker modifiers.",
            modifiers = { "shulker" },
            min = 1,
            max = 1,
            permission = "citizens.npc.shulker")
    @Requirements(selected = true, ownership = true, types = { EntityType.SHULKER })
    public static void shulker(CommandContext args, CommandSender sender, NPC npc, @Flag("peek") Integer peek,
            @Flag("color") DyeColor color) throws CommandException {
        ShulkerTrait trait = npc.getOrAddTrait(ShulkerTrait.class);
        boolean hasArg = false;
        if (peek != null) {
            trait.setPeek((byte) (int) peek);
            Messaging.sendTr(sender, Messages.SHULKER_PEEK_SET, npc.getName(), peek);
            hasArg = true;
        }
        if (args.hasValueFlag("color")) {
            if (color == null) {
                Messaging.sendErrorTr(sender, Messages.INVALID_SHULKER_COLOR, Util.listValuesPretty(DyeColor.values()));
                return;
            }
            trait.setColor(color);
            Messaging.sendTr(sender, Messages.SHULKER_COLOR_SET, npc.getName(), Util.prettyEnum(color));
            hasArg = true;
        }
        if (!hasArg)
            throw new CommandUsageException();
    }
}
