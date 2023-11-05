package net.citizensnpcs.nms.v1_15_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLargeFireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_15_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_15_R1.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_15_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_15_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.EntityLargeFireball;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumPistonReaction;
import net.minecraft.server.v1_15_R1.FluidType;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.Tag;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;

public class LargeFireballController extends MobEntityController {
    public LargeFireballController() {
        super(EntityLargeFireballNPC.class);
    }

    @Override
    public LargeFireball getBukkitEntity() {
        return (LargeFireball) super.getBukkitEntity();
    }

    public static class EntityLargeFireballNPC extends EntityLargeFireball implements NPCHolder {
        private final CitizensNPC npc;

        public EntityLargeFireballNPC(EntityTypes<? extends EntityLargeFireball> types, World world) {
            this(types, world, null);
        }

        public EntityLargeFireballNPC(EntityTypes<? extends EntityLargeFireball> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public boolean b(Tag<FluidType> tag) {
            if (npc == null)
                return super.b(tag);
            Vec3D old = getMot().add(0, 0, 0);
            boolean res = super.b(tag);
            if (!npc.isPushableByFluids()) {
                setMot(old);
            }
            return res;
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
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new LargeFireballNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public EnumPistonReaction getPushReaction() {
            return Util.callPistonPushEvent(npc) ? EnumPistonReaction.IGNORE : super.getPushReaction();
        }

        @Override
        public void h(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.h(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void tick() {
            if (npc != null) {
                npc.update();
                if (!npc.isProtected()) {
                    super.tick();
                }
            } else {
                super.tick();
            }
        }

        @Override
        public void updateSize() {
            if (npc == null) {
                super.updateSize();
            } else {
                NMSImpl.setSize(this, justCreated);
            }
        }
    }

    public static class LargeFireballNPC extends CraftLargeFireball implements ForwardingNPCHolder {
        public LargeFireballNPC(EntityLargeFireballNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
