package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;

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

@TraitName("foxtrait")
public class FoxTrait extends Trait {
    @Persist
    private boolean crouching = false;
    @Persist
    private boolean faceplanted;
    @Persist
    private boolean interested;
    @Persist
    private boolean pouncing;
    @Persist
    private boolean sitting = false;
    @Persist
    private boolean sleeping = false;
    @Persist
    private Fox.Type type = Fox.Type.RED;

    public FoxTrait() {
        super("foxtrait");
    }

    public Fox.Type getType() {
        return type;
    }

    public boolean isCrouching() {
        return crouching;
    }

    public boolean isFaceplanted() {
        return faceplanted;
    }

    public boolean isInterested() {
        return interested;
    }

    public boolean isPouncing() {
        return pouncing;
    }

    public boolean isSitting() {
        return sitting;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Fox) {
            Fox fox = (Fox) npc.getEntity();
            fox.setSitting(sitting);
            fox.setCrouching(crouching);
            fox.setSleeping(sleeping);
            fox.setFoxType(type);
        }
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public void setFaceplanted(boolean faceplanted) {
        this.faceplanted = faceplanted;
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
    }

    public void setPouncing(boolean pouncing) {
        this.pouncing = pouncing;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
    }

    public void setType(Fox.Type type) {
        this.type = type;
    }

    @Command(
            aliases = { "npc" },
            usage = "fox --type type --sleeping [true|false] --sitting [true|false] --crouching [true|false] --interested [true|false] --pouncing [true|false] --faceplanted [true|false]",
            desc = "",
            modifiers = { "fox" },
            min = 1,
            max = 1,
            permission = "citizens.npc.fox")
    @Requirements(selected = true, ownership = true, types = EntityType.FOX)
    public static void fox(CommandContext args, CommandSender sender, NPC npc, @Flag("sleeping") Boolean sleeping,
            @Flag("sitting") Boolean sitting, @Flag("crouching") Boolean crouching,
            @Flag(value = "type", completions = { "RED", "SNOW" }) String rawtype, @Flag("pouncing") Boolean pouncing,
            @Flag("interested") Boolean interested, @Flag("faceplanted") Boolean faceplanted) throws CommandException {
        FoxTrait trait = npc.getOrAddTrait(FoxTrait.class);
        String output = "";
        if (rawtype != null) {
            Fox.Type type = Util.matchEnum(Fox.Type.values(), args.getFlag("type"));
            if (type == null)
                throw new CommandUsageException(
                        Messaging.tr(Messages.INVALID_FOX_TYPE, Util.listValuesPretty(Fox.Type.values())), null);
            trait.setType(type);
            output += ' ' + Messaging.tr(Messages.FOX_TYPE_SET, args.getFlag("type"), npc.getName());
        }
        if (sleeping != null) {
            trait.setSleeping(sleeping);
            output += ' '
                    + Messaging.tr(sleeping ? Messages.FOX_SLEEPING_SET : Messages.FOX_SLEEPING_UNSET, npc.getName());
        }
        if (sitting != null) {
            trait.setSitting(sitting);
            output += ' '
                    + Messaging.tr(sitting ? Messages.FOX_SITTING_SET : Messages.FOX_SITTING_UNSET, npc.getName());
        }
        if (crouching != null) {
            trait.setCrouching(crouching);
            output += ' ' + Messaging.tr(crouching ? Messages.FOX_CROUCHING_SET : Messages.FOX_CROUCHING_UNSET,
                    npc.getName());
        }
        if (interested != null) {
            trait.setInterested(interested);
            output += ' ' + Messaging.tr(interested ? Messages.FOX_INTERESTED_SET : Messages.FOX_INTERESTED_UNSET,
                    npc.getName());
        }
        if (pouncing != null) {
            trait.setPouncing(pouncing);
            output += ' '
                    + Messaging.tr(pouncing ? Messages.FOX_POUNCING_SET : Messages.FOX_POUNCING_UNSET, npc.getName());
        }
        if (faceplanted != null) {
            trait.setFaceplanted(faceplanted);
            output += ' ' + Messaging.tr(faceplanted ? Messages.FOX_FACEPLANTED_SET : Messages.FOX_FACEPLANTED_UNSET,
                    npc.getName());
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
