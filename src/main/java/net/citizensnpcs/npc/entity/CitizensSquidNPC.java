package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntitySquid;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Squid;

public class CitizensSquidNPC extends CitizensMobNPC {

    public CitizensSquidNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySquidNPC.class);
    }

    @Override
    public Squid getBukkitEntity() {
        return (Squid) getHandle().getBukkitEntity();
    }

    public static class EntitySquidNPC extends EntitySquid implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return npc;
        }

        public EntitySquidNPC(World world, NPC npc) {
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