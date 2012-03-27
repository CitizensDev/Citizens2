package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Ghast;

public class CitizensGhastNPC extends CitizensMobNPC {

    public CitizensGhastNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityGhastNPC.class);
    }

    @Override
    public Ghast getBukkitEntity() {
        return (Ghast) getHandle().getBukkitEntity();
    }

    public static class EntityGhastNPC extends EntityGhast implements NPCHandle {
        private final NPC npc;

        public EntityGhastNPC(World world, NPC npc) {
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