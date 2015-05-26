package net.citizensnpcs.trait;

import java.lang.reflect.Constructor;
import java.util.Map;

import net.citizensnpcs.Settings.Setting;
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
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    @Persist("owner_required")
    private boolean ownerRequired;

    public Controllable() {
        super("controllable");
    }

    public Controllable(boolean enabled) {
        this();
        this.enabled = enabled;
    }

    @Override
    public void configure(CommandContext args) {
        if (args.hasFlag('f')) {
            explicitType = EntityType.BLAZE;
        } else if (args.hasFlag('g')) {
            explicitType = EntityType.OCELOT;
        } else if (args.hasFlag('o')) {
            explicitType = EntityType.UNKNOWN;
        } else if (args.hasFlag('r')) {
            explicitType = null;
        } else if (args.hasValueFlag("explicittype"))
            explicitType = Util.matchEntityType(args.getFlag("explicittype"));
        if (npc.isSpawned()) {
            loadController();
        }
    }

    private void enterOrLeaveVehicle(Player player) {
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        if (getHandle().passenger != null) {
            if (getHandle().passenger == handle) {
                player.leaveVehicle();
            }
            return;
        }
        if (ownerRequired && !npc.getTrait(Owner.class).isOwnedBy(handle.getBukkitEntity())) {
            return;
        }
        handle.mount(getHandle());
    }

    private net.minecraft.server.v1_8_R3.Entity getHandle() {
        return NMS.getHandle(npc.getEntity());
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
        EntityType type = npc.getEntity().getType();
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
        Entity passenger = npc.getEntity().getPassenger();
        if (passenger != null && passenger != toMount) {
            return false;
        }
        enterOrLeaveVehicle(toMount);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!npc.isSpawned() || !enabled)
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
        if (!enabled || !npc.isSpawned() || getHandle().passenger == null
                || !(getHandle().passenger.getBukkitEntity() instanceof Player))
            return;
        controller.run((Player) getHandle().passenger.getBukkitEntity());
    }

    @Override
    public void save(DataKey key) {
        if (explicitType == null) {
            key.removeKey("explicittype");
        } else {
            key.setString("explicittype", explicitType.name());
        }
    }

    public boolean setEnabled(boolean enabled) {
        this.enabled = enabled;
        return enabled;
    }

    private void setMountedYaw(net.minecraft.server.v1_8_R3.Entity handle) {
        if (handle instanceof EntityEnderDragon || !Setting.USE_BOAT_CONTROLS.asBoolean())
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

    public void setOwnerRequired(boolean ownerRequired) {
        this.ownerRequired = ownerRequired;
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        if (!enabled && getHandle().passenger != null) {
            getHandle().passenger.getBukkitEntity().leaveVehicle();
        }
        return enabled;
    }

    private double updateHorizontalSpeed(net.minecraft.server.v1_8_R3.Entity handle,
            net.minecraft.server.v1_8_R3.Entity passenger, double speed, float speedMod) {
        double oldSpeed = Math.sqrt(handle.motX * handle.motX + handle.motZ * handle.motZ);
        double horizontal = ((EntityLiving) passenger).ba;
        if (horizontal > 0.0D) {
            double dXcos = -Math.sin(passenger.yaw * Math.PI / 180.0F);
            double dXsin = Math.cos(passenger.yaw * Math.PI / 180.0F);
            handle.motX += dXcos * speed * 0.5;
            handle.motZ += dXsin * speed * 0.5;
        }
        handle.motX += passenger.motX * speedMod;
        handle.motZ += passenger.motZ * speedMod;

        double newSpeed = Math.sqrt(handle.motX * handle.motX + handle.motZ * handle.motZ);
        if (newSpeed > 0.35D) {
            double movementFactor = 0.35D / newSpeed;
            handle.motX *= movementFactor;
            handle.motZ *= movementFactor;
            newSpeed = 0.35D;
        }

        if (newSpeed > oldSpeed && speed < 0.35D) {
            return (float) Math.min(0.35D, (speed + ((0.35D - speed) / 35.0D)));
        } else {
            return (float) Math.max(0.07D, (speed - ((speed - 0.07D) / 35.0D)));
        }
    }

    public class GroundController implements MovementController {
        private int jumpTicks = 0;
        private double speed = 0.07D;

        @Override
        public void leftClick(PlayerInteractEvent event) {
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
            net.minecraft.server.v1_8_R3.Entity handle = getHandle();
            net.minecraft.server.v1_8_R3.Entity passenger = ((CraftPlayer) rider).getHandle();
            boolean onGround = handle.onGround;
            float speedMod = npc.getNavigator().getDefaultParameters()
                    .modifiedSpeed((onGround ? GROUND_SPEED : AIR_SPEED));
            speed = updateHorizontalSpeed(handle, passenger, speed, speedMod);

            boolean shouldJump = NMS.shouldJump(passenger);
            if (shouldJump) {
                if (handle.onGround && jumpTicks == 0) {
                    getHandle().motY = JUMP_VELOCITY;
                    jumpTicks = 10;
                }
            } else {
                jumpTicks = 0;
            }
            jumpTicks = Math.max(0, jumpTicks - 1);

            setMountedYaw(handle);
        }

        private static final float AIR_SPEED = 1.5F;
        private static final float GROUND_SPEED = 4F;
        private static final float JUMP_VELOCITY = 0.6F;
    }

    public class LookAirController implements MovementController {
        private boolean paused = false;

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
            net.minecraft.server.v1_8_R3.Entity handle = getHandle();
            handle.motX = dir.getX();
            handle.motY = dir.getY();
            handle.motZ = dir.getZ();
            setMountedYaw(handle);
        }
    }

    public static interface MovementController {
        void leftClick(PlayerInteractEvent event);

        void rightClick(PlayerInteractEvent event);

        void rightClickEntity(NPCRightClickEvent event);

        void run(Player rider);
    }

    public class PlayerInputAirController implements MovementController {
        private boolean paused = false;
        private double speed;

        @Override
        public void leftClick(PlayerInteractEvent event) {
            paused = !paused;
        }

        @Override
        public void rightClick(PlayerInteractEvent event) {
            getHandle().motY = -0.3F;
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
            net.minecraft.server.v1_8_R3.Entity handle = getHandle();
            net.minecraft.server.v1_8_R3.Entity passenger = ((CraftPlayer) rider).getHandle();

            speed = updateHorizontalSpeed(handle, passenger, speed, 1F);
            boolean shouldJump = NMS.shouldJump(passenger);
            if (shouldJump) {
                handle.motY = 0.3F;
            }
            handle.motY *= 0.98F;
        }
    }

    public static void registerControllerType(EntityType type, Class<? extends MovementController> clazz) {
        controllerTypes.put(type, clazz);
    }

    private static final Map<EntityType, Class<? extends MovementController>> controllerTypes = Maps
            .newEnumMap(EntityType.class);

    static {
        controllerTypes.put(EntityType.BAT, PlayerInputAirController.class);
        controllerTypes.put(EntityType.BLAZE, PlayerInputAirController.class);
        controllerTypes.put(EntityType.ENDER_DRAGON, PlayerInputAirController.class);
        controllerTypes.put(EntityType.GHAST, PlayerInputAirController.class);
        controllerTypes.put(EntityType.WITHER, PlayerInputAirController.class);
        controllerTypes.put(EntityType.UNKNOWN, LookAirController.class);
    }
}
