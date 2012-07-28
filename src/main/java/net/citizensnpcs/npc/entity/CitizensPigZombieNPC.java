package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityPigZombie;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.PigZombie;
import org.bukkit.util.Vector;

public class CitizensPigZombieNPC extends CitizensMobNPC {

    public CitizensPigZombieNPC(int id, String name) {
        super(id, name, EntityPigZombieNPC.class);
    }

    @Override
    public PigZombie getBukkitEntity() {
        return (PigZombie) getHandle().getBukkitEntity();
    }

    public static class EntityPigZombieNPC extends EntityPigZombie implements NPCHolder {
        private final CitizensNPC npc;

        public EntityPigZombieNPC(World world) {
            this(world, null);
        }

        public EntityPigZombieNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                goalSelector = new PathfinderGoalSelector();
                targetSelector = new PathfinderGoalSelector();
            }
        }

        @Override
        public void b_(double x, double y, double z) {
            if (npc == null) {
                super.b_(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0)
                return;
            NPCPushEvent event = Util.callPushEvent(npc, new Vector(x, y, z));
            if (!event.isCancelled())
                super.b_(x, y, z);
            // when another entity collides, b_ is called to push the NPC
            // so we prevent b_ from doing anything if the event is cancelled.
        }

        @Override
        public void collide(net.minecraft.server.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            Util.callCollisionEvent(npc, entity);
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