package net.citizensnpcs.trait;

import org.bukkit.craftbukkit.v1_10_R1.entity.CraftRabbit;
import org.bukkit.entity.Rabbit;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("rabbittype")
public class RabbitType extends Trait {
    private Rabbit rabbit;
    @Persist
    private RabbitTypes type = RabbitTypes.BROWN;

    public RabbitType() {
        super("rabbittype");
    }

    @Override
    public void onSpawn() {
        rabbit = npc.getEntity() instanceof Rabbit ? (Rabbit) npc.getEntity() : null;
        setType(type);
    }

    public void setType(RabbitTypes type) {
        this.type = type;
        if (rabbit != null && rabbit.isValid()) {
            ((CraftRabbit) rabbit).getHandle().setRabbitType(type.type);
        }
    }

    public enum RabbitTypes {
        BLACK(2),
        BLACKANDWHITE(3),
        BROWN(0),
        GOLD(4),
        KILLER(99),
        SALTANDPEPPER(5),
        WHITE(1);
        public int type;

        RabbitTypes(int type) {
            this.type = type;
        }
    }
}
