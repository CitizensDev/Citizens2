package net.citizensnpcs.nms.v1_15_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Boat;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_15_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_15_R1.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_15_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_15_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.EntityBoat;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumMoveType;
import net.minecraft.server.v1_15_R1.EnumPistonReaction;
import net.minecraft.server.v1_15_R1.Fluid;
import net.minecraft.server.v1_15_R1.FluidType;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.Tag;
import net.minecraft.server.v1_15_R1.TagsFluid;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;

public class BoatController extends MobEntityController {
    public BoatController() {
        super(EntityBoatNPC.class);
    }

    @Override
    public Boat getBukkitEntity() {
        return (Boat) super.getBukkitEntity();
    }

    public static class BoatNPC extends CraftBoat implements ForwardingNPCHolder {
        public BoatNPC(EntityBoatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityBoatNPC extends EntityBoat implements NPCHolder {
        private double aD;

        private float aE;
        private EnumStatus aF;
        private EnumStatus aG;
        private float aq;
        private float as;
        private final CitizensNPC npc;

        public EntityBoatNPC(EntityTypes<? extends EntityBoat> types, World world) {
            this(types, world, null);
        }

        public EntityBoatNPC(EntityTypes<? extends EntityBoat> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public boolean b(Tag<FluidType> tag) {
            if (npc == null) {
                return super.b(tag);
            }
            Vec3D old = getMot().add(0, 0, 0);
            boolean res = super.b(tag);
            if (!npc.isPushableByFluids()) {
                setMot(old);
            }
            return res;
        }

        @Override
        public void collide(net.minecraft.server.v1_15_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
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
        public EnumPistonReaction getPushReaction() {
            return Util.callPistonPushEvent(npc) ? EnumPistonReaction.IGNORE : super.getPushReaction();
        }

        @Override
        public void h(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.h(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        private EnumStatus s() {
            EnumStatus entityboat_enumstatus = v();
            if (entityboat_enumstatus != null) {
                this.aD = (getBoundingBox()).maxY;
                return entityboat_enumstatus;
            }
            if (u())
                return EnumStatus.IN_WATER;
            float f = l();
            if (f > 0.0F) {
                this.aE = f;
                return EnumStatus.ON_LAND;
            }
            return EnumStatus.IN_AIR;
        }

        @Override
        public void tick() {
            if (npc != null) {
                npc.update();
                this.aG = this.aF;
                this.aF = s();
                double d1 = isNoGravity() ? 0.0D : -0.04D;
                double d2 = 0.0D;
                this.aq = 0.05F;
                if (this.aG == EnumStatus.IN_AIR && this.aF != EnumStatus.IN_AIR && this.aF != EnumStatus.ON_LAND) {
                    this.aD = e(1.0D);
                    setPosition(locX(), (k() - getHeight()) + 0.101D, locZ());
                    setMot(getMot().d(1.0D, 0.0D, 1.0D));
                    this.aF = EnumStatus.IN_WATER;
                } else {
                    if (this.aF == EnumStatus.IN_WATER) {
                        d2 = (this.aD - locY()) / getHeight();
                        this.aq = 0.9F;
                    } else if (this.aF == EnumStatus.UNDER_FLOWING_WATER) {
                        d1 = -7.0E-4D;
                        this.aq = 0.9F;
                    } else if (this.aF == EnumStatus.UNDER_WATER) {
                        d2 = 0.01D;
                        this.aq = 0.45F;
                    } else if (this.aF == EnumStatus.IN_AIR) {
                        this.aq = 0.9F;
                    } else if (this.aF == EnumStatus.ON_LAND) {
                        this.aq = this.aE;
                        if (getRidingPassenger() instanceof EntityHuman)
                            this.aE /= 2.0F;
                    }
                    Vec3D vec3d = getMot();
                    setMot(vec3d.x * this.aq, vec3d.y + d1, vec3d.z * this.aq);
                    this.as *= this.aq;
                    if (d2 > 0.0D) {
                        Vec3D vec3d1 = getMot();
                        setMot(vec3d1.x, (vec3d1.y + d2 * 0.0615D) * 0.75D, vec3d1.z);
                    }
                }
                move(EnumMoveType.SELF, getMot());
                if (isVehicle()) {
                    this.yaw += this.as;
                }
            } else {
                super.tick();
            }
        }

        private boolean u() {
            boolean m = false;
            AxisAlignedBB axisalignedbb = getBoundingBox();
            int i = MathHelper.floor(axisalignedbb.minX);
            int j = MathHelper.f(axisalignedbb.maxX);
            int k = MathHelper.floor(axisalignedbb.minY);
            int l = MathHelper.f(axisalignedbb.minY + 0.001D);
            int i1 = MathHelper.floor(axisalignedbb.minZ);
            int j1 = MathHelper.f(axisalignedbb.maxZ);
            boolean flag = false;
            this.aD = Double.MIN_VALUE;
            BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.r();
            Throwable throwable = null;
            try {
                for (int k1 = i; k1 < j; k1++) {
                    for (int l1 = k; l1 < l; l1++) {
                        for (int i2 = i1; i2 < j1; i2++) {
                            blockposition_pooledblockposition.d(k1, l1, i2);
                            Fluid fluid = this.world.getFluid(blockposition_pooledblockposition);
                            if (fluid.a(TagsFluid.WATER)) {
                                float f = l1 + fluid.getHeight(this.world, blockposition_pooledblockposition);
                                this.aD = Math.max(f, this.aD);
                                m = flag | ((axisalignedbb.minY < f) ? true : false);
                            }
                        }
                    }
                }
            } catch (Throwable throwable1) {
                throwable = throwable1;
                throw throwable1;
            } finally {
                if (blockposition_pooledblockposition != null)
                    if (throwable != null) {
                        try {
                            blockposition_pooledblockposition.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    } else {
                        blockposition_pooledblockposition.close();
                    }
            }
            return m;
        }

        @Override
        public void updateSize() {
            if (npc == null) {
                super.updateSize();
            } else {
                NMSImpl.setSize(this, justCreated);
            }
        }

        private EnumStatus v() {
            AxisAlignedBB axisalignedbb = getBoundingBox();
            double d0 = axisalignedbb.maxY + 0.001D;
            int i = MathHelper.floor(axisalignedbb.minX);
            int j = MathHelper.f(axisalignedbb.maxX);
            int k = MathHelper.floor(axisalignedbb.maxY);
            int l = MathHelper.f(d0);
            int i1 = MathHelper.floor(axisalignedbb.minZ);
            int j1 = MathHelper.f(axisalignedbb.maxZ);
            boolean flag = false;
            BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.r();
            Throwable throwable = null;
            try {
                for (int k1 = i; k1 < j; k1++) {
                    for (int l1 = k; l1 < l; l1++) {
                        for (int i2 = i1; i2 < j1; i2++) {
                            blockposition_pooledblockposition.d(k1, l1, i2);
                            Fluid fluid = this.world.getFluid(blockposition_pooledblockposition);
                            if (fluid.a(TagsFluid.WATER) && d0 < (blockposition_pooledblockposition.getY()
                                    + fluid.getHeight(this.world, blockposition_pooledblockposition))) {
                                if (!fluid.isSource()) {
                                    EnumStatus entityboat_enumstatus = EnumStatus.UNDER_FLOWING_WATER;
                                    return entityboat_enumstatus;
                                }
                                flag = true;
                            }
                        }
                    }
                }
                return flag ? EnumStatus.UNDER_WATER : null;
            } catch (Throwable throwable1) {
                throwable = throwable1;
                throw throwable1;
            } finally {
                if (blockposition_pooledblockposition != null)
                    if (throwable != null) {
                        try {
                            blockposition_pooledblockposition.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    } else {
                        blockposition_pooledblockposition.close();
                    }
            }
        }
    }
}
