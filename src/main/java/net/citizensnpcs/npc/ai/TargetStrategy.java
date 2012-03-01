package net.citizensnpcs.npc.ai;

import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMonster;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class TargetStrategy implements PathStrategy {
    private final EntityLiving handle, target;
    private final boolean aggro;
    private PathStrategy current = null;

    public TargetStrategy(CitizensNPC handle, LivingEntity target, boolean aggro) {
        this.handle = handle.getHandle();
        this.target = ((CraftLivingEntity) target).getHandle();
        this.aggro = aggro;
    }

    @Override
    public boolean update() {
        if (target == null || target.dead)
            return true;
        current = new MoveStrategy(handle, handle.world.findPath(handle, target, 16F, true, false, false, true));
        if (aggro && canAttack()) {
            if (handle instanceof EntityMonster) {
                ((EntityMonster) handle).a(target);
            } else if (handle instanceof EntityHuman) {
                ((EntityHuman) handle).attack(target);
            }
        }

        current.update();
        return false;
    }

    private boolean canAttack() {
        return handle.attackTicks == 0
                && (handle.boundingBox.e > target.boundingBox.b && handle.boundingBox.b < target.boundingBox.e)
                && distanceSquared() <= ATTACK_DISTANCE && handle.h(target);
    }

    private static final double ATTACK_DISTANCE = 1.75 * 1.75;

    private double distanceSquared() {
        return handle.getBukkitEntity().getLocation().distanceSquared(target.getBukkitEntity().getLocation());
    }
}