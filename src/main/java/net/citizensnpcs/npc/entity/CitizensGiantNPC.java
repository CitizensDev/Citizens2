package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityGiantZombie;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Giant;

public class CitizensGiantNPC extends CitizensMobNPC {

    public CitizensGiantNPC(int id, String name) {
        super(id, name, EntityGiantNPC.class);
    }

    @Override
    public Giant getBukkitEntity() {
        return (Giant) getHandle().getBukkitEntity();
    }

    public static class EntityGiantNPC extends EntityGiantZombie implements NPCHandle {
        private final CitizensNPC npc;

        public EntityGiantNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
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