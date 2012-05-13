package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Ghast;

public class CitizensGhastNPC extends CitizensMobNPC {

    public CitizensGhastNPC(int id, String name) {
        super(id, name, EntityGhastNPC.class);
    }

    @Override
    public Ghast getBukkitEntity() {
        return (Ghast) getHandle().getBukkitEntity();
    }

    public static class EntityGhastNPC extends EntityGhast implements NPCHandle {
        private final CitizensNPC npc;

        public EntityGhastNPC(World world) {
            this(world, null);
        }

        public EntityGhastNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
            if (npc != null)
                npc.update();
            else
                super.d_();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}