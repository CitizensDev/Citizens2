package net.citizensnpcs.nms.v1_10_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftBat;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.Bat;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_10_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_10_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityBat;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.SoundEffect;
import net.minecraft.server.v1_10_R1.World;

public class BatController extends MobEntityController {
    public BatController() {
        super(EntityBatNPC.class);
    }

    @Override
    public Bat getBukkitEntity() {
        return (Bat) super.getBukkitEntity();
    }

    public static class BatNPC extends CraftBat implements NPCHolder {
        private final CitizensNPC npc;

        public BatNPC(EntityBatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityBatNPC extends EntityBat implements NPCHolder {
        private final CitizensNPC npc;

        public EntityBatNPC(World world) {
            this(world, null);
        }

        public EntityBatNPC(World world, NPC npc) {
            super(world);
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
        public void a(Entity entity, float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, evt -> super.a(entity, (float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public boolean a(EntityPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.a(player));
        }

        @Override
        public int aY() {
            return NMS.getFallDistance(npc, super.aY());
        }

        @Override
        public boolean bg() {
            return npc == null ? super.bg() : npc.isPushableByFluids();
        }

        @Override
        protected SoundEffect bV() {
            return NMSImpl.getSoundEffect(npc, super.bV(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        protected SoundEffect bW() {
            return NMSImpl.getSoundEffect(npc, super.bW(), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public float ck() {
            return NMS.getJumpPower(npc, super.ck());
        }

        @Override
        public void collide(net.minecraft.server.v1_10_R1.Entity entity) {
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
        public void g(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        protected SoundEffect G() {
            return NMSImpl.getSoundEffect(npc, super.G(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder)) {
                bukkitEntity = new BatNPC(this);
            }
            return super.getBukkitEntity();
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
        protected void L() {
            if (npc == null) {
                super.L();
            }
        }

        @Override
        public void M() {
            if (npc == null) {
                super.M();
            } else {
                NMSImpl.updateAI(this);
                npc.update();
            }
        }
    }
}