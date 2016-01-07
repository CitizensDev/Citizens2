package net.citizensnpcs.trait;

import org.bukkit.entity.Skeleton;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("skeletontype")
public class NPCSkeletonType extends Trait {
    private Skeleton skeleton;
    @Persist
    private org.bukkit.entity.Skeleton.SkeletonType type = org.bukkit.entity.Skeleton.SkeletonType.NORMAL;

    public NPCSkeletonType() {
        super("skeletontype");
    }

    @Override
    public void onSpawn() {
        skeleton = npc.getEntity() instanceof Skeleton ? (Skeleton) npc.getEntity() : null;
    }

    @Override
    public void run() {
        if (skeleton != null) {
            skeleton.setSkeletonType(type);
        }
    }

    public void setType(org.bukkit.entity.Skeleton.SkeletonType type) {
        this.type = type;
    }
}
