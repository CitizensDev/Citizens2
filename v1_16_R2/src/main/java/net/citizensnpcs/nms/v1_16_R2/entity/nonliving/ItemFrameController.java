package net.citizensnpcs.nms.v1_16_R2.entity.nonliving;

import net.citizensnpcs.nms.v1_16_R2.entity.MobEntityController;
import net.citizensnpcs.nms.v1_16_R2.util.NMSImpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftItemFrame;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.EntityItemFrame;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.EnumDirection;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.World;

public class ItemFrameController extends MobEntityController {
    public ItemFrameController() {
        super(EntityItemFrameNPC.class);
    }

    @Override
    protected Entity createEntity(Location at, NPC npc) {
        Entity e = super.createEntity(at, npc);
        EntityItemFrame item = (EntityItemFrame) ((CraftEntity) e).getHandle();
        item.setDirection(EnumDirection.EAST);
        item.blockPosition = new BlockPosition(at.getX(), at.getY(), at.getZ());
        return e;
    }

    @Override
    public ItemFrame getBukkitEntity() {
        return (ItemFrame) super.getBukkitEntity();
    }

    public static class EntityItemFrameNPC extends EntityItemFrame implements NPCHolder {
        private final CitizensNPC npc;

        public EntityItemFrameNPC(EntityTypes<? extends EntityItemFrame> types, World world) {
            this(types, world, null);
        }

        public EntityItemFrameNPC(EntityTypes<? extends EntityItemFrame> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void collide(net.minecraft.server.v1_16_R2.Entity entity) {
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
        public void i(double x, double y, double z) {
            if (npc == null) {
                super.i(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.i(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.i(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new ItemFrameNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean survives() {
            return npc == null || !npc.isProtected() ? super.survives() : true;
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

    public static class ItemFrameNPC extends CraftItemFrame implements NPCHolder {
        private final CitizensNPC npc;

        public ItemFrameNPC(EntityItemFrameNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
            Material id = Material.STONE;
            int data = npc.data().get(NPC.ITEM_DATA_METADATA, npc.data().get("falling-block-data", 0));
            if (npc.data().has(NPC.ITEM_ID_METADATA)) {
                id = Material.getMaterial(npc.data().<String> get(NPC.ITEM_ID_METADATA));
            }
            getItem().setType(id);
            getItem().setDurability((short) data);
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
