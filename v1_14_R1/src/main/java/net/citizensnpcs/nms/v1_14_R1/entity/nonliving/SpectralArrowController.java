package net.citizensnpcs.nms.v1_14_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_14_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_14_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_14_R1.EntitySpectralArrow;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.World;

public class SpectralArrowController extends MobEntityController {
    public SpectralArrowController() {
        super(EntitySpectralArrowNPC.class);
    }

    @Override
    public Arrow getBukkitEntity() {
        return (Arrow) super.getBukkitEntity();
    }

    public static class EntitySpectralArrowNPC extends EntitySpectralArrow implements NPCHolder {
        private final CitizensNPC npc;

        public EntitySpectralArrowNPC(EntityTypes<? extends EntitySpectralArrow> types, World world) {
            this(types, world, null);
        }

        public EntitySpectralArrowNPC(EntityTypes<? extends EntitySpectralArrow> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void collide(net.minecraft.server.v1_14_R1.Entity entity) {
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
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new SpectralArrowNPC(this));
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

    public static class SpectralArrowNPC extends CraftArrow implements NPCHolder {
        private final CitizensNPC npc;

        public SpectralArrowNPC(EntitySpectralArrowNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}
