package net.citizensnpcs.nms.v1_16_R3.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftBat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Bat;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R3.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_16_R3.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_16_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityBat;
import net.minecraft.server.v1_16_R3.EntityBoat;
import net.minecraft.server.v1_16_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumPistonReaction;
import net.minecraft.server.v1_16_R3.FluidType;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.SoundEffect;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;

public class BatController extends MobEntityController {
    public BatController() {
        super(EntityBatNPC.class);
    }

    @Override
    public Bat getBukkitEntity() {
        return (Bat) super.getBukkitEntity();
    }

    public static class BatNPC extends CraftBat implements ForwardingNPCHolder {
        public BatNPC(EntityBatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityBatNPC extends EntityBat implements NPCHolder {
        private final CitizensNPC npc;

        public EntityBatNPC(EntityTypes<? extends EntityBat> types, World world) {
            this(types, world, null);
        }

        public EntityBatNPC(EntityTypes<? extends EntityBat> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                setAsleep(false);
            }
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void a(float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, (evt) -> super.a((float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public boolean a(Tag<FluidType> tag, double d0) {
            if (npc == null) {
                return super.a(tag, d0);
            }
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
        public float dJ() {
            return NMS.getJumpPower(npc, super.dJ());
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new BatNPC(this));
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
        public SoundEffect getSoundAmbient() {
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
        public void mobTick() {
            if (npc == null) {
                super.mobTick();
            } else {
                NMSImpl.updateMinecraftAIState(npc, this);
                if (npc.useMinecraftAI()) {
                    super.mobTick();
                }
                npc.update();
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract)) {
                return !npc.isProtected();
            }
            return super.n(entity);
        }
    }
}
