package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityMagmaCube;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.MagmaCube;

public class CitizensMagmaCubeNPC extends CitizensMobNPC {

    public CitizensMagmaCubeNPC(int id, String name) {
        super(id, name, EntityMagmaCubeNPC.class);
    }

    @Override
    public MagmaCube getBukkitEntity() {
        return (MagmaCube) getHandle().getBukkitEntity();
    }

    public static class EntityMagmaCubeNPC extends EntityMagmaCube implements NPCHandle {
        private final CitizensNPC npc;

        public EntityMagmaCubeNPC(World world) {
            this(world, null);
        }

        public EntityMagmaCubeNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            setSize(3);
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