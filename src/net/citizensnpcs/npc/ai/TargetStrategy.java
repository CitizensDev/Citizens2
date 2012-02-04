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
        this.handle = (EntityLiving) handle.getHandle();
        this.target = ((CraftLivingEntity) target).getHandle();
        this.aggro = aggro;
    }

    @Override
    public boolean update() {
        if (target == null || target.dead)
            return true;
        current = new MoveStrategy(handle, handle.world.findPath(handle, target, 16F));
        // NPCManager.faceEntity(handle.getBukkitEntity(),
        // target.getBukkitEntity());
        if (aggro) {
            if (handle instanceof EntityMonster) {
                ((EntityMonster) handle).d(target);
            } else if (handle instanceof EntityHuman) {
                ((EntityHuman) handle).attack(target);
            }
        }
        current.update();
        return false;
    }
}
