package net.citizensnpcs.nms.v1_13_R2.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLingeringPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.entity.MobEntityController;
import net.citizensnpcs.nms.v1_13_R2.util.NMSBoundingBox;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.EntityPotion;
import net.minecraft.server.v1_13_R2.EnumPistonReaction;
import net.minecraft.server.v1_13_R2.FluidType;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.Tag;
import net.minecraft.server.v1_13_R2.World;

public class ThrownPotionController extends MobEntityController {
    public ThrownPotionController() {
        super(EntityThrownPotionNPC.class);
    }

    @Override
    public ThrownPotion getBukkitEntity() {
        return (ThrownPotion) super.getBukkitEntity();
    }

    public static class EntityThrownPotionNPC extends EntityPotion implements NPCHolder {
        private final CitizensNPC npc;

        public EntityThrownPotionNPC(World world) {
            this(world, null);
        }

        public EntityThrownPotionNPC(World world, NPC npc) {
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
                if (getItem() != null && getItem().getItem().equals(Items.LINGERING_POTION)) {
                    bukkitEntity = new LingeringThrownPotionNPC(this);
                } else {
                    bukkitEntity = new SplashThrownPotionNPC(this);
                }
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
        public void tick() {
            if (npc != null) {
                npc.update();
            } else {
                super.tick();
            }
        }
    }

    public static class LingeringThrownPotionNPC extends CraftLingeringPotion implements NPCHolder {
        private final CitizensNPC npc;

        public LingeringThrownPotionNPC(EntityThrownPotionNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class SplashThrownPotionNPC extends CraftLingeringPotion implements NPCHolder {
        private final CitizensNPC npc;

        public SplashThrownPotionNPC(EntityThrownPotionNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}