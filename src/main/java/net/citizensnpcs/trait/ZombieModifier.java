package net.citizensnpcs.trait;

import org.bukkit.entity.Zombie;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("zombiemodifier")
public class ZombieModifier extends Trait {
    @Persist
    private boolean baby;
    @Persist
    private boolean villager;
    private boolean zombie;

    public ZombieModifier() {
        super("zombiemodifier");
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Zombie) {
            ((Zombie) npc.getEntity()).setVillager(villager);
            ((Zombie) npc.getEntity()).setBaby(baby);
            zombie = true;
        } else
            zombie = false;
    }

    public boolean toggleBaby() {
        baby = !baby;
        if (zombie)
            ((Zombie) npc.getEntity()).setBaby(baby);
        return baby;
    }

    public boolean toggleVillager() {
        villager = !villager;
        if (zombie)
            ((Zombie) npc.getEntity()).setVillager(villager);
        return villager;
    }
}