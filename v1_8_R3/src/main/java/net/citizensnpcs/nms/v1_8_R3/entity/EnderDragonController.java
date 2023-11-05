package net.citizensnpcs.nms.v1_8_R3.entity;

import java.lang.invoke.MethodHandle;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_8_R3.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_8_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.versioned.EnderDragonTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Vec3D;
import net.minecraft.server.v1_8_R3.World;

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
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void a(Entity entity, float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, evt -> super.a(entity, (float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public int aE() {
            return NMS.getFallDistance(npc, super.aE());
        }

        @Override
        public float bE() {
            return NMS.getJumpPower(npc, super.bE());
        }

        @Override
        protected String bo() {
            return NMSImpl.getSoundEffect(npc, super.bo(), NPC.Metadata.HURT_SOUND);
        }

        @Override
        protected String bp() {
            return NMSImpl.getSoundEffect(npc, super.bp(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        public boolean cc() {
            return NMSImpl.isLeashed(npc, super::cc, this);
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
        protected void D() {
            if (npc == null) {
                super.D();
            }
        }

        @Override
        protected boolean dealDamage(DamageSource source, float f) {
            if (npc == null)
                return super.dealDamage(source, f);

            Vec3D old = new Vec3D(motX, motY, motZ);
            boolean res = super.dealDamage(source, f);
            motX = old.a;
            motY = old.b;
            motZ = old.c;

            return res;
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
                bukkitEntity = new EnderDragonNPC(this);
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void m() {
            if (npc != null) {
                npc.update();

                if (this.bl < 0) {
                    for (int i = 0; i < this.bk.length; ++i) {
                        this.bk[i][0] = this.yaw;
                        this.bk[i][1] = this.locY;
                    }
                }
                if (++this.bl == this.bk.length) {
                    this.bl = 0;
                }
                this.bk[this.bl][0] = this.yaw;
                this.bk[this.bl][1] = this.locY;

                float[][] pos = NMS.calculateDragonPositions(yaw,
                        new double[][] { b(0, 1F), b(5, 1F), b(10, 1F), b(12, 1F), b(14, 1F), b(16, 1F) });
                for (int j = 0; j < children.length; ++j) {
                    Vec3D vec3 = new Vec3D(this.children[j].locX, this.children[j].locY, this.children[j].locZ);
                    children[j].setPosition(this.locX + pos[j][0], this.locY + pos[j][1], this.locZ + pos[j][2]);
                    children[j].lastX = vec3.a;
                    children[j].lastY = vec3.b;
                    children[j].lastZ = vec3.c;
                }
                if (getBukkitEntity().getPassenger() != null) {
                    yaw = getBukkitEntity().getPassenger().getLocation().getYaw() - 180;
                }
                if (motX != 0 || motY != 0 || motZ != 0) {
                    motX *= 0.98;
                    motY *= 0.98;
                    motZ *= 0.98;
                    if (getBukkitEntity().getPassenger() == null) {
                        yaw = Util.getDragonYaw(getBukkitEntity(), motX, motZ);
                    }
                    setPosition(locX + motX, locY + motY, locZ + motZ);
                }
                if (npc.hasTrait(EnderDragonTrait.class) && npc.getOrAddTrait(EnderDragonTrait.class).isDestroyWalls()
                        && NMSImpl.ENDERDRAGON_CHECK_WALLS != null) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            this.bx |= (boolean) NMSImpl.ENDERDRAGON_CHECK_WALLS.invoke(this,
                                    children[i].getBoundingBox());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (npc.data().get(NPC.Metadata.COLLIDABLE, false)) {
                    try {
                        KNOCKBACK.invoke(this, this.world.getEntities(this,
                                children[6].getBoundingBox().grow(4.0, 2.0, 4.0).c(0.0, -2.0, 0.0)));
                        KNOCKBACK.invoke(this, this.world.getEntities(this,
                                children[7].getBoundingBox().grow(4.0, 2.0, 4.0).c(0.0, -2.0, 0.0)));
                        HURT.invoke(this, this.world.getEntities(this, children[0].getBoundingBox().grow(1, 1, 1.0)));
                        HURT.invoke(this, this.world.getEntities(this, children[1].getBoundingBox().grow(1, 1, 1.0)));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } else {
                super.m();
            }
        }

        @Override
        protected String z() {
            return NMSImpl.getSoundEffect(npc, super.z(), NPC.Metadata.AMBIENT_SOUND);
        }

        private static final MethodHandle HURT = NMS.getMethodHandle(EntityEnderDragon.class, "b", true,
                java.util.List.class);
        private static final MethodHandle KNOCKBACK = NMS.getMethodHandle(EntityEnderDragon.class, "a", true,
                java.util.List.class);
    }
}