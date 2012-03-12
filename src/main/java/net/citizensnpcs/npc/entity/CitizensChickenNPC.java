package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityChicken;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Chicken;

public class CitizensChickenNPC extends CitizensMobNPC {

    public CitizensChickenNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityChickenNPC.class);
    }

    @Override
    public Chicken getBukkitEntity() {
        return (Chicken) getHandle().getBukkitEntity();
    }

    public static class EntityChickenNPC extends EntityChicken implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return this.npc;
        }

        public EntityChickenNPC(World world, NPC npc) {
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