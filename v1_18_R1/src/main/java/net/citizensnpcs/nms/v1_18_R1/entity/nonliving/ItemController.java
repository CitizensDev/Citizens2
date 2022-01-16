package net.citizensnpcs.nms.v1_18_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemController extends AbstractEntityController {
    public ItemController() {
        super(EntityItemNPC.class);
    }

    @Override
    protected org.bukkit.entity.Entity createEntity(Location at, NPC npc) {
        ServerLevel ws = ((CraftWorld) at.getWorld()).getHandle();
        Material id = Material.STONE;
        int data = npc.data().get(NPC.ITEM_DATA_METADATA, npc.data().get("falling-block-data", 0));
        if (npc.data().has(NPC.ITEM_ID_METADATA)) {
            id = Material.getMaterial(npc.data().<String> get(NPC.ITEM_ID_METADATA), false);
        }
        if (id == Material.AIR) {
            id = Material.STONE;
            Messaging.severe(npc.getId(), "invalid Material: converted to stone");
        }
        final EntityItemNPC handle = new EntityItemNPC(ws, npc, at.getX(), at.getY(), at.getZ(),
                CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(id,
                        npc.data().get(NPC.ITEM_AMOUNT_METADATA, 1), (short) data)));
        return handle.getBukkitEntity();
    }

    @Override
    public Item getBukkitEntity() {
        return (Item) super.getBukkitEntity();
    }

    public static class EntityItemNPC extends ItemEntity implements NPCHolder {
        private final CitizensNPC npc;

        public EntityItemNPC(EntityType<? extends ItemEntity> types, Level level) {
            super(types, level);
            this.npc = null;
        }

        public EntityItemNPC(Level world, NPC npc, double x, double y, double z, ItemStack stack) {
            super(world, x, y, z, stack);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new ItemNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void playerTouch(Player entityhuman) {
            if (npc == null) {
                super.playerTouch(entityhuman);
            }
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
            } else {
                super.tick();
            }
        }
    }

    public static class ItemNPC extends CraftItem implements NPCHolder {
        private final CitizensNPC npc;

        public ItemNPC(EntityItemNPC entity) {
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
