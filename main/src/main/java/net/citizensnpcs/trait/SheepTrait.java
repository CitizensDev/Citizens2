package net.citizensnpcs.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists {@link Sheep} metadata.
 */
@TraitName("sheeptrait")
public class SheepTrait extends Trait {
    @Persist("color")
    private DyeColor color = DyeColor.WHITE;
    @Persist("sheared")
    private boolean sheared = false;

    public SheepTrait() {
        super("sheeptrait");
    }

    public DyeColor getColor() {
        return color;
    }

    public boolean isSheared() {
        return sheared;
    }

    @EventHandler
    private void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (npc != null && npc.isProtected() && npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Sheep) {
            Sheep sheep = (Sheep) npc.getEntity();
            sheep.setSheared(sheared);
            sheep.setColor(color);
        }
    }

    /**
     * @see Sheep#setColor(DyeColor)
     */
    public void setColor(DyeColor color) {
        this.color = color;
    }

    /**
     * @see Sheep#setSheared(boolean)
     */
    public void setSheared(boolean sheared) {
        this.sheared = sheared;
    }

    public boolean toggleSheared() {
        return sheared = !sheared;
    }
}
