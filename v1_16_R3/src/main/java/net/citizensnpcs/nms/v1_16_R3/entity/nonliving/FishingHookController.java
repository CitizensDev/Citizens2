package net.citizensnpcs.nms.v1_16_R3.entity.nonliving;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftFishHook;
import org.bukkit.entity.FishHook;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R3.entity.MobEntityController;
import net.citizensnpcs.nms.v1_16_R3.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_16_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityFishingHook;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.Items;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;

public class FishingHookController extends MobEntityController {
    public FishingHookController() {
        super(EntityFishingHookNPC.class);
    }

    @Override
    public FishHook getBukkitEntity() {
        return (FishHook) super.getBukkitEntity();
    }

    public static class EntityFishingHookNPC extends EntityFishingHook implements NPCHolder {
        private final CitizensNPC npc;

        public EntityFishingHookNPC(EntityTypes<? extends EntityFishingHook> types, World world) {
            this(types, world, null);
        }

        public EntityFishingHookNPC(EntityTypes<? extends EntityFishingHook> types, World world, NPC npc) {
            super(new EntityPlayer(world.getServer().getServer(), (WorldServer) world,
                    new GameProfile(UUID.randomUUID(), "dummyfishhook"),
                    new PlayerInteractManager((WorldServer) world)) {
            }, world, 0, 0);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void collide(net.minecraft.server.v1_16_R3.Entity entity) {
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
                NMSImpl.setBukkitEntity(this, new FishingHookNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public double h(Entity entity) {
            // distance check in k()
            if (entity == getShooter()) {
                return 0D;
            }
            return super.h(entity);
        }

        @Override
        public void i(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.i(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void tick() {
            if (npc != null) {
                ((EntityHuman) getShooter()).setHealth(20F);
                getShooter().dead = false;
                ((EntityHuman) getShooter()).inventory.items.set(((EntityHuman) getShooter()).inventory.itemInHandIndex,
                        new ItemStack(Items.FISHING_ROD, 1));
                try {
                    D.invoke(this, 0);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                npc.update();
            } else {
                super.tick();
            }
        }

        private static MethodHandle D = NMS.getSetter(EntityFishingHook.class, "d");
    }

    public static class FishingHookNPC extends CraftFishHook implements ForwardingNPCHolder {
        public FishingHookNPC(EntityFishingHookNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
