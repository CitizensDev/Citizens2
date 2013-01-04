package net.citizensnpcs.command;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.RequirementMissingException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Sets;

public class RequirementsProcessor implements CommandAnnotationProcessor {
    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return Requirements.class;
    }

    @Override
    public void process(CommandSender sender, CommandContext context, Annotation instance, Object[] methodArgs)
            throws CommandException {
        Requirements requirements = (Requirements) instance;
        NPC npc = (methodArgs.length >= 3 && methodArgs[2] instanceof NPC) ? (NPC) methodArgs[2] : null;

        // Requirements
        if (requirements.selected()) {
            boolean canRedefineSelected = context.hasValueFlag("id") && sender.hasPermission("npc.select");
            String error = Messaging.tr(Messages.COMMAND_MUST_HAVE_SELECTED);
            if (canRedefineSelected) {
                npc = CitizensAPI.getNPCRegistry().getById(context.getFlagInteger("id"));
                if (npc == null)
                    error += ' ' + Messaging.tr(Messages.COMMAND_ID_NOT_FOUND, context.getFlagInteger("id"));
            }
            if (npc == null)
                throw new RequirementMissingException(error);
        }

        if (requirements.ownership() && npc != null && !sender.hasPermission("citizens.admin")
                && !npc.getTrait(Owner.class).isOwnedBy(sender))
            throw new RequirementMissingException(Messaging.tr(Messages.COMMAND_MUST_BE_OWNER));

        if (npc == null)
            return;
        for (Class<? extends Trait> clazz : requirements.traits()) {
            if (!npc.hasTrait(clazz))
                throw new RequirementMissingException(Messaging.tr(Messages.COMMAND_MISSING_TRAIT,
                        clazz.getSimpleName()));
        }

        Set<EntityType> types = Sets.newEnumSet(Arrays.asList(requirements.types()), EntityType.class);
        if (types.contains(EntityType.UNKNOWN))
            types = EnumSet.allOf(EntityType.class);
        types.removeAll(Sets.newHashSet(requirements.excludedTypes()));

        EntityType type = npc.getTrait(MobType.class).getType();
        if (!types.contains(type)) {
            throw new RequirementMissingException(Messaging.tr(Messages.COMMAND_REQUIREMENTS_INVALID_MOB_TYPE,
                    type.getName()));
        }
    }
}
