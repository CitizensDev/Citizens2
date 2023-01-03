package net.citizensnpcs.trait;

import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.EntityDim;
import net.citizensnpcs.util.NMS;

// TODO: could set a function instead which replaces makeBoundingBox
@TraitName("boundingbox")
public class BoundingBoxTrait extends Trait {
    private EntityDim base;
    @Persist
    private float height = -1;
    @Persist
    private float scale = -1;
    @Persist
    private float width = -1;

    public BoundingBoxTrait() {
        super("boundingbox");
    }

    public EntityDim getAdjustedBoundingBox() {
        EntityDim desired = base.clone();
        if (scale != -1) {
            desired = desired.mul(scale);
        }
        return new EntityDim(width == -1 ? desired.width : width, height == -1 ? desired.height : height);
    }

    @Override
    public void onSpawn() {
        base = EntityDim.from(npc.getEntity());
    }

    @Override
    public void run() {
        if (!npc.isUpdating(NPCUpdate.PACKET) || !npc.isSpawned())
            return;

        EntityDim desired = getAdjustedBoundingBox();
        if (!desired.equals(EntityDim.from(npc.getEntity()))) {
            NMS.setDimensions(npc.getEntity(), desired);
        }
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setScale(float s) {
        this.scale = s;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}