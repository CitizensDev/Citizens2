package net.citizensnpcs.npc.ai;

import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMonster;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class MCTargetStrategy implements PathStrategy {
    private final boolean aggro;
    private final EntityLiving handle, target;

    public MCTargetStrategy(CitizensNPC handle, LivingEntity target, boolean aggro) {
        this.handle = handle.getHandle();
        this.target = ((CraftLivingEntity) target).getHandle();
        this.aggro = aggro;
    }

    private boolean canAttack() {
        return handle.attackTicks == 0
                && (handle.boundingBox.e > target.boundingBox.b && handle.boundingBox.b < target.boundingBox.e)
                && distanceSquared() <= ATTACK_DISTANCE && handle.h(target);
    }

    private double distanceSquared() {
        return handle.getBukkitEntity().getLocation().distanceSquared(target.getBukkitEntity().getLocation());
    }

    @Override
    public boolean update() {
        if (target == null || target.dead)
            return true;
        new MCNavigationStrategy(handle, target).update();
        handle.getControllerLook().a(target, 10.0F, handle.D());
        if (aggro && canAttack()) {
            if (handle instanceof EntityMonster) {
                ((EntityMonster) handle).a(target);
            } else if (handle instanceof EntityHuman) {
                ((EntityHuman) handle).attack(target);
            }
        }

        return false;
    }

    private static final double ATTACK_DISTANCE = 1.75 * 1.75;
}