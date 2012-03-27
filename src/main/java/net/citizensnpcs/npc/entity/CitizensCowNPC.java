package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityCow;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Cow;

public class CitizensCowNPC extends CitizensMobNPC {

    public CitizensCowNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityCowNPC.class);
    }

    @Override
    public Cow getBukkitEntity() {
        return (Cow) getHandle().getBukkitEntity();
    }

    public static class EntityCowNPC extends EntityCow implements NPCHandle {
        private final NPC npc;

        public EntityCowNPC(World world, NPC npc) {
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