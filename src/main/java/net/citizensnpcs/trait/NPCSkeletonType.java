package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.entity.Skeleton;

public class NPCSkeletonType extends Trait {
    private boolean skeleton;
    @Persist
    private org.bukkit.entity.Skeleton.SkeletonType type = org.bukkit.entity.Skeleton.SkeletonType.NORMAL;

    public NPCSkeletonType() {
        super("skeletontype");
    }

    @Override
    public void onSpawn() {
        skeleton = npc.getBukkitEntity() instanceof Skeleton;
    }

    @Override
    public void run() {
        if (skeleton)
            ((Skeleton) npc.getBukkitEntity()).setSkeletonType(type);
    }

    public void setType(org.bukkit.entity.Skeleton.SkeletonType type) {
        this.type = type;
    }
}
