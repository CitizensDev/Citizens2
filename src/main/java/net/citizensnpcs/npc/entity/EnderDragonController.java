package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.util.Vector;

public class EnderDragonController extends MobEntityController {
    public EnderDragonController() {
        super(EntityEnderDragonNPC.class);
    }

    @Override
    public EnderDragon getBukkitEntity() {
        return (EnderDragon) super.getBukkitEntity();
    }

    public static class EnderDragonNPC extends CraftEnderDragon implements NPCHolder {
        private final CitizensNPC npc;

        public EnderDragonNPC(EntityEnderDragonNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityEnderDragonNPC extends EntityEnderDragon implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEnderDragonNPC(World world) {
            this(world, null);
        }

        public EntityEnderDragonNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        protected String bo() {
            return npc == null ? super.bo() : npc.data().get(NPC.HURT_SOUND_METADATA, super.bo());
        }

        @Override
        protected String bp() {
            return npc == null ? super.bp() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.bp());
        }

        @Override
        public boolean cc() {
            if (npc == null)
                return super.cc();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.cc();
            if (super.cc()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void collide(net.minecraft.server.v1_8_R3.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        protected void D() {
            if (npc == null) {
                super.D();
            }
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
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new EnderDragonNPC(this);
            return super.getBukkitEntity();
        }

        private float getCorrectYaw(double tX, double tZ) {
            if (locZ > tZ)
                return (float) (-Math.toDegrees(Math.atan((locX - tX) / (locZ - tZ))));
            if (locZ < tZ) {
                return (float) (-Math.toDegrees(Math.atan((locX - tX) / (locZ - tZ)))) + 180.0F;
            }
            return yaw;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void m() {
            if (npc != null) {
                npc.update();
                if (motX != 0 || motY != 0 || motZ != 0) {
                    motX *= 0.98;
                    motY *= 0.98;
                    motZ *= 0.98;
                    yaw = getCorrectYaw(locX + motX, locZ + motZ);
                    setPosition(locX + motX, locY + motY, locZ + motZ);
                }
            } else {
                super.m();
            }
        }

        @Override
        protected String z() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.z()
                    : npc.data().get(NPC.AMBIENT_SOUND_METADATA, super.z());
        }
    }
}