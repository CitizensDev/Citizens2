package net.citizensnpcs.npc.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftBat;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Bat;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_9_R2.EntityBat;
import net.minecraft.server.v1_9_R2.MinecraftKey;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.SoundEffect;
import net.minecraft.server.v1_9_R2.World;

public class BatController extends MobEntityController {
    public BatController() {
        super(EntityBatNPC.class);
    }

    @Override
    public Bat getBukkitEntity() {
        return (Bat) super.getBukkitEntity();
    }

    public static class BatNPC extends CraftBat implements NPCHolder {
        private final CitizensNPC npc;

        public BatNPC(EntityBatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityBatNPC extends EntityBat implements NPCHolder {
        private final CitizensNPC npc;

        public EntityBatNPC(World world) {
            this(world, null);
        }

        public EntityBatNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
                setFlying(false);
            }
        }

        @Override
        protected SoundEffect bS() {
            return npc == null || !npc.data().has(NPC.HURT_SOUND_METADATA) ? super.bS()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.HURT_SOUND_METADATA, SoundEffect.a.b(super.bS()).toString())));
        }

        @Override
        protected SoundEffect bT() {
            return npc == null || !npc.data().has(NPC.DEATH_SOUND_METADATA) ? super.bT()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.DEATH_SOUND_METADATA, SoundEffect.a.b(super.bT()).toString())));
        }

        @Override
        public void collide(net.minecraft.server.v1_9_R2.Entity entity) {
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
        public void enderTeleportTo(double d0, double d1, double d2) {
            if (npc == null)
                super.enderTeleportTo(d0, d1, d2);
            NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                super.enderTeleportTo(d0, d1, d2);
            }
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
        protected SoundEffect G() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.G()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.AMBIENT_SOUND_METADATA, SoundEffect.a.b(super.G()).toString())));
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new BatNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isLeashed() {
            if (npc == null) {
                return super.isLeashed();
            }
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.isLeashed();
            if (super.isLeashed()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        protected void L() {
            if (npc == null) {
                super.L();
            }
        }

        @Override
        public void M() {
            if (npc == null) {
                super.M();
            } else {
                NMS.updateAI(this);
                npc.update();
            }
        }

        public void setFlying(boolean flying) {
            setAsleep(flying);
        }
    }
}