package net.citizensnpcs.trait.versioned;

import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("tropicalfishtrait")
public class TropicalFishTrait extends Trait {
    @Persist
    private DyeColor bodyColor = DyeColor.BLUE;
    @Persist
    private Pattern pattern = Pattern.BRINELY;
    @Persist
    private DyeColor patternColor = DyeColor.BLUE;

    public TropicalFishTrait() {
        super("tropicalfishtrait");
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof TropicalFish) {
            TropicalFish fish = (TropicalFish) npc.getEntity();
            fish.setBodyColor(bodyColor);
            fish.setPatternColor(patternColor);
            fish.setPattern(pattern);
        }
    }

    public void setBodyColor(DyeColor color) {
        this.bodyColor = color;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setPatternColor(DyeColor color) {
        this.patternColor = color;
    }
}
