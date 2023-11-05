package net.citizensnpcs.nms.v1_20_R2.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_20_R2.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_20_R2.util.MobAI;
import net.citizensnpcs.nms.v1_20_R2.util.MobAI.ForwardingMobAI;
import net.citizensnpcs.nms.v1_20_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_20_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmorStandController extends MobEntityController {
    public ArmorStandController() {
        super(EntityArmorStandNPC.class);
    }

    @Override
    public org.bukkit.entity.ArmorStand getBukkitEntity() {
        return (org.bukkit.entity.ArmorStand) super.getBukkitEntity();
    }

    public static class ArmorStandNPC extends CraftArmorStand implements ForwardingNPCHolder {
        public ArmorStandNPC(EntityArmorStandNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityArmorStandNPC extends ArmorStand implements NPCHolder, ForwardingMobAI {
        private MobAI ai;
        private final CitizensNPC npc;

        public EntityArmorStandNPC(EntityType<? extends ArmorStand> types, Level level) {
            this(types, level, null);
        }

        public EntityArmorStandNPC(EntityType<? extends ArmorStand> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                ai = new BasicMobAI(this);
            }
        }

        @Override
        public MobAI getAI() {
            return ai;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new ArmorStandNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
            if (npc == null)
                return super.interactAt(entityhuman, vec3d, enumhand);
            PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
                    (org.bukkit.entity.Player) entityhuman.getBukkitEntity(), getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            return event.isCancelled() ? InteractionResult.FAIL : InteractionResult.SUCCESS;
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
        public void push(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.push(vector.getX(), vector.getY(), vector.getZ());
            }
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
            super.tick();
            if (npc != null) {
                npc.update();
                ai.tickAI();
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
}
