package net.citizensnpcs.nms.v1_14_R1.trait;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.entity.TropicalFish.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

public class Commands {
    @Command(
            aliases = { "npc" },
            usage = "bossbar --color [color] --title [title] --visible [visible] --flags [flags]",
            desc = "Edit bossbar properties",
            modifiers = { "bossbar" },
            min = 1,
            max = 1)
    @Requirements(selected = true, ownership = true, types = { EntityType.WITHER, EntityType.ENDER_DRAGON })
    public void bossbar(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        BossBarTrait trait = npc.getTrait(BossBarTrait.class);
        if (args.hasValueFlag("color")) {
            BarColor color = Util.matchEnum(BarColor.values(), args.getFlag("color"));
            trait.setColor(color);
        }
        if (args.hasValueFlag("title")) {
            trait.setTitle(args.getFlag("title"));
        }
        if (args.hasValueFlag("visible")) {
            trait.setVisible(Boolean.parseBoolean(args.getFlag("visible")));
        }
        if (args.hasValueFlag("flags")) {
            List<BarFlag> flags = Lists.newArrayList();
            for (String s : Splitter.on(',').omitEmptyStrings().trimResults().split(args.getFlag("flags"))) {
                BarFlag flag = Util.matchEnum(BarFlag.values(), s);
                if (flag != null) {
                    flags.add(flag);
                }
            }
            trait.setFlags(flags);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "cat (-s/-n) --type type",
            desc = "Sets cat modifiers",
            modifiers = { "cat" },
            min = 1,
            max = 1,
            permission = "citizens.npc.cat")
    @Requirements(selected = true, ownership = true, types = EntityType.CAT)
    public void cat(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        CatTrait trait = npc.getTrait(CatTrait.class);
        String output = "";
        if (args.hasValueFlag("type")) {
            if (args.getFlagInteger("size") <= 0) {
                throw new CommandUsageException();
            }
            Cat.Type type = Util.matchEnum(Cat.Type.values(), args.getFlag("type"));
            if (type == null) {
                throw new CommandUsageException(Messages.INVALID_CAT_TYPE, Util.listValuesPretty(Cat.Type.values()));
            }
            trait.setType(type);
            output += ' ' + Messaging.tr(Messages.CAT_TYPE_SET, args.getFlag("type"));
        }
        if (args.hasFlag('s')) {
            trait.setSitting(true);
            output += ' ' + Messaging.tr(Messages.CAT_STARTED_SITTING);
        } else if (args.hasFlag('n')) {
            trait.setSitting(false);
            output += ' ' + Messaging.tr(Messages.CAT_STOPPED_SITTING);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "llama (--color color) (--strength strength)",
            desc = "Sets llama modifiers",
            modifiers = { "llama" },
            min = 1,
            max = 1,
            permission = "citizens.npc.llama")
    @Requirements(selected = true, ownership = true, types = { EntityType.LLAMA, EntityType.TRADER_LLAMA })
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

    @Command(
            aliases = { "npc" },
            usage = "panda --gene (main gene) --hgene (hidden gene)",
            desc = "Sets panda modifiers",
            modifiers = { "panda" },
            min = 1,
            max = 1,
            permission = "citizens.npc.panda")
    @Requirements(selected = true, ownership = true, types = EntityType.PANDA)
    public void panda(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        PandaTrait trait = npc.getTrait(PandaTrait.class);
        String output = "";
        if (args.hasValueFlag("gene")) {
            if (args.getFlagInteger("size") <= 0) {
                throw new CommandUsageException();
            }
            Panda.Gene gene = Util.matchEnum(Panda.Gene.values(), args.getFlag("gene"));
            if (gene == null) {
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            }
            trait.setMainGene(gene);
            output += ' ' + Messaging.tr(Messages.PANDA_MAIN_GENE_SET, args.getFlag("gene"));
        }
        if (args.hasValueFlag("hgene")) {
            if (args.getFlagInteger("size") <= 0) {
                throw new CommandUsageException();
            }
            Panda.Gene gene = Util.matchEnum(Panda.Gene.values(), args.getFlag("hgene"));
            if (gene == null) {
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            }
            trait.setMainGene(gene);
            output += ' ' + Messaging.tr(Messages.PANDA_HIDDEN_GENE_SET, args.getFlag("hgene"));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "parrot (--variant variant)",
            desc = "Sets parrot modifiers",
            modifiers = { "parrot" },
            min = 1,
            max = 1,
            permission = "citizens.npc.parrot")
    @Requirements(selected = true, ownership = true, types = EntityType.PARROT)
    public void parrot(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        ParrotTrait trait = npc.getTrait(ParrotTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            String variantRaw = args.getFlag("variant");
            Variant variant = Util.matchEnum(Variant.values(), variantRaw);
            if (variant == null) {
                String valid = Util.listValuesPretty(Variant.values());
                throw new CommandException(Messages.INVALID_PARROT_VARIANT, valid);
            }
            trait.setVariant(variant);
            output += Messaging.tr(Messages.PARROT_VARIANT_SET, Util.prettyEnum(variant));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "phantom (--size size)",
            desc = "Sets phantom modifiers",
            modifiers = { "phantom" },
            min = 1,
            max = 1,
            permission = "citizens.npc.phantom")
    @Requirements(selected = true, ownership = true, types = EntityType.PHANTOM)
    public void phantom(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        PhantomTrait trait = npc.getTrait(PhantomTrait.class);
        String output = "";
        if (args.hasValueFlag("size")) {
            if (args.getFlagInteger("size") <= 0) {
                throw new CommandUsageException();
            }
            trait.setSize(args.getFlagInteger("size"));
            output += Messaging.tr(Messages.PHANTOM_STATE_SET, args.getFlagInteger("size"));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "pufferfish (--state state)",
            desc = "Sets pufferfish modifiers",
            modifiers = { "pufferfish" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pufferfish")
    @Requirements(selected = true, ownership = true, types = EntityType.PUFFERFISH)
    public void pufferfish(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        PufferFishTrait trait = npc.getTrait(PufferFishTrait.class);
        String output = "";
        if (args.hasValueFlag("state")) {
            int state = Math.min(Math.max(args.getFlagInteger("state"), 0), 3);
            trait.setPuffState(state);
            output += Messaging.tr(Messages.PUFFERFISH_STATE_SET, state);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
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
    public void shulker(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        ShulkerTrait trait = npc.getTrait(ShulkerTrait.class);
        boolean hasArg = false;
        if (args.hasValueFlag("peek")) {
            int peek = (byte) args.getFlagInteger("peek");
            trait.setPeek(peek);
            Messaging.sendTr(sender, Messages.SHULKER_PEEK_SET, npc.getName(), peek);
            hasArg = true;
        }
        if (args.hasValueFlag("color")) {
            DyeColor color = Util.matchEnum(DyeColor.values(), args.getFlag("color"));
            if (color == null) {
                Messaging.sendErrorTr(sender, Messages.INVALID_SHULKER_COLOR, Util.listValuesPretty(DyeColor.values()));
                return;
            }
            trait.setColor(color);
            Messaging.sendTr(sender, Messages.SHULKER_COLOR_SET, npc.getName(), Util.prettyEnum(color));
            hasArg = true;
        }
        if (!hasArg) {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "tfish (--body color) (--pattern pattern) (--patterncolor color)",
            desc = "Sets tropical fish modifiers",
            modifiers = { "tfish" },
            min = 1,
            max = 1,
            permission = "citizens.npc.tropicalfish")
    @Requirements(selected = true, ownership = true, types = EntityType.TROPICAL_FISH)
    public void tropicalfish(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        TropicalFishTrait trait = npc.getTrait(TropicalFishTrait.class);
        String output = "";
        if (args.hasValueFlag("body")) {
            DyeColor color = Util.matchEnum(DyeColor.values(), args.getFlag("body"));
            if (color == null) {
                throw new CommandException(Messages.INVALID_TROPICALFISH_COLOR,
                        Util.listValuesPretty(DyeColor.values()));
            }
            trait.setBodyColor(color);
            output += Messaging.tr(Messages.TROPICALFISH_BODY_COLOR_SET, Util.prettyEnum(color));
        }
        if (args.hasValueFlag("patterncolor")) {
            DyeColor color = Util.matchEnum(DyeColor.values(), args.getFlag("patterncolor"));
            if (color == null) {
                throw new CommandException(Messages.INVALID_TROPICALFISH_COLOR,
                        Util.listValuesPretty(DyeColor.values()));
            }
            trait.setPatternColor(color);
            output += Messaging.tr(Messages.TROPICALFISH_PATTERN_COLOR_SET, Util.prettyEnum(color));
        }
        if (args.hasValueFlag("pattern")) {
            Pattern pattern = Util.matchEnum(Pattern.values(), args.getFlag("pattern"));
            if (pattern == null) {
                throw new CommandException(Messages.INVALID_TROPICALFISH_PATTERN,
                        Util.listValuesPretty(Pattern.values()));
            }
            trait.setPattern(pattern);
            output += Messaging.tr(Messages.TROPICALFISH_PATTERN_SET, Util.prettyEnum(pattern));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        } else {
            throw new CommandUsageException();
        }
    }
}
