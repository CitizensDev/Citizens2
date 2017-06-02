package net.citizensnpcs.nms.v1_12_R1.trait;

import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("llamatrait")
public class LlamaTrait extends Trait {
    @Persist
    private Color color = Color.BROWN;
    @Persist
    private int strength = 3;

    public LlamaTrait() {
        super("llamatrait");
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Llama) {
            Llama llama = (Llama) npc.getEntity();
            llama.setColor(color);
            llama.setStrength(strength);
        }
    }

    public void setColor(Llama.Color color) {
        this.color = color;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }
}
