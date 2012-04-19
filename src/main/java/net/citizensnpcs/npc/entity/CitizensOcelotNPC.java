package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityOcelot;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Ocelot;

public class CitizensOcelotNPC extends CitizensMobNPC {

    public CitizensOcelotNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityOcelotNPC.class);
    }

    @Override
    public Ocelot getBukkitEntity() {
        return (Ocelot) getHandle().getBukkitEntity();
    }

    public static class EntityOcelotNPC extends EntityOcelot implements NPCHandle {
        private final CitizensNPC npc;

        public EntityOcelotNPC(World world, CitizensNPC npc) {
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