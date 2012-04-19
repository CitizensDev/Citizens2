package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Slime;

public class CitizensSlimeNPC extends CitizensMobNPC {

    public CitizensSlimeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySlimeNPC.class);
    }

    @Override
    public Slime getBukkitEntity() {
        return (Slime) getHandle().getBukkitEntity();
    }

    public static class EntitySlimeNPC extends EntitySlime implements NPCHandle {
        private final CitizensNPC npc;

        public EntitySlimeNPC(World world, CitizensNPC npc) {
            super(world);
            this.npc = npc;
            setSize(3);
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