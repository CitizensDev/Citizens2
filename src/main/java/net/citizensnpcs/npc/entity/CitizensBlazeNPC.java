package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityBlaze;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftBlaze;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class CitizensBlazeNPC extends CitizensMobNPC {

    public CitizensBlazeNPC(int id, String name) {
        super(id, name, EntityBlazeNPC.class);
    }

    @Override
    public Blaze getBukkitEntity() {
        return (Blaze) super.getBukkitEntity();
    }

    public static class BlazeNPC extends CraftBlaze implements NPCHolder {
        private final CitizensNPC npc;

        public BlazeNPC(EntityBlazeNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityBlazeNPC extends EntityBlaze implements NPCHolder {
        private final CitizensNPC npc;

        public EntityBlazeNPC(World world) {
            this(world, null);
        }

        public EntityBlazeNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
                NMS.setPersistent(this);
            }
        }

        @Override
        public void bn() {
            if (npc != null)
                npc.update();
            else
                super.bn();
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
        public Entity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new BlazeNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}