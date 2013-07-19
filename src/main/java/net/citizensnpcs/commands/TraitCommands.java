package net.citizensnpcs.commands;

import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Requirements(selected = true, ownership = true)
public class TraitCommands {
    @Command(aliases = { "특성", "tr" }, usage = "추가 [특성 이름]...", desc = "NPC에 특성을 추가합니다", modifiers = {
            "추가", "a" }, min = 1, permission = "citizens.npc.trait")
    public void add(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> added = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        for (String traitName : Splitter.on(',').split(args.getJoinedStrings(0))) {
            if (!sender.hasPermission("citizens.npc.trait." + traitName)) {
                failed.add(String.format("%s: No permission", traitName));
                continue;
            }

            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
            if (clazz == null) {
                failed.add(String.format("%s: 특성을 찾을 수 없습니다", traitName));
                continue;
            }
            if (npc.hasTrait(clazz)) {
                failed.add(String.format("%s: 이미 추가되었습니다", traitName));
                continue;
            }
            npc.addTrait(clazz);
            added.add(StringHelper.wrap(traitName));
        }
        if (added.size() > 0)
            Messaging.sendTr(sender, Messages.TRAITS_ADDED, Joiner.on(", ").join(added));
        if (failed.size() > 0)
            Messaging.sendTr(sender, Messages.TRAITS_FAILED_TO_ADD, Joiner.on(", ").join(failed));
    }

    @Command(
            aliases = { "특성", "trc" },
            usage = "[특성 이름] (플래그)",
            desc = "특성을 설정합니다",
            modifiers = { "*" },
            min = 1,
            flags = "*",
            permission = "citizens.npc.trait-configure")
    public void configure(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String traitName = args.getString(0);
        if (!sender.hasPermission("citizens.npc.trait-configure." + traitName))
            throw new NoPermissionsException();
        Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(args.getString(0));
        if (clazz == null)
            throw new CommandException(Messages.TRAIT_NOT_FOUND);
        if (!CommandConfigurable.class.isAssignableFrom(clazz))
            throw new CommandException(Messages.TRAIT_NOT_CONFIGURABLE);
        if (!npc.hasTrait(clazz))
            throw new CommandException(Messages.TRAIT_NOT_FOUND_ON_NPC);
        CommandConfigurable trait = (CommandConfigurable) npc.getTrait(clazz);
        trait.configure(args);
    }

    @Command(
            aliases = { "특성", "tr" },
            usage = "제거 [특성 이름]...",
            desc = "NPC의 특성을 제거합니다",
            modifiers = { "제거", "rem", "r" },
            min = 1,
            permission = "citizens.npc.trait")
    public void remove(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> removed = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        for (String traitName : Splitter.on(',').split(args.getJoinedStrings(0))) {
            if (!sender.hasPermission("citizens.npc.trait." + traitName)) {
                failed.add(String.format("%s: No permission", traitName));
                continue;
            }

            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
            if (clazz == null) {
                failed.add(String.format("%s: 특성을 찾을 수 없습니다", traitName));
                continue;
            }
            boolean hasTrait = npc.hasTrait(clazz);
            if (!hasTrait) {
                failed.add(String.format("%s: 포함된 특성이 아닙니다", traitName));
                continue;
            }
            npc.removeTrait(clazz);
            removed.add(StringHelper.wrap(traitName));
        }
        if (removed.size() > 0)
            Messaging.sendTr(sender, Messages.TRAITS_REMOVED, Joiner.on(", ").join(removed));
        if (failed.size() > 0)
            Messaging.sendTr(sender, Messages.FAILED_TO_REMOVE, Joiner.on(", ").join(failed));
    }

    @Command(
            aliases = { "특성", "tr" },
            usage = "[특성 이름], [특성 이름]...",
            desc = "NPC의 특성 이름을 전환합니다",
            modifiers = { "*" },
            min = 1,
            permission = "citizens.npc.trait")
    public void toggle(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> added = Lists.newArrayList();
        List<String> removed = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        for (String traitName : Splitter.on(',').split(args.getJoinedStrings(0))) {
            if (!sender.hasPermission("citizens.npc.trait." + traitName)) {
                failed.add(String.format("%s: 권한 없음", traitName));
                continue;
            }

            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
            if (clazz == null) {
                failed.add(String.format("%s: 특성을 찾을 수 없습니다", traitName));
                continue;
            }
            boolean remove = npc.hasTrait(clazz);
            if (remove) {
                npc.removeTrait(clazz);
                removed.add(StringHelper.wrap(traitName));
                continue;
            }
            npc.addTrait(clazz);
            added.add(StringHelper.wrap(traitName));
        }
        if (added.size() > 0)
            Messaging.sendTr(sender, Messages.TRAITS_ADDED, Joiner.on(", ").join(added));
        if (removed.size() > 0)
            Messaging.sendTr(sender, Messages.TRAITS_REMOVED, Joiner.on(", ").join(removed));
        if (failed.size() > 0)
            Messaging.sendTr(sender, Messages.TRAITS_FAILED_TO_CHANGE, Joiner.on(", ").join(failed));
    }
}
