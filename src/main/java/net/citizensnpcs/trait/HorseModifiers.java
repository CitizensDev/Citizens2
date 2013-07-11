package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;

public class HorseModifiers extends Trait {
    @Persist("carryingChest")
    private boolean carryingChest;
    @Persist("color")
    private Color color = Color.CREAMY;
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
        if (npc.getBukkitEntity() instanceof Horse) {
            Horse horse = (Horse) npc.getBukkitEntity();
            horse.setCarryingChest(carryingChest);
            horse.setColor(color);
            horse.setStyle(style);
            horse.setVariant(type);
        }
    }
}
