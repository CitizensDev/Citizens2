package net.citizensnpcs.trait;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("armorstandtrait")
public class ArmorStandTrait extends Trait {
    @Persist
    private EulerAngle body;
    @Persist
    private boolean gravity = true;
    @Persist
    private boolean hasarms = true;
    @Persist
    private boolean hasbaseplate = true;
    @Persist
    private EulerAngle head;
    @Persist
    private EulerAngle leftArm;
    @Persist
    private EulerAngle leftLeg;
    @Persist
    private boolean marker;
    @Persist
    private EulerAngle rightArm;
    @Persist
    private EulerAngle rightLeg;
    @Persist
    private boolean small;
    @Persist
    private boolean visible = true;

    public ArmorStandTrait() {
        super("armorstandtrait");
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof ArmorStand) {
            ArmorStand entity = (ArmorStand) npc.getEntity();
            if (leftArm != null) {
                entity.setLeftArmPose(leftArm);
            }
            if (leftLeg != null) {
                entity.setLeftLegPose(leftLeg);
            }
            if (rightArm != null) {
                entity.setRightArmPose(rightArm);
            }
            if (rightLeg != null) {
                entity.setRightLegPose(rightLeg);
            }
            if (body != null) {
                entity.setBodyPose(body);
            }
            if (head != null) {
                entity.setHeadPose(head);
            }
        }
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof ArmorStand) {
            ArmorStand entity = (ArmorStand) npc.getEntity();
            body = entity.getBodyPose();
            leftArm = entity.getLeftArmPose();
            leftLeg = entity.getLeftLegPose();
            rightArm = entity.getRightArmPose();
            rightLeg = entity.getRightLegPose();
            head = entity.getHeadPose();
            entity.setVisible(visible);
            entity.setGravity(gravity);
            entity.setArms(hasarms);
            entity.setBasePlate(hasbaseplate);
            entity.setSmall(small);
            entity.setMarker(marker);
        }
    }

    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }

    public void setHasArms(boolean arms) {
        this.hasarms = arms;
    }

    public void setHasBaseplate(boolean baseplate) {
        this.hasbaseplate = baseplate;
    }

    public void setMarker(boolean marker) {
        this.marker = marker;
    }

    public void setSmall(boolean small) {
        this.small = small;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
