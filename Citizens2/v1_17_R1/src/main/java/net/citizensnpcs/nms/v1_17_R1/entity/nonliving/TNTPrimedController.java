package net.citizensnpcs.nms.v1_17_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftTNTPrimed;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_17_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_17_R1.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_17_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_17_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TNTPrimedController extends MobEntityController {
    public TNTPrimedController() {
        super(EntityTNTPrimedNPC.class);
    }

    @Override
    public TNTPrimed getBukkitEntity() {
        return (TNTPrimed) super.getBukkitEntity();
    }

    public static class EntityTNTPrimedNPC extends PrimedTnt implements NPCHolder {
        private final CitizensNPC npc;

        public EntityTNTPrimedNPC(EntityType<? extends PrimedTnt> types, Level level) {
            this(types, level, null);
        }

        public EntityTNTPrimedNPC(EntityType<? extends PrimedTnt> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public boolean broadcastToPlayer(ServerPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.broadcastToPlayer(player));
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new TNTPrimedNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public PushReaction getPistonPushReaction() {
            return Util.callPistonPushEvent(npc) ? PushReaction.IGNORE : super.getPistonPushReaction();
        }

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        @Override
        protected AABB makeBoundingBox() {
            return NMSBoundingBox.makeBB(npc, super.makeBoundingBox());
        }

        @Override
        public void push(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.push(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void push(Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.push(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        private int fuseRenewalDelay = 9; // give client some time to make the animation look vanilla-like
        @Override
        public void tick() {
            if (npc != null) {
                if (fuseRenewalDelay-- <= 0) {
                    // DataWatcher refuses to mark dirty if we don't give different values
                    setFuse(Integer.MAX_VALUE - 1);
                    setFuse(Integer.MAX_VALUE);
                    fuseRenewalDelay = 9;
                }
                npc.update();
            } else {
                super.tick();
            }
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(Tag<Fluid> Tag, double d0) {
            if (npc == null)
                return super.updateFluidHeightAndDoFluidPushing(Tag, d0);
            Vec3 old = getDeltaMovement().add(0, 0, 0);
            boolean res = super.updateFluidHeightAndDoFluidPushing(Tag, d0);
            if (!npc.isPushableByFluids()) {
                setDeltaMovement(old);
            }
            return res;
        }
    }

    public static class TNTPrimedNPC extends CraftTNTPrimed implements ForwardingNPCHolder {
        public TNTPrimedNPC(EntityTNTPrimedNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
