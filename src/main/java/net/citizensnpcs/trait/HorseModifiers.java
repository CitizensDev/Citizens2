package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;

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
    @Persist("type")
    private Variant type = Variant.HORSE;

    public HorseModifiers() {
        super("horsemodifiers");
    }

    public Color getColor() {
        return color;
    }

    public Style getStyle() {
        return style;
    }

    public Variant getType() {
        return type;
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

    public void setCarryingChest(boolean carryingChest) {
        this.carryingChest = carryingChest;
        updateModifiers();
    }

    public void setColor(Horse.Color color) {
        this.color = color;
        updateModifiers();
    }

    public void setStyle(Horse.Style style) {
        this.style = style;
        updateModifiers();
    }

    public void setType(Horse.Variant type) {
        this.type = type;
        updateModifiers();
    }

    private void updateModifiers() {
        if (npc.getEntity() instanceof Horse) {
            Horse horse = (Horse) npc.getEntity();
            horse.setCarryingChest(carryingChest);
            horse.setColor(color);
            horse.setStyle(style);
            horse.setVariant(type);
            horse.getInventory().setArmor(armor);
            horse.getInventory().setSaddle(saddle);
        }
    }
}
