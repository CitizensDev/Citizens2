package net.citizensnpcs.nms.v1_11_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftZombieHorse;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_11_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_11_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_11_R1.AxisAlignedBB;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityHorseZombie;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.GenericAttributes;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.SoundEffect;
import net.minecraft.server.v1_11_R1.World;

public class HorseZombieController extends MobEntityController {
    public HorseZombieController() {
        super(EntityHorseZombieNPC.class);
    }

    @Override
    public void create(Location at, NPC npc) {
        npc.getOrAddTrait(HorseModifiers.class);
        super.create(at, npc);
    }

    @Override
    public ZombieHorse getBukkitEntity() {
        return (ZombieHorse) super.getBukkitEntity();
    }

    public static class EntityHorseZombieNPC extends EntityHorseZombie implements NPCHolder {
        private double baseMovementSpeed;

        private final CitizensNPC npc;

        private boolean riding;

        public EntityHorseZombieNPC(World world) {
            this(world, null);
        }

        public EntityHorseZombieNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                ((AbstractHorse) getBukkitEntity())
                        .setDomestication(((AbstractHorse) getBukkitEntity()).getMaxDomestication());
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
        public int aY() {
            return NMS.getFallDistance(npc, super.aY());
        }

        @Override
        public boolean bg() {
            return npc == null ? super.bg() : npc.isPushableByFluids();
        }

        @Override
        protected SoundEffect bW() {
            return NMSImpl.getSoundEffect(npc, super.bW(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        protected SoundEffect bX() {
            return NMSImpl.getSoundEffect(npc, super.bX(), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public float cl() {
            return NMS.getJumpPower(npc, super.cl());
        }

        @Override
        public void collide(net.minecraft.server.v1_11_R1.Entity entity) {
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
        public void g(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.g(f, f1);
            } else {
                NMSImpl.flyingMoveLogic(this, f, f1);
            }
        }

        @Override
        protected SoundEffect G() {
            return NMSImpl.getSoundEffect(npc, super.G(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder)) {
                bukkitEntity = new HorseZombieNPC(this);
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
            super.M();
            if (npc != null) {
                if (npc.hasTrait(Controllable.class) && npc.getOrAddTrait(Controllable.class).isEnabled()) {
                    riding = getBukkitEntity().getPassenger() != null;
                    getAttributeInstance(GenericAttributes.MOVEMENT_SPEED)
                            .setValue(baseMovementSpeed * npc.getNavigator().getDefaultParameters().speedModifier());
                } else {
                    riding = false;
                }
                if (riding) {
                    c(4, true);
                }
                NMS.setStepHeight(getBukkitEntity(), 1);
                npc.update();
            }
        }

        @Override
        public boolean m_() {
            if (npc == null || !npc.isFlyable())
                return super.m_();
            else
                return false;
        }
    }

    public static class HorseZombieNPC extends CraftZombieHorse implements NPCHolder {
        private final CitizensNPC npc;

        public HorseZombieNPC(EntityHorseZombieNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}