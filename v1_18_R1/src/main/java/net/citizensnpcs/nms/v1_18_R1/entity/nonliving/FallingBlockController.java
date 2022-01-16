package net.citizensnpcs.nms.v1_18_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftFallingBlock;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_18_R1.util.NMSImpl;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FallingBlockController extends AbstractEntityController {
    public FallingBlockController() {
        super(EntityFallingBlockNPC.class);
    }

    @Override
    protected org.bukkit.entity.Entity createEntity(Location at, NPC npc) {
        ServerLevel ws = ((CraftWorld) at.getWorld()).getHandle();
        Block id = Blocks.STONE;
        int data = npc.data().get(NPC.ITEM_DATA_METADATA, npc.data().get("falling-block-data", 0));
        // TODO: how to incorporate this - probably delete?
        if (npc.data().has("falling-block-id") || npc.data().has(NPC.ITEM_ID_METADATA)) {
            id = CraftMagicNumbers.getBlock(Material.getMaterial(
                    npc.data().<String> get(NPC.ITEM_ID_METADATA, npc.data().<String> get("falling-block-id")), false));
        }
        final EntityFallingBlockNPC handle = new EntityFallingBlockNPC(ws, npc, at.getX(), at.getY(), at.getZ(),
                id.defaultBlockState());
        return handle.getBukkitEntity();
    }

    @Override
    public FallingBlock getBukkitEntity() {
        return (FallingBlock) super.getBukkitEntity();
    }

    public static class EntityFallingBlockNPC extends FallingBlockEntity implements NPCHolder {
        private final CitizensNPC npc;

        public EntityFallingBlockNPC(EntityType<? extends FallingBlockEntity> types, Level level) {
            this(types, level, null);
        }

        public EntityFallingBlockNPC(EntityType<? extends FallingBlockEntity> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        public EntityFallingBlockNPC(Level world, NPC npc, double d0, double d1, double d2, BlockState data) {
            super(world, d0, d1, d2, data);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new FallingBlockNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void push(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.push(vector.getX(), vector.getY(), vector.getZ());
            }
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
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public Entity teleportTo(ServerLevel worldserver, BlockPos location) {
            if (npc == null)
                return super.teleportTo(worldserver, location);
            return NMSImpl.teleportAcrossWorld(this, worldserver, location);
        }

        @Override
        public void tick() {
            if (npc != null) {
                npc.update();
                Vec3 mot = getDeltaMovement();
                if (Math.abs(mot.x) > EPSILON || Math.abs(mot.y) > EPSILON || Math.abs(mot.z) > EPSILON) {
                    mot = mot.multiply(0.98, 0.98, 0.98);
                    setDeltaMovement(mot);
                    move(MoverType.SELF, mot);
                }
            } else {
                super.tick();
            }
        }

        private static final double EPSILON = 0.001;
    }

    public static class FallingBlockNPC extends CraftFallingBlock implements NPCHolder {
        private final CitizensNPC npc;

        public FallingBlockNPC(EntityFallingBlockNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        public void setType(Material material, int data) {
            npc.data().setPersistent(NPC.ITEM_ID_METADATA, material.name());
            npc.data().setPersistent(NPC.ITEM_DATA_METADATA, data);
            if (npc.isSpawned()) {
                npc.despawn(DespawnReason.PENDING_RESPAWN);
                npc.spawn(npc.getStoredLocation(), SpawnReason.RESPAWN);
            }
        }
    }
}
