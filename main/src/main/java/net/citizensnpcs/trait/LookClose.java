package net.citizensnpcs.trait;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLookCloseChangeTargetEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.RotationTrait.PacketRotationSession;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

/**
 * Persists the /npc lookclose metadata
 *
 */
@TraitName("lookclose")
public class LookClose extends Trait implements Toggleable {
    @Persist("disablewhilenavigating")
    private boolean disableWhileNavigating = Setting.DISABLE_LOOKCLOSE_WHILE_NAVIGATING.asBoolean();
    @Persist("enabled")
    private boolean enabled = Setting.DEFAULT_LOOK_CLOSE.asBoolean();
    @Persist
    private boolean enableRandomLook = Setting.DEFAULT_RANDOM_LOOK_CLOSE.asBoolean();
    @Persist("headonly")
    private boolean headOnly;
    @Persist("linkedbody")
    private boolean linkedBody;
    private Player lookingAt;
    @Persist("perplayer")
    private boolean perPlayer;
    @Persist
    private int randomLookDelay = Setting.DEFAULT_RANDOM_LOOK_DELAY.asTicks();
    @Persist
    private float[] randomPitchRange = { 0, 0 };
    @Persist
    private boolean randomSwitchTargets;
    @Persist
    private float[] randomYawRange = { 0, 360 };
    private double range = Setting.DEFAULT_LOOK_CLOSE_RANGE.asDouble();
    @Persist("realisticlooking")
    private boolean realisticLooking = Setting.DEFAULT_REALISTIC_LOOKING.asBoolean();
    private final Map<UUID, PacketRotationSession> sessions = Maps.newHashMapWithExpectedSize(4);
    private int t;
    @Persist("targetnpcs")
    private boolean targetNPCs;

    public LookClose() {
        super("lookclose");
    }

    private boolean canSee(Player player) {
        if (player == null || !player.isValid())
            return false;
        return realisticLooking && npc.getEntity() instanceof LivingEntity
                ? ((LivingEntity) npc.getEntity()).hasLineOfSight(player)
                : true;
    }

    /**
     * Returns whether the target can be seen. Will use realistic line of sight if {@link #setRealisticLooking(boolean)}
     * is true.
     */
    public boolean canSeeTarget() {
        return canSee(lookingAt);
    }

    public boolean disableWhileNavigating() {
        return disableWhileNavigating;
    }

    /**
     * Finds a new look-close target
     */
    public void findNewTarget() {
        if (perPlayer) {
            lookingAt = null;
            RotationTrait rotationTrait = npc.getOrAddTrait(RotationTrait.class);
            Set<UUID> seen = Sets.newHashSet();
            for (Player player : getNearbyPlayers()) {
                PacketRotationSession session = sessions.get(player.getUniqueId());
                if (session == null) {
                    sessions.put(player.getUniqueId(),
                            session = rotationTrait.createPacketSession(
                                    rotationTrait.getGlobalParameters().clone().linkedBody(linkedBody)
                                            .headOnly(headOnly).uuidFilter(player.getUniqueId()).persist(true)));
                }
                session.getSession().rotateToFace(player);
                seen.add(player.getUniqueId());
            }
            for (Iterator<UUID> iterator = sessions.keySet().iterator(); iterator.hasNext();) {
                UUID uuid = iterator.next();
                if (!seen.contains(uuid)) {
                    rotationTrait.resetPlayerToPhysicalSession(uuid);
                    iterator.remove();
                }
            }
            return;
        } else if (sessions.size() > 0) {
            RotationTrait rotationTrait = npc.getOrAddTrait(RotationTrait.class);
            for (UUID uuid : sessions.keySet()) {
                rotationTrait.resetPlayerToPhysicalSession(uuid);
            }
            sessions.clear();
        }
        if (lookingAt != null && !isValid(lookingAt)) {
            NPCLookCloseChangeTargetEvent event = new NPCLookCloseChangeTargetEvent(npc, lookingAt, null);
            Bukkit.getPluginManager().callEvent(event);
            if (event.getNewTarget() != null && isValid(event.getNewTarget())) {
                lookingAt = event.getNewTarget();
            } else {
                lookingAt = null;
            }
        }
        Player old = lookingAt;
        if (lookingAt != null) {
            if (randomSwitchTargets && t <= 0) {
                List<Player> options = getNearbyPlayers();
                if (options.size() > 0) {
                    lookingAt = options.get(Util.getFastRandom().nextInt(options.size()));
                    t = randomLookDelay;
                }
            }
        } else {
            double min = Double.MAX_VALUE;
            Location npcLoc = npc.getStoredLocation();
            for (Player player : getNearbyPlayers()) {
                double dist = player.getLocation().distance(npcLoc);
                if (dist > min) {
                    continue;
                }
                min = dist;
                lookingAt = player;
            }
        }
        if (old != lookingAt) {
            NPCLookCloseChangeTargetEvent event = new NPCLookCloseChangeTargetEvent(npc, old, lookingAt);
            Bukkit.getPluginManager().callEvent(event);
            if (lookingAt != event.getNewTarget() && event.getNewTarget() != null && !isValid(event.getNewTarget()))
                return;
            lookingAt = event.getNewTarget();
        }
    }

