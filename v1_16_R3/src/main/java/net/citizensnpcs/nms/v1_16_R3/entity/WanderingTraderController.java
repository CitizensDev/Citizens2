package net.citizensnpcs.nms.v1_16_R3.entity;

import java.lang.invoke.MethodHandle;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftWanderingTrader;
import org.bukkit.entity.WanderingTrader;
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
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityBoat;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityLightning;
import net.minecraft.server.v1_16_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityVillagerTrader;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.EnumInteractionResult;
import net.minecraft.server.v1_16_R3.EnumPistonReaction;
import net.minecraft.server.v1_16_R3.FluidType;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.MerchantRecipe;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.SoundEffect;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;

public class WanderingTraderController extends MobEntityController {
    public WanderingTraderController() {
        super(EntityWanderingTraderNPC.class);
    }

    @Override
    public WanderingTrader getBukkitEntity() {
        return (WanderingTrader) super.getBukkitEntity();
    }

    public static class EntityWanderingTraderNPC extends EntityVillagerTrader implements NPCHolder {
        private boolean blockingATrade;

        private boolean blockTrades = true;

        private final CitizensNPC npc;

        public EntityWanderingTraderNPC(EntityTypes<? extends EntityVillagerTrader> types, World world) {
            this(types, world, null);
        }

        public EntityWanderingTraderNPC(EntityTypes<? extends EntityVillagerTrader> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void a(DataWatcherObject<?> datawatcherobject) {
            if (npc == null) {
                super.a(datawatcherobject);
                return;
            }
            NMSImpl.checkAndUpdateHeight(this, datawatcherobject, super::a);
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
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
        public EnumInteractionResult b(EntityHuman entityhuman, EnumHand enumhand) {
            if (npc != null && blockTrades) {
                blockingATrade = true;
                List<MerchantRecipe> list = getOffers();
                if (list != null) {
                    list.clear();
                }
            }
            return super.b(entityhuman, enumhand);
        }

        @Override
        public boolean b(float f, float f1) {
            if (npc == null || !npc.isFlyable())
                return super.b(f, f1);
            return false;
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
        public boolean eN() {
            if (blockingATrade) {
                blockingATrade = false;
                return true;
            }
            return super.eN();
        }

        @Override
        public void g(Vec3D vec3d) {
            if (npc == null || !npc.isFlyable()) {
                super.g(vec3d);
            } else {
                NMSImpl.flyingMoveLogic(this, vec3d);
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new WanderingTraderNPC(this));
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

        public boolean isBlockingTrades() {
            return blockTrades;
        }

        @Override
        public boolean isClimbing() {
            if (npc == null || !npc.isFlyable())
                return super.isClimbing();
            else
                return false;
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        public void mobTick() {
            if (npc != null) {
                NMSImpl.updateMinecraftAIState(npc, this);
            }
            super.mobTick();
            if (npc != null) {
                try {
                    if (bq != null) {
                        bq.invoke(this, 10); // DespawnDelay
                    }
                } catch (Throwable e) {
                }
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
        public void onLightningStrike(WorldServer server, EntityLightning entitylightning) {
            if (npc == null) {
                super.onLightningStrike(server, entitylightning);
            }
        }

        public void setBlockTrades(boolean blocked) {
            this.blockTrades = blocked;
        }

        private static final MethodHandle bq = NMS.getSetter(EntityVillagerTrader.class, "bq");
    }

    public static class WanderingTraderNPC extends CraftWanderingTrader implements ForwardingNPCHolder {
        public WanderingTraderNPC(EntityWanderingTraderNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
