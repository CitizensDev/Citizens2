package net.citizensnpcs.nms.v1_18_R2.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPufferFish;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_18_R2.util.EntityMoveControl;
import net.citizensnpcs.nms.v1_18_R2.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_18_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_18_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.versioned.PufferFishTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PufferFishController extends MobEntityController {
    public PufferFishController() {
        super(EntityPufferFishNPC.class);
    }

    @Override
    public org.bukkit.entity.PufferFish getBukkitEntity() {
        return (org.bukkit.entity.PufferFish) super.getBukkitEntity();
    }

    public static class EntityPufferFishNPC extends Pufferfish implements NPCHolder {
        private final CitizensNPC npc;

        private MoveControl oldMoveController;

        public EntityPufferFishNPC(EntityType<? extends Pufferfish> types, Level level) {
            this(types, level, null);
        }

        public EntityPufferFishNPC(EntityType<? extends Pufferfish> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                this.oldMoveController = this.moveControl;
                this.moveControl = new MoveControl(this);
            }
        }

        @Override
        public void aiStep() {
            boolean lastInWater = this.verticalCollision;
            int lastPuffState = getPuffState();
            if (npc != null) {
                this.verticalCollision = false;
                setPuffState(0);
            }
            super.aiStep();
            if (npc != null) {
                this.verticalCollision = lastInWater;
                setPuffState(lastPuffState);
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
        public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
            if (npc == null || !npc.isFlyable()) {
                return super.causeFallDamage(f, f1, damagesource);
            }
            return false;
        }

        @Override
        public void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
        }

        @Override
        protected void checkFallDamage(double d0, boolean flag, BlockState iblockdata, BlockPos blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.checkFallDamage(d0, flag, iblockdata, blockposition);
            }
        }

        @Override
        public void customServerAiStep() {
            super.customServerAiStep();
            if (npc != null) {
                NMSImpl.updateMinecraftAIState(npc, this);
                if (npc.useMinecraftAI() && this.moveControl != this.oldMoveController) {
                    this.moveControl = this.oldMoveController;
                }
                if (!npc.useMinecraftAI() && this.moveControl == this.oldMoveController) {
                    this.moveControl = new EntityMoveControl(this);
                }
                npc.update();
            }
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return NMSImpl.getSoundEffect(npc, super.getAmbientSound(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new PufferFishNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return NMSImpl.getSoundEffect(npc, super.getDeathSound(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        public EntityDimensions getDimensions(Pose entitypose) {
            if (npc == null) {
                return super.getDimensions(entitypose);
            }
            return super.getDimensions(entitypose).scale(1 / s(getPuffState())).scale(0.5F);
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.getHurtSound(damagesource), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public float getJumpPower() {
            return NMS.getJumpPower(npc, super.getJumpPower());
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
        public PushReaction getPistonPushReaction() {
            return Util.callPistonPushEvent(npc) ? PushReaction.IGNORE : super.getPistonPushReaction();
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
        protected InteractionResult mobInteract(Player entityhuman, InteractionHand enumhand) {
            if (npc == null || !npc.isProtected())
                return super.mobInteract(entityhuman, enumhand);
            ItemStack itemstack = entityhuman.getItemInHand(enumhand);
            if (itemstack.getItem() == Items.WATER_BUCKET && isAlive()) {
                return InteractionResult.FAIL;
            }
            return super.mobInteract(entityhuman, enumhand);
        }

        @Override
        public boolean onClimbable() {
            if (npc == null || !npc.isFlyable()) {
                return super.onClimbable();
            } else {
                return false;
            }
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
        public void tick() {
            if (npc != null) {
                NMSImpl.resetPuffTicks(this);
            }
            super.tick();
            PufferFishTrait trait = null;
            if (npc != null && (trait = npc.getTraitNullable(PufferFishTrait.class)) != null) {
                setPuffState(trait.getPuffState());
            }
        }

        @Override
        public void travel(Vec3 vec3d) {
            if (npc == null || !npc.isFlyable()) {
                if (!NMSImpl.moveFish(npc, this, vec3d)) {
                    super.travel(vec3d);
                }
            } else {
                NMSImpl.flyingMoveLogic(this, vec3d);
            }
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

        private static float s(int i) {
            switch (i) {
                case 0:
                    return 0.5F;
                case 1:
                    return 0.7F;
                default:
                    return 1.0F;
            }
        }
    }

    public static class PufferFishNPC extends CraftPufferFish implements ForwardingNPCHolder {
        public PufferFishNPC(EntityPufferFishNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
