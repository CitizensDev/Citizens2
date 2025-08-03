package net.citizensnpcs.commands;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.NPCTraitCommandAttachEvent;
import net.citizensnpcs.api.event.NPCTraitCommandDetachEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;

@Requirements(selected = true, ownership = true)
public class TraitCommands {
    private final Citizens plugin;

    public TraitCommands(Citizens plugin) {
        this.plugin = plugin;
    }

    @Command(
            aliases = { "trait" },
            usage = "add [trait name]...",
            desc = "",
            modifiers = { "add", "a" },
            min = 2,
            permission = "citizens.npc.trait")
    public void add(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> added = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        for (String traitName : Splitter.on(',').split(args.getJoinedStrings(1))) {
            if (!sender.hasPermission("citizens.npc.trait." + traitName)
                    && !sender.hasPermission("citizens.npc.trait.*")) {
                failed.add(String.format("%s: No permission", traitName));
                continue;
            }
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
            if (clazz == null) {
                failed.add(String.format("%s: Trait not found", traitName));
                continue;
            }
            if (npc.hasTrait(clazz)) {
                failed.add(String.format("%s: Already added", traitName));
                continue;
            }
            addTrait(npc, clazz, sender);
            added.add(StringHelper.wrap(traitName));
        }
        if (added.size() > 0) {
            Messaging.sendTr(sender, Messages.TRAITS_ADDED, Joiner.on(", ").join(added));
        }
        if (failed.size() > 0) {
            Messaging.sendTr(sender, Messages.TRAITS_FAILED_TO_ADD, Joiner.on(", ").join(failed));
        }
    }

    private void addTrait(NPC npc, Class<? extends Trait> clazz, CommandSender sender) {
        npc.addTrait(clazz);
        Bukkit.getPluginManager().callEvent(new NPCTraitCommandAttachEvent(npc, clazz, sender));
    }

    @Command(
            aliases = { "trait" },
            usage = "clearsaves [trait name]",
            desc = "",
            modifiers = { "clearsaves" },
            min = 2,
            permission = "citizens.npc.trait.clearsaves")
    public void clearsaves(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> added = Splitter.on(',').splitToStream(args.getJoinedStrings(1))
                .map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        plugin.getDefaultNPCDataStore().clearTraitData(added);
        Messaging.sendTr(sender, Messages.TRAIT_DATA_CLEARED, Joiner.on(", ").join(added));
    }

    @Command(
            aliases = { "trait" },
            usage = "remove [trait names]...",
            desc = "",
            modifiers = { "remove", "rem", "r" },
            min = 2,
            permission = "citizens.npc.trait")
    public void remove(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> removed = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        for (String traitName : Splitter.on(',').split(args.getJoinedStrings(1))) {
            if (!sender.hasPermission("citizens.npc.trait." + traitName)
                    && !sender.hasPermission("citizens.npc.trait.*")) {
                failed.add(String.format("%s: No permission", traitName));
                continue;
            }
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
            if (clazz == null) {
                failed.add(String.format("%s: Trait not found", traitName));
                continue;
            }
            boolean hasTrait = npc.hasTrait(clazz);
            if (!hasTrait) {
                failed.add(String.format("%s: Trait not attached", traitName));
                continue;
            }
            removeTrait(npc, clazz, sender);
            removed.add(StringHelper.wrap(traitName));
        }
        if (removed.size() > 0) {
            Messaging.sendTr(sender, Messages.TRAITS_REMOVED, Joiner.on(", ").join(removed));
        }
        if (failed.size() > 0) {
            Messaging.sendTr(sender, Messages.FAILED_TO_REMOVE, Joiner.on(", ").join(failed));
        }
    }

    private void removeTrait(NPC npc, Class<? extends Trait> clazz, CommandSender sender) {
        Bukkit.getPluginManager().callEvent(new NPCTraitCommandDetachEvent(npc, clazz, sender));
        npc.removeTrait(clazz);
    }

    @Command(
            aliases = { "trait" },
            usage = "[trait name], [trait name]...",
            desc = "",
            modifiers = { "*" },
            min = 1,
            permission = "citizens.npc.trait")
    public void toggle(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> added = Lists.newArrayList();
        List<String> removed = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        for (String traitName : Splitter.on(',').split(args.getJoinedStrings(0))) {
            if (!sender.hasPermission("citizens.npc.trait." + traitName)
                    && !sender.hasPermission("citizens.npc.trait.*")) {
                failed.add(String.format("%s: No permission", traitName));
                continue;
            }
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
            if (clazz == null) {
                failed.add(String.format("%s: Trait not found", traitName));
                continue;
            }
            boolean remove = npc.hasTrait(clazz);
            if (remove) {
                removeTrait(npc, clazz, sender);
                removed.add(StringHelper.wrap(traitName));
                continue;
            }
            addTrait(npc, clazz, sender);
            added.add(StringHelper.wrap(traitName));
        }
        if (added.size() > 0) {
            Messaging.sendTr(sender, Messages.TRAITS_ADDED, Joiner.on(", ").join(added));
        }
        if (removed.size() > 0) {
            Messaging.sendTr(sender, Messages.TRAITS_REMOVED, Joiner.on(", ").join(removed));
        }
        if (failed.size() > 0) {
            Messaging.send(sender, "Failed to toggle traits", Joiner.on(", ").join(failed));
        }
    }
}
