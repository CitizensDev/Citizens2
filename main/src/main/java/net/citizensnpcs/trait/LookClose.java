package net.citizensnpcs.trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

/**
 * Persists the /npc lookclose metadata
 *
 */
@TraitName("lookclose")
public class LookClose extends Trait implements Toggleable, CommandConfigurable {
    @Persist("enabled")
    private boolean enabled = Setting.DEFAULT_LOOK_CLOSE.asBoolean();
    @Persist
    private boolean enableRandomLook = Setting.DEFAULT_RANDOM_LOOK_CLOSE.asBoolean();
    private Player lookingAt;
    @Persist
    private int randomLookDelay = Setting.DEFAULT_RANDOM_LOOK_DELAY.asInt();
    @Persist
    private float[] randomPitchRange = { -10, 0 };
    @Persist
    private float[] randomYawRange = { 0, 360 };
    private double range = Setting.DEFAULT_LOOK_CLOSE_RANGE.asDouble();
    @Persist("realisticlooking")
    private boolean realisticLooking = Setting.DEFAULT_REALISTIC_LOOKING.asBoolean();
    private int t;

    public LookClose() {
        super("lookclose");
    }

    /**
     * Returns whether the target can be seen. Will use realistic line of sight if {@link #setRealisticLooking(boolean)}
     * is true.
     */
    public boolean canSeeTarget() {
        return realisticLooking && npc.getEntity() instanceof LivingEntity
                ? ((LivingEntity) npc.getEntity()).hasLineOfSight(lookingAt)
                : lookingAt != null && lookingAt.isValid();
    }

    @Override
    public void configure(CommandContext args) {
        range = args.getFlagDouble("range", args.getFlagDouble("r", range));
        realisticLooking = args.hasFlag('r');
    }

    /**
     * Finds a new look-close target
     */
    public void findNewTarget() {
        List<Player> nearby = new ArrayList<>();
        for (Entity entity : npc.getEntity().getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player))
                continue;

            Player player = (Player) entity;
            if (CitizensAPI.getNPCRegistry().getNPC(entity) != null || player.getGameMode() == GameMode.SPECTATOR
                    || entity.getLocation(CACHE_LOCATION).getWorld() != NPC_LOCATION.getWorld()
                    || player.hasPotionEffect(PotionEffectType.INVISIBILITY) || isPluginVanished((Player) entity))
                continue;
            nearby.add(player);
        }

        if (!nearby.isEmpty()) {
            nearby.sort((o1, o2) -> {
                Location l1 = o1.getLocation(CACHE_LOCATION);
                Location l2 = o2.getLocation(CACHE_LOCATION2);
                if (!NPC_LOCATION.getWorld().equals(l1.getWorld()) || !NPC_LOCATION.getWorld().equals(l2.getWorld())) {
                    return -1;
                }
                return Double.compare(l1.distanceSquared(NPC_LOCATION), l2.distanceSquared(NPC_LOCATION));
            });

            lookingAt = nearby.get(0);
        }
    }

    private boolean hasInvalidTarget() {
        if (lookingAt == null)
            return true;
        if (!lookingAt.isOnline() || lookingAt.getWorld() != npc.getEntity().getWorld()
                || lookingAt.getLocation(PLAYER_LOCATION).distanceSquared(NPC_LOCATION) > range * range) {
            lookingAt = null;
        }
        return lookingAt == null;
    }

    private boolean isEqual(float[] array) {
        return Math.abs(array[0] - array[1]) < 0.001;
    }

    private boolean isPluginVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void load(DataKey key) {
        range = key.getDouble("range");
    }

    /**
     * Enables/disables the trait
     */
    public void lookClose(boolean lookClose) {
        enabled = lookClose;
    }

    @Override
    public void onDespawn() {
        lookingAt = null;
    }

    private void randomLook() {
        Random rand = new Random();
        float pitch = isEqual(randomPitchRange) ? randomPitchRange[0]
                : rand.doubles(randomPitchRange[0], randomPitchRange[1]).iterator().next().floatValue();
        float yaw = isEqual(randomYawRange) ? randomYawRange[0]
                : rand.doubles(randomYawRange[0], randomYawRange[1]).iterator().next().floatValue();
        Util.assumePose(npc.getEntity(), yaw, pitch);
    }

    @Override
    public void run() {
        if (!enabled || !npc.isSpawned()) {
            return;
        }
        if (npc.getNavigator().isNavigating() && Setting.DISABLE_LOOKCLOSE_WHILE_NAVIGATING.asBoolean()) {
            return;
        }
        npc.getEntity().getLocation(NPC_LOCATION);
        if (hasInvalidTarget()) {
            findNewTarget();
        }
        if (npc.getNavigator().isNavigating()) {
            npc.getNavigator().setPaused(lookingAt != null);
        } else if (lookingAt == null && enableRandomLook && t <= 0) {
            randomLook();
            t = randomLookDelay;
        }
        t--;
        if (lookingAt != null && canSeeTarget()) {
            Util.faceEntity(npc.getEntity(), lookingAt);
            if (npc.getEntity().getType().name().toLowerCase().contains("shulker")) {
                NMS.setPeekShulker(npc.getEntity(), 100 - (int) Math
                        .floor(npc.getStoredLocation().distanceSquared(lookingAt.getLocation(PLAYER_LOCATION))));
            }
        }
    }

    @Override
    public void save(DataKey key) {
        key.setDouble("range", range);
    }

    /**
     * Enables random looking - will look at a random {@link Location} every so often if enabled.
     */
    public void setRandomLook(boolean enableRandomLook) {
        this.enableRandomLook = enableRandomLook;
    }

    /**
     * Sets the delay between random looking in ticks
     */
    public void setRandomLookDelay(int delay) {
        this.randomLookDelay = delay;
    }

    public void setRandomLookPitchRange(float min, float max) {
        this.randomPitchRange = new float[] { min, max };
    }

    public void setRandomLookYawRange(float min, float max) {
        this.randomYawRange = new float[] { min, max };
    }

    /**
     * Sets the maximum range in blocks to look at other Entities
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * Enables/disables realistic looking (using line of sight checks). More computationally expensive.
     */
    public void setRealisticLooking(boolean realistic) {
        this.realisticLooking = realistic;
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    @Override
    public String toString() {
        return "LookClose{" + enabled + "}";
    }

    private static final Location CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final Location CACHE_LOCATION2 = new Location(null, 0, 0, 0);
    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
    private static final Location PLAYER_LOCATION = new Location(null, 0, 0, 0);
}