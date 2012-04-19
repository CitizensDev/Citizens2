package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntitySilverfish;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Silverfish;

public class CitizensSilverfishNPC extends CitizensMobNPC {

    public CitizensSilverfishNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySilverfishNPC.class);
    }

    @Override
    public Silverfish getBukkitEntity() {
        return (Silverfish) getHandle().getBukkitEntity();
    }

    public static class EntitySilverfishNPC extends EntitySilverfish implements NPCHandle {
        private final CitizensNPC npc;

        public EntitySilverfishNPC(World world, CitizensNPC npc) {
            super(world);
            this.npc = npc;
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