package net.citizensnpcs.nms.v1_18_R1.trait;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.versioned.AxolotlTrait;
import net.citizensnpcs.trait.versioned.BeeTrait;
import net.citizensnpcs.trait.versioned.BossBarTrait;
import net.citizensnpcs.trait.versioned.CatTrait;
import net.citizensnpcs.trait.versioned.FoxTrait;
import net.citizensnpcs.trait.versioned.LlamaTrait;
import net.citizensnpcs.trait.versioned.MushroomCowTrait;
import net.citizensnpcs.trait.versioned.PandaTrait;
import net.citizensnpcs.trait.versioned.ParrotTrait;
import net.citizensnpcs.trait.versioned.PhantomTrait;
import net.citizensnpcs.trait.versioned.PolarBearTrait;
import net.citizensnpcs.trait.versioned.PufferFishTrait;
import net.citizensnpcs.trait.versioned.ShulkerTrait;
import net.citizensnpcs.trait.versioned.SnowmanTrait;
import net.citizensnpcs.trait.versioned.TropicalFishTrait;
import net.citizensnpcs.trait.versioned.VillagerTrait;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

public class Commands {
    @Command(
            aliases = { "npc" },
            usage = "axolotl (-d) (--variant variant)",
            desc = "Sets axolotl modifiers",
            modifiers = { "axolotl" },
            min = 1,
            max = 1,
            flags = "d",
            permission = "citizens.npc.axolotl")
    @Requirements(selected = true, ownership = true, types = EntityType.AXOLOTL)
    public void axolotl(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        AxolotlTrait trait = npc.getOrAddTrait(AxolotlTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            Axolotl.Variant variant = Util.matchEnum(Axolotl.Variant.values(), args.getFlag("variant"));
            if (variant == null) {
                throw new CommandException(Messages.INVALID_AXOLOTL_VARIANT,
                        Util.listValuesPretty(Axolotl.Variant.values()));
            }
            trait.setVariant(variant);
            output += ' ' + Messaging.tr(Messages.AXOLOTL_VARIANT_SET, args.getFlag("variant"));
        }
        if (args.hasFlag('d')) {
            trait.setPlayingDead(!trait.isPlayingDead());
            output += ' ' + (trait.isPlayingDead() ? Messaging.tr(Messages.AXOLOTL_PLAYING_DEAD, npc.getName())
                    : Messaging.tr(Messages.AXOLOTL_NOT_PLAYING_DEAD, npc.getName()));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "bee (-s/-n) --anger anger",
            desc = "Sets bee modifiers",
            modifiers = { "bee" },
            min = 1,
            max = 1,
            flags = "sn",
            permission = "citizens.npc.bee")
    @Requirements(selected = true, ownership = true, types = EntityType.BEE)
    public void bee(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        BeeTrait trait = npc.getOrAddTrait(BeeTrait.class);
        String output = "";
        if (args.hasValueFlag("anger")) {
            int anger = args.getFlagInteger("anger");
            if (anger < 0) {
                throw new CommandException(Messages.INVALID_BEE_ANGER);
            }
            trait.setAnger(anger);
            output += ' ' + Messaging.tr(Messages.BEE_ANGER_SET, args.getFlag("anger"));
        }
        if (args.hasFlag('s')) {
            trait.setStung(!trait.hasStung());
            output += ' ' + (trait.hasStung() ? Messaging.tr(Messages.BEE_STUNG, npc.getName())
                    : Messaging.tr(Messages.BEE_NOT_STUNG, npc.getName()));
        }
        if (args.hasFlag('n')) {
            trait.setNectar(!trait.hasNectar());
            output += ' ' + (trait.hasNectar() ? Messaging.tr(Messages.BEE_HAS_NECTAR, npc.getName())
                    : Messaging.tr(Messages.BEE_NO_NECTAR, npc.getName()));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "bossbar --style [style] --color [color] --title [title] --visible [visible] --flags [flags]",
            desc = "Edit bossbar properties",
            modifiers = { "bossbar" },
            min = 1,
            max = 1)
    @Requirements(selected = true, ownership = true)
    public void bossbar(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        BossBarTrait trait = npc.getOrAddTrait(BossBarTrait.class);
        if (args.hasValueFlag("style")) {
            BarStyle style = Util.matchEnum(BarStyle.values(), args.getFlag("style"));
            if (style != null) {
                trait.setStyle(style);
            }
        }
        if (args.hasValueFlag("color")) {
            BarColor color = Util.matchEnum(BarColor.values(), args.getFlag("color"));
            if (color != null) {
                trait.setColor(color);
            }
        }
        if (args.hasValueFlag("title")) {
            trait.setTitle(Colorizer.parseColors(args.getFlag("title")));
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
            usage = "cat (-s/-n/-l) --type type --ccolor collar color",
            desc = "Sets cat modifiers",
            modifiers = { "cat" },
            min = 1,
            max = 1,
            flags = "snl",
            permission = "citizens.npc.cat")
    @Requirements(selected = true, ownership = true, types = EntityType.CAT)
    public void cat(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        CatTrait trait = npc.getOrAddTrait(CatTrait.class);
        String output = "";
        if (args.hasValueFlag("type")) {
            Cat.Type type = Util.matchEnum(Cat.Type.values(), args.getFlag("type"));
            if (type == null) {
                throw new CommandUsageException(Messages.INVALID_CAT_TYPE, Util.listValuesPretty(Cat.Type.values()));
            }
            trait.setType(type);
            output += ' ' + Messaging.tr(Messages.CAT_TYPE_SET, args.getFlag("type"));
        }
        if (args.hasValueFlag("ccolor")) {
            DyeColor color = Util.matchEnum(DyeColor.values(), args.getFlag("ccolor"));
            if (color == null) {
                throw new CommandUsageException(Messages.INVALID_CAT_COLLAR_COLOR,
                        Util.listValuesPretty(DyeColor.values()));
            }
            trait.setCollarColor(color);
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
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "fox --type type --sleeping [true|false] --sitting [true|false] --crouching [true|false]",
            desc = "Sets fox modifiers",
            modifiers = { "fox" },
            min = 1,
            max = 1,
            permission = "citizens.npc.fox")
    @Requirements(selected = true, ownership = true, types = EntityType.FOX)
    public void fox(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        FoxTrait trait = npc.getOrAddTrait(FoxTrait.class);
        String output = "";
        if (args.hasValueFlag("type")) {
            Fox.Type type = Util.matchEnum(Fox.Type.values(), args.getFlag("type"));
            if (type == null) {
                throw new CommandUsageException(Messages.INVALID_FOX_TYPE, Util.listValuesPretty(Fox.Type.values()));
            }
            trait.setType(type);
            output += ' ' + Messaging.tr(Messages.FOX_TYPE_SET, args.getFlag("type"));
        }
        if (args.hasValueFlag("sleeping")) {
            boolean sleeping = Boolean.parseBoolean(args.getFlag("sleeping"));
            trait.setSleeping(sleeping);
            output += ' ' + Messaging.tr(sleeping ? Messages.FOX_SLEEPING_SET : Messages.FOX_SLEEPING_UNSET);
        }
        if (args.hasValueFlag("sitting")) {
            boolean sitting = Boolean.parseBoolean(args.getFlag("sitting"));
            trait.setSitting(sitting);
            output += ' ' + Messaging.tr(sitting ? Messages.FOX_SITTING_SET : Messages.FOX_SITTING_UNSET);
        }
        if (args.hasValueFlag("crouching")) {
            boolean crouching = Boolean.parseBoolean(args.getFlag("crouching"));
            trait.setCrouching(crouching);
            output += ' ' + Messaging.tr(crouching ? Messages.FOX_CROUCHING_SET : Messages.FOX_CROUCHING_UNSET);
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
        LlamaTrait trait = npc.getOrAddTrait(LlamaTrait.class);
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

    @Command(
            aliases = { "npc" },
            usage = "mcow (--variant [variant])",
            desc = "Sets mushroom cow modifiers.",
            modifiers = { "mcow", "mushroomcow" },
            min = 1,
            max = 1,
            permission = "citizens.npc.mushroomcow")
    @Requirements(selected = true, ownership = true, types = { EntityType.MUSHROOM_COW })
    public void mushroomcow(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        MushroomCowTrait trait = npc.getOrAddTrait(MushroomCowTrait.class);
        boolean hasArg = false;
        if (args.hasValueFlag("variant")) {
            MushroomCow.Variant variant = Util.matchEnum(MushroomCow.Variant.values(), args.getFlag("variant"));
            if (variant == null) {
                Messaging.sendErrorTr(sender, Messages.INVALID_MUSHROOM_COW_VARIANT,
                        Util.listValuesPretty(MushroomCow.Variant.values()));
                return;
            }
            trait.setVariant(variant);
            Messaging.sendTr(sender, Messages.MUSHROOM_COW_VARIANT_SET, npc.getName(), variant);
            hasArg = true;
        }
        if (!hasArg) {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "panda --gene (main gene) --hgene (hidden gene) -s(itting)",
            desc = "Sets panda modifiers",
            modifiers = { "panda" },
            flags = "s",
            min = 1,
            max = 1,
            permission = "citizens.npc.panda")
    @Requirements(selected = true, ownership = true, types = EntityType.PANDA)
    public void panda(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        PandaTrait trait = npc.getOrAddTrait(PandaTrait.class);
        String output = "";
        if (args.hasValueFlag("gene")) {
            Panda.Gene gene = Util.matchEnum(Panda.Gene.values(), args.getFlag("gene"));
            if (gene == null) {
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            }
            trait.setMainGene(gene);
            output += ' ' + Messaging.tr(Messages.PANDA_MAIN_GENE_SET, args.getFlag("gene"));
        }
        if (args.hasValueFlag("hgene")) {
            Panda.Gene gene = Util.matchEnum(Panda.Gene.values(), args.getFlag("hgene"));
            if (gene == null) {
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            }
            trait.setHiddenGene(gene);
            output += ' ' + Messaging.tr(Messages.PANDA_HIDDEN_GENE_SET, args.getFlag("hgene"));
        }
        if (args.hasFlag('s')) {
            boolean isSitting = trait.toggleSitting();
            output += ' ' + Messaging.tr(isSitting ? Messages.PANDA_SITTING : Messages.PANDA_STOPPED_SITTING);
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
        ParrotTrait trait = npc.getOrAddTrait(ParrotTrait.class);
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
        PhantomTrait trait = npc.getOrAddTrait(PhantomTrait.class);
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
            usage = "polarbear (-r)",
            desc = "Sets polarbear modifiers.",
            modifiers = { "polarbear" },
            min = 1,
            max = 1,
            flags = "r",
            permission = "citizens.npc.polarbear")
    @Requirements(selected = true, ownership = true, types = { EntityType.POLAR_BEAR })
    public void polarbear(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        PolarBearTrait trait = npc.getOrAddTrait(PolarBearTrait.class);
        String output = "";
        if (args.hasFlag('r')) {
            trait.setRearing(!trait.isRearing());
            output += Messaging.tr(
                    trait.isRearing() ? Messages.POLAR_BEAR_REARING : Messages.POLAR_BEAR_STOPPED_REARING,
                    npc.getName());
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
        PufferFishTrait trait = npc.getOrAddTrait(PufferFishTrait.class);
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
        ShulkerTrait trait = npc.getOrAddTrait(ShulkerTrait.class);
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
            usage = "snowman (-d[erp])",
            desc = "Sets snowman modifiers.",
            modifiers = { "snowman" },
            min = 1,
            max = 1,
            flags = "d",
            permission = "citizens.npc.snowman")
    @Requirements(selected = true, ownership = true, types = { EntityType.SNOWMAN })
    public void snowman(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        SnowmanTrait trait = npc.getOrAddTrait(SnowmanTrait.class);
        boolean hasArg = false;
        if (args.hasFlag('d')) {
            boolean isDerp = trait.toggleDerp();
            Messaging.sendTr(sender, isDerp ? Messages.SNOWMAN_DERP_SET : Messages.SNOWMAN_DERP_STOPPED, npc.getName());
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
        TropicalFishTrait trait = npc.getOrAddTrait(TropicalFishTrait.class);
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

    @Command(
            aliases = { "npc" },
            usage = "villager (--level level) (--type type) (--profession profession)",
            desc = "Sets villager modifiers",
            modifiers = { "villager" },
            min = 1,
            max = 1,
            permission = "citizens.npc.villager")
    @Requirements(selected = true, ownership = true, types = EntityType.VILLAGER)
    public void villager(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        VillagerTrait trait = npc.getOrAddTrait(VillagerTrait.class);
        String output = "";
        if (args.hasValueFlag("level")) {
            if (args.getFlagInteger("level") < 0) {
                throw new CommandUsageException();
            }
            trait.setLevel(args.getFlagInteger("level"));
            output += " " + Messaging.tr(Messages.VILLAGER_LEVEL_SET, args.getFlagInteger("level"));
        }
        if (args.hasValueFlag("type")) {
            Villager.Type type = Util.matchEnum(Villager.Type.values(), args.getFlag("type"));
            if (type == null) {
                throw new CommandException(Messages.INVALID_VILLAGER_TYPE,
                        Util.listValuesPretty(Villager.Type.values()));
            }
            trait.setType(type);
            output += " " + Messaging.tr(Messages.VILLAGER_TYPE_SET, args.getFlag("type"));
        }
        if (args.hasValueFlag("profession")) {
            Profession parsed = Util.matchEnum(Profession.values(), args.getFlag("profession"));
            if (parsed == null) {
                throw new CommandException(Messages.INVALID_PROFESSION, args.getFlag("profession"),
                        Joiner.on(',').join(Profession.values()));
            }
            npc.getOrAddTrait(VillagerProfession.class).setProfession(parsed);
            output += " " + Messaging.tr(Messages.PROFESSION_SET, npc.getName(), args.getFlag("profession"));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else {
            throw new CommandUsageException();
        }
    }
}
