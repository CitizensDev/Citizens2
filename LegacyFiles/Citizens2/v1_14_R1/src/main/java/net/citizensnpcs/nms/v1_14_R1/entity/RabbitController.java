package net.citizensnpcs.nms.v1_14_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftRabbit;
import org.bukkit.entity.Rabbit;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_14_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_14_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.DataWatcherObject;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityBoat;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityRabbit;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumPistonReaction;
import net.minecraft.server.v1_14_R1.FluidType;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.SoundEffect;
import net.minecraft.server.v1_14_R1.Tag;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;

public class RabbitController extends MobEntityController {
    public RabbitController() {
        super(EntityRabbitNPC.class);
    }

    @Override
    public Rabbit getBukkitEntity() {
        return (Rabbit) super.getBukkitEntity();
    }

    public static class EntityRabbitNPC extends EntityRabbit implements NPCHolder {
        private final CitizensNPC npc;

        public EntityRabbitNPC(EntityTypes<? extends EntityRabbit> types, World world) {
            this(types, world, null);
        }

        public EntityRabbitNPC(EntityTypes<? extends EntityRabbit> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void a(DataWatcherObject<?> datawatcherobject) {
            if (npc == null) {
                super.a(datawatcherobject);
                return;
            }
            NMSImpl.checkAndUpdateHeight(this, datawatcherobject, super::a);
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public void a(Entity entity, float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, evt -> super.a(entity, (float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public boolean a(EntityPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.a(player));
        }

        @Override
        public void b(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.b(f, f1);
            }
        }

        @Override
        public boolean b(Tag<FluidType> tag) {
            if (npc == null)
                return super.b(tag);
            Vec3D old = getMot().add(0, 0, 0);
            boolean res = super.b(tag);
            if (!npc.isPushableByFluids()) {
                setMot(old);
            }
            return res;
        }

        @Override
        public int bv() {
            return NMS.getFallDistance(npc, super.bv());
        }

        @Override
        protected void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_14_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public float cX() {
            return NMS.getJumpPower(npc, super.cX());
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        public void e(Vec3D vec3d) {
            if (npc == null || !npc.isFlyable()) {
                super.e(vec3d);
            } else {
                NMSImpl.flyingMoveLogic(this, vec3d);
            }
        }

        @Override
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new RabbitNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public EntityLiving getGoalTarget() {
            return npc != null ? null : super.getGoalTarget();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public EnumPistonReaction getPushReaction() {
            return Util.callPistonPushEvent(npc) ? EnumPistonReaction.IGNORE : super.getPushReaction();
        }

        @Override
        protected SoundEffect getSoundAmbient() {
            return NMSImpl.getSoundEffect(npc, super.getSoundAmbient(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        protected SoundEffect getSoundDeath() {
            return NMSImpl.getSoundEffect(npc, super.getSoundDeath(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        protected SoundEffect getSoundHurt(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.getSoundHurt(damagesource), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public boolean isClimbing() {
            if (npc == null || !npc.isFlyable())
                return super.isClimbing();
            else
                return false;
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        public void mobTick() {
            if (npc != null) {
                super.mobTick();
                if (npc.getNavigator().isNavigating()) {
                    NMS.setShouldJump(getBukkitEntity());
                }
                npc.update();
            } else {
                super.mobTick();
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract))
                return !npc.isProtected();
            return super.n(entity);
        }

        @Override
        public void setRabbitType(int type) {
            if (npc == null) {
                super.setRabbitType(type);
                return;
            }
            if (npc.useMinecraftAI()) {
                if (goalSelector.c().count() == 0) {
                    initPathfinder(); // make sure the evil goals include the default AI goals
                }
                super.setRabbitType(type);
                NMSImpl.clearGoals(goalSelector, targetSelector);
            } else if (NMSImpl.getRabbitTypeField() != null) {
                datawatcher.set(NMSImpl.getRabbitTypeField(), type);
            }
        }
    }

    public static class RabbitNPC extends CraftRabbit implements NPCHolder {
        private final CitizensNPC npc;

        public RabbitNPC(EntityRabbitNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}
