package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityGiantZombie;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Giant;

public class CitizensGiantNPC extends CitizensMobNPC {

    public CitizensGiantNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityGiantNPC.class);
    }

    @Override
    public Giant getBukkitEntity() {
        return (Giant) getHandle().getBukkitEntity();
    }

    public static class EntityGiantNPC extends EntityGiantZombie implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return npc;
        }

        public EntityGiantNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }
    }
}