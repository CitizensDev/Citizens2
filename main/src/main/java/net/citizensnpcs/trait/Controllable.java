package net.citizensnpcs.trait;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.google.common.collect.Maps;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

/**
 * Persists the controllable status for /npc controllable
 *
 * A controllable {@link NPC} can be mounted by a {@link Player} using right click or /npc mount and moved around using
 * e.g. arrow keys.
 */
@TraitName("controllable")
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

    /**
     * Configures the explicit type parameter.
     *
     * @see #setExplicitType(EntityType)
     */
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
        } else if (args.hasValueFlag("explicittype")) {
            explicitType = Util.matchEnum(EntityType.values(), args.getFlag("explicittype"));
        }
        if (npc.isSpawned()) {
            loadController();
        }
    }

    private void enterOrLeaveVehicle(Player player) {
        List<Entity> passengers = NMS.getPassengers(player);
        if (passengers.size() > 0) {
            if (passengers.contains(player)) {
                player.leaveVehicle();
            }
            return;
        }
        if (!player.hasPermission(
                "citizens.npc.controllable." + npc.getEntity().getType().name().toLowerCase().replace("_", ""))
                || !player.hasPermission("citizens.npc.controllable")
                || ownerRequired && !npc.getOrAddTrait(Owner.class).isOwnedBy(player))
            return;

        NMS.mount(npc.getEntity(), player);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("explicittype")) {
            explicitType = Util.matchEnum(EntityType.values(), key.getString("explicittype"));
        }
    }

    private void loadController() {
        EntityType type = npc.getEntity().getType();
        if (explicitType != null) {
            type = explicitType;
        }
        if (!(npc.getEntity() instanceof LivingEntity) && !(npc.getEntity() instanceof Vehicle) && (explicitType == null
                || explicitType == EntityType.UNKNOWN || npc.getEntity().getType() == explicitType)) {
            controller = new LookAirController();
            return;
        }
        Constructor<? extends MovementController> innerConstructor = CONTROLLER_TYPES.get(type);
        if (innerConstructor == null) {
            controller = new GroundController();
            return;
        }
        try {
            if (innerConstructor.getParameterCount() == 0) {
                controller = innerConstructor.newInstance();
            } else {
                controller = innerConstructor.newInstance(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            controller = new GroundController();
        }
    }

    /**
     * Attempts to mount the {@link Player} onto the {@link NPC}.
     *
     * @param toMount
     *            the player to mount
     * @return whether the mount was successful
     */
    public boolean mount(Player toMount) {
        List<Entity> passengers = NMS.getPassengers(npc.getEntity());
        if (passengers.size() != 0)
            return false;
        boolean found = false;
        for (Entity passenger : passengers) {
            if (passenger != null && passenger == toMount) {
                found = true;
                break;
            }
        }
        if (found)
            return false;
        enterOrLeaveVehicle(toMount);
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (!npc.isSpawned() || !enabled)
            return;
        Action performed = event.getAction();
        if (!NMS.getPassengers(npc.getEntity()).contains(event.getPlayer()))
            return;
        switch (performed) {
            case RIGHT_CLICK_BLOCK:
                if (event.isCancelled())
                    return;
            case RIGHT_CLICK_AIR:
                controller.rightClick(event);
                break;
            case LEFT_CLICK_BLOCK:
                if (event.isCancelled())
                    return;
            case LEFT_CLICK_AIR:
                controller.leftClick(event);
                break;
            default:
                break;
        }
    }

    @EventHandler
    private void onRightClick(NPCRightClickEvent event) {
        if (!enabled || !npc.isSpawned() || !event.getNPC().equals(npc))
            return;
        controller.rightClickEntity(event);
        event.setDelayedCancellation(true);
    }

    @Override
    public void onSpawn() {
        loadController();
    }

    @Override
    public void run() {
        if (!enabled || !npc.isSpawned())
            return;
        List<Entity> passengers = NMS.getPassengers(npc.getEntity());
        if (passengers.size() == 0 || !(passengers.get(0) instanceof Player) || npc.getNavigator().isNavigating())
            return;
        controller.run((Player) passengers.get(0));
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

    /**
     * Configures the explicit typei.e. whether the NPC should be controlled as if it was a certain {@link EntityType}.
     *
     * @param type
     *            the explicit type
     */
    public void setExplicitType(EntityType type) {
        explicitType = type;
    }

    private void setMountedYaw(Entity entity) {
        if (entity instanceof EnderDragon || !Setting.USE_BOAT_CONTROLS.asBoolean())
            return; // EnderDragon handles this separately
        Location loc = entity.getLocation();
        Vector vel = entity.getVelocity();
        if (vel.lengthSquared() == 0)
            return;

        double tX = loc.getX() + vel.getX();
        double tZ = loc.getZ() + vel.getZ();
        if (loc.getZ() > tZ) {
            loc.setYaw((float) -Math.toDegrees(Math.atan((loc.getX() - tX) / (loc.getZ() - tZ))) + 180F);
        } else if (loc.getZ() < tZ) {
            loc.setYaw((float) -Math.toDegrees(Math.atan((loc.getX() - tX) / (loc.getZ() - tZ))));
        }
        NMS.look(entity, loc.getYaw(), loc.getPitch());
    }

    /**
     * Sets whether the {@link Player} attempting to mount the {@link NPC} must actually own the {@link NPC} to mount
     * it.
     *
     * @see Owner#isOwnedBy(org.bukkit.command.CommandSender)
     */
    public void setOwnerRequired(boolean ownerRequired) {
        this.ownerRequired = ownerRequired;
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        if (!enabled && NMS.getPassengers(npc.getEntity()).size() > 0) {
            NMS.getPassengers(npc.getEntity()).get(0).leaveVehicle();
        }
        return enabled;
    }

    private double updateHorizontalSpeed(Entity handle, Entity passenger, double speed, float speedMod,
            double maxSpeed) {
        Vector vel = handle.getVelocity();
        double oldSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
        double horizontal = NMS.getHorizontalMovement(passenger);
        if (Math.abs(Math.abs(horizontal) - 0.98) > 0.02)
            return speed;
        double yaw = passenger.getLocation().getYaw();
        if (horizontal > 0.0D) {
            double dXcos = -Math.sin(yaw * Math.PI / 180.0F);
            double dXsin = Math.cos(yaw * Math.PI / 180.0F);

            vel = vel.setX(dXcos * speed * speedMod).setZ(dXsin * speed * speedMod);
        }
        vel = vel.add(new Vector(
                passenger.getVelocity().getX() * speedMod * Setting.CONTROLLABLE_GROUND_DIRECTION_MODIFIER.asDouble(),
                0D,
                passenger.getVelocity().getZ() * speedMod * Setting.CONTROLLABLE_GROUND_DIRECTION_MODIFIER.asDouble()))
                .multiply(0.98);

        double newSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
        if (newSpeed > maxSpeed) {
            vel = vel.multiply(new Vector(maxSpeed / newSpeed, 1, maxSpeed / newSpeed));
            newSpeed = maxSpeed;
        }
        handle.setVelocity(vel);

        if (newSpeed > oldSpeed && speed < maxSpeed)
            return (float) Math.min(maxSpeed, speed + (maxSpeed - speed) / 50.0D);
        else
            return (float) Math.max(0, speed - speed / 50.0D);
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
            boolean onGround = NMS.isOnGround(npc.getEntity());
            float speedMod = npc.getNavigator().getDefaultParameters()
                    .modifiedSpeed(onGround ? GROUND_SPEED : AIR_SPEED);
            if (!Util.isHorse(npc.getEntity().getType())) {
                // use minecraft horse physics
                speed = updateHorizontalSpeed(npc.getEntity(), rider, speed, speedMod,
                        Setting.MAX_CONTROLLABLE_GROUND_SPEED.asDouble());
            }
            boolean shouldJump = NMS.shouldJump(rider);
            if (shouldJump) {
                if (onGround && jumpTicks == 0) {
                    npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(JUMP_VELOCITY));
                    jumpTicks = 10;
                }
            } else {
                jumpTicks = 0;
            }
            jumpTicks = Math.max(0, jumpTicks - 1);
            setMountedYaw(npc.getEntity());
        }

        private static final float AIR_SPEED = 0.5F;
        private static final float GROUND_SPEED = 0.5F;
        private static final float JUMP_VELOCITY = 0.5F;
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
                npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.001));
                return;
            }
            Vector dir = rider.getEyeLocation().getDirection();
            dir.multiply(npc.getNavigator().getDefaultParameters().speedModifier());
            npc.getEntity().setVelocity(dir);
            setMountedYaw(npc.getEntity());
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
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(-0.25F));
        }

        @Override
        public void rightClickEntity(NPCRightClickEvent event) {
            enterOrLeaveVehicle(event.getClicker());
        }

        @Override
        public void run(Player rider) {
            if (paused) {
                npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.001F));
                return;
            }
            speed = updateHorizontalSpeed(npc.getEntity(), rider, speed, 1F,
                    Setting.MAX_CONTROLLABLE_FLIGHT_SPEED.asDouble());
            boolean shouldJump = NMS.shouldJump(rider);
            if (shouldJump) {
                npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.25F));
            }
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().multiply(new Vector(1, 0.98, 1)));
            setMountedYaw(npc.getEntity());
        }
    }

    /**
     * Register a movement controller for a certain {@link EntityType} to be used for {@link NPC}s with that type.
     *
     * Default controllers are registered for BAT, BLAZE, ENDER_DRAGON, GHAST, WITHER and PARROT using
     * {@link PlayerInputAirController}.
     *
     * @param type
     *            the entity type
     * @param clazz
     *            the controller class
     */
    public static void registerControllerType(EntityType type, Class<? extends MovementController> clazz) {
        try {
            Constructor<? extends MovementController> constructor = clazz.getConstructor(Controllable.class);
            constructor.setAccessible(true);
            CONTROLLER_TYPES.put(type, constructor);
            return;
        } catch (Exception e) {
            try {
                Constructor<? extends MovementController> constructor = clazz.getConstructor();
                constructor.setAccessible(true);
                CONTROLLER_TYPES.put(type, constructor);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    private static Map<EntityType, Constructor<? extends MovementController>> CONTROLLER_TYPES = Maps
            .newEnumMap(EntityType.class);

    static {
        registerControllerType(EntityType.BAT, PlayerInputAirController.class);
        registerControllerType(EntityType.BLAZE, PlayerInputAirController.class);
        registerControllerType(EntityType.ENDER_DRAGON, PlayerInputAirController.class);
        registerControllerType(EntityType.GHAST, PlayerInputAirController.class);
        registerControllerType(EntityType.WITHER, PlayerInputAirController.class);
        try {
            registerControllerType(EntityType.valueOf("PARROT"), PlayerInputAirController.class);
        } catch (IllegalArgumentException ex) {
        }
        try {
            registerControllerType(EntityType.valueOf("PHANTOM"), PlayerInputAirController.class);
        } catch (IllegalArgumentException ex) {
        }
        registerControllerType(EntityType.UNKNOWN, LookAirController.class);
    }
}
