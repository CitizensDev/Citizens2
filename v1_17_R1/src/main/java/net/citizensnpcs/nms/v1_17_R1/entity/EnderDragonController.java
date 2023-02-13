package net.citizensnpcs.nms.v1_17_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_17_R1.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_17_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_17_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
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
        public void dismountTo(double d0, double d1, double d2) {
            NMS.enderTeleportTo(npc, d0, d1, d2, () -> super.dismountTo(d0, d1, d2));
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
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
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
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(Tag<Fluid> Tag, double d0) {
            return NMSImpl.fluidPush(npc, this, () -> super.updateFluidHeightAndDoFluidPushing(Tag, d0));
        }
    }
}
