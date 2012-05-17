package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;

public class WoolColor extends Trait implements Listener {
    private DyeColor color = DyeColor.WHITE;
    private final NPC npc;
    boolean sheep = false;

    public WoolColor(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            color = DyeColor.valueOf(key.getString(""));
        } catch (Exception ex) {
            color = DyeColor.WHITE;
        }
    }

    @Override
    public void onNPCSpawn() {
        if (npc.getBukkitEntity() instanceof Sheep) {
            ((Sheep) npc.getBukkitEntity()).setColor(color);
            sheep = true;
        } else
            sheep = false;
    }

    @EventHandler
    public void onSheepDyeWool(SheepDyeWoolEvent event) {
        if (npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getEntity())))
            event.setCancelled(true);
    }

    @Override
    public void save(DataKey key) {
        key.setString("", color.name());
    }

    public void setColor(DyeColor color) {
        this.color = color;
        if (sheep)
            ((Sheep) npc.getBukkitEntity()).setColor(color);
    }

    @Override
    public String toString() {
        return "WoolColor{" + color.name() + "}";
    }
}