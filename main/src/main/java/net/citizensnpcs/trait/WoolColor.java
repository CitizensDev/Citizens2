package net.citizensnpcs.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.SheepDyeWoolEvent;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;

@TraitName("woolcolor")
public class WoolColor extends Trait {
    private DyeColor color = DyeColor.WHITE;
    boolean sheep = false;

    public WoolColor() {
        super("woolcolor");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            color = DyeColor.valueOf(key.getString(""));
        } catch (Exception ex) {
            color = DyeColor.WHITE;
        }
    }

    @EventHandler
    public void onSheepDyeWool(SheepDyeWoolEvent event) {
        if (npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getEntity())))
            event.setCancelled(true);
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Sheep) {
            ((Sheep) npc.getEntity()).setColor(color);
            sheep = true;
        } else {
            sheep = false;
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString("", color.name());
    }

    public void setColor(DyeColor color) {
        this.color = color;
        if (sheep) {
            ((Sheep) npc.getEntity()).setColor(color);
        }
    }

    @Override
    public String toString() {
        return "WoolColor{" + color.name() + "}";
    }
}