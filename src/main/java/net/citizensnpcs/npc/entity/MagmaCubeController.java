package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.EntityMagmaCube;
import net.minecraft.server.v1_6_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftMagmaCube;
import org.bukkit.entity.MagmaCube;
import org.bukkit.util.Vector;

public class MagmaCubeController extends MobEntityController {
    public MagmaCubeController() {
        super(EntityMagmaCubeNPC.class);
    }

    @Override
    public MagmaCube getBukkitEntity() {
        return (MagmaCube) super.getBukkitEntity();
    }

    public static class EntityMagmaCubeNPC extends EntityMagmaCube implements NPCHolder {
        private final CitizensNPC npc;

        public EntityMagmaCubeNPC(World world) {
            this(world, null);
        }

        public EntityMagmaCubeNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                setSize(3);
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void bh() {
            super.bh();
            if (npc != null)
                npc.update();
        }

        @Override
        public boolean bH() {
            if (npc == null)
                return super.bH();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.bH();
            return false; // shouldLeash
        }

        @Override
        public void bk() {
            if (npc == null)
                super.bk();
            else {
                NMS.updateAI(this);
                npc.update();
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_6_R2.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
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
                bukkitEntity = new MagmaCubeNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class MagmaCubeNPC extends CraftMagmaCube implements NPCHolder {
        private final CitizensNPC npc;

        public MagmaCubeNPC(EntityMagmaCubeNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}