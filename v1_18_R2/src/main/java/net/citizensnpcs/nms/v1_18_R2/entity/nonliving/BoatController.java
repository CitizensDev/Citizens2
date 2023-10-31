package net.citizensnpcs.nms.v1_18_R2.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_18_R2.entity.MobEntityController;
import net.citizensnpcs.nms.v1_18_R2.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_18_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_18_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BoatController extends MobEntityController {
    public BoatController() {
        super(EntityBoatNPC.class);
    }

    @Override
    public org.bukkit.entity.Boat getBukkitEntity() {
        return (org.bukkit.entity.Boat) super.getBukkitEntity();
    }

    public static class BoatNPC extends CraftBoat implements ForwardingNPCHolder {
        public BoatNPC(EntityBoatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityBoatNPC extends Boat implements NPCHolder {
        private double aC;

        private float aD;
        private Status aE;
        private Status aF;
        private double ap;
        private double ar;
        private final CitizensNPC npc;

        public EntityBoatNPC(EntityType<? extends Boat> types, Level level) {
            this(types, level, null);
        }

        public EntityBoatNPC(EntityType<? extends Boat> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new BoatNPC(this));
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

        private Status getStatus() {
            Status entityboat_Status = u();
            if (entityboat_Status != null) {
                this.aC = (getBoundingBox()).maxY;
                return entityboat_Status;
            }
            if (t())
                return Status.IN_WATER;
            float f = getGroundFriction();
            if (f > 0.0F) {
                this.aD = f;
                return Status.ON_LAND;
            }
            return Status.IN_AIR;
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
        public void refreshDimensions() {
            if (npc == null) {
                super.refreshDimensions();
            } else {
                NMSImpl.setSize(this, firstTick);
            }
        }

        @Override
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        private boolean t() {
            boolean m = false;
            AABB axisalignedbb = getBoundingBox();
            int i = Mth.floor(axisalignedbb.minX);
            int j = Mth.ceil(axisalignedbb.maxX);
            int k = Mth.floor(axisalignedbb.minY);
            int l = Mth.ceil(axisalignedbb.minY + 0.001D);
            int i1 = Mth.floor(axisalignedbb.minZ);
            int j1 = Mth.ceil(axisalignedbb.maxZ);
            boolean flag = false;
            this.aC = Double.MIN_VALUE;
            BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();
            for (int k1 = i; k1 < j; k1++) {
                for (int l1 = k; l1 < l; l1++) {
                    for (int i2 = i1; i2 < j1; i2++) {
                        blockposition_mutableblockposition.set(k1, l1, i2);
                        FluidState fluid = this.level.getFluidState(blockposition_mutableblockposition);
                        if (fluid.is(FluidTags.WATER)) {
                            float f = l1 + fluid.getHeight(this.level, blockposition_mutableblockposition);
                            this.aC = Math.max(f, this.aC);
                            m = flag | ((axisalignedbb.minY < f) ? true : false);
                        }
                    }
                }
            }
            return m;
        }

        @Override
        public Entity teleportTo(ServerLevel worldserver, BlockPos location) {
            if (npc == null)
                return super.teleportTo(worldserver, location);
            return NMSImpl.teleportAcrossWorld(this, worldserver, location);
        }

        @Override
        public void tick() {
            if (npc != null) {
                npc.update();
                this.aF = this.aE;
                aE = getStatus();
                double d1 = isNoGravity() ? 0.0D : -0.04D;
                double d2 = 0.0D;
                this.ap = 0.05F;
                if (this.aF == Status.IN_AIR && this.aE != Status.IN_AIR && this.aE != Status.ON_LAND) {
                    this.aC = getY(1.0D);
                    setPos(getX(), (getWaterLevelAbove() - getBbHeight()) + 0.101D, getZ());
                    setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
                    this.aE = Status.IN_WATER;
                } else {
                    if (this.aE == Status.IN_WATER) {
                        d2 = (this.aC - getY()) / getBbHeight();
                        this.ap = 0.9F;
                    } else if (this.aE == Status.UNDER_FLOWING_WATER) {
                        d1 = -7.0E-4D;
                        this.ap = 0.9F;
                    } else if (this.aE == Status.UNDER_WATER) {
                        d2 = 0.01D;
                        this.ap = 0.45F;
                    } else if (this.aE == Status.IN_AIR) {
                        this.ap = 0.9F;
                    } else if (this.aE == Status.ON_LAND) {
                        this.ap = this.aD;
                        if (getControllingPassenger() instanceof ServerPlayer) {
                            this.aD /= 2.0F;
                        }
                    }
                    Vec3 vec3d = getDeltaMovement();
                    setDeltaMovement(vec3d.x * this.ap, vec3d.y + d1, vec3d.z * this.ap);
                    this.ar *= this.ap;
                    if (d2 > 0.0D) {
                        Vec3 vec3d1 = getDeltaMovement();
                        setDeltaMovement(vec3d1.x, (vec3d1.y + d2 * 0.0615D), vec3d1.z);
                    }
                }
                move(MoverType.SELF, getDeltaMovement());
                if (isVehicle()) {
                    setYRot((float) (getYRot() + this.ar));
                }
            } else {
                super.tick();
            }
        }

        private Status u() {
            AABB axisalignedbb = getBoundingBox();
            double d0 = axisalignedbb.maxY + 0.001D;
            int i = Mth.floor(axisalignedbb.minX);
            int j = Mth.ceil(axisalignedbb.maxX);
            int k = Mth.floor(axisalignedbb.maxY);
            int l = Mth.ceil(d0);
            int i1 = Mth.floor(axisalignedbb.minZ);
            int j1 = Mth.ceil(axisalignedbb.maxZ);
            boolean flag = false;
            BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();
            for (int k1 = i; k1 < j; k1++) {
                for (int l1 = k; l1 < l; l1++) {
                    for (int i2 = i1; i2 < j1; i2++) {
                        blockposition_mutableblockposition.set(k1, l1, i2);
                        FluidState fluid = this.level.getFluidState(blockposition_mutableblockposition);
                        if (fluid.is(FluidTags.WATER) && d0 < (blockposition_mutableblockposition.getY()
                                + fluid.getHeight(this.level, blockposition_mutableblockposition))) {
                            if (!fluid.isSource())
                                return Status.UNDER_FLOWING_WATER;
                            flag = true;
                        }
                    }
                }
            }
            return flag ? Status.UNDER_WATER : null;
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
            if (npc == null) {
                return super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            }
            Vec3 old = getDeltaMovement().add(0, 0, 0);
            boolean res = super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            if (!npc.isPushableByFluids()) {
                setDeltaMovement(old);
            }
            return res;
        }
    }
}
