package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.EntityPigZombie;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.PigZombie;

public class CitizensPigZombieNPC extends CitizensMobNPC {

    public CitizensPigZombieNPC(int id, String name) {
        super(id, name, EntityPigZombieNPC.class);
    }

    @Override
    public PigZombie getBukkitEntity() {
        return (PigZombie) getHandle().getBukkitEntity();
    }

    public static class EntityPigZombieNPC extends EntityPigZombie implements NPCHolder {
        private final CitizensNPC npc;

        public EntityPigZombieNPC(World world) {
            this(world, null);
        }

        public EntityPigZombieNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
            if (npc != null)
                npc.update();
            else
                super.d_();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}