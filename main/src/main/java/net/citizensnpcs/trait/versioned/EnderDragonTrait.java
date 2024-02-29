package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("enderdragontrait")
public class EnderDragonTrait extends Trait {
    @Persist
    private boolean destroyWalls;
    @Persist
    private EnderDragon.Phase phase;

    public EnderDragonTrait() {
        super("enderdragontrait");
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isDestroyWalls() {
        return destroyWalls;
    }

    @Override
    public void onSpawn() {
        updateModifiers();
    }

    public void setDestroyWalls(boolean destroyWalls) {
        this.destroyWalls = destroyWalls;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
        updateModifiers();
    }

    private void updateModifiers() {
        if (!(npc.getEntity() instanceof EnderDragon))
            return;
        EnderDragon dragon = (EnderDragon) npc.getEntity();
        if (phase != null) {
            dragon.setPhase(phase);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "enderdragon --phase [phase] --destroywalls [true|false]",
            desc = "",
            modifiers = { "enderdragon" },
            min = 1,
            max = 1,
            permission = "citizens.npc.enderdragon")
    @Requirements(ownership = true, selected = true, types = EntityType.ENDER_DRAGON)
    public static void enderdragon(CommandContext args, CommandSender sender, NPC npc,
            @Flag("phase") EnderDragon.Phase phase, @Flag("destroywalls") Boolean destroyWalls)
            throws CommandException {
        EnderDragonTrait trait = npc.getOrAddTrait(EnderDragonTrait.class);
        if (phase != null) {
            trait.setPhase(phase);
        }
        if (destroyWalls != null) {
            trait.setDestroyWalls(destroyWalls);
        }
    }
}
