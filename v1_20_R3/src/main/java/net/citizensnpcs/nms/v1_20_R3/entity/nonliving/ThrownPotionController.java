package net.citizensnpcs.nms.v1_20_R3.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftThrownPotion;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_20_R3.entity.MobEntityController;
import net.citizensnpcs.nms.v1_20_R3.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_20_R3.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_20_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ThrownPotionController extends MobEntityController {
    public ThrownPotionController() {
        super(EntityThrownPotionNPC.class);
    }

    @Override
    public org.bukkit.entity.ThrownPotion getBukkitEntity() {
        return (org.bukkit.entity.ThrownPotion) super.getBukkitEntity();
    }

    public static class EntityThrownPotionNPC extends ThrownPotion implements NPCHolder {
        private final CitizensNPC npc;

        public EntityThrownPotionNPC(EntityType<? extends ThrownPotion> types, Level level) {
            this(types, level, null);
        }

        public EntityThrownPotionNPC(EntityType<? extends ThrownPotion> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                if (getItem() != null && getItem().getItem().equals(Items.LINGERING_POTION)) {
                    NMSImpl.setBukkitEntity(this, new LingeringThrownPotionNPC(this));
                } else {
                    NMSImpl.setBukkitEntity(this, new SplashThrownPotionNPC(this));
                }
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public PushReaction getPistonPushReaction() {
            return Util.callPistonPushEvent(npc) ? PushReaction.IGNORE : super.getPistonPushReaction();
        }

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        @Override
        protected AABB makeBoundingBox() {
            return NMSBoundingBox.makeBB(npc, super.makeBoundingBox());
        }

        @Override
        public void push(Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.push(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public Entity teleportTo(ServerLevel worldserver, Vec3 location) {
            if (npc == null)
                return super.teleportTo(worldserver, location);
            return NMSImpl.teleportAcrossWorld(this, worldserver, location);
        }

        @Override
        public void tick() {
            if (npc != null) {
                npc.update();
            } else {
                super.tick();
            }
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
            if (npc == null)
                return super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            Vec3 old = getDeltaMovement().add(0, 0, 0);
            boolean res = super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            if (!npc.isPushableByFluids()) {
                setDeltaMovement(old);
            }
            return res;
        }
    }

    public static class LingeringThrownPotionNPC extends CraftThrownPotion implements NPCHolder {
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

    public static class SplashThrownPotionNPC extends CraftThrownPotion implements ForwardingNPCHolder {
        public SplashThrownPotionNPC(EntityThrownPotionNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
