package net.citizensnpcs.npc.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftDragonFireball;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.DragonFireball;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_10_R1.EntityDragonFireball;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.World;

public class DragonFireballController extends MobEntityController {
    public DragonFireballController() {
        super(EntityDragonFireballNPC.class);
    }

    @Override
    public DragonFireball getBukkitEntity() {
        return (DragonFireball) super.getBukkitEntity();
    }

    public static class DragonFireballNPC extends CraftDragonFireball implements NPCHolder {
        private final CitizensNPC npc;

        public DragonFireballNPC(EntityDragonFireballNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityDragonFireballNPC extends EntityDragonFireball implements NPCHolder {
        private final CitizensNPC npc;

        public EntityDragonFireballNPC(World world) {
            this(world, null);
        }

        public EntityDragonFireballNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
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
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null) {
                bukkitEntity = new DragonFireballNPC(this);
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void m() {
            if (npc != null) {
                npc.update();
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)) {
                    super.m();
                }
            } else {
                super.m();
            }
        }

        @Override
        public void setSize(float f, float f1) {
            if (npc == null) {
                super.setSize(f, f1);
            } else {
                NMS.setSize(this, f, f1, justCreated);
            }
        }
    }
}