package net.citizensnpcs.trait;

import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Location;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.EntityDim;
import net.citizensnpcs.util.NMS;

@TraitName("boundingbox")
public class BoundingBoxTrait extends Trait implements Supplier<BoundingBox> {
    private EntityDim base;
    private Function<EntityDim, BoundingBox> function;
    @Persist
    private float height = -1;
    @Persist
    private float scale = -1;
    @Persist
    private float width = -1;

    public BoundingBoxTrait() {
        super("boundingbox");
    }

    @Override
    public BoundingBox get() {
        Location location = npc.getEntity().getLocation();
        if (function != null) {
            BoundingBox bb = function.apply(getAdjustedBoundingBox());
            NMS.setDimensions(npc.getEntity(), bb.toDimensions());
            return bb.add(location);
        }
        EntityDim dim = getAdjustedBoundingBox();
        NMS.setDimensions(npc.getEntity(), dim);
        return new BoundingBox(location.getX() - dim.width / 2, location.getY(), location.getZ() - dim.width / 2,
                location.getX() + dim.width / 2, location.getY() + dim.height, location.getZ() + dim.width / 2);
    }

    public EntityDim getAdjustedBoundingBox() {
        EntityDim desired = base;
        if (scale != -1) {
            desired = desired.mul(scale);
        }
        return new EntityDim(width == -1 ? desired.width : width, height == -1 ? desired.height : height);
    }

    @Override
    public void onDespawn() {
        npc.data().remove(NPC.Metadata.BOUNDING_BOX_FUNCTION);
    }

    @Override
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void onSpawn() {
        base = EntityDim.from(npc.getEntity());
        npc.data().set(NPC.Metadata.BOUNDING_BOX_FUNCTION, this);
    }

    public void setBoundingBoxFunction(Function<EntityDim, BoundingBox> func) {
        function = func;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}