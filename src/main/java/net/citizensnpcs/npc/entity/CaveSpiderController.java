package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_4_R1.EntityCaveSpider;
import net.minecraft.server.v1_4_R1.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftCaveSpider;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftEntity;
import org.bukkit.entity.CaveSpider;
import org.bukkit.util.Vector;

public class CaveSpiderController extends MobEntityController {
    public CaveSpiderController() {
        super(EntityCaveSpiderNPC.class);
    }

    @Override
    public CaveSpider getBukkitEntity() {
        return (CaveSpider) super.getBukkitEntity();
    }

    public static class CaveSpiderNPC extends CraftCaveSpider implements NPCHolder {
        private final CitizensNPC npc;

        public CaveSpiderNPC(EntityCaveSpiderNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityCaveSpiderNPC extends EntityCaveSpider implements NPCHolder {
        private final CitizensNPC npc;

        public EntityCaveSpiderNPC(World world) {
            this(world, null);
        }

        public EntityCaveSpiderNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);

            }
        }

        @Override
        public float bB() {
            return npc == null ? super.bB() : super.bB() * npc.getNavigator().getLocalParameters().speed();
        }

        @Override
        public void bl() {
            super.bl();
            if (npc != null)
                npc.update();
        }

        @Override
        public void bn() {
            if (npc == null)
                super.bn();
            else {
                NMS.updateAI(this);
                npc.update();
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_4_R1.Entity entity) {
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
                bukkitEntity = new CaveSpiderNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}