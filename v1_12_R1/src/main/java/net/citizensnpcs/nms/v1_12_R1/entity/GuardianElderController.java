package net.citizensnpcs.nms.v1_12_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftElderGuardian;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_12_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_12_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityGuardianElder;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumPistonReaction;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.SoundEffect;
import net.minecraft.server.v1_12_R1.World;

public class GuardianElderController extends MobEntityController {
    public GuardianElderController() {
        super(EntityGuardianElderNPC.class);
    }

    @Override
    public ElderGuardian getBukkitEntity() {
        return (ElderGuardian) super.getBukkitEntity();
    }

    public static class EntityGuardianElderNPC extends EntityGuardianElder implements NPCHolder {
        private final CitizensNPC npc;

        public EntityGuardianElderNPC(World world) {
            this(world, null);
        }

        public EntityGuardianElderNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
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
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            }
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
                bukkitEntity = new GuardianElderNPC(this);
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
        public boolean m_() {
            if (npc == null || !npc.isFlyable())
                return super.m_();
            else
                return false;
        }

        @Override
        public void n() {
            if (npc == null) {
                super.n();
            } else {
                NMSImpl.updateAI(this);
                npc.update();
            }
        }

        @Override
        public void setSize(float f, float f1) {
            if (npc == null) {
                super.setSize(f, f1);
            } else {
                NMSImpl.setSize(this, f, f1, justCreated);
            }
        }
    }

    public static class GuardianElderNPC extends CraftElderGuardian implements NPCHolder {
        private final CitizensNPC npc;

        public GuardianElderNPC(EntityGuardianElderNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}