package net.citizensnpcs.trait;

import java.lang.invoke.MethodHandle;

import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

/**
 * Persists various {@link Horse} metadata.
 *
 * @see Horse
 */
@TraitName("horsemodifiers")
public class HorseModifiers extends Trait {
    @Persist("armor")
    private ItemStack armor = null;
    @Persist("carryingChest")
    private boolean carryingChest;
    @Persist("color")
    private Color color = Color.CREAMY;
    @Persist("saddle")
    private ItemStack saddle = null;
    @Persist("style")
    private Style style = Style.NONE;

    public HorseModifiers() {
        super("horsemodifiers");
    }

    public ItemStack getArmor() {
        return armor;
    }

    /**
     * @see Horse#getColor()
     */
    public Color getColor() {
        return color;
    }

    public ItemStack getSaddle() {
        return saddle;
    }

    /**
     * @see Horse#getStyle()
     */
    public Style getStyle() {
        return style;
    }

    @Override
    public void onSpawn() {
        updateModifiers();
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Horse) {
            Horse horse = (Horse) npc.getEntity();
            saddle = horse.getInventory().getSaddle();
            armor = horse.getInventory().getArmor();
        }
    }

    public void setArmor(ItemStack armor) {
        this.armor = armor;
    }

    /**
     * @see Horse#setCarryingChest(boolean)
     */
    public void setCarryingChest(boolean carryingChest) {
        this.carryingChest = carryingChest;
        updateModifiers();
    }

    /**
     * @see Horse#setColor(Color)
     */
    public void setColor(Horse.Color color) {
        this.color = color;
        updateModifiers();
    }

    public void setSaddle(ItemStack saddle) {
        this.saddle = saddle;
    }

    /**
     * @see Horse#setStyle(Style)
     */
    public void setStyle(Horse.Style style) {
        this.style = style;
        updateModifiers();
    }

    private void updateModifiers() {
        if (npc.getEntity() instanceof Horse) {
            Horse horse = (Horse) npc.getEntity();
            horse.setColor(color);
            horse.setStyle(style);
            horse.getInventory().setArmor(armor);
            horse.getInventory().setSaddle(saddle);
        }
        if (CARRYING_CHEST_METHOD == null)
            return;
        if (npc.getEntity() instanceof ChestedHorse) {
            try {
                CARRYING_CHEST_METHOD.invoke(npc.getEntity(), carryingChest);
            } catch (Throwable e) {
            }
        }
    }

    private static MethodHandle CARRYING_CHEST_METHOD;

    static {
        try {
            CARRYING_CHEST_METHOD = NMS.getMethodHandle(Class.forName("org.bukkit.entity.ChestedHorse"),
                    "setCarryingChest", false, boolean.class);
        } catch (Throwable e) {
        }
    }
}
