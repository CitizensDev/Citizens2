package net.citizensnpcs.nms.v1_20_R3.entity.nonliving;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftFishHook;
import org.bukkit.entity.FishHook;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_20_R3.entity.MobEntityController;
import net.citizensnpcs.nms.v1_20_R3.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_20_R3.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_20_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FishingHookController extends MobEntityController {
    public FishingHookController() {
        super(EntityFishingHookNPC.class);
    }

    @Override
    protected org.bukkit.entity.Entity createEntity(Location at, NPC npc) {
        ServerLevel level = ((CraftWorld) at.getWorld()).getHandle();
        ServerPlayer sp = new ServerPlayer(level.getServer(), level,
                new GameProfile(UUID.randomUUID(), "dummyfishhook"), ClientInformation.createDefault()) {
        };
        sp.setPos(at.getX(), at.getY(), at.getZ());
        sp.setYRot(at.getYaw());
        sp.setXRot(at.getPitch());
        sp.setHealth(20F);
        sp.getInventory().items.set(sp.getInventory().selected, new ItemStack(Items.FISHING_ROD, 1));
        final EntityFishingHookNPC handle = new EntityFishingHookNPC(EntityType.FISHING_BOBBER, level, npc, sp);
        return handle.getBukkitEntity();
    }

    @Override
    public FishHook getBukkitEntity() {
        return (FishHook) super.getBukkitEntity();
    }

    public static class EntityFishingHookNPC extends FishingHook implements NPCHolder {
        private final CitizensNPC npc;

        public EntityFishingHookNPC(EntityType<? extends FishingHook> types, Level level) {
            this(types, level, null, null);
        }

        public EntityFishingHookNPC(EntityType<? extends FishingHook> types, Level level, NPC npc, ServerPlayer sp) {
            super(sp, level, 0, 0);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public double distanceToSqr(Entity entity) {
            if (entity == getPlayerOwner())
                return 0D;
            return super.distanceToSqr(entity);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new FishingHookNPC(this));
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

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        @Override
        protected AABB makeBoundingBox() {
            return NMSBoundingBox.makeBB(npc, super.makeBoundingBox());
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
        public Entity teleportTo(ServerLevel worldserver, Vec3 location) {
            if (npc == null)
                return super.teleportTo(worldserver, location);
            return NMSImpl.teleportAcrossWorld(this, worldserver, location);
        }

        @Override
        public void tick() {
            if (npc != null) {
                getPlayerOwner().unsetRemoved();
                NMSImpl.setLife(this, 0);
                npc.update();
            } else {
                super.tick();
            }
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

    public static class FishingHookNPC extends CraftFishHook implements ForwardingNPCHolder {
        public FishingHookNPC(EntityFishingHookNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
