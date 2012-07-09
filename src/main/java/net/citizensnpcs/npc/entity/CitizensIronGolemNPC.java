package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.IronGolem;

public class CitizensIronGolemNPC extends CitizensMobNPC {

    public CitizensIronGolemNPC(int id, String name) {
        super(id, name, EntityIronGolemNPC.class);
    }

    @Override
    public IronGolem getBukkitEntity() {
        return (IronGolem) getHandle().getBukkitEntity();
    }

    public static class EntityIronGolemNPC extends EntityIronGolem implements NPCHolder {
        private final CitizensNPC npc;

        public EntityIronGolemNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void z_() {
            super.z_();
            npc.update();
        }
    }
}