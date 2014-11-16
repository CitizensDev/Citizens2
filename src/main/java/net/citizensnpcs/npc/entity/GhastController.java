package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_7_R4.EntityGhast;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftGhast;
import org.bukkit.entity.Ghast;
import org.bukkit.util.Vector;

public class GhastController extends MobEntityController {
    public GhastController() {
        super(EntityGhastNPC.class);
    }

    @Override
    public Ghast getBukkitEntity() {
        return (Ghast) super.getBukkitEntity();
    }

    public static class EntityGhastNPC extends EntityGhast implements NPCHolder {
        private final CitizensNPC npc;

        public EntityGhastNPC(World world) {
            this(world, null);
        }

        public EntityGhastNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        protected String aT() {
            return npc == null ? super.aT() : npc.data().get(NPC.HURT_SOUND_METADATA, super.aT());
        }

        @Override
        protected String aU() {
            return npc == null ? super.aU() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.aU());
        }

        @Override
        public boolean bN() {
            if (npc == null)
                return super.bN();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.bN();
            if (super.bN()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void bq() {
            if (npc != null) {
                npc.update();
            } else
                super.bq();
        }

        @Override
        public void collide(net.minecraft.server.v1_7_R4.Entity entity) {
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
                bukkitEntity = new GhastNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        protected String t() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.t() : npc.data().get(
                    NPC.AMBIENT_SOUND_METADATA, super.t());
        }

        @Override
        protected void w() {
            if (npc == null) {
                super.w();
            }
        }
    }

    public static class GhastNPC extends CraftGhast implements NPCHolder {
        private final CitizensNPC npc;

        public GhastNPC(EntityGhastNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}