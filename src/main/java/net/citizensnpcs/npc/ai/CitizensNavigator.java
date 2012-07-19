package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;
import java.util.Map;

import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Maps;

public class CitizensNavigator implements Navigator {
    private PathStrategy executing;
    private final CitizensNPC npc;
    private float speed;

    public CitizensNavigator(CitizensNPC npc) {
        this.npc = npc;
        this.speed = getSpeedFor(npc.getHandle());
    }

    @Override
    public void cancelNavigation() {
        if (executing != null) {
            Bukkit.getPluginManager().callEvent(new NavigationCancelEvent(this));
        }
        executing = null;
    }

    @Override
    public EntityTarget getEntityTarget() {
        return executing instanceof EntityTarget ? (EntityTarget) executing : null;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    private float getSpeedFor(EntityLiving from) {
        EntityType entityType = from.getBukkitEntity().getType();
        Float cached = MOVEMENT_SPEEDS.get(entityType);
        if (cached != null)
            return cached;
        if (SPEED_FIELD == null) {
            MOVEMENT_SPEEDS.put(entityType, DEFAULT_SPEED);
            return DEFAULT_SPEED;
        }
        try {
            float speed = SPEED_FIELD.getFloat(from);
            MOVEMENT_SPEEDS.put(entityType, speed);
            return speed;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return DEFAULT_SPEED;
        }
    }

    @Override
    public Location getTargetAsLocation() {
        return executing.getTargetAsLocation();
    }

    @Override
    public TargetType getTargetType() {
        return executing.getTargetType();
    }

    @Override
    public boolean isNavigating() {
        return executing != null;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        PathStrategy newStrategy = new MCTargetStrategy(npc, target, aggressive, speed);
        switchStrategyTo(newStrategy);
    }

    @Override
    public void setTarget(Location target) {
        PathStrategy newStrategy = new MCNavigationStrategy(npc, target, speed);
        switchStrategyTo(newStrategy);
    }

    private void switchStrategyTo(PathStrategy newStrategy) {
        if (executing != null)
            Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));

        executing = newStrategy;

        Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
    }

    public void update() {
        if (executing == null)
            return;
        boolean finished = executing.update();
        if (finished) {
            Bukkit.getPluginManager().callEvent(new NavigationCompleteEvent(this));
            executing = null;
        }
    }

    private static final float DEFAULT_SPEED = 0.3F;
    private static final Map<EntityType, Float> MOVEMENT_SPEEDS = Maps.newEnumMap(EntityType.class);
    private static Field SPEED_FIELD;
    static {
        MOVEMENT_SPEEDS.put(EntityType.IRON_GOLEM, 0.15F);
        MOVEMENT_SPEEDS.put(EntityType.CHICKEN, 0.25F);
        MOVEMENT_SPEEDS.put(EntityType.COW, 0.2F);
        MOVEMENT_SPEEDS.put(EntityType.SHEEP, 0.25F);
        MOVEMENT_SPEEDS.put(EntityType.VILLAGER, 0.3F);
        MOVEMENT_SPEEDS.put(EntityType.SNOWMAN, 0.25F);
        try {
            SPEED_FIELD = EntityLiving.class.getDeclaredField("bb");
            SPEED_FIELD.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
