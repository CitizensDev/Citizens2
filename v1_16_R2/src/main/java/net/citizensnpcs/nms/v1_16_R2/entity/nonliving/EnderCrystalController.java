package net.citizensnpcs.nms.v1_16_R2.entity.nonliving;

import net.citizensnpcs.nms.v1_16_R2.entity.MobEntityController;
import net.citizensnpcs.nms.v1_16_R2.util.NMSImpl;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R2.EntityEnderCrystal;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.World;

public class EnderCrystalController extends MobEntityController {
    public EnderCrystalController() {
        super(EntityEnderCrystalNPC.class);
    }

    @Override
    public EnderCrystal getBukkitEntity() {
        return (EnderCrystal) super.getBukkitEntity();
    }

    public static class EnderCrystalNPC extends CraftEnderCrystal implements NPCHolder {
        private final CitizensNPC npc;

        public EnderCrystalNPC(EntityEnderCrystalNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityEnderCrystalNPC extends EntityEnderCrystal implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEnderCrystalNPC(EntityTypes<? extends EntityEnderCrystal> types, World world) {
            this(types, world, null);
        }

        public EntityEnderCrystalNPC(EntityTypes<? extends EntityEnderCrystal> types, World world, NPC npc) {
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
                NMSImpl.setBukkitEntity(this, new EnderCrystalNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
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
}