    private List<Player> getNearbyPlayers() {
        List<Player> options = Lists.newArrayList();
        Location npcLoc = npc.getStoredLocation();
        Iterable<Player> nearby = targetNPCs
                ? npc.getEntity().getNearbyEntities(range, range, range).stream()
                        .filter(e -> e.getType() == EntityType.PLAYER && e.getWorld() == npcLoc.getWorld())
                        .map(e -> (Player) e).collect(Collectors.toList())
                : CitizensAPI.getLocationLookup().getNearbyPlayers(npcLoc, range);
        for (Player player : nearby) {
            if (player == lookingAt || !targetNPCs && CitizensAPI.getNPCRegistry().getNPC(player) != null) {
                continue;
            }
            if (player.getLocation().getWorld() != npcLoc.getWorld() || isInvisible(player)) {
                continue;
            }
            options.add(player);
        }
        return options;
    }

    public int getRandomLookDelay() {
        return randomLookDelay;
    }

    public float[] getRandomLookPitchRange() {
        return randomPitchRange;
    }

    public float[] getRandomLookYawRange() {
        return randomYawRange;
    }

    public double getRange() {
        return range;
    }

    public Player getTarget() {
        return lookingAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHeadOnly() {
        return headOnly;
    }

    private boolean isInvisible(Player player) {
        return player.getGameMode() == GameMode.SPECTATOR || player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                || isPluginVanished(player) || !canSee(player);
    }

    private boolean isPluginVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean())
                return true;
        }
        return false;
    }

    public boolean isRandomLook() {
        return enableRandomLook;
    }

    private boolean isValid(Player entity) {
        return entity.isOnline() && entity.isValid() && entity.getWorld() == npc.getEntity().getWorld()
                && entity.getLocation().distance(npc.getStoredLocation()) <= range && !isInvisible(entity);
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
        NPCLookCloseChangeTargetEvent event = new NPCLookCloseChangeTargetEvent(npc, lookingAt, null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getNewTarget() != null && isValid(event.getNewTarget())) {
            lookingAt = event.getNewTarget();
        } else {
            lookingAt = null;
        }
    }

    private void randomLook() {
        float pitch = isEqual(randomPitchRange) ? randomPitchRange[0]
                : Util.getFastRandom().doubles(randomPitchRange[0], randomPitchRange[1]).iterator().next().floatValue();
        float yaw = isEqual(randomYawRange) ? randomYawRange[0]
                : Util.getFastRandom().doubles(randomYawRange[0], randomYawRange[1]).iterator().next().floatValue();
        npc.getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToHave(yaw, pitch);
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) {
            lookingAt = null;
            return;
        }
        if (enableRandomLook) {
            if (!npc.getNavigator().isNavigating() && lookingAt == null && t <= 0) {
                randomLook();
                t = randomLookDelay;
            }
        }
        t--;

        if (!enabled || npc.getNavigator().isNavigating() && disableWhileNavigating()) {
            lookingAt = null;
            return;
        }
        findNewTarget();

        if (npc.getNavigator().isNavigating() || npc.getNavigator().isPaused()) {
            npc.getNavigator().setPaused(lookingAt != null);
        }
        if (lookingAt == null)
            return;

        RotationTrait rot = npc.getOrAddTrait(RotationTrait.class);
        rot.getGlobalParameters().headOnly(headOnly);
        rot.getGlobalParameters().linkedBody(linkedBody);
        rot.getPhysicalSession().rotateToFace(lookingAt);

        if (npc.getEntity().getType().name().equals("SHULKER")) {
            boolean wasSilent = npc.getEntity().isSilent();
            npc.getEntity().setSilent(true);
            NMS.setPeekShulker(npc.getEntity(),
                    100 - 4 * (int) Math.floor(npc.getStoredLocation().distanceSquared(lookingAt.getLocation())));
            npc.getEntity().setSilent(wasSilent);
        }
    }

    @Override
    public void save(DataKey key) {
        key.setDouble("range", range);
    }

    public void setDisableWhileNavigating(boolean set) {
        disableWhileNavigating = set;
    }

    public void setHeadOnly(boolean headOnly) {
        this.headOnly = headOnly;
    }

    public void setLinkedBody(boolean linkedBody) {
        this.linkedBody = linkedBody;
    }

    public void setPerPlayer(boolean perPlayer) {
        this.perPlayer = perPlayer;
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
        randomLookDelay = delay;
    }

    public void setRandomLookPitchRange(float min, float max) {
        randomPitchRange = new float[] { min, max };
    }

    public void setRandomLookYawRange(float min, float max) {
        randomYawRange = new float[] { min, max };
    }

    public void setRandomlySwitchTargets(boolean randomSwitchTargets) {
        this.randomSwitchTargets = randomSwitchTargets;
    }

    /**
     * Sets the maximum range in blocks to look at other Entities
     */
    public void setRange(double range) {
        this.range = range;
    }

    /**
     * Enables/disables realistic looking (using line of sight checks). More computationally expensive.
     */
    public void setRealisticLooking(boolean realistic) {
        realisticLooking = realistic;
    }

    public void setTargetNPCs(boolean target) {
        targetNPCs = target;
    }

    public boolean targetNPCs() {
        return targetNPCs;
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

    public boolean useRealisticLooking() {
        return realisticLooking;
    }

    private static boolean isEqual(float[] array) {
        return Math.abs(array[0] - array[1]) < 0.001;
    }
}