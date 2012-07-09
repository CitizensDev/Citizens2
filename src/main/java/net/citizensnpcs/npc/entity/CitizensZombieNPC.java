package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Zombie;

public class CitizensZombieNPC extends CitizensMobNPC {

    public CitizensZombieNPC(int id, String name) {
        super(id, name, EntityZombieNPC.class);
    }

    @Override
    public Zombie getBukkitEntity() {
        return (Zombie) getHandle().getBukkitEntity();
    }

    public static class EntityZombieNPC extends EntityZombie implements NPCHandle {
        private final CitizensNPC npc;

        public EntityZombieNPC(World world) {
            this(world, null);
        }

        public EntityZombieNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void z_() {
            super.z_();
            if (npc != null)
                npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}