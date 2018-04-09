package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Rabbit;

@TraitName("rabbittype")
public class RabbitType extends Trait {
    private Rabbit rabbit;
    @Persist
    private Rabbit.Type type = Rabbit.Type.BROWN;

    public RabbitType() {
        super("rabbittype");
    }

    @Override
    public void onSpawn() {
        rabbit = npc.getEntity() instanceof Rabbit ? (Rabbit) npc.getEntity() : null;
        setType(type);
    }

    public void setType(Rabbit.Type type) {
        this.type = type;
        if (rabbit != null && rabbit.isValid()) {
            rabbit.setRabbitType(type);
        }
    }
}
