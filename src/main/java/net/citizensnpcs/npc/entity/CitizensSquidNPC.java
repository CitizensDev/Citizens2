package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMSReflection;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntitySquid;
import net.minecraft.server.World;

import org.bukkit.entity.Squid;
import org.bukkit.util.Vector;

public class CitizensSquidNPC extends CitizensMobNPC {

    public CitizensSquidNPC(int id, String name) {
        super(id, name, EntitySquidNPC.class);
    }

    @Override
    public Squid getBukkitEntity() {
    	if (getHandle() == null) return null;
        return (Squid) getHandle().getBukkitEntity();
    }

    public static class EntitySquidNPC extends EntitySquid implements NPCHolder {
        private final CitizensNPC npc;

        public EntitySquidNPC(World world) {
            this(world, null);
        }

        public EntitySquidNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMSReflection.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void be() {
            if (npc != null)
                npc.update();
            else
                super.be();
        }

        @Override
        public void collide(net.minecraft.server.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            Util.callCollisionEvent(npc, entity);
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0)
                return;
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

    }
}