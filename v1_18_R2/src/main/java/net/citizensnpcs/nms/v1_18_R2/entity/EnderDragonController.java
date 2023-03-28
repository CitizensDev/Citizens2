package net.citizensnpcs.nms.v1_18_R2.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_18_R2.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_18_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_18_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EnderDragonController extends MobEntityController {
    public EnderDragonController() {
        super(EntityEnderDragonNPC.class);
    }

    @Override
    public org.bukkit.entity.EnderDragon getBukkitEntity() {
        return (org.bukkit.entity.EnderDragon) super.getBukkitEntity();
    }

    public static class EnderDragonNPC extends CraftEnderDragon implements ForwardingNPCHolder {
        public EnderDragonNPC(EntityEnderDragonNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityEnderDragonNPC extends EnderDragon implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEnderDragonNPC(EntityType<? extends EnderDragon> types, Level level) {
            this(types, level, null);
        }

        public EntityEnderDragonNPC(EntityType<? extends EnderDragon> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void aiStep() {
            if (npc != null) {
                npc.update();
                NMSImpl.updateMinecraftAIState(npc, this);
            }
            if (npc != null && !npc.useMinecraftAI()) {
                if (this.posPointer < 0) {
                    for (int i = 0; i < this.positions.length; ++i) {
                        this.positions[i][0] = this.getYRot();
                        this.positions[i][1] = this.getY();
                    }
                }

                if (++this.posPointer == this.positions.length) {
                    this.posPointer = 0;
                }

                this.positions[this.posPointer][0] = this.getYRot();
                this.positions[this.posPointer][1] = this.getY();

                float[][] pos = NMS.calculateDragonPositions(getYRot(),
                        new double[][] { getLatencyPos(0, 1F), getLatencyPos(5, 1F), getLatencyPos(10, 1F),
                                getLatencyPos(12, 1F), getLatencyPos(14, 1F), getLatencyPos(16, 1F) });
                for (int j = 0; j < subEntities.length; ++j) {
                    Vec3 vec3 = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(),
                            this.subEntities[j].getZ());
                    subEntities[j].setPos(this.getX() + pos[j][0], this.getY() + pos[j][1], this.getZ() + pos[j][2]);
                    subEntities[j].xo = subEntities[j].xOld = vec3.x;
                    subEntities[j].yo = subEntities[j].yOld = vec3.y;
                    subEntities[j].zo = subEntities[j].zOld = vec3.z;
                }

                if (getFirstPassenger() != null) {
                    setYRot(getFirstPassenger().getBukkitYaw() - 180);
                }
                Vec3 mot = getDeltaMovement();
                if (mot.x != 0 || mot.y != 0 || mot.z != 0) {
                    mot = mot.multiply(0.98, 0.98, 0.98);
                    if (getFirstPassenger() == null) {
                        setYRot(Util.getDragonYaw(getBukkitEntity(), mot.x, mot.z));
                    }
                    setPos(getX() + mot.x, getY() + mot.y, getZ() + mot.z);
                    setDeltaMovement(mot);
                }
            } else {
                super.aiStep();
            }
        }

        @Override
        protected boolean canRide(Entity entity) {
            if (npc != null && (entity instanceof Boat || entity instanceof AbstractMinecart)) {
                return !npc.isProtected();
            }
            return super.canRide(entity);
        }

        @Override
        public void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return NMSImpl.getSoundEffect(npc, super.getAmbientSound(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new EnderDragonNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return NMSImpl.getSoundEffect(npc, super.getDeathSound(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.getHurtSound(damagesource), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public int getMaxFallDistance() {
            return NMS.getFallDistance(npc, super.getMaxFallDistance());
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        @Override
        public void knockback(double strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, (float) strength, dx, dz, (evt) -> super.knockback((float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
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
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        protected boolean reallyHurt(DamageSource source, float f) {
            if (npc == null)
                return super.reallyHurt(source, f);

            Vec3 old = getDeltaMovement();
            boolean res = super.reallyHurt(source, f);
            if (getPhaseManager().getCurrentPhase() == EnderDragonPhase.HOVERING) {
                setDeltaMovement(old);
            }
            return res;
        }

        @Override
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public Entity teleportTo(ServerLevel worldserver, BlockPos location) {
            if (npc == null)
                return super.teleportTo(worldserver, location);
            return NMSImpl.teleportAcrossWorld(this, worldserver, location);
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
            return NMSImpl.fluidPush(npc, this, () -> super.updateFluidHeightAndDoFluidPushing(tagkey, d0));
        }
    }
}
