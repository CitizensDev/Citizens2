package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.craftbukkit.entity.CraftSkeleton;

public class SkeletonType extends Trait {
    @Persist
    private int type = 0;
    private boolean skeleton;

    public SkeletonType() {
        super("skeletontype");
    }

    public void setType(int type) {
        this.type = Math.max(0, Math.min(1, type));
    }

    @Override
    public void onSpawn() {
        skeleton = npc.getBukkitEntity() instanceof CraftSkeleton;
    }

    @Override
    public void run() {
        if (skeleton)
            ((CraftSkeleton) npc.getBukkitEntity()).getHandle().setSkeletonType(type);
    }
}
