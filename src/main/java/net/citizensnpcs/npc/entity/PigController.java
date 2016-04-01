package net.citizensnpcs.npc.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPig;
import org.bukkit.craftbukkit.v1_9_R1.event.CraftEventFactory;
import org.bukkit.entity.Pig;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.EnchantmentManager;
import net.minecraft.server.v1_9_R1.EntityLightning;
import net.minecraft.server.v1_9_R1.EntityPig;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import net.minecraft.server.v1_9_R1.MobEffects;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.SoundEffect;
import net.minecraft.server.v1_9_R1.Vec3D;
import net.minecraft.server.v1_9_R1.World;

public class PigController extends MobEntityController {
    public PigController() {
        super(EntityPigNPC.class);
    }

    @Override
    public Pig getBukkitEntity() {
        return (Pig) super.getBukkitEntity();
    }

    public static class EntityPigNPC extends EntityPig implements NPCHolder {
        private final CitizensNPC npc;

        public EntityPigNPC(World world) {
            this(world, null);
        }

        public EntityPigNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        public void _g(float f, float f1) {
            if ((co()) || (bx())) {
                if ((isInWater())) {
                    double d1 = this.locY;
                    float f4 = 0.8F;
                    float f3 = 0.02F;
                    float f2 = EnchantmentManager.d(this);
                    if (f2 > 3.0F) {
                        f2 = 3.0F;
                    }
                    if (!this.onGround) {
                        f2 *= 0.5F;
                    }
                    if (f2 > 0.0F) {
                        f4 += (0.54600006F - f4) * f2 / 3.0F;
                        f3 += (ck() - f3) * f2 / 3.0F;
                    }
                    a(f, f1, f3);
                    move(this.motX, this.motY, this.motZ);
                    this.motX *= f4;
                    this.motY *= 0.800000011920929D;
                    this.motZ *= f4;
                    this.motY -= 0.02D;
                    if ((this.positionChanged)
                            && (c(this.motX, this.motY + 0.6000000238418579D - this.locY + d1, this.motZ))) {
                        this.motY = 0.30000001192092896D;
                    }
                } else if ((an())) {
                    double d1 = this.locY;
                    a(f, f1, 0.02F);
                    move(this.motX, this.motY, this.motZ);
                    this.motX *= 0.5D;
                    this.motY *= 0.5D;
                    this.motZ *= 0.5D;
                    this.motY -= 0.02D;
                    if ((this.positionChanged)
                            && (c(this.motX, this.motY + 0.6000000238418579D - this.locY + d1, this.motZ))) {
                        this.motY = 0.30000001192092896D;
                    }
                } else if (cB()) {
                    if (this.motY > -0.5D) {
                        this.fallDistance = 1.0F;
                    }
                    Vec3D vec3d = aB();
                    float f5 = this.pitch * 0.017453292F;

                    double d0 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                    double d2 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
                    double d3 = vec3d.b();
                    float f6 = MathHelper.cos(f5);

                    f6 = (float) (f6 * f6 * Math.min(1.0D, d3 / 0.4D));
                    this.motY += -0.08D + f6 * 0.06D;
                    if ((this.motY < 0.0D) && (d0 > 0.0D)) {
                        double d4 = this.motY * -0.1D * f6;
                        this.motY += d4;
                        this.motX += vec3d.x * d4 / d0;
                        this.motZ += vec3d.z * d4 / d0;
                    }
                    if (f5 < 0.0F) {
                        double d4 = d2 * -MathHelper.sin(f5) * 0.04D;
                        this.motY += d4 * 3.2D;
                        this.motX -= vec3d.x * d4 / d0;
                        this.motZ -= vec3d.z * d4 / d0;
                    }
                    if (d0 > 0.0D) {
                        this.motX += (vec3d.x / d0 * d2 - this.motX) * 0.1D;
                        this.motZ += (vec3d.z / d0 * d2 - this.motZ) * 0.1D;
                    }
                    this.motX *= 0.9900000095367432D;
                    this.motY *= 0.9800000190734863D;
                    this.motZ *= 0.9900000095367432D;
                    move(this.motX, this.motY, this.motZ);
                    if ((this.positionChanged) && (!this.world.isClientSide)) {
                        double d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
                        double d5 = d2 - d4;
                        float f7 = (float) (d5 * 10.0D - 3.0D);
                        if (f7 > 0.0F) {
                            a(e((int) f7), 1.0F, 1.0F);
                            damageEntity(DamageSource.j, f7);
                        }
                    }
                    if ((this.onGround) && (!this.world.isClientSide) && (getFlag(7))
                            && (!CraftEventFactory.callToggleGlideEvent(this, false).isCancelled())) {
                        setFlag(7, false);
                    }
                } else {
                    float f8 = 0.91F;
                    BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                            .c(this.locX, getBoundingBox().b - 1.0D, this.locZ);
                    if (this.onGround) {
                        f8 = this.world.getType(blockposition_pooledblockposition).getBlock().frictionFactor * 0.91F;
                    }
                    float f4 = 0.16277136F / (f8 * f8 * f8);
                    float f3;
                    if (this.onGround) {
                        f3 = ck() * f4;
                    } else {
                        f3 = this.aQ;
                    }
                    a(f, f1, f3);
                    f8 = 0.91F;
                    if (this.onGround) {
                        f8 = this.world.getType(
                                blockposition_pooledblockposition.d(this.locX, getBoundingBox().b - 1.0D, this.locZ))
                                .getBlock().frictionFactor * 0.91F;
                    }
                    if (n_()) {
                        float f2 = 0.15F;
                        this.motX = MathHelper.a(this.motX, -f2, f2);
                        this.motZ = MathHelper.a(this.motZ, -f2, f2);
                        this.fallDistance = 0.0F;
                        if (this.motY < -0.15D) {
                            this.motY = -0.15D;
                        }
                        boolean flag = (isSneaking());
                        if ((flag) && (this.motY < 0.0D)) {
                            this.motY = 0.0D;
                        }
                    }
                    move(this.motX, this.motY, this.motZ);
                    if ((this.positionChanged) && (n_())) {
                        this.motY = 0.2D;
                    }
                    if (hasEffect(MobEffects.LEVITATION)) {
                        this.motY += (0.05D * (getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - this.motY) * 0.2D;
                    } else {
                        blockposition_pooledblockposition.d(this.locX, 0.0D, this.locZ);
                        if ((this.world.isClientSide) && ((!this.world.isLoaded(blockposition_pooledblockposition))
                                || (!this.world.getChunkAtWorldCoords(blockposition_pooledblockposition).p()))) {
                            if (this.locY > 0.0D) {
                                this.motY = -0.1D;
                            } else {
                                this.motY = 0.0D;
                            }
                        } else {
                            this.motY -= 0.08D;
                        }
                    }
                    this.motY *= 0.9800000190734863D;
                    this.motX *= f8;
                    this.motZ *= f8;
                    blockposition_pooledblockposition.t();
                }
            }
            this.aE = this.aF;
            double d1 = this.locX - this.lastX;
            double d0 = this.locZ - this.lastZ;
            float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;
            if (f2 > 1.0F) {
                f2 = 1.0F;
            }
            this.aF += (f2 - this.aF) * 0.4F;
            this.aG += this.aF;
        }

        @Override
        public void a(boolean flag) {
            float oldw = width;
            float oldl = length;
            super.a(flag);
            if (oldw != width || oldl != length) {
                this.setPosition(locX - 0.01, locY, locZ - 0.01);
                this.setPosition(locX + 0.01, locY, locZ + 0.01);
            }
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        protected SoundEffect bR() {
            return npc == null || !npc.data().has(NPC.HURT_SOUND_METADATA) ? super.bR()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.HURT_SOUND_METADATA, SoundEffect.a.b(super.bR()).toString())));
        }

        @Override
        protected SoundEffect bS() {
            return npc == null || !npc.data().has(NPC.DEATH_SOUND_METADATA) ? super.bS()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.DEATH_SOUND_METADATA, SoundEffect.a.b(super.bR()).toString())));
        }

        @Override
        public void collide(net.minecraft.server.v1_9_R1.Entity entity) {
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
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            }
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
        public void g(float f, float f1) {
            if (npc == null) {
                super.g(f, f1);
            } else if (!npc.isFlyable()) {
                _g(f, f1);
            } else {
                NMS.flyingMoveLogic(this, f, f1);
            }
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
                bukkitEntity = new PigNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
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
        protected void L() {
            if (npc == null) {
                super.L();
            }
        }

        @Override
        public void M() {
            super.M();
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        public boolean n_() {
            if (npc == null || !npc.isFlyable()) {
                return super.n_();
            } else {
                return false;
            }
        }

        @Override
        public void onLightningStrike(EntityLightning entitylightning) {
            if (npc == null) {
                super.onLightningStrike(entitylightning);
            }
        }
    }

    public static class PigNPC extends CraftPig implements NPCHolder {
        private final CitizensNPC npc;

        public PigNPC(EntityPigNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}