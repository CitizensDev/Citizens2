package net.citizensnpcs.nms.v1_15_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPufferFish;
import org.bukkit.entity.PufferFish;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_15_R1.util.NMSImpl;
import net.citizensnpcs.nms.v1_15_R1.util.PlayerControllerMove;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.versioned.PufferFishTrait;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.ControllerMove;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityBoat;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_15_R1.EntityPose;
import net.minecraft.server.v1_15_R1.EntityPufferFish;
import net.minecraft.server.v1_15_R1.EntitySize;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumHand;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.Items;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.SoundEffect;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;

public class PufferFishController extends MobEntityController {
    public PufferFishController() {
        super(EntityPufferFishNPC.class);
    }

    @Override
    public PufferFish getBukkitEntity() {
        return (PufferFish) super.getBukkitEntity();
    }

    public static class EntityPufferFishNPC extends EntityPufferFish implements NPCHolder {
        private final CitizensNPC npc;
        private ControllerMove oldMoveController;

        public EntityPufferFishNPC(EntityTypes<? extends EntityPufferFish> types, World world) {
            this(types, world, null);
        }

        public EntityPufferFishNPC(EntityTypes<? extends EntityPufferFish> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMSImpl.clearGoals(npc, goalSelector, targetSelector);
                this.oldMoveController = this.moveController;
                this.moveController = new ControllerMove(this);
            }
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
            if (npc == null || !npc.isProtected())
                return super.a(entityhuman, enumhand);
            ItemStack itemstack = entityhuman.b(enumhand);
            if (itemstack.getItem() == Items.WATER_BUCKET && isAlive()) {
                return false;
            }
            return super.a(entityhuman, enumhand);
        }

        @Override
        public EntitySize a(EntityPose entitypose) {
            if (npc == null) {
                return super.a(entitypose);
            }
            return super.a(entitypose).a(1 / s(getPuffState())).a(0.5F);
        }

        @Override
        public boolean b(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                return super.b(f, f1);
            }
            return false;
        }

        @Override
        public void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
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
        public boolean doAITick() {
            return false;
        }

        @Override
        public void e(Vec3D vec3d) {
            if (npc == null || !npc.isFlyable()) {
                super.e(vec3d);
            } else {
                NMSImpl.flyingMoveLogic(this, vec3d);
            }
        }

        @Override
        public void enderTeleportTo(double d0, double d1, double d2) {
            if (npc == null) {
                super.enderTeleportTo(d0, d1, d2);
                return;
            }
            NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                super.enderTeleportTo(d0, d1, d2);
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new PufferFishNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        protected SoundEffect getSoundAmbient() {
            return NMSImpl.getSoundEffect(npc, super.getSoundAmbient(), NPC.AMBIENT_SOUND_METADATA);
        }

        @Override
        protected SoundEffect getSoundDeath() {
            return NMSImpl.getSoundEffect(npc, super.getSoundDeath(), NPC.DEATH_SOUND_METADATA);
        }

        @Override
        protected SoundEffect getSoundHurt(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.getSoundHurt(damagesource), NPC.HURT_SOUND_METADATA);
        }

        @Override
        public void h(double x, double y, double z) {
            if (npc == null) {
                super.h(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.h(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.h(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public boolean isClimbing() {
            if (npc == null || !npc.isFlyable()) {
                return super.isClimbing();
            } else {
                return false;
            }
        }

        @Override
        public boolean isLeashed() {
            if (npc == null)
                return super.isLeashed();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.isLeashed();
            if (super.isLeashed()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void mobTick() {
            super.mobTick();
            if (npc != null) {
                NMSImpl.updateMinecraftAIState(npc, this);
                if (npc.useMinecraftAI() && this.moveController != this.oldMoveController) {
                    this.moveController = this.oldMoveController;
                }
                if (!npc.useMinecraftAI() && this.moveController == this.oldMoveController) {
                    this.moveController = new PlayerControllerMove(this);
                }
                npc.update();
            }
        }

        @Override
        public void movementTick() {
            boolean lastInWater = this.v;
            int lastPuffState = getPuffState();
            if (npc != null) {
                this.v = false;
                setPuffState(0);
            }
            super.movementTick();
            if (npc != null) {
                this.v = lastInWater;
                setPuffState(lastPuffState);
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract)) {
                return !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            }
            return super.n(entity);
        }

        @Override
        public void tick() {
            if (npc != null) {
                NMSImpl.resetPuffTicks(this);
            }
            super.tick();
            if (npc != null && npc.hasTrait(PufferFishTrait.class)) {
                setPuffState(npc.getOrAddTrait(PufferFishTrait.class).getPuffState());
            }
        }

        private static float s(int i) {
            switch (i) {
                case 0:
                    return 0.5F;
                case 1:
                    return 0.7F;
                default:
                    return 1.0F;
            }
        }
    }

    public static class PufferFishNPC extends CraftPufferFish implements NPCHolder {
        private final CitizensNPC npc;

        public PufferFishNPC(EntityPufferFishNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}
