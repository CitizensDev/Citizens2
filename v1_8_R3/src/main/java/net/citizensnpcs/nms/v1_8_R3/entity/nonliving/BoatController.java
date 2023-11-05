package net.citizensnpcs.nms.v1_8_R3.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Boat;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_8_R3.entity.MobEntityController;
import net.citizensnpcs.nms.v1_8_R3.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_8_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.EntityBoat;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Material;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;

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
        private double b;
        private final CitizensNPC npc;

        public EntityBoatNPC(World world) {
            this(world, null);
        }

        public EntityBoatNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void collide(net.minecraft.server.v1_8_R3.Entity entity) {
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
        public void g(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder)) {
                bukkitEntity = new BoatNPC(this);
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void setSize(float f, float f1) {
            if (npc == null) {
                super.setSize(f, f1);
            } else {
                NMSImpl.setSize(this, f, f1, justCreated);
            }
        }

        @Override
        public void t_() {
            if (npc != null) {
                npc.update();
                updateBoat();
            } else {
                super.t_();
            }
        }

        private void updateBoat() {
            this.lastX = this.locX;
            this.lastY = this.locY;
            this.lastZ = this.locZ;
            byte b0 = 5;
            double d0 = 0.0D;
            for (int i = 0; i < b0; i++) {
                double d1 = getBoundingBox().b + (getBoundingBox().e - getBoundingBox().b) * (i + 0) / b0 - 0.125D;
                double d2 = getBoundingBox().b + (getBoundingBox().e - getBoundingBox().b) * (i + 1) / b0 - 0.125D;
                AxisAlignedBB axisalignedbb = new AxisAlignedBB(getBoundingBox().a, d1, getBoundingBox().c,
                        getBoundingBox().d, d2, getBoundingBox().f);
                if (this.world.b(axisalignedbb, Material.WATER)) {
                    d0 += 1.0D / b0;
                }
            }
            double d3 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
            if (d3 > 0.2975D) {
                double d4 = Math.cos(this.yaw * Math.PI / 180.0D);
                double d5 = Math.sin(this.yaw * Math.PI / 180.0D);
                for (int j = 0; j < 1.0D + d3 * 60.0D; j++) {
                    double d6 = this.random.nextFloat() * 2.0F - 1.0F;
                    double d7 = (this.random.nextInt(2) * 2 - 1) * 0.7D;
                    if (this.random.nextBoolean()) {
                        double d8 = this.locX - d4 * d6 * 0.8D + d5 * d7;
                        double d9 = this.locZ - d5 * d6 * 0.8D - d4 * d7;
                        this.world.addParticle(EnumParticle.WATER_SPLASH, d8, this.locY - 0.125D, d9, this.motX,
                                this.motY, this.motZ);
                    } else {
                        double d8 = this.locX + d4 + d5 * d6 * 0.7D;
                        double d9 = this.locZ + d5 - d4 * d6 * 0.7D;
                        this.world.addParticle(EnumParticle.WATER_SPLASH, d8, this.locY - 0.125D, d9, this.motX,
                                this.motY, this.motZ);
                    }
                }
            }
            if (d0 < 1.0D) {
                double d = d0 * 2.0D - 1.0D;
                this.motY += 0.04D * d;
            } else {
                if (this.motY < 0.0D) {
                    this.motY /= 2.0D;
                }
                this.motY += 0.007D;
            }
            if (this.passenger instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) this.passenger;
                float f = this.passenger.yaw + -entityliving.aZ * 90.0F;
                this.motX += -Math.sin(f * 3.1415927F / 180.0F) * this.b * entityliving.ba * 0.05000000074505806D;
                this.motZ += Math.cos(f * 3.1415927F / 180.0F) * this.b * entityliving.ba * 0.05000000074505806D;
            } else if (this.unoccupiedDeceleration >= 0.0D) {
                this.motX *= this.unoccupiedDeceleration;
                this.motZ *= this.unoccupiedDeceleration;
                if (this.motX <= 1.0E-5D) {
                    this.motX = 0.0D;
                }
                if (this.motZ <= 1.0E-5D) {
                    this.motZ = 0.0D;
                }
            }
            double d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
            if (d4 > 0.35D) {
                double d = 0.35D / d4;
                this.motX *= d;
                this.motZ *= d;
                d4 = 0.35D;
            }
            if (d4 > d3 && this.b < 0.35D) {
                this.b += (0.35D - this.b) / 35.0D;
                if (this.b > 0.35D) {
                    this.b = 0.35D;
                }
            } else {
                this.b -= (this.b - 0.07D) / 35.0D;
                if (this.b < 0.07D) {
                    this.b = 0.07D;
                }
            }
            for (int k = 0; k < 4; k++) {
                int l = MathHelper.floor(this.locX + (k % 2 - 0.5D) * 0.8D);
                int j = MathHelper.floor(this.locZ + (k / 2 - 0.5D) * 0.8D);
                for (int i1 = 0; i1 < 2; i1++) {
                    int j1 = MathHelper.floor(this.locY) + i1;
                    BlockPosition blockposition = new BlockPosition(l, j1, j);
                    Block block = this.world.getType(blockposition).getBlock();
                    if (block == Blocks.SNOW_LAYER) {
                        if (!CraftEventFactory.callEntityChangeBlockEvent(this, l, j1, j, Blocks.AIR, 0)
                                .isCancelled()) {
                            this.world.setAir(blockposition);
                            this.positionChanged = false;
                        }
                    } else if (block == Blocks.WATERLILY) {
                        if (!CraftEventFactory.callEntityChangeBlockEvent(this, l, j1, j, Blocks.AIR, 0)
                                .isCancelled()) {
                            this.world.setAir(blockposition, true);
                            this.positionChanged = false;
                        }
                    }
                }
            }
            if (this.onGround && !this.landBoats) {
                this.motX *= 0.5D;
                this.motY *= 0.5D;
                this.motZ *= 0.5D;
            }
            move(this.motX, this.motY, this.motZ);
            if (this.positionChanged && d3 > 0.2975D) {
            } else {
                this.motX *= 0.99D;
                this.motY *= 0.95D;
                this.motZ *= 0.99D;
            }
            this.pitch = 0.0F;
            double d5 = this.yaw;
            double d10 = this.lastX - this.locX;
            double d11 = this.lastZ - this.locZ;
            if (d10 * d10 + d11 * d11 > 0.001D) {
                d5 = (float) (MathHelper.b(d11, d10) * 180.0D / Math.PI);
            }
            double d12 = MathHelper.g(d5 - this.yaw);
            if (d12 > 20.0D) {
                d12 = 20.0D;
            }
            if (d12 < -20.0D) {
                d12 = -20.0D;
            }
            this.yaw += d12;
        }
    }
}