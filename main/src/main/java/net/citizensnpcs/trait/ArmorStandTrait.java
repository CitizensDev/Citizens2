package net.citizensnpcs.trait;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import net.citizensnpcs.api.npc.NPC;
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

    public boolean getGravity() {
        return gravity;
    }

    public boolean getHasArms() {
        return hasarms;
    }

    public boolean getHasBaseplate() {
        return hasbaseplate;
    }

    public boolean isMarker() {
        return marker;
    }

    public boolean isSmall() {
        return small;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void onPreSpawn() {
        onSpawn();
    }

    @Override
    public void onSpawn() {
        if (!(npc.getEntity() instanceof ArmorStand))
            return;
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
        entity.setVisible(visible);
        entity.setGravity(gravity);
        entity.setArms(hasarms);
        entity.setBasePlate(hasbaseplate);
        entity.setSmall(small);
        entity.setMarker(marker);
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof ArmorStand))
            return;
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

    public void setAsHelperEntity(NPC parent) {
        npc.addTrait(new ClickRedirectTrait(parent));
        setAsPointEntity();
    }

    public void setAsHelperEntityWithName(NPC parent) {
        npc.addTrait(new ClickRedirectTrait(parent));
        setAsPointEntityWithName();
    }

    /**
     * Configures the entity as an invisible point entity, e.g. for mounting NPCs on top, nameplates, etc.
     */
    public void setAsPointEntity() {
        setGravity(false);
        setHasArms(false);
        setHasBaseplate(false);
        setSmall(true);
        setMarker(true);
        setVisible(false);
        npc.setProtected(true);
        npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
    }

    public void setAsPointEntityWithName() {
        setAsPointEntity();
        npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, true);
    }

    /**
     * @see ArmorStand#setGravity(boolean)
     */
    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }

    /**
     * @see ArmorStand#setArms(boolean)
     */
    public void setHasArms(boolean arms) {
        hasarms = arms;
    }

    /**
     * @see ArmorStand#setBasePlate(boolean)
     */
    public void setHasBaseplate(boolean baseplate) {
        hasbaseplate = baseplate;
    }

    /**
     * @see ArmorStand#setMarker(boolean)
     */
    public void setMarker(boolean marker) {
        this.marker = marker;
    }

    /**
     * @see ArmorStand#setSmall(boolean)
     */
    public void setSmall(boolean small) {
        this.small = small;
    }

    /**
     * @see ArmorStand#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
