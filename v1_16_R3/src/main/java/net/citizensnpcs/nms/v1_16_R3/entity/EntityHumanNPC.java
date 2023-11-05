package net.citizensnpcs.nms.v1_16_R3.entity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.nms.v1_16_R3.network.EmptyNetHandler;
import net.citizensnpcs.nms.v1_16_R3.network.EmptyNetworkManager;
import net.citizensnpcs.nms.v1_16_R3.util.EmptyAdvancementDataPlayer;
import net.citizensnpcs.nms.v1_16_R3.util.MobAI;
import net.citizensnpcs.nms.v1_16_R3.util.MobAI.ForwardingMobAI;
import net.citizensnpcs.nms.v1_16_R3.util.NMSImpl;
import net.citizensnpcs.nms.v1_16_R3.util.PlayerlistTracker;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinPacketTracker;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.EnumProtocolDirection;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.SoundEffect;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder, SkinnableEntity, ForwardingMobAI {
    private MobAI ai;
    private final Map<EnumItemSlot, ItemStack> equipmentCache = Maps.newEnumMap(EnumItemSlot.class);
    private int jumpTicks = 0;
    private final CitizensNPC npc;
    private PlayerlistTracker playerlistTracker;

    private final SkinPacketTracker skinTracker;

    public EntityHumanNPC(MinecraftServer minecraftServer, WorldServer world, GameProfile gameProfile,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, gameProfile, playerInteractManager);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.3D);
        this.npc = (CitizensNPC) npc;
        if (npc != null) {
            ai = new BasicMobAI(this);
            skinTracker = new SkinPacketTracker(this);
            playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);
            initialise(minecraftServer);
        } else {
            skinTracker = null;
        }
    }

    @Override
    protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
        if (npc == null || !npc.isFlyable()) {
            super.a(d0, flag, block, blockposition);
        }
    }

    @Override
    public boolean a(EntityPlayer entityplayer) {
        if (npc != null && playerlistTracker == null)
            return false;
        return super.a(entityplayer);
    }

    @Override
    public boolean b(float f, float f1) {
        if (npc == null || !npc.isFlyable())
            return super.b(f, f1);
        return false;
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
    public boolean damageEntity(DamageSource damagesource, float f) {
        // knock back velocity is cancelled and sent to client for handling when
        // the entity is a player. there is no client so make this happen
        // manually.
        boolean damaged = super.damageEntity(damagesource, f);
        if (damaged && velocityChanged) {
            velocityChanged = false;
            Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(),
                    (Runnable) () -> EntityHumanNPC.this.velocityChanged = true);
        }
        return damaged;
    }

    @Override
    public void die() {
        super.die();
        getAdvancementData().a();
    }

    @Override
    public void die(DamageSource damagesource) {
        // players that die are not normally removed from the world. when the
        // NPC dies, we are done with the instance and it should be removed.
        if (dead)
            return;

        super.die(damagesource);
        Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(),
                (Runnable) () -> ((WorldServer) world).removeEntity(EntityHumanNPC.this), 15); // give enough time for
                                                                                               // death and smoke
                                                                                               // animation
    }

    @Override
    public void g(Vec3D vec3d) {
        if (npc == null || !npc.isFlyable()) {
            super.g(vec3d);
        } else {
            NMSImpl.flyingMoveLogic(this, vec3d);
        }
    }

    @Override
    public MobAI getAI() {
        return ai;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
            NMSImpl.setBukkitEntity(this, new PlayerNPC(this));
        }
        return super.getBukkitEntity();
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    @Override
    public IChatBaseComponent getPlayerListName() {
        if (Setting.DISABLE_TABLIST.asBoolean())
            return new ChatComponentText("");
        return super.getPlayerListName();
    }

    @Override
    public String getSkinName() {
        String skinName = npc.getOrAddTrait(SkinTrait.class).getSkinName();
        if (skinName == null) {
            skinName = npc.getName();
        }
        return skinName.toLowerCase();
    }

    @Override
    public SkinPacketTracker getSkinTracker() {
        return skinTracker;
    }

    @Override
    protected SoundEffect getSoundDeath() {
        return NMSImpl.getSoundEffect(npc, super.getSoundDeath(), NPC.Metadata.DEATH_SOUND);
    }

    @Override
    protected SoundEffect getSoundHurt(DamageSource damagesource) {
        return NMSImpl.getSoundEffect(npc, super.getSoundHurt(damagesource), NPC.Metadata.HURT_SOUND);
    }

    @Override
    public void i(double x, double y, double z) {
        Vector vector = Util.callPushEvent(npc, x, y, z);
        if (vector != null) {
            super.i(vector.getX(), vector.getY(), vector.getZ());
        }
    }

    @Override
    public boolean inBlock() {
        if (npc == null || noclip || isSleeping())
            return super.inBlock();
        return Util.inBlock(getBukkitEntity());
    }

    private void initialise(MinecraftServer minecraftServer) {
        try {
            NetworkManager conn = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
            playerConnection = new EmptyNetHandler(minecraftServer, conn, this);
            conn.setPacketListener(playerConnection);
        } catch (IOException e) {
        }
        invulnerableTicks = 0;
        NMS.setStepHeight(getBukkitEntity(), 1); // the default (0) breaks step climbing
        setSkinFlags((byte) 0xFF);
        EmptyAdvancementDataPlayer.clear(this.getAdvancementData());
        NMSImpl.setAdvancement(this.getBukkitEntity(),
                new EmptyAdvancementDataPlayer(minecraftServer.getDataFixer(), minecraftServer.getPlayerList(),
                        minecraftServer.getAdvancementData(), CitizensAPI.getDataFolder().getParentFile(), this));
    }

    @Override
    public boolean isClimbing() {
        if (npc == null || !npc.isFlyable())
            return super.isClimbing();
        else
            return false;
    }

    @Override
    public boolean isCollidable() {
        return npc == null ? super.isCollidable()
                : npc.data().has(NPC.Metadata.COLLIDABLE) ? npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE)
                        : !npc.isProtected();
    }

    private void moveOnCurrentHeading() {
        if (jumping) {
            if (onGround && jumpTicks == 0) {
                jump();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        aR *= 0.98F;
        aT *= 0.98F;
        moveWithFallDamage(new Vec3D(this.aR, this.aS, this.aT)); // movement method
        NMS.setHeadYaw(getBukkitEntity(), yaw);
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    private void moveWithFallDamage(Vec3D vec) {
        double y = this.locY();
        g(vec);
        if (!npc.isProtected()) {
            a(this.locY() - y, onGround);
        }
    }

    @Override
    public Packet<?> P() {
        return super.P();
    }

    @Override
    public void playerTick() {
        if (npc == null) {
            super.playerTick();
            return;
        }
        entityBaseTick();
        boolean navigating = npc.getNavigator().isNavigating() || ai.getMoveControl().b();
        if (!navigating && getBukkitEntity() != null
                && (!npc.hasTrait(Gravity.class) || npc.getOrAddTrait(Gravity.class).hasGravity())
                && Util.isLoaded(getBukkitEntity().getLocation(LOADED_LOCATION))
                && (!npc.isProtected() || SpigotUtil.checkYSafe(locY(), getBukkitEntity().getWorld()))) {
            moveWithFallDamage(new Vec3D(0, 0, 0));
        }
        Vec3D mot = getMot();
        if (Math.abs(mot.getX()) < EPSILON && Math.abs(mot.getY()) < EPSILON && Math.abs(mot.getZ()) < EPSILON) {
            setMot(new Vec3D(0, 0, 0));
        }
        if (navigating) {
            if (!NMSImpl.isNavigationFinished(ai.getNavigation())) {
                NMSImpl.updateNavigation(ai.getNavigation());
            }
            moveOnCurrentHeading();
        }
        ai.getMoveControl().a();
        ai.getJumpControl().b();
        collideNearby();
    }

    @Override
    public void setSkinFlags(byte flags) {
        // set skin flag byte
        getDataWatcher().set(EntityHuman.bi, flags);
    }

    @Override
    public void setSkinName(String name) {
        npc.getOrAddTrait(SkinTrait.class).setSkinName(name);
    }

    @Override
    public void setSkinName(String name, boolean forceUpdate) {
        npc.getOrAddTrait(SkinTrait.class).setSkinName(name, forceUpdate);
    }

    @Override
    public void setSkinPersistent(String skinName, String signature, String data) {
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(skinName, signature, data);
    }

    public void setTracked(PlayerlistTracker tracker) {
        this.playerlistTracker = tracker;
    }

    @Override
    public void tick() {
        super.tick();
        if (npc == null)
            return;
        noclip = isSpectator();
        Bukkit.getServer().getPluginManager().unsubscribeFromPermission("bukkit.broadcast.user", getBukkitEntity());
        boolean navigating = npc.getNavigator().isNavigating();
        updatePackets(navigating);
        npc.update();
        if (npc.data().get(NPC.Metadata.PICKUP_ITEMS, false)) {
            AxisAlignedBB axisalignedbb;
            if (this.isPassenger() && !this.getVehicle().dead) {
                axisalignedbb = this.getBoundingBox().b(this.getVehicle().getBoundingBox()).grow(1.0, 0.0, 1.0);
            } else {
                axisalignedbb = this.getBoundingBox().grow(1.0, 0.5, 1.0);
            }
            for (Entity entity : this.world.getEntities(this, axisalignedbb)) {
                if (!entity.dead) {
                    entity.pickup(this);
                }
            }
        }
        /*
         double diff = this.yaw - this.aK;
         if (diff != 40 && diff != -40) {
         ++this.yawUpdateRequiredTicks;
         }
         if (this.yawUpdateRequiredTicks > 5) {
         this.yaw = (diff > -40 && diff < 0) || (diff > 0 && diff > 40) ? this.aK - 40 : this.aK + 40;
         this.yawUpdateRequiredTicks = 0;
         }
         */
    }

    private void updatePackets(boolean navigating) {
        if (!npc.isUpdating(NPCUpdate.PACKET))
            return;

        updateEffects = true;

        boolean itemChanged = false;
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            ItemStack equipment = getEquipment(slot);
            ItemStack cache = equipmentCache.get(slot);
            if (((cache != null) || (equipment != null))
                    && (cache == null ^ equipment == null || !ItemStack.equals(cache, equipment))) {
                if (cache != null && !cache.isEmpty()) {
                    this.getAttributeMap().a(cache.a(slot));
                }
                if (equipment != null && !equipment.isEmpty()) {
                    this.getAttributeMap().b(equipment.a(slot));
                }
                itemChanged = true;
            }
            equipmentCache.put(slot, equipment);
        }
        if (!itemChanged)
            return;
        Location current = getBukkitEntity().getLocation();
        List<Pair<EnumItemSlot, ItemStack>> list = Lists.newArrayList();
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            list.add(Pair.of(slot, getEquipment(slot)));
        }
        NMSImpl.sendPacketsNearby(getBukkitEntity(), current, new PacketPlayOutEntityEquipment(getId(), list));
    }

    public static class PlayerNPC extends CraftPlayer implements NPCHolder, SkinnableEntity {
        private final CraftServer cserver;
        private final CitizensNPC npc;

        private PlayerNPC(EntityHumanNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
            this.cserver = (CraftServer) Bukkit.getServer();
            npc.getOrAddTrait(Inventory.class);
        }

        @Override
        public Player getBukkitEntity() {
            return this;
        }

        @Override
        public EntityHumanNPC getHandle() {
            return (EntityHumanNPC) this.entity;
        }

        @Override
        public List<MetadataValue> getMetadata(String metadataKey) {
            return cserver.getEntityMetadata().getMetadata(this, metadataKey);
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public String getSkinName() {
            return ((SkinnableEntity) this.entity).getSkinName();
        }

        @Override
        public SkinPacketTracker getSkinTracker() {
            return ((SkinnableEntity) this.entity).getSkinTracker();
        }

        @Override
        public boolean hasMetadata(String metadataKey) {
            return cserver.getEntityMetadata().hasMetadata(this, metadataKey);
        }

        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {
            cserver.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
        }

        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
            cserver.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
        }

        @Override
        public void setSkinFlags(byte flags) {
            ((SkinnableEntity) this.entity).setSkinFlags(flags);
        }

        @Override
        public void setSkinName(String name) {
            ((SkinnableEntity) this.entity).setSkinName(name);
        }

        @Override
        public void setSkinName(String skinName, boolean forceUpdate) {
            ((SkinnableEntity) this.entity).setSkinName(skinName, forceUpdate);
        }

        @Override
        public void setSkinPersistent(String skinName, String signature, String data) {
            ((SkinnableEntity) this.entity).setSkinPersistent(skinName, signature, data);
        }
    }

    private static final float EPSILON = 0.003F;
    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}
