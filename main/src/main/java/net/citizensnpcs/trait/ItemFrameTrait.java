package net.citizensnpcs.trait;

import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists {@link ItemFrame} metadata.
 */
@TraitName("itemframe")
public class ItemFrameTrait extends Trait {
    @Persist
    private Boolean fixed;
    @Persist
    private ItemStack item;
    @Persist
    private Rotation rotation = Rotation.NONE;
    @Persist
    private boolean visible = true;

    public ItemFrameTrait() {
        super("itemframe");
    }

    public Boolean getFixed() {
        return fixed;
    }

    public ItemStack getItem() {
        return item;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) npc.getEntity();
            if (rotation != null) {
                frame.setRotation(rotation);
            }
            if (item != null) {
                frame.setItem(item);
            }
            if (fixed != null) {
                frame.setFixed(fixed);
            } else {
                frame.setFixed(npc.isProtected());
            }
            frame.setVisible(visible);
        }
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
        onSpawn();
    }

    public void setItem(ItemStack item) {
        this.item = item;
        onSpawn();
    }

    public void setRotation(Rotation rot) {
        rotation = rot;
        onSpawn();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        onSpawn();
    }
}
