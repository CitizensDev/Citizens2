package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityEnderDragon;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.EnderDragon;

public class CitizensEnderDragonNPC extends CitizensMobNPC {

    public CitizensEnderDragonNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityEnderDragonNPC.class);
    }

    @Override
    public EnderDragon getBukkitEntity() {
        return (EnderDragon) getHandle().getBukkitEntity();
    }

    public static class EntityEnderDragonNPC extends EntityEnderDragon implements NPCHandle {
        private final CitizensNPC npc;

        public EntityEnderDragonNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }

        @Override
        public void e() {
            npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}