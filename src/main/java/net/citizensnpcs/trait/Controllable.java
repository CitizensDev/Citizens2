package net.citizensnpcs.trait;

import java.lang.reflect.Constructor;
import java.util.Map;

import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_5_R3.EntityEnderDragon;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntityPlayer;

import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.google.common.collect.Maps;

//TODO: reduce reliance on CitizensNPC
public class Controllable extends Trait implements Toggleable, CommandConfigurable {
    private MovementController controller = new GroundController();
    @Persist
    private boolean enabled = true;
    private EntityType explicitType;

    public Controllable() {
        super("controllable");
    }

    public Controllable(boolean enabled) {
        this();
        this.enabled = enabled;
    }

    @Override
    public void configure(CommandContext args) {
        if (args.hasFlag('f'))
            explicitType = EntityType.BLAZE;
        else if (args.hasFlag('g'))
            explicitType = EntityType.OCELOT;
        else if (args.hasFlag('r'))
            explicitType = null;
        else if (args.hasValueFlag("explicittype"))
            explicitType = Util.matchEntityType(args.getFlag("explicittype"));
        if (npc.isSpawned())
            loadController();
    }

    private void enterOrLeaveVehicle(Player player) {
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        if (getHandle().passenger != null) {
            if (getHandle().passenger == handle)
                player.leaveVehicle();
            return;
        }
        if (npc.getTrait(Owner.class).isOwnedBy(handle.getBukkitEntity()))
            handle.setPassengerOf(getHandle());
    }

    private EntityLiving getHandle() {
        return ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("explicittype"))
            explicitType = Util.matchEntityType(key.getString("explicittype"));
    }

    private void loadController() {
        EntityType type = npc.getBukkitEntity().getType();
        if (explicitType != null)
            type = explicitType;
        Class<? extends MovementController> clazz = controllerTypes.get(type);
        if (clazz == null) {
            controller = new GroundController();
            return;
        }
        Constructor<? extends MovementController> innerConstructor = null;
        try {
            innerConstructor = clazz.getConstructor(Controllable.class);
            innerConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (innerConstructor == null) {
                controller = clazz.newInstance();
            } else
                controller = innerConstructor.newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            controller = new GroundController();
        }
    }

    public boolean mount(Player toMount) {
        Entity passenger = npc.getBukkitEntity().getPassenger();
        if (passenger != null && passenger != toMount)
            return false;
        enterOrLeaveVehicle(toMount);
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!npc.isSpawned() || !enabled || !event.getPlayer().isSneaking())
            return;
        EntityPlayer handle = ((CraftPlayer) event.getPlayer()).getHandle();
        Action performed = event.getAction();
        if (!handle.equals(getHandle().passenger))
            return;
        switch (performed) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                controller.rightClick(event);
                break;
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                controller.leftClick(event);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!enabled || !npc.isSpawned() || !event.getNPC().equals(npc))
            return;
        controller.rightClickEntity(event);
    }

    @Override
    public void onSpawn() {
        loadController();
    }

    @Override
    public void run() {
        if (!enabled || !npc.isSpawned() || getHandle().passenger == null)
            return;
        controller.run((Player) getHandle().passenger.getBukkitEntity());
    }

    @Override
    public void save(DataKey key) {
        if (explicitType == null) {
            key.removeKey("explicittype");
        } else
            key.setString("explicittype", explicitType.name());
    }

    private void setMountedYaw(EntityLiving handle) {
        if (handle instanceof EntityEnderDragon)
            return; // EnderDragon handles this separately
        double tX = handle.locX + handle.motX;
        double tZ = handle.locZ + handle.motZ;
        if (handle.locZ > tZ) {
            handle.yaw = (float) -Math.toDegrees(Math.atan((handle.locX - tX) / (handle.locZ - tZ))) + 180F;
        } else if (handle.locZ < tZ) {
            handle.yaw = (float) -Math.toDegrees(Math.atan((handle.locX - tX) / (handle.locZ - tZ)));
        }
        NMS.setHeadYaw(handle, handle.yaw);
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        if (!enabled && getHandle().passenger != null)
            getHandle().passenger.getBukkitEntity().leaveVehicle();
        return enabled;
    }

    public class AirController implements MovementController {
        boolean paused = false;

        @Override
        public void leftClick(PlayerInteractEvent event) {
            paused = !paused;
        }

        @Override
        public void rightClick(PlayerInteractEvent event) {
            paused = !paused;
        }

        @Override
        public void rightClickEntity(NPCRightClickEvent event) {
            enterOrLeaveVehicle(event.getClicker());
        }

        @Override
        public void run(Player rider) {
            if (paused) {
                getHandle().motY = 0.001;
                return;
            }
            Vector dir = rider.getEyeLocation().getDirection();
            dir.multiply(npc.getNavigator().getDefaultParameters().speedModifier());
            EntityLiving handle = getHandle();
            handle.motX = dir.getX();
            handle.motY = dir.getY();
            handle.motZ = dir.getZ();
            setMountedYaw(handle);
        }
    }

    public class GroundController implements MovementController {
        private void jump() {
            boolean allowed = getHandle().onGround;
            if (!allowed)
                return;
            getHandle().motY = JUMP_VELOCITY;
        }

        @Override
        public void leftClick(PlayerInteractEvent event) {
            jump();
        }

        @Override
        public void rightClick(PlayerInteractEvent event) {
        }

        @Override
        public void rightClickEntity(NPCRightClickEvent event) {
            enterOrLeaveVehicle(event.getClicker());
        }

        @Override
        public void run(Player rider) {
            EntityLiving handle = getHandle();
            boolean onGround = handle.onGround;
            float speedMod = npc.getNavigator().getDefaultParameters()
                    .modifiedSpeed((onGround ? GROUND_SPEED : AIR_SPEED));
            handle.motX += handle.passenger.motX * speedMod;
            handle.motZ += handle.passenger.motZ * speedMod;
            setMountedYaw(handle);
        }

        private static final float AIR_SPEED = 1.5F;
        private static final float GROUND_SPEED = 4F;
        private static final float JUMP_VELOCITY = 0.6F;
    }

    public static interface MovementController {
        void leftClick(PlayerInteractEvent event);

        void rightClick(PlayerInteractEvent event);

        void rightClickEntity(NPCRightClickEvent event);

        void run(Player rider);
    }

    private static final Map<EntityType, Class<? extends MovementController>> controllerTypes = Maps
            .newEnumMap(EntityType.class);

    static {
        controllerTypes.put(EntityType.BAT, AirController.class);
        controllerTypes.put(EntityType.BLAZE, AirController.class);
        controllerTypes.put(EntityType.ENDER_DRAGON, AirController.class);
        controllerTypes.put(EntityType.GHAST, AirController.class);
        controllerTypes.put(EntityType.WITHER, AirController.class);
    }
}
