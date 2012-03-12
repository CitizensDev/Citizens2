package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityWeatherLighting;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Pig;

public class CitizensPigNPC extends CitizensMobNPC {

    public CitizensPigNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityPigNPC.class);
    }

    @Override
    public Pig getBukkitEntity() {
        return (Pig) getHandle().getBukkitEntity();
    }

    public static class EntityPigNPC extends EntityPig implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return this.npc;
        }

        public EntityPigNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }

        @Override
        public void a(EntityWeatherLighting entityweatherlighting) {
        }
    }
}