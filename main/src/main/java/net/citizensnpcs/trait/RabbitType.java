package net.citizensnpcs.trait;

import org.bukkit.entity.Rabbit;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists Rabbit metadata.
 *
 * @see Rabbit#getRabbitType()
 */
@TraitName("rabbittype")
public class RabbitType extends Trait {
    @Persist
    private Rabbit.Type type = Rabbit.Type.BROWN;

    public RabbitType() {
        super("rabbittype");
    }

    public Rabbit.Type getRabbitType() {
        return type;
    }

    @Override
    public void onSpawn() {
        if (npc.getCosmeticEntity() instanceof Rabbit) {
            ((Rabbit) npc.getCosmeticEntity()).setRabbitType(type);
        }
    }

    public void setType(Rabbit.Type type) {
        this.type = type;
    }
}
