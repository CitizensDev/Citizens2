package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityLightning;
import net.minecraft.server.World;

import org.bukkit.entity.Creeper;
import org.bukkit.util.Vector;

public class CitizensCreeperNPC extends CitizensMobNPC {

    public CitizensCreeperNPC(int id, String name) {
        super(id, name, EntityCreeperNPC.class);
    }

    @Override
    public Creeper getBukkitEntity() {
        return (Creeper) super.getBukkitEntity();
    }

    public static class EntityCreeperNPC extends EntityCreeper implements NPCHolder {
        private final CitizensNPC npc;

        public EntityCreeperNPC(World world) {
            this(world, null);
        }

        public EntityCreeperNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void a(EntityLightning entitylightning) {
            if (npc == null)
                super.a(entitylightning);
        }

        @Override
        public void bb() {
            if (npc == null)
                super.bb();
            // check despawn method, we only want to despawn on chunk unload.
        }

        @Override
        public void bc() {
            super.bc();
            if (npc != null)
                npc.update();
        }

        @Override
        public void collide(net.minecraft.server.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity);
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
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