package net.citizensnpcs.nms.v1_13_R2.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftMule;
import org.bukkit.entity.Mule;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_13_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityBoat;
import net.minecraft.server.v1_13_R2.EntityHorseMule;
import net.minecraft.server.v1_13_R2.EntityMinecartAbstract;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumPistonReaction;
import net.minecraft.server.v1_13_R2.FluidType;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.SoundEffect;
import net.minecraft.server.v1_13_R2.Tag;
import net.minecraft.server.v1_13_R2.World;

public class HorseMuleController extends MobEntityController {
    public HorseMuleController() {
        super(EntityHorseMuleNPC.class);
    }

    @Override
    public void create(Location at, NPC npc) {
        npc.getOrAddTrait(HorseModifiers.class);
        super.create(at, npc);
    }

    @Override
    public Mule getBukkitEntity() {
        return (Mule) super.getBukkitEntity();
    }

    public static class EntityHorseMuleNPC extends EntityHorseMule implements NPCHolder {
        private double baseMovementSpeed;

        private final CitizensNPC npc;

        private boolean riding;

        public EntityHorseMuleNPC(World world) {
            this(world, null);
        }

        public EntityHorseMuleNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                ((Mule) getBukkitEntity()).setDomestication(((Mule) getBukkitEntity()).getMaxDomestication());
                baseMovementSpeed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
            }
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void a(boolean flag) {
            if (npc == null) {
                super.a(flag);
                return;
            }
            NMSImpl.checkAndUpdateHeight(this, flag, super::a);
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
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
        public void a(float f, float f1, float f2) {
            if (npc == null || !npc.isFlyable()) {
                super.a(f, f1, f2);
            } else {
                NMSImpl.flyingMoveLogic(this, f, f1, f2);
            }
        }

        @Override
        public boolean b(Tag<FluidType> tag) {
            if (npc == null)
                return super.b(tag);
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
        public boolean bT() {
            if (npc != null && riding)
                return true;
            return super.bT();
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
            if (npc != null && !(bukkitEntity instanceof NPCHolder)) {
                bukkitEntity = new HorseMuleNPC(this);
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
                if (npc.hasTrait(Controllable.class) && npc.getOrAddTrait(Controllable.class).isEnabled()) {
                    riding = getBukkitEntity().getPassengers().size() > 0;
                    getAttributeInstance(GenericAttributes.MOVEMENT_SPEED)
                            .setValue(baseMovementSpeed * npc.getNavigator().getDefaultParameters().speedModifier());
                } else {
                    riding = false;
                }
                if (riding) {
                    d(4, true); // datawatcher method
                }
                NMS.setStepHeight(getBukkitEntity(), 1);
                npc.update();
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract))
                return !npc.isProtected();
            return super.n(entity);
        }

        @Override
        public boolean z_() {
            if (npc == null || !npc.isFlyable())
                return super.z_();
            else
                return false;
        }
    }

    public static class HorseMuleNPC extends CraftMule implements NPCHolder {
        private final CitizensNPC npc;

        public HorseMuleNPC(EntityHorseMuleNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}