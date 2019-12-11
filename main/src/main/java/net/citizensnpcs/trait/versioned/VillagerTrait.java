package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Villager;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("villagertrait")
public class VillagerTrait extends Trait {
    @Persist
    private int level = 1;
    @Persist
    private Villager.Type type;

    public VillagerTrait() {
        super("villagertrait");
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof Villager))
            return;
        if (type != null) {
            ((Villager) npc.getEntity()).setVillagerType(type);
        }
        level = Math.min(5, Math.max(1, level));
        ((Villager) npc.getEntity()).setVillagerLevel(level);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setType(Villager.Type type) {
        this.type = type;
    }
}
