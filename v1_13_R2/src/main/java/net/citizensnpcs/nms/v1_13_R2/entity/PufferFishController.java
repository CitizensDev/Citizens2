package net.citizensnpcs.nms.v1_13_R2.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPufferFish;
import org.bukkit.entity.PufferFish;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_13_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.versioned.PufferFishTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityBoat;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityMinecartAbstract;
import net.minecraft.server.v1_13_R2.EntityPufferFish;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.EnumPistonReaction;
import net.minecraft.server.v1_13_R2.FluidType;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.SoundEffect;
import net.minecraft.server.v1_13_R2.Tag;
import net.minecraft.server.v1_13_R2.World;

public class PufferFishController extends MobEntityController {
    public PufferFishController() {
        super(EntityPufferFishNPC.class);
    }

    @Override
    public PufferFish getBukkitEntity() {
        return (PufferFish) super.getBukkitEntity();
    }

    public static class EntityPufferFishNPC extends EntityPufferFish implements NPCHolder {
        private final CitizensNPC npc;

        public EntityPufferFishNPC(World world) {
            this(world, null);
        }

        public EntityPufferFishNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                this.moveController = new ControllerMove(this);
            }
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public void a(Entity entity, float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, (evt) -> super.a(entity, (float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
            if (npc == null || !npc.isProtected())
                return super.a(entityhuman, enumhand);
            ItemStack itemstack = entityhuman.b(enumhand);
            if (itemstack.getItem() == Items.WATER_BUCKET && isAlive()) {
                return false;
            }
            return super.a(entityhuman, enumhand);
        }

        @Override
        public void a(float f, float f1, float f2) {
            if (npc == null || !npc.isFlyable()) {
                if (!NMSImpl.moveFish(npc, this, f, f1, f2)) {
                    super.a(f, f1, f2);
                }
            } else {
                NMSImpl.flyingMoveLogic(this, f, f1, f2);
            }
        }

        @Override
        public boolean b(Tag<FluidType> tag) {
            if (npc == null) {
                return super.b(tag);
            }
            double mx = motX;
            double my = motY;
            double mz = motZ;
            boolean res = super.b(tag);
            if (!npc.isPushableByFluids()) {
                motX = mx;
                motY = my;
                motZ = mz;
            }
            return res;
        }

        @Override
        public int bn() {
            return NMS.getFallDistance(npc, super.bn());
        }

        @Override
        public void c(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.c(f, f1);
            }
        }

        @Override
        public float cG() {
            return NMS.getJumpPower(npc, super.cG());
        }

        @Override
        public void collide(net.minecraft.server.v1_13_R2.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        protected SoundEffect cs() {
            return NMSImpl.getSoundEffect(npc, super.cs(), NPC.Metadata.DEATH_SOUND);
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
        protected SoundEffect D() {
            return NMSImpl.getSoundEffect(npc, super.D(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder))
                bukkitEntity = new PufferFishNPC(this);
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
        protected void I() {
            if (npc == null) {
                super.I();
            }
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        public void mobTick() {
            super.mobTick();
            if (npc != null) {
                npc.update();
                if (npc.hasTrait(PufferFishTrait.class)) {
                    setPuffState(npc.getOrAddTrait(PufferFishTrait.class).getPuffState());
                }
            }
        }

        @Override
        public void movementTick() {
            boolean lastInWater = this.C;
            if (npc != null) {
                this.C = false;
            }
            super.movementTick();
            if (npc != null) {
                this.C = lastInWater;
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract)) {
                return !npc.isProtected();
            }
            return super.n(entity);
        }

        @Override
        public boolean z_() {
            if (npc == null || !npc.isFlyable()) {
                return super.z_();
            } else {
                return false;
            }
        }
    }

    public static class PufferFishNPC extends CraftPufferFish implements NPCHolder {
        private final CitizensNPC npc;

        public PufferFishNPC(EntityPufferFishNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}
