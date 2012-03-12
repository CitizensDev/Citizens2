package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityMagmaCube;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.MagmaCube;

public class CitizensMagmaCubeNPC extends CitizensMobNPC {

    public CitizensMagmaCubeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityMagmaCubeNPC.class);
    }

    @Override
    public MagmaCube getBukkitEntity() {
        return (MagmaCube) getHandle().getBukkitEntity();
    }

    public static class EntityMagmaCubeNPC extends EntityMagmaCube implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return this.npc;
        }

        public EntityMagmaCubeNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            setSize(3);
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }
    }
}