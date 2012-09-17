package net.citizensnpcs.trait;

import java.lang.reflect.Constructor;
import java.util.Map;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.google.common.collect.Maps;

//TODO: reduce reliance on CitizensNPC
public class Controllable extends Trait implements Toggleable {
    private Controller controller = new GroundController();
    private boolean enabled;

    public Controllable() {
        super("controllable");
    }

    private void enterOrLeaveVehicle(Player player) {
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        if (getHandle().passenger != null) {
            if (getHandle().passenger == handle)
                player.leaveVehicle();
            return;
        }
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
        enabled = key.getBoolean("enabled");
    }

    public boolean mount(Player toMount) {
        if (npc.getBukkitEntity().getPassenger() != null)
            return false;
        ((CraftPlayer) toMount).getHandle().setPassengerOf(getHandle());
        return true;
    }

    @EventHandler
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
        EntityType type = npc.getBukkitEntity().getType();
        Class<? extends Controller> clazz = controllerTypes.get(type);
        if (clazz == null) {
            controller = new GroundController();
            return;
        }
        try {
            Constructor<? extends Controller> innerConstructor = clazz.getConstructor(Controllable.class);
            if (innerConstructor == null) {
                controller = clazz.newInstance();
            } else
                controller = innerConstructor.newInstance(this);
        } catch (Exception e) {
            controller = new GroundController();
        }
    }

    @Override
    public void run() {
        if (!enabled || !npc.isSpawned() || getHandle().passenger == null)
            return;
        controller.run((Player) getHandle().passenger.getBukkitEntity());
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("enabled", enabled);
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        if (!enabled && getHandle().passenger != null)
            getHandle().passenger.getBukkitEntity().leaveVehicle();
        return enabled;
    }

    private class AirController implements Controller {
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
            if (paused)
                return;
            Vector dir = rider.getEyeLocation().getDirection();
            double y = dir.getY();
            dir.multiply(npc.getNavigator().getDefaultParameters().speedModifier()).setY(y);
            EntityLiving handle = getHandle();
            handle.motX += dir.getX();
            handle.motY += dir.getY();
            handle.motZ += dir.getZ();
        }
    }

    private static interface Controller {
        void leftClick(PlayerInteractEvent event);

        void rightClick(PlayerInteractEvent event);

        void rightClickEntity(NPCRightClickEvent event);

        void run(Player rider);
    }

    private class GroundController implements Controller {
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
        }

        private static final float AIR_SPEED = 1.5F;
        private static final float GROUND_SPEED = 4F;
        private static final float JUMP_VELOCITY = 0.6F;
    }

    private static final Map<EntityType, Class<? extends Controller>> controllerTypes = Maps
            .newEnumMap(EntityType.class);

    static {
        controllerTypes.put(EntityType.BLAZE, AirController.class);
        controllerTypes.put(EntityType.ENDER_DRAGON, AirController.class);
        controllerTypes.put(EntityType.GHAST, AirController.class);
    }
}
