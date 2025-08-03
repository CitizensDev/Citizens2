package net.citizensnpcs.nms.v1_12_R1.entity;

import java.lang.invoke.MethodHandle;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_12_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_12_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.versioned.EnderDragonTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.DragonControllerPhase;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumPistonReaction;
import net.minecraft.server.v1_12_R1.IEntitySelector;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.SoundEffect;
import net.minecraft.server.v1_12_R1.Vec3D;
import net.minecraft.server.v1_12_R1.World;

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
        public boolean a(EntityPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.a(player));
        }

        @Override
        public int bg() {
            return NMS.getFallDistance(npc, super.bg());
        }

        @Override
        public boolean bo() {
            return npc == null ? super.bo() : npc.isPushableByFluids();
        }

        @Override
        protected SoundEffect cf() {
            return NMSImpl.getSoundEffect(npc, super.cf(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        public void collide(net.minecraft.server.v1_12_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public float ct() {
            return NMS.getJumpPower(npc, super.ct());
        }

        @Override
        protected SoundEffect d(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.d(damagesource), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        protected boolean dealDamage(DamageSource source, float f) {
            if (npc == null)
                return super.dealDamage(source, f);

            Vec3D old = new Vec3D(motX, motY, motZ);
            boolean res = super.dealDamage(source, f);
            if (getDragonControllerManager().a().getControllerPhase() == DragonControllerPhase.k) {
                motX = old.x;
                motY = old.y;
                motZ = old.z;
            }
            return res;
        }

        @Override
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        protected SoundEffect F() {
            return NMSImpl.getSoundEffect(npc, super.F(), NPC.Metadata.AMBIENT_SOUND);
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
        public EnumPistonReaction getPushReaction() {
            return Util.callPistonPushEvent(npc) ? EnumPistonReaction.IGNORE : super.getPushReaction();
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        protected void L() {
            if (npc == null) {
                super.L();
            }
        }

        @Override
        public void n() {
            if (npc != null) {
                npc.update();
            }
            if (npc != null) {
                if (getDragonControllerManager().a().getControllerPhase() == DragonControllerPhase.j) {
                    setHealth(0F);
                    return;
                }
                if (this.c < 0) {
                    for (int i = 0; i < this.b.length; ++i) {
                        this.b[i][0] = this.yaw;
                        this.b[i][1] = this.locY;
                    }
                }
                if (++this.c == this.b.length) {
                    this.c = 0;
                }
                this.b[this.c][0] = this.yaw;
                this.b[this.c][1] = this.locY;

                float[][] pos = NMS.calculateDragonPositions(yaw,
                        new double[][] { a(0, 1F), a(5, 1F), a(10, 1F), a(12, 1F), a(14, 1F), a(16, 1F) });
                for (int j = 0; j < children.length; ++j) {
                    Vec3D vec3 = new Vec3D(this.children[j].locX, this.children[j].locY, this.children[j].locZ);
                    children[j].setPosition(this.locX + pos[j][0], this.locY + pos[j][1], this.locZ + pos[j][2]);
                    children[j].lastX = vec3.x;
                    children[j].lastY = vec3.y;
                    children[j].lastZ = vec3.z;
                }
                if (getBukkitEntity().getPassenger() != null) {
                    yaw = getBukkitEntity().getPassenger().getLocation().getYaw() - 180;
                }
                if (motX != 0 || motY != 0 || motZ != 0) {
                    motX *= 0.98;
                    motY *= 0.98;
                    motZ *= 0.98;
                    if (getBukkitEntity().getPassenger() == null) {
                        yaw = Util.getYawFromVelocity(getBukkitEntity(), motX, motZ);
                    }
                    setPosition(locX + motX, locY + motY, locZ + motZ);
                }
                if (npc.hasTrait(EnderDragonTrait.class) && npc.getOrAddTrait(EnderDragonTrait.class).isDestroyWalls()
                        && NMSImpl.ENDERDRAGON_CHECK_WALLS != null) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            this.bG |= (boolean) NMSImpl.ENDERDRAGON_CHECK_WALLS.invoke(this,
                                    children[i].getBoundingBox());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (npc.data().get(NPC.Metadata.COLLIDABLE, false)) {
                    try {
                        KNOCKBACK.invoke(this, this.world.getEntities(this,
                                children[6].getBoundingBox().grow(4.0, 2.0, 4.0).d(0.0, -2.0, 0.0), IEntitySelector.e));
                        KNOCKBACK.invoke(this, this.world.getEntities(this,
                                children[7].getBoundingBox().grow(4.0, 2.0, 4.0).d(0.0, -2.0, 0.0), IEntitySelector.e));
                        HURT.invoke(this,
                                this.world.getEntities(this, children[0].getBoundingBox().g(1.0), IEntitySelector.e));
                        HURT.invoke(this,
                                this.world.getEntities(this, children[1].getBoundingBox().g(1.0), IEntitySelector.e));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } else {
                super.n();
            }
        }

        private static final MethodHandle HURT = NMS.getMethodHandle(EntityEnderDragon.class, "b", true,
                java.util.List.class);
        private static final MethodHandle KNOCKBACK = NMS.getMethodHandle(EntityEnderDragon.class, "a", true,
                java.util.List.class);
    }
}