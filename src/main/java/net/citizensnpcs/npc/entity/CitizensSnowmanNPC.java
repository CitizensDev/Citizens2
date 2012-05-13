package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntitySnowman;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Snowman;

public class CitizensSnowmanNPC extends CitizensMobNPC {

    public CitizensSnowmanNPC(int id, String name) {
        super(id, name, EntitySnowmanNPC.class);
    }

    @Override
    public Snowman getBukkitEntity() {
        return (Snowman) getHandle().getBukkitEntity();
    }

    public static class EntitySnowmanNPC extends EntitySnowman implements NPCHandle {
        private final CitizensNPC npc;

        public EntitySnowmanNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void z_() {
            super.z_();
            npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}