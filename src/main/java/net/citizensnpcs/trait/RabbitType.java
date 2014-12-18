package net.citizensnpcs.trait;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftRabbit;
import org.bukkit.entity.Rabbit;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_8_R1.EntityRabbit;

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
    }

    @Override
    public void run() {
        if (rabbit != null) {
        	((EntityRabbit)((CraftRabbit)rabbit).getHandle()).r(type.type);
        }
    }

    public void setType(RabbitTypes type) {
    	
        this.type = type;
    }
    public enum RabbitTypes {
    	
    	BROWN(0),
    	WHITE(1),
        BLACK(2),
        BLACKANDWHITE(3),
        GOLD(4),
        SALTANDPEPPER(5),
        KILLER(99);
        public int type;
    	RabbitTypes (int type) {
    		this.type = type;
    	}
    }
}
