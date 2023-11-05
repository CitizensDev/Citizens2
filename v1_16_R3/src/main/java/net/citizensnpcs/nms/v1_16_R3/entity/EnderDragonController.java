package net.citizensnpcs.nms.v1_16_R3.entity;

import java.lang.invoke.MethodHandle;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R3.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_16_R3.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_16_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.versioned.EnderDragonTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DragonControllerPhase;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityBoat;
import net.minecraft.server.v1_16_R3.EntityEnderDragon;
import net.minecraft.server.v1_16_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumPistonReaction;
import net.minecraft.server.v1_16_R3.FluidType;
import net.minecraft.server.v1_16_R3.IEntitySelector;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.SoundEffect;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;

public class EnderDragonController extends MobEntityController {
    public EnderDragonController() {
        super(EntityEnderDragonNPC.class);
    }

    @Override
    public EnderDragon getBukkitEntity() {
        return (EnderDragon) super.getBukkitEntity();
    }

    public static class EnderDragonNPC extends CraftEnderDragon implements ForwardingNPCHolder {
        public EnderDragonNPC(EntityEnderDragonNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityEnderDragonNPC extends EntityEnderDragon implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEnderDragonNPC(EntityTypes<? extends EntityEnderDragon> types, World world) {
            this(types, world, null);
        }

        public EntityEnderDragonNPC(EntityTypes<? extends EntityEnderDragon> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void a(float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, evt -> super.a((float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public boolean a(Tag<FluidType> tag, double d0) {
            if (npc == null)
                return super.a(tag, d0);
            Vec3D old = getMot().add(0, 0, 0);
            boolean res = super.a(tag, d0);
            if (!npc.isPushableByFluids()) {
                setMot(old);
            }
            return res;
        }

        @Override
        public int bP() {
            return NMS.getFallDistance(npc, super.bP());
        }

        @Override
        public void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_16_R3.Entity entity) {
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
        protected boolean dealDamage(DamageSource source, float f) {
            if (npc == null)
                return super.dealDamage(source, f);

            Vec3D old = getMot();
            boolean res = super.dealDamage(source, f);
            if (getDragonControllerManager().a().getControllerPhase() == DragonControllerPhase.HOVER) {
                setMot(old);
            }
            return res;
        }

        @Override
        public float dJ() {
            return NMS.getJumpPower(npc, super.dJ());
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new EnderDragonNPC(this));
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
        public void i(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.i(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        public void movementTick() {
            if (npc != null) {
                npc.update();
                NMSImpl.updateMinecraftAIState(npc, this);
            }
            if (npc != null && !npc.useMinecraftAI()) {
                if (dl()) {
                    setHealth(0F);
                    return;
                }
                if (this.d < 0) {
                    for (int i = 0; i < this.c.length; ++i) {
                        this.c[i][0] = this.yaw;
                        this.c[i][1] = this.locY();
                    }
                }
                if (++this.d == this.c.length) {
                    this.d = 0;
                }
                this.c[this.d][0] = this.yaw;
                this.c[this.d][1] = this.locY();

                float[][] pos = NMS.calculateDragonPositions(yaw,
                        new double[][] { a(0, 1F), a(5, 1F), a(10, 1F), a(12, 1F), a(14, 1F), a(16, 1F) });
                for (int j = 0; j < children.length; ++j) {
                    Vec3D vec3 = new Vec3D(this.children[j].locX(), this.children[j].locY(), this.children[j].locZ());
                    children[j].setPosition(this.locX() + pos[j][0], this.locY() + pos[j][1], this.locZ() + pos[j][2]);
                    children[j].lastX = children[j].D = vec3.x;
                    children[j].lastY = children[j].E = vec3.y;
                    children[j].lastZ = children[j].F = vec3.z;
                }
                if (getRidingPassenger() != null) {
                    yaw = getRidingPassenger().getBukkitYaw() - 180;
                }
                Vec3D mot = getMot();
                if (mot.getX() != 0 || mot.getY() != 0 || mot.getZ() != 0) {
                    mot = mot.d(0.98, 0.98, 0.98);
                    if (getRidingPassenger() == null) {
                        yaw = Util.getDragonYaw(getBukkitEntity(), mot.x, mot.z);
                    }
                    setPosition(locX() + mot.getX(), locY() + mot.getY(), locZ() + mot.getZ());
                    setMot(mot);
                }
                if (npc.hasTrait(EnderDragonTrait.class) && npc.getOrAddTrait(EnderDragonTrait.class).isDestroyWalls()
                        && NMSImpl.ENDERDRAGON_CHECK_WALLS != null) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            this.br |= (boolean) NMSImpl.ENDERDRAGON_CHECK_WALLS.invoke(this,
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
                super.movementTick();
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract))
                return !npc.isProtected();
            return super.n(entity);
        }

        private static final MethodHandle HURT = NMS.getMethodHandle(EntityEnderDragon.class, "b", true, List.class);
        private static final MethodHandle KNOCKBACK = NMS.getMethodHandle(EntityEnderDragon.class, "a", true,
                List.class);
    }
}
