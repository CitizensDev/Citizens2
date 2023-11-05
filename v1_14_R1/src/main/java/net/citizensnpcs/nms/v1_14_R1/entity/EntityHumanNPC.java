package net.citizensnpcs.nms.v1_14_R1.entity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.nms.v1_14_R1.network.EmptyNetHandler;
import net.citizensnpcs.nms.v1_14_R1.network.EmptyNetworkManager;
import net.citizensnpcs.nms.v1_14_R1.util.EmptyAdvancementDataPlayer;
import net.citizensnpcs.nms.v1_14_R1.util.NMSImpl;
import net.citizensnpcs.nms.v1_14_R1.util.PlayerControllerJump;
import net.citizensnpcs.nms.v1_14_R1.util.PlayerControllerMove;
import net.citizensnpcs.nms.v1_14_R1.util.PlayerNavigation;
import net.citizensnpcs.nms.v1_14_R1.util.PlayerlistTracker;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinPacketTracker;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_14_R1.AttributeInstance;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumGamemode;
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.EnumProtocolDirection;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.NavigationAbstract;
import net.minecraft.server.v1_14_R1.NetworkManager;
import net.minecraft.server.v1_14_R1.Packet;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_14_R1.PathType;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.SoundEffect;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.WorldServer;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder, SkinnableEntity {
    private final Map<PathType, Float> bz = Maps.newEnumMap(PathType.class);
    private PlayerControllerJump controllerJump;
    private PlayerControllerMove controllerMove;
    private final Map<EnumItemSlot, ItemStack> equipmentCache = Maps.newEnumMap(EnumItemSlot.class);
    private int jumpTicks = 0;
    private PlayerNavigation navigation;
    private final CitizensNPC npc;
    private final Location packetLocationCache = new Location(null, 0, 0, 0);
    private PlayerlistTracker playerlistTracker;
    private final SkinPacketTracker skinTracker;

    public EntityHumanNPC(MinecraftServer minecraftServer, WorldServer world, GameProfile gameProfile,
            PlayerInteractManager playerInteractManager, NPC npc) {
        super(minecraftServer, world, gameProfile, playerInteractManager);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.3D);
        this.npc = (CitizensNPC) npc;
        if (npc != null) {
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

    public float a(PathType pathtype) {
        return this.bz.containsKey(pathtype) ? this.bz.get(pathtype) : pathtype.a();
    }

    public void a(PathType pathtype, float f) {
        this.bz.put(pathtype, f);
    }

    @Override
    public void b(float f, float f1) {
        if (npc == null || !npc.isFlyable()) {
            super.b(f, f1);
        }
    }

    @Override
    public void collide(net.minecraft.server.v1_14_R1.Entity entity) {
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
    public void e(Vec3D vec3d) {
        if (npc == null || !npc.isFlyable()) {
            super.e(vec3d);
        } else {
            NMSImpl.flyingMoveLogic(this, vec3d);
        }
    }

    @Override
    public void f(double x, double y, double z) {
        Vector vector = Util.callPushEvent(npc, x, y, z);
        if (vector != null) {
            super.f(vector.getX(), vector.getY(), vector.getZ());
        }
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
            NMSImpl.setBukkitEntity(this, new PlayerNPC(this));
        }
        return super.getBukkitEntity();
    }

    public PlayerControllerJump getControllerJump() {
        return controllerJump;
    }

    public PlayerControllerMove getControllerMove() {
        return controllerMove;
    }

    public NavigationAbstract getNavigation() {
        return navigation;
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
        AttributeInstance range = getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        if (range == null) {
            range = getAttributeMap().b(GenericAttributes.FOLLOW_RANGE);
        }
        range.setValue(Setting.DEFAULT_PATHFINDING_RANGE.asDouble());
        controllerJump = new PlayerControllerJump(this);
        controllerMove = new PlayerControllerMove(this);
        navigation = new PlayerNavigation(this, world);
        invulnerableTicks = 0;
        NMS.setStepHeight(getBukkitEntity(), 1); // the default (0) breaks step climbing
        setSkinFlags((byte) 0xFF);
        EmptyAdvancementDataPlayer.clear(this.getAdvancementData());
        NMSImpl.setAdvancement(this.getBukkitEntity(),
                new EmptyAdvancementDataPlayer(minecraftServer, CitizensAPI.getDataFolder().getParentFile(), this));
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

    public boolean isNavigating() {
        return npc.getNavigator().isNavigating();
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
        bb *= 0.98F;
        bd *= 0.98F;
        be *= 0.9F;
        moveWithFallDamage(new Vec3D(this.bb, this.bc, this.bd)); // movement method
        NMS.setHeadYaw(getBukkitEntity(), yaw);
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    private void moveWithFallDamage(Vec3D vec) {
        double y = this.locY;
        e(vec);
        if (!npc.isProtected()) {
            a(this.locY - y, onGround);
        }
    }

    @Override
    public void playerTick() {
        if (npc == null) {
            super.playerTick();
            return;
        }
        entityBaseTick();
        boolean navigating = npc.getNavigator().isNavigating() || controllerMove.b();
        if (!navigating && getBukkitEntity() != null
                && (!npc.hasTrait(Gravity.class) || npc.getOrAddTrait(Gravity.class).hasGravity())
                && Util.isLoaded(getBukkitEntity().getLocation(LOADED_LOCATION))
                && (!npc.isProtected() || SpigotUtil.checkYSafe(locY, getBukkitEntity().getWorld()))) {
            moveWithFallDamage(new Vec3D(0, 0, 0));
        }
        Vec3D mot = getMot();
        if (Math.abs(mot.getX()) < EPSILON && Math.abs(mot.getY()) < EPSILON && Math.abs(mot.getZ()) < EPSILON) {
            setMot(new Vec3D(0, 0, 0));
        }
        if (navigating) {
            if (!NMSImpl.isNavigationFinished(navigation)) {
                NMSImpl.updateNavigation(navigation);
            }
            moveOnCurrentHeading();
        }
        updateAI();
        collideNearby();
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
    }

    public void setMoveDestination(double x, double y, double z, double speed) {
        controllerMove.a(x, y, z, speed);
    }

    public void setShouldJump() {
        controllerJump.jump();
    }

    @Override
    public void setSkinFlags(byte flags) {
        // set skin flag byte
        getDataWatcher().set(EntityHuman.bt, flags);
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
        this.noclip = isSpectator();
        Bukkit.getServer().getPluginManager().unsubscribeFromPermission("bukkit.broadcast.user", getBukkitEntity());
        boolean navigating = npc.getNavigator().isNavigating();
        updatePackets(navigating);
        npc.update();
    }

    public void updateAI() {
        controllerMove.a();
        controllerJump.b();
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
                itemChanged = true;
                if (cache != null && !cache.isEmpty()) {
                    this.getAttributeMap().a(cache.a(slot));
                }
                if (equipment != null && !equipment.isEmpty()) {
                    this.getAttributeMap().b(equipment.a(slot));
                }
            }
            equipmentCache.put(slot, equipment);
        }
        if (!itemChanged)
            return;
        Location current = getBukkitEntity().getLocation(packetLocationCache);
        Packet<?>[] packets = new Packet[EnumItemSlot.values().length];
        int i = 0;
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            packets[i++] = new PacketPlayOutEntityEquipment(getId(), slot, getEquipment(slot));
        }
        NMSImpl.sendPacketsNearby(getBukkitEntity(), current, packets);
    }

    public void updatePathfindingRange(float pathfindingRange) {
        this.navigation.setRange(pathfindingRange);
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
        public void setGameMode(GameMode mode) {
            super.setGameMode(mode);
            getHandle().noclip = mode == GameMode.SPECTATOR;
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

    private static final float EPSILON = 0.005F;
    private static final Location LOADED_LOCATION = new Location(null, 0, 0, 0);
}
