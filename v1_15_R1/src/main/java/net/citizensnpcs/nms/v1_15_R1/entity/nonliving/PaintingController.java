package net.citizensnpcs.nms.v1_15_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPainting;
import org.bukkit.entity.Painting;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_15_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_15_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_15_R1.EntityPainting;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.World;

public class PaintingController extends MobEntityController {
    public PaintingController() {
        super(EntityPaintingNPC.class);
    }

    @Override
    public Painting getBukkitEntity() {
        return (Painting) super.getBukkitEntity();
    }

    public static class EntityPaintingNPC extends EntityPainting implements NPCHolder {
        private final CitizensNPC npc;

        public EntityPaintingNPC(EntityTypes<? extends EntityPainting> types, World world) {
            this(types, world, null);
        }

        public EntityPaintingNPC(EntityTypes<? extends EntityPainting> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void collide(net.minecraft.server.v1_15_R1.Entity entity) {
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
        public void h(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.h(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new PaintingNPC(this));
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

    public static class PaintingNPC extends CraftPainting implements NPCHolder {
        private final CitizensNPC npc;

        public PaintingNPC(EntityPaintingNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}
