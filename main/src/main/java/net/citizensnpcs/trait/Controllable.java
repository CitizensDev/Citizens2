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
            explicitType = Util.matchEntityType(args.getFlag("explicittype"));
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
        if (ownerRequired && !npc.getTrait(Owner.class).isOwnedBy(player)) {
            return;
        }
        NMS.mount(npc.getEntity(), player);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("explicittype")) {
            explicitType = Util.matchEntityType(key.getString("explicittype"));
        }
    }

    private void loadController() {
        EntityType type = npc.getEntity().getType();
        if (explicitType != null) {
            type = explicitType;
        }
        if (!(npc.getEntity() instanceof LivingEntity) && (explicitType == null || explicitType == EntityType.UNKNOWN
                || npc.getEntity().getType() == explicitType)) {
            controller = new LookAirController();
            return;
        }
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
            } else {
                controller = innerConstructor.newInstance(this);
            }
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
        Action performed = event.getAction();
        if (NMS.getPassengers(npc.getEntity()).contains(npc.getEntity()))
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
        if (!enabled || !npc.isSpawned())
            return;
        List<Entity> passengers = NMS.getPassengers(npc.getEntity());
        if (passengers.size() == 0 || !(passengers.get(0) instanceof Player))
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

    private void setMountedYaw(Entity entity) {
        if (entity instanceof EnderDragon || !Setting.USE_BOAT_CONTROLS.asBoolean())
            return; // EnderDragon handles this separately
        Location loc = entity.getLocation();
        Vector vel = entity.getVelocity();
        if (vel.lengthSquared() == 0) {
            return;
        }

        double tX = loc.getX() + vel.getX();
        double tZ = loc.getZ() + vel.getZ();
        if (loc.getZ() > tZ) {
            loc.setYaw((float) -Math.toDegrees(Math.atan((loc.getX() - tX) / (loc.getZ() - tZ))) + 180F);
        } else if (loc.getZ() < tZ) {
            loc.setYaw((float) -Math.toDegrees(Math.atan((loc.getX() - tX) / (loc.getZ() - tZ))));
        }
        NMS.look(entity, loc.getYaw(), loc.getPitch());
    }

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

    private double updateHorizontalSpeed(Entity handle, Entity passenger, double speed, float speedMod) {
        Vector vel = handle.getVelocity();
        double oldSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
        double horizontal = NMS.getHorizontalMovement(passenger);
        double yaw = passenger.getLocation().getYaw();
        if (horizontal > 0.0D) {
            double dXcos = -Math.sin(yaw * Math.PI / 180.0F);
            double dXsin = Math.cos(yaw * Math.PI / 180.0F);

            vel = vel.setX(dXcos * speed * 0.5).setZ(dXsin * speed * 0.5);
        }
        vel = vel.add(
                new Vector(passenger.getVelocity().getX() * speedMod, 0D, passenger.getVelocity().getZ() * speedMod));

        double newSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
        if (newSpeed > 0.35D) {
            double movementFactor = 0.35D / newSpeed;
            vel = vel.multiply(new Vector(movementFactor, 1, movementFactor));
            newSpeed = 0.35D;
        }
        handle.setVelocity(vel);

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
            boolean onGround = NMS.isOnGround(npc.getEntity());
            float speedMod = npc.getNavigator().getDefaultParameters()
                    .modifiedSpeed((onGround ? GROUND_SPEED : AIR_SPEED));
            speed = updateHorizontalSpeed(npc.getEntity(), rider, speed, speedMod);

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
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(-0.3F));
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

            speed = updateHorizontalSpeed(npc.getEntity(), rider, speed, 1F);
            boolean shouldJump = NMS.shouldJump(rider);
            if (shouldJump) {
                npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.3F));
            }
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().multiply(new Vector(1, 0.98, 1)));
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
