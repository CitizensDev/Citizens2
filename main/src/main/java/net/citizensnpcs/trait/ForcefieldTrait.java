package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("forcefieldtrait")
public class ForcefieldTrait extends Trait {
    @Persist
    private Double height;
    @Persist
    private Double strength;
    @Persist
    private Double verticalStrength;
    @Persist
    private Double width;

    public ForcefieldTrait() {
        super("forcefieldtrait");
    }

    public double getHeight() {
        return height == null ? npc.getEntity().getHeight() : height;
    }

    public double getStrength() {
        return strength == null ? 0.1 : strength;
    }

    public double getVerticalStrength() {
        return verticalStrength == null ? 0 : verticalStrength;
    }

    public double getWidth() {
        return width == null ? npc.getEntity().getWidth() : width;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        double height = getHeight();
        double width = getWidth();
        double strength = getStrength();
        Location base = npc.getEntity().getLocation();
        for (Player player : CitizensAPI.getLocationLookup().getNearbyVisiblePlayers(npc.getEntity(),
                new double[] { base.getX() - width / 1.9, base.getY(), base.getZ() - width / 1.9 },
                new double[] { base.getX() + width / 1.9, base.getY() + height, base.getZ() + width / 1.9 })) {
            Vector diff = player.getLocation().subtract(base).toVector();
            if (diff.isZero())
                continue;
            diff = diff.normalize().setY(getVerticalStrength());
            Vector force = player.getVelocity().add(diff.multiply(strength));
            player.setVelocity(force);
        }
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public void setStrength(Double strength) {
        this.strength = strength;
    }

    public void setVerticalStrength(Double verticalStrength) {
        this.verticalStrength = verticalStrength;
    }

    public void setWidth(Double width) {
        this.width = width;
    }
}
