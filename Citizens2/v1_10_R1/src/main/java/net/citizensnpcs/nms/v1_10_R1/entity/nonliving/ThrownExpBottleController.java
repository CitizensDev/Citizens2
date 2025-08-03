package net.citizensnpcs.nms.v1_10_R1.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftThrownExpBottle;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_10_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_10_R1.util.NMSBoundingBox;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.EntityThrownExpBottle;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.World;

public class ThrownExpBottleController extends MobEntityController {
    public ThrownExpBottleController() {
        super(EntityThrownExpBottleNPC.class);
    }

    @Override
    public ThrownExpBottle getBukkitEntity() {
        return (ThrownExpBottle) super.getBukkitEntity();
    }

    public static class EntityThrownExpBottleNPC extends EntityThrownExpBottle implements NPCHolder {
        private final CitizensNPC npc;

        public EntityThrownExpBottleNPC(World world) {
            this(world, null);
        }

        public EntityThrownExpBottleNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public boolean a(EntityPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.a(player));
        }

        @Override
        public boolean bg() {
            return npc == null ? super.bg() : npc.isPushableByFluids();
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
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder)) {
                bukkitEntity = new ThrownExpBottleNPC(this);
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
                if (!npc.isProtected()) {
                    super.m();
                }
            } else {
                super.m();
            }
        }
    }

    public static class ThrownExpBottleNPC extends CraftThrownExpBottle implements NPCHolder {
        private final CitizensNPC npc;

        public ThrownExpBottleNPC(EntityThrownExpBottleNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}