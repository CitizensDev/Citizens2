package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Zombie;

public class CitizensZombieNPC extends CitizensMobNPC {

    public CitizensZombieNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityZombieNPC.class);
    }

    @Override
    public Zombie getBukkitEntity() {
        return (Zombie) getHandle().getBukkitEntity();
    }

    public static class EntityZombieNPC extends EntityZombie implements NPCHandle {
        private final CitizensNPC npc;

        public EntityZombieNPC(World world, CitizensNPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
            npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}