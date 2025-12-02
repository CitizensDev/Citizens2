package net.citizensnpcs.npc.ai.tree;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.citizensnpcs.api.ai.tree.BehaviorRegistry;
import net.citizensnpcs.api.ai.tree.BehaviorSignals;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.InstantBehavior;
import net.citizensnpcs.api.expr.ExpressionRegistry;
import net.citizensnpcs.api.expr.ExpressionRegistry.ExpressionValue;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Util;

/**
 * Adds behaviors that depend on implementation classes.
 */
public class CitizensBehaviorRegistry extends BehaviorRegistry {
    public CitizensBehaviorRegistry(ExpressionRegistry expressionRegistry) {
        super(expressionRegistry);
    }

    public CitizensBehaviorRegistry(ExpressionRegistry expressionRegistry, BehaviorSignals signalManager) {
        super(expressionRegistry, signalManager);
    }

    @Override
    protected void registerDefaults() {
        super.registerDefaults();

        registerBehavior("print", (params, context) -> {
            String message = context.getArgOrParam(0, "message", params, null);
            if (message == null && params != null) {
                message = params.getString("");
            }
            if (message == null)
                return null;

            ExpressionValue messageHolder = getExpressionRegistry().parseValue(message);

            return (InstantBehavior) () -> {
                String msg = messageHolder.evaluateAsString(context.getScope());
                Messaging.log(msg);
                return BehaviorStatus.SUCCESS;
            };
        });

        registerBehavior("say", (params, context) -> {
            String message = context.getArgOrParam(0, "message", params, null);
            if (message == null && params != null) {
                message = params.getString("");
            }
            if (message == null)
                return null;

            ExpressionValue messageHolder = getExpressionRegistry().parseValue(message);

            return (InstantBehavior) () -> {
                NPC npc = context.getNPC();
                String msg = messageHolder.evaluateAsString(context.getScope());
                Util.runCommand(npc, null, "say " + msg, false, false);
                return BehaviorStatus.SUCCESS;
            };
        });

        // Makes the NPC look at coordinates
        registerBehavior("look", (params, context) -> {
            String xStr = context.getArgOrParam(0, "x", params, null);
            String yStr = context.getArgOrParam(1, "y", params, null);
            String zStr = context.getArgOrParam(2, "z", params, null);

            if (xStr == null || yStr == null || zStr == null)
                return null;

            ExpressionValue xHolder = getExpressionRegistry().parseValue(xStr);
            ExpressionValue yHolder = getExpressionRegistry().parseValue(yStr);
            ExpressionValue zHolder = getExpressionRegistry().parseValue(zStr);

            return new InstantBehavior() {
                @Override
                public BehaviorStatus run() {
                    NPC npc = context.getNPC();
                    double x = xHolder.evaluateAsNumber(context.getScope());
                    double y = yHolder.evaluateAsNumber(context.getScope());
                    double z = zHolder.evaluateAsNumber(context.getScope());

                    Location target = new Location(npc.getStoredLocation().getWorld(), x, y, z);
                    npc.faceLocation(target);
                    return BehaviorStatus.SUCCESS;
                }

                @Override
                public boolean shouldExecute() {
                    return context.getNPC().isSpawned();
                }
            };
        });

        // Teleports the NPC
        registerBehavior("teleport", (params, context) -> {
            String xStr = context.getArgOrParam(0, "x", params, null);
            String yStr = context.getArgOrParam(1, "y", params, null);
            String zStr = context.getArgOrParam(2, "z", params, null);
            String worldStr = context.getArgOrParam(3, "z", params, null);

            if (xStr == null || yStr == null || zStr == null)
                return null;

            ExpressionValue xHolder = getExpressionRegistry().parseValue(xStr);
            ExpressionValue yHolder = getExpressionRegistry().parseValue(yStr);
            ExpressionValue zHolder = getExpressionRegistry().parseValue(zStr);
            ExpressionValue worldHolder = worldStr == null ? null : getExpressionRegistry().parseValue(worldStr);

            return new InstantBehavior() {

                @Override
                public BehaviorStatus run() {
                    NPC npc = context.getNPC();
                    double x = xHolder.evaluateAsNumber(context.getScope());
                    double y = yHolder.evaluateAsNumber(context.getScope());
                    double z = zHolder.evaluateAsNumber(context.getScope());

                    Location target = new Location(worldHolder == null ? npc.getStoredLocation().getWorld()
                            : Bukkit.getWorld(worldHolder.evaluateAsString(context.getScope())), x, y, z);
                    npc.teleport(target, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
                    return BehaviorStatus.SUCCESS;
                }

                @Override
                public boolean shouldExecute() {
                    return context.getNPC().isSpawned();
                }
            };
        });
    }
}
