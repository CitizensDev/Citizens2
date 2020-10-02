package net.citizensnpcs.nms.v1_16_R2.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Boat;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R2.entity.MobEntityController;
import net.citizensnpcs.nms.v1_16_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R2.AxisAlignedBB;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.EntityBoat;
import net.minecraft.server.v1_16_R2.EntityHuman;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.EnumMoveType;
import net.minecraft.server.v1_16_R2.Fluid;
import net.minecraft.server.v1_16_R2.MathHelper;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.TagsFluid;
import net.minecraft.server.v1_16_R2.Vec3D;
import net.minecraft.server.v1_16_R2.World;

public class BoatController extends MobEntityController {
    public BoatController() {
        super(EntityBoatNPC.class);
    }

    @Override
    public Boat getBukkitEntity() {
        return (Boat) super.getBukkitEntity();
    }

    public static class BoatNPC extends CraftBoat implements NPCHolder {
        private final CitizensNPC npc;

        public BoatNPC(EntityBoatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityBoatNPC extends EntityBoat implements NPCHolder {
        private double aC;
        private float aD;
        private EnumStatus aE;
        private EnumStatus aF;
        private double ap;
        private double ar;
        private final CitizensNPC npc;

        public EntityBoatNPC(EntityTypes<? extends EntityBoat> types, World world) {
            this(types, world, null);
        }

        public EntityBoatNPC(EntityTypes<? extends EntityBoat> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void collide(net.minecraft.server.v1_16_R2.Entity entity) {
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

        private EnumStatus getStatus() {
            EnumStatus entityboat_enumstatus = u();
            if (entityboat_enumstatus != null) {
                this.aC = (getBoundingBox()).maxY;
                return entityboat_enumstatus;
            }
            if (t())
                return EnumStatus.IN_WATER;
            float f = k();
            if (f > 0.0F) {
                this.aD = f;
                return EnumStatus.ON_LAND;
            }
            return EnumStatus.IN_AIR;
        }

        @Override
        public void i(double x, double y, double z) {
            if (npc == null) {
                super.i(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.i(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.i(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        private boolean t() {
            boolean m = false;
            AxisAlignedBB axisalignedbb = getBoundingBox();
            int i = MathHelper.floor(axisalignedbb.minX);
            int j = MathHelper.f(axisalignedbb.maxX);
            int k = MathHelper.floor(axisalignedbb.minY);
            int l = MathHelper.f(axisalignedbb.minY + 0.001D);
            int i1 = MathHelper.floor(axisalignedbb.minZ);
            int j1 = MathHelper.f(axisalignedbb.maxZ);
            boolean flag = false;
            this.aC = Double.MIN_VALUE;
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            for (int k1 = i; k1 < j; k1++) {
                for (int l1 = k; l1 < l; l1++) {
                    for (int i2 = i1; i2 < j1; i2++) {
                        blockposition_mutableblockposition.d(k1, l1, i2);
                        Fluid fluid = this.world.getFluid(blockposition_mutableblockposition);
                        if (fluid.a(TagsFluid.WATER)) {
                            float f = l1 + fluid.getHeight(this.world, blockposition_mutableblockposition);
                            this.aC = Math.max(f, this.aC);
                            m = flag | ((axisalignedbb.minY < f) ? true : false);
                        }
                    }
                }
            }
            return m;
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
                if (this.aF == EnumStatus.IN_AIR && this.aE != EnumStatus.IN_AIR && this.aE != EnumStatus.ON_LAND) {
                    this.aC = e(1.0D);
                    setPosition(locX(), (i() - getHeight()) + 0.101D, locZ());
                    setMot(getMot().d(1.0D, 0.0D, 1.0D));
                    this.aE = EnumStatus.IN_WATER;
                } else {
                    if (this.aE == EnumStatus.IN_WATER) {
                        d2 = (this.aC - locY()) / getHeight();
                        this.ap = 0.9F;
                    } else if (this.aE == EnumStatus.UNDER_FLOWING_WATER) {
                        d1 = -7.0E-4D;
                        this.ap = 0.9F;
                    } else if (this.aE == EnumStatus.UNDER_WATER) {
                        d2 = 0.01D;
                        this.ap = 0.45F;
                    } else if (this.aE == EnumStatus.IN_AIR) {
                        this.ap = 0.9F;
                    } else if (this.aE == EnumStatus.ON_LAND) {
                        this.ap = this.aD;
                        if (getRidingPassenger() instanceof EntityHuman) {
                            this.aD /= 2.0F;
                        }
                    }
                    Vec3D vec3d = getMot();
                    setMot(vec3d.x * this.ap, vec3d.y + d1, vec3d.z * this.ap);
                    this.ar *= this.ap;
                    if (d2 > 0.0D) {
                        Vec3D vec3d1 = getMot();
                        setMot(vec3d1.x, (vec3d1.y + d2 * 0.0615D), vec3d1.z);
                    }
                }
                move(EnumMoveType.SELF, getMot());
                if (isVehicle()) {
                    this.yaw += this.ar;
                }
            } else {
                super.tick();
            }
        }

        private EnumStatus u() {
            AxisAlignedBB axisalignedbb = getBoundingBox();
            double d0 = axisalignedbb.maxY + 0.001D;
            int i = MathHelper.floor(axisalignedbb.minX);
            int j = MathHelper.f(axisalignedbb.maxX);
            int k = MathHelper.floor(axisalignedbb.maxY);
            int l = MathHelper.f(d0);
            int i1 = MathHelper.floor(axisalignedbb.minZ);
            int j1 = MathHelper.f(axisalignedbb.maxZ);
            boolean flag = false;
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            for (int k1 = i; k1 < j; k1++) {
                for (int l1 = k; l1 < l; l1++) {
                    for (int i2 = i1; i2 < j1; i2++) {
                        blockposition_mutableblockposition.d(k1, l1, i2);
                        Fluid fluid = this.world.getFluid(blockposition_mutableblockposition);
                        if (fluid.a(TagsFluid.WATER) && d0 < (blockposition_mutableblockposition.getY()
                                + fluid.getHeight(this.world, blockposition_mutableblockposition))) {
                            if (!fluid.isSource())
                                return EnumStatus.UNDER_FLOWING_WATER;
                            flag = true;
                        }
                    }
                }
            }
            return flag ? EnumStatus.UNDER_WATER : null;
        }

        @Override
        public void updateSize() {
            if (npc == null) {
                super.updateSize();
            } else {
                NMSImpl.setSize(this, justCreated);
            }
        }
    }
}
