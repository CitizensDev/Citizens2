package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import com.google.common.base.Joiner;

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
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

@TraitName("villagertrait")
public class VillagerTrait extends Trait {
    @Persist
    private int level = 1;
    @Persist
    private Villager.Type type;

    public VillagerTrait() {
        super("villagertrait");
    }

    public int getLevel() {
        return level;
    }

    public Villager.Type getType() {
        return type;
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof Villager))
            return;
        if (type != null) {
            ((Villager) npc.getEntity()).setVillagerType(type);
        }
        level = Math.min(5, Math.max(1, level));
        ((Villager) npc.getEntity()).setVillagerLevel(level);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setType(Villager.Type type) {
        this.type = type;
    }

    @Command(
            aliases = { "npc" },
            usage = "villager (--level level) (--type type) (--profession profession) -s(hake head)",
            desc = "",
            modifiers = { "villager" },
            min = 1,
            max = 1,
            flags = "s",
            permission = "citizens.npc.villager")
    @Requirements(selected = true, ownership = true, types = EntityType.VILLAGER)
    public static void villager(CommandContext args, CommandSender sender, NPC npc,
            @Flag("profession") Profession profession, @Flag("type") Villager.Type type, @Flag("level") Integer level)
            throws CommandException {
        VillagerTrait trait = npc.getOrAddTrait(VillagerTrait.class);
        String output = "";
        if (level != null) {
            if (level < 0)
                throw new CommandUsageException();
            trait.setLevel(level);
            output += " " + Messaging.tr(Messages.VILLAGER_LEVEL_SET, level);
        }
        if (args.hasValueFlag("type")) {
            if (type == null)
                throw new CommandException(Messages.INVALID_VILLAGER_TYPE,
                        Util.listValuesPretty(Villager.Type.values()));
            trait.setType(type);
            output += " " + Messaging.tr(Messages.VILLAGER_TYPE_SET, args.getFlag("type"));
        }
        if (args.hasValueFlag("profession")) {
            if (profession == null)
                throw new CommandException(Messages.INVALID_PROFESSION, args.getFlag("profession"),
                        Joiner.on(',').join(Profession.values()));
            npc.getOrAddTrait(VillagerProfession.class).setProfession(profession);
            output += " " + Messaging.tr(Messages.PROFESSION_SET, npc.getName(), args.getFlag("profession"));
        }
        if (args.hasFlag('s')) {
            ((Villager) npc.getEntity()).shakeHead();
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
