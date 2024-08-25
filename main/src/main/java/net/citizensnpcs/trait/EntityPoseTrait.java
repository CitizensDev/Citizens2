package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("entitypose")
public class EntityPoseTrait extends Trait {
    @Persist
    private EntityPose pose;

    public EntityPoseTrait() {
        super("entitypose");
    }

    public EntityPose getPose() {
        return pose;
    }

    @Override
    public void run() {
        if (pose == null || !npc.isSpawned())
            return;
        NMS.setPose(npc.getEntity(), pose);
    }

    public void setPose(EntityPose pose) {
        this.pose = pose;
    }

    public enum EntityPose {
        CROAKING(8),
        CROUCHING(5),
        DIGGING(14),
        DYING(7),
        EMERGING(13),
        FALL_FLYING(1),
        INHALING(17),
        LONG_JUMPING(6),
        ROARING(11),
        SHOOTING(16),
        SITTING(10),
        SLEEPING(2),
        SLIDING(15),
        SNIFFING(12),
        SPIN_ATTACK(4),
        STANDING(0),
        SWIMMING(3),
        USING_TONGUE(9);

        private final int id;

        private EntityPose(int id) {
            this.id = id;
        }

        @Deprecated
        /**
         * Internal minecraft ID
         */
        public int id() {
            return id;
        }
    }
}
