package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Sheep;

public class CitizensSheepNPC extends CitizensMobNPC {

    public CitizensSheepNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySheepNPC.class);
    }

    @Override
    public Sheep getBukkitEntity() {
        return (Sheep) getHandle().getBukkitEntity();
    }

    public static class EntitySheepNPC extends EntitySheep implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return this.npc;
        }

        public EntitySheepNPC(World world, NPC npc) {
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