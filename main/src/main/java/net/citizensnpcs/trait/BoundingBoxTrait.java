package net.citizensnpcs.trait;

import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.api.CitizensAPI;
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
    private NPC interaction;
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
            BoundingBox bb = function.apply(getAdjustedDimensions());
            NMS.setDimensions(npc.getEntity(), bb.toDimensions());
            return bb.add(location);
        }
        EntityDim dim = getAdjustedDimensions();
        NMS.setDimensions(npc.getEntity(), dim);
        return new BoundingBox(location.getX() - dim.width / 2, location.getY(), location.getZ() - dim.width / 2,
                location.getX() + dim.width / 2, location.getY() + dim.height, location.getZ() + dim.width / 2);
    }

    public EntityDim getAdjustedDimensions() {
        EntityDim desired = base;
        if (scale != -1) {
            desired = desired.mul(scale);
        }
        return new EntityDim(width == -1 ? desired.width : width, height == -1 ? desired.height : height);
    }

    @Override
    public void onDespawn() {
        npc.data().remove(NPC.Metadata.BOUNDING_BOX_FUNCTION);
        if (interaction != null) {
            interaction.destroy();
            interaction = null;
        }
    }

    @Override
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity().getType().toString().contains("BLOCK_DISPLAY")) {
            BoundingBox bb = NMS.getCollisionBox(((BlockDisplay) npc.getEntity()).getBlock());
            base = EntityDim.from(bb);
        } else {
            base = EntityDim.from(npc.getEntity());
        }
        npc.data().set(NPC.Metadata.BOUNDING_BOX_FUNCTION, this);
        if (!SUPPORTS_INTERACTION)
            return;
        interaction = CitizensAPI.getTemporaryNPCRegistry().createNPC(EntityType.INTERACTION, "");
        interaction.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        interaction.addTrait(new ClickRedirectTrait(npc));
        interaction.spawn(npc.getStoredLocation());
        if (SUPPORTS_RESPONSIVE) {
            ((Interaction) interaction.getEntity()).setResponsive(true);
        }
    }

    @Override
    public void run() {
        if (interaction == null)
            return;
        EntityDim dim = getAdjustedDimensions();
        interaction.teleport(npc.getEntity().getLocation(), TeleportCause.PLUGIN);
        Interaction box = ((Interaction) interaction.getEntity());
        box.setInteractionWidth(dim.width);
        box.setInteractionHeight(dim.height);
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

    private static boolean SUPPORTS_INTERACTION = true;
    private static boolean SUPPORTS_RESPONSIVE = true;

    static {
        try {
            Class<?> clazz = Class.forName("org.bukkit.entity.Interaction");
            try {
                clazz.getMethod("isResponsive");
            } catch (NoSuchMethodException | SecurityException e) {
                SUPPORTS_RESPONSIVE = false;
            }
        } catch (ClassNotFoundException e) {
            SUPPORTS_INTERACTION = false;
        }
    }
}