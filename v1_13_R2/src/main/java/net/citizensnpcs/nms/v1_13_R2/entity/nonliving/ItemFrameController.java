package net.citizensnpcs.nms.v1_13_R2.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftItemFrame;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.entity.MobEntityController;
import net.citizensnpcs.nms.v1_13_R2.util.NMSBoundingBox;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.EntityItemFrame;
import net.minecraft.server.v1_13_R2.EnumDirection;
import net.minecraft.server.v1_13_R2.EnumPistonReaction;
import net.minecraft.server.v1_13_R2.FluidType;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.Tag;
import net.minecraft.server.v1_13_R2.World;

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

        public EntityItemFrameNPC(World world) {
            this(world, null);
        }

        public EntityItemFrameNPC(World world, NPC npc) {
            super(world);
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
            double mx = motX;
            double my = motY;
            double mz = motZ;
            boolean res = super.b(tag);
            if (!npc.isPushableByFluids()) {
                motX = mx;
                motY = my;
                motZ = mz;
            }
            return res;
        }

        @Override
        public void collide(net.minecraft.server.v1_13_R2.Entity entity) {
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
            if (npc != null && !(bukkitEntity instanceof NPCHolder)) {
                bukkitEntity = new ItemFrameNPC(this);
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
            setItem(npc.getItemProvider().get());
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}