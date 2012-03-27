package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.IronGolem;

public class CitizensIronGolemNPC extends CitizensMobNPC {

    public CitizensIronGolemNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityIronGolemNPC.class);
    }

    @Override
    public IronGolem getBukkitEntity() {
        return (IronGolem) getHandle().getBukkitEntity();
    }

    public static class EntityIronGolemNPC extends EntityIronGolem implements NPCHandle {
        private final NPC npc;

        public EntityIronGolemNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}