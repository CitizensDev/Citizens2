package net.citizensnpcs.nms.v1_21_R5.entity.nonliving;

import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R5.CraftServer;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftEntity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.nms.v1_21_R5.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_21_R5.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_21_R5.util.NMSImpl;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BoatController extends AbstractEntityController {
    @Override
    protected org.bukkit.entity.Entity createEntity(Location at, NPC npc) {
        EntityType<? extends Boat> type = EntityType.OAK_BOAT;
        Item item = Items.OAK_BOAT;
        switch (npc.getOrAddTrait(MobType.class).getType()) {
            case ACACIA_BOAT:
                type = EntityType.ACACIA_BOAT;
                item = Items.ACACIA_BOAT;
                break;
            case BIRCH_BOAT:
                type = EntityType.BIRCH_BOAT;
                item = Items.BIRCH_BOAT;
                break;
            case CHERRY_BOAT:
                type = EntityType.CHERRY_BOAT;
                item = Items.CHERRY_BOAT;
                break;
            case DARK_OAK_BOAT:
                type = EntityType.DARK_OAK_BOAT;
                item = Items.DARK_OAK_BOAT;
                break;
            case JUNGLE_BOAT:
                type = EntityType.JUNGLE_BOAT;
                item = Items.JUNGLE_BOAT;
                break;
            case MANGROVE_BOAT:
                type = EntityType.MANGROVE_BOAT;
                item = Items.MANGROVE_BOAT;
                break;
            case OAK_BOAT:
                break;
            case SPRUCE_BOAT:
                type = EntityType.SPRUCE_BOAT;
                item = Items.SPRUCE_BOAT;
                break;
            default:
                break;
        }
        final Item fitem = item;
        final EntityBoatNPC handle = new EntityBoatNPC(type, ((CraftWorld) at.getWorld()).getHandle(), () -> fitem,
                npc);
        return handle.getBukkitEntity();
    }

    @Override
    public org.bukkit.entity.Boat getBukkitEntity() {
        return (org.bukkit.entity.Boat) super.getBukkitEntity();
    }

    public static class BoatNPC extends CraftBoat implements ForwardingNPCHolder {
        public BoatNPC(EntityBoatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityBoatNPC extends Boat implements NPCHolder {
        private float invFriction;
        private float landFriction;
        private Status lastStatus;
        private final CitizensNPC npc;
        private Status status;
        private double waterLevel;

        public EntityBoatNPC(EntityType<? extends Boat> types, Level level, Supplier<Item> supplier) {
            this(types, level, supplier, null);
        }

        public EntityBoatNPC(EntityType<? extends Boat> types, Level level, Supplier<Item> supplier, NPC npc) {
            super(types, level, supplier);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public boolean broadcastToPlayer(ServerPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.broadcastToPlayer(player));
        }

        private boolean checkInWater() {
            AABB axisalignedbb = this.getBoundingBox();
            int i = Mth.floor(axisalignedbb.minX);
            int j = Mth.ceil(axisalignedbb.maxX);
            int k = Mth.floor(axisalignedbb.minY);
            int l = Mth.ceil(axisalignedbb.minY + 0.001);
            int i1 = Mth.floor(axisalignedbb.minZ);
            int j1 = Mth.ceil(axisalignedbb.maxZ);
            boolean flag = false;
            this.waterLevel = -1.7976931348623157E308;
            BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();

            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = k; l1 < l; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        blockposition_mutableblockposition.set(k1, l1, i2);
                        FluidState fluid = this.level().getFluidState(blockposition_mutableblockposition);
                        if (fluid.is(FluidTags.WATER)) {
                            float f = l1 + fluid.getHeight(this.level(), blockposition_mutableblockposition);
                            this.waterLevel = Math.max(f, this.waterLevel);
                            flag |= axisalignedbb.minY < f;
                        }
                    }
                }
            }
            return flag;
        }

        private void floatBoat() {
            double d0 = -this.getGravity();
            double d1 = 0.0;
            this.invFriction = 0.05F;
            if (this.lastStatus == net.minecraft.world.entity.vehicle.Boat.Status.IN_AIR
                    && this.status != net.minecraft.world.entity.vehicle.Boat.Status.IN_AIR
                    && this.status != net.minecraft.world.entity.vehicle.Boat.Status.ON_LAND) {
                this.waterLevel = this.getY(1.0);
                double d2 = this.getWaterLevelAbove() - this.getBbHeight() + 0.101;
                if (this.level().noCollision(this, this.getBoundingBox().move(0.0, d2 - this.getY(), 0.0))) {
                    this.move(MoverType.SELF, new Vec3(0.0, d2 - this.getY(), 0.0));
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                }
                this.status = net.minecraft.world.entity.vehicle.Boat.Status.IN_WATER;
            } else {
                if (this.status == net.minecraft.world.entity.vehicle.Boat.Status.IN_WATER) {
                    d1 = (this.waterLevel - this.getY()) / this.getBbHeight();
                    this.invFriction = 0.9F;
                } else if (this.status == net.minecraft.world.entity.vehicle.Boat.Status.UNDER_FLOWING_WATER) {
                    d0 = -7.0E-4;
                    this.invFriction = 0.9F;
                } else if (this.status == net.minecraft.world.entity.vehicle.Boat.Status.UNDER_WATER) {
                    d1 = 0.009999999776482582;
                    this.invFriction = 0.45F;
                } else if (this.status == net.minecraft.world.entity.vehicle.Boat.Status.IN_AIR) {
                    this.invFriction = 0.9F;
                } else if (this.status == net.minecraft.world.entity.vehicle.Boat.Status.ON_LAND) {
                    this.invFriction = this.landFriction;
                    if (this.getControllingPassenger() instanceof Player) {
                        this.landFriction /= 2.0F;
                    }
                }
                Vec3 vec3d = this.getDeltaMovement();
                this.setDeltaMovement(vec3d.x * this.invFriction, vec3d.y + d0, vec3d.z * this.invFriction);
                if (d1 > 0.0) {
                    Vec3 vec3d1 = this.getDeltaMovement();
                    this.setDeltaMovement(vec3d1.x, (vec3d1.y + d1 * (this.getDefaultGravity() / 0.65)) * 0.75,
                            vec3d1.z);
                }
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new BoatNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public PushReaction getPistonPushReaction() {
            return Util.callPistonPushEvent(npc) ? PushReaction.IGNORE : super.getPistonPushReaction();
        }

        private Status getStatus() {
            Status entityboat_Status = isUnderwater();
            if (entityboat_Status != null) {
                this.waterLevel = getBoundingBox().maxY;
                return entityboat_Status;
            }
            if (checkInWater())
                return Status.IN_WATER;
            float f = getGroundFriction();
            if (f > 0.0F) {
                this.landFriction = f;
                return Status.ON_LAND;
            }
            return Status.IN_AIR;
        }

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        private Status isUnderwater() {
            AABB axisalignedbb = this.getBoundingBox();
            double d0 = axisalignedbb.maxY + 0.001;
            int i = Mth.floor(axisalignedbb.minX);
            int j = Mth.ceil(axisalignedbb.maxX);
            int k = Mth.floor(axisalignedbb.maxY);
            int l = Mth.ceil(d0);
            int i1 = Mth.floor(axisalignedbb.minZ);
            int j1 = Mth.ceil(axisalignedbb.maxZ);
            boolean flag = false;
            BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();

            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = k; l1 < l; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        blockposition_mutableblockposition.set(k1, l1, i2);
                        FluidState fluid = this.level().getFluidState(blockposition_mutableblockposition);
                        if (fluid.is(FluidTags.WATER) && d0 < blockposition_mutableblockposition.getY()
                                + fluid.getHeight(this.level(), blockposition_mutableblockposition)) {
                            if (!fluid.isSource())
                                return net.minecraft.world.entity.vehicle.Boat.Status.UNDER_FLOWING_WATER;
                            flag = true;
                        }
                    }
                }
            }
            return flag ? Status.UNDER_WATER : null;
        }

        @Override
        protected AABB makeBoundingBox(Vec3 vec3) {
            return NMSBoundingBox.makeBB(npc, super.makeBoundingBox(vec3));
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
        public void refreshDimensions() {
            if (npc == null) {
                super.refreshDimensions();
            } else {
                NMSImpl.setSize(this, firstTick);
            }
        }

        @Override
        public boolean save(ValueOutput save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public Entity teleport(TeleportTransition transition) {
            if (npc == null)
                return super.teleport(transition);
            return NMSImpl.teleportAcrossWorld(this, transition);
        }

        @Override
        public void tick() {
            if (npc == null) {
                super.tick();
                return;
            }
            baseTick();
            if (entityData.isDirty()) {
                for (Player p : CitizensAPI.getLocationLookup().getNearbyPlayers(getBukkitEntity().getLocation(), 64)) {
                    NMSImpl.sendPacket(p, new ClientboundSetEntityDataPacket(this.getId(), entityData.packDirty()));
                }
            }
            if (getControllingPassenger() instanceof NPCHolder
                    && ((NPCHolder) getControllingPassenger()).getNPC().getNavigator().isNavigating()) {
                setDeltaMovement(getControllingPassenger().getDeltaMovement().multiply(20, 0, 20));
            }
            npc.update();
            if (getHurtTime() > 0) {
                setHurtTime(getHurtTime() - 1);
            }
            if (getDamage() > 0.0F) {
                setDamage(getDamage() - 1.0F);
            }
            lastStatus = status;
            status = getStatus();
            floatBoat();
            move(MoverType.SELF, getDeltaMovement());
            applyEffectsFromBlocks();
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
            if (npc == null)
                return super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            Vec3 old = getDeltaMovement().add(0, 0, 0);
            boolean res = super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            if (!npc.isPushableByFluids()) {
                setDeltaMovement(old);
            }
            return res;
        }
    }
}
