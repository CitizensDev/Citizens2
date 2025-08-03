package net.citizensnpcs.api.command;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.RequirementMissingException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.Messaging;

public class RequirementsProcessor implements CommandAnnotationProcessor {
    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return Requirements.class;
    }

    @Override
    public void process(CommandSender sender, CommandContext context, Annotation instance, Object[] methodArgs)
            throws CommandException {
        Requirements requirements = (Requirements) instance;
        NPC npc = methodArgs.length >= 3 && methodArgs[2] instanceof NPC ? (NPC) methodArgs[2] : null;

        boolean canRedefineSelected = (context.hasValueFlag("uuid") || context.hasValueFlag("id"))
                && sender.hasPermission("npc.select");
        String error = Messaging.tr(CommandMessages.MUST_HAVE_SELECTED);
        if (canRedefineSelected) {
            if (context.hasValueFlag("uuid")) {
                npc = CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(UUID.fromString(context.getFlag("uuid")));
            } else {
                npc = CitizensAPI.getNPCRegistry().getById(context.getFlagInteger("id"));
            }
            if (methodArgs.length >= 3) {
                methodArgs[2] = npc;
            }
            if (npc == null) {
                error += ' '
                        + Messaging.tr(CommandMessages.ID_NOT_FOUND, context.getFlag("id", context.getFlag("uuid")));
            }
        }
        if (requirements.selected() && npc == null)
            throw new RequirementMissingException(error);

        if (requirements.ownership() && npc != null && !sender.hasPermission("citizens.admin")
                && !npc.getOrAddTrait(Owner.class).isOwnedBy(sender))
            throw new RequirementMissingException(Messaging.tr(CommandMessages.MUST_BE_OWNER));

        if (npc == null)
            return;

        for (Class<? extends Trait> clazz : requirements.traits()) {
            if (!npc.hasTrait(clazz))
                throw new RequirementMissingException(
                        Messaging.tr(CommandMessages.MISSING_TRAIT, clazz.getSimpleName()));
        }
        Set<EntityType> types = Sets.newEnumSet(Arrays.asList(requirements.types()), EntityType.class);
        if (types.contains(EntityType.UNKNOWN)) {
            types = EnumSet.allOf(EntityType.class);
        }
        types.removeAll(Sets.newHashSet(requirements.excludedTypes()));

        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        if (!types.contains(type))
            throw new RequirementMissingException(Messaging.tr(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE,
                    type.name().toLowerCase(Locale.ROOT).replace('_', ' ')));
        if (requirements.livingEntity() && !type.isAlive())
            throw new RequirementMissingException(
                    Messaging.tr(CommandMessages.REQUIREMENTS_MUST_BE_LIVING_ENTITY, methodArgs));
    }
}
