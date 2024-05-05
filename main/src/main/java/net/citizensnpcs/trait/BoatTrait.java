package net.citizensnpcs.trait;

import org.bukkit.entity.Boat;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("boattrait")
public class BoatTrait extends Trait {
    @Persist
    private Boat.Type type;

    public BoatTrait() {
        super("boattrait");
    }

    public Boat.Type getType() {
        return type;
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Boat) {
            if (type != null) {
                ((Boat) npc.getEntity()).setBoatType(type);
            }
        }
    }

    public void setType(Boat.Type type) {
        this.type = type;
        onSpawn();
    }
}