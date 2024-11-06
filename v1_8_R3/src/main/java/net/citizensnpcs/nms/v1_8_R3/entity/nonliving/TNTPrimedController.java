package net.citizensnpcs.nms.v1_8_R3.entity.nonliving;

import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateEntityNBT;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_8_R3.entity.MobEntityController;
import net.citizensnpcs.nms.v1_8_R3.util.NMSBoundingBox;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;

public class TNTPrimedController extends MobEntityController {
    public TNTPrimedController() {
        super(EntityTNTPrimedNPC.class);
    }

    @Override
    public TNTPrimed getBukkitEntity() {
        return (TNTPrimed) super.getBukkitEntity();
    }

    public static class EntityTNTPrimedNPC extends EntityTNTPrimed implements NPCHolder {
        private final CitizensNPC npc;

        public EntityTNTPrimedNPC(World world) {
            this(world, null);
        }

        public EntityTNTPrimedNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            this.fuseTicks = Integer.MAX_VALUE;
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
        public boolean aL() {
            return npc == null ? super.aL() : npc.isPushableByFluids();
        }

        @Override
        public void collide(net.minecraft.server.v1_8_R3.Entity entity) {
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
                bukkitEntity = new TNTPrimedNPC(this);
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        private int fuseRenewalDelay = 9;
        @Override
        public void t_() {
            if (npc != null) {
                if (fuseRenewalDelay-- <= 0) {
                    final NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    e(nbtTagCompound); // dump the entity NBT so let client update it as we don't have DataWatcher for fuse in this MC version
                    ((WorldServer) getWorld()).getTracker().a(this, new PacketPlayOutUpdateEntityNBT(getId(), nbtTagCompound));
                    fuseRenewalDelay = 9;
                }
                npc.update();
            } else {
                super.t_();
            }
        }
    }

    public static class TNTPrimedNPC extends CraftTNTPrimed implements NPCHolder {
        private final CitizensNPC npc;

        public TNTPrimedNPC(EntityTNTPrimedNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}