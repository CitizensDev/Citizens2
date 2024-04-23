package net.citizensnpcs.nms.v1_20_R4.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_20_R4.entity.MobEntityController;
import net.citizensnpcs.nms.v1_20_R4.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_20_R4.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_20_R4.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlockDisplayController extends MobEntityController {
    public BlockDisplayController() {
        super(EntityBlockDisplayNPC.class);
    }

    @Override
    protected org.bukkit.entity.Entity createEntity(Location at, NPC npc) {
        final EntityBlockDisplayNPC handle = new EntityBlockDisplayNPC(EntityType.BLOCK_DISPLAY,
                ((CraftWorld) at.getWorld()).getHandle(), npc);
        if (npc != null) {
            ((org.bukkit.entity.BlockDisplay) handle.getBukkitEntity())
                    .setBlock(npc.getItemProvider().get().getType().createBlockData());
        }
        return handle.getBukkitEntity();
    }

    @Override
    public org.bukkit.entity.BlockDisplay getBukkitEntity() {
        return (org.bukkit.entity.BlockDisplay) super.getBukkitEntity();
    }

    public static class BlockDisplayNPC extends CraftBlockDisplay implements ForwardingNPCHolder {
        public BlockDisplayNPC(EntityBlockDisplayNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityBlockDisplayNPC extends BlockDisplay implements NPCHolder {
        private final CitizensNPC npc;

        public EntityBlockDisplayNPC(EntityType<? extends BlockDisplay> types, Level level) {
            this(types, level, null);
        }

        public EntityBlockDisplayNPC(EntityType<? extends BlockDisplay> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new BlockDisplayNPC(this));
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
            super.tick();
            if (npc != null) {
                npc.update();
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
}
