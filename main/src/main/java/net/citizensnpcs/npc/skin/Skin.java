package net.citizensnpcs.npc.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.profile.ProfileFetcher;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.SkinProperty;

/**
 * Stores data for a single skin.
 */
public class Skin {
    private boolean fetching;
    private int fetchRetries = -1;
    private boolean hasFetched;
    private volatile boolean isValid = true;
    private final Map<SkinnableEntity, Void> pending = new WeakHashMap<>(15);
    private BukkitTask retryTask;
    private volatile SkinProperty skinData;
    private volatile UUID skinId;
    private final String skinName;

    /**
     * Constructor.
     *
     * @param skinName
     *            The name of the player the skin belongs to.
     * @param forceUpdate
     */
    Skin(String skinName) {
        this.skinName = skinName.toLowerCase();

        synchronized (CACHE) {
            if (CACHE.containsKey(this.skinName))
                throw new IllegalArgumentException("There is already a skin named " + skinName);

            CACHE.put(this.skinName, this);
        }
        // fetch();
    }

    /**
     * Apply the skin data to the specified skinnable entity.
     *
     * <p>
     * If invoked before the skin data is ready, the skin is retrieved and the skin is automatically applied to the
     * entity at a later time.
     * </p>
     *
     * @param entity
     *            The skinnable entity.
     *
     * @return True if skin was applied, false if the data is being retrieved.
     */
    public boolean apply(SkinnableEntity entity) {
        Preconditions.checkNotNull(entity);

        NPC npc = entity.getNPC();
        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        // Use npc cached skin if available.
        // If npc requires latest skin, cache is used for faster availability until the latest skin can be loaded.
        String cachedName = npc.data().get(CACHED_SKIN_UUID_NAME_METADATA);
        String texture = skinTrait.getTexture();
        if (skinName.equals(cachedName) && texture != null && !texture.equals("cache")) {
            setNPCTexture(entity, new SkinProperty("textures", texture, skinTrait.getSignature()));

            // check if NPC prefers to use cached skin over the latest skin.
            if (entity.getNPC().data().has("player-skin-use-latest")) {
                entity.getNPC().data().remove("player-skin-use-latest");
            }
            if (!skinTrait.shouldUpdateSkins())
                // cache preferred
                return true;
        }
        if (!hasSkinData()) {
            String defaultSkinName = ChatColor.stripColor(npc.getName()).toLowerCase();

            if (npc.hasTrait(SkinTrait.class) && skinName.equals(defaultSkinName)
                    && !npc.getOrAddTrait(SkinTrait.class).fetchDefaultSkin())
                return false;

            if (hasFetched) {
                return true;
            } else {
                if (!fetching) {
                    fetch();
                }
                pending.put(entity, null);
                return false;
            }
        }
        setNPCSkinData(entity, skinName, skinId, skinData);

        return true;
    }

    /**
     * Apply the skin data to the specified skinnable entity and respawn the NPC.
     *
     * @param entity
     *            The skinnable entity.
     */
    public void applyAndRespawn(SkinnableEntity entity) {
        Preconditions.checkNotNull(entity);

        if (!apply(entity))
            return;

        NPC npc = entity.getNPC();

        if (!npc.isSpawned())
            return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation(), SpawnReason.RESPAWN);
        });
    }

    private void fetch() {
        int maxRetries = Setting.MAX_NPC_SKIN_RETRIES.asInt();
        if (maxRetries > -1 && fetchRetries >= maxRetries) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Reached max skin fetch retries for '" + skinName + "'");
            }
            return;
        }
        if (skinName.length() < 3 || skinName.length() > 16) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Skin name invalid length '" + skinName + "'");
            }
            return;
        }
        if (skinName.toLowerCase().startsWith("cit-"))
            return;

        fetching = true;

        ProfileFetcher.fetch(skinName, request -> {
            hasFetched = true;

            switch (request.getResult()) {
                case NOT_FOUND:
                    isValid = false;
                    break;
                case TOO_MANY_REQUESTS:
                    if (maxRetries == 0) {
                        break;
                    }
                    fetchRetries++;
                    long delay = Setting.NPC_SKIN_RETRY_DELAY.asTicks();
                    retryTask = Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), (Runnable) this::fetch,
                            delay);

                    Messaging.idebug(() -> "Retrying skin fetch for '" + skinName + "' in " + delay + " ticks.");
                    break;
                case SUCCESS:
                    GameProfile profile = request.getProfile();
                    setData(profile);
                    break;
                default:
                    break;
            }
        });
    }

    private void fetchForced() {
        int maxRetries = Setting.MAX_NPC_SKIN_RETRIES.asInt();
        if (maxRetries > -1 && fetchRetries >= maxRetries) {
            Messaging.idebug(() -> "Reached max skin fetch retries for '" + skinName + "'");
            return;
        }
        if (skinName.length() < 3 || skinName.length() > 16) {
            Messaging.idebug(() -> "Skin name invalid length '" + skinName + "'");
            return;
        }
        if (skinName.toLowerCase().startsWith("cit-"))
            return;

        fetching = true;

        ProfileFetcher.fetchForced(skinName, request -> {
            hasFetched = true;

            switch (request.getResult()) {
                case NOT_FOUND:
                    isValid = false;
                    break;
                case TOO_MANY_REQUESTS:
                    if (maxRetries == 0) {
                        break;
                    }
                    fetchRetries++;
                    int delay = Setting.NPC_SKIN_RETRY_DELAY.asTicks();
                    retryTask = Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(),
                            (Runnable) this::fetchForced, delay);

                    Messaging.idebug(() -> "Retrying skin fetch for '" + skinName + "' in " + delay + " ticks.");
                    break;
                case SUCCESS:
                    GameProfile profile = request.getProfile();
                    setData(profile);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Get the ID of the player the skin belongs to.
     *
     * @return The skin ID or null if it has not been retrieved yet or the skin is invalid.
     */
    @Nullable
    public UUID getSkinId() {
        return skinId;
    }

    /**
     * Get the name of the skin.
     */
    public String getSkinName() {
        return skinName;
    }

    /**
     * Determine if the skin data has been retrieved.
     */
    public boolean hasSkinData() {
        return skinData != null;
    }

    /**
     * Determine if the skin is valid.
     */
    public boolean isValid() {
        return isValid;
    }

    private void setData(@Nullable GameProfile profile) {
        if (profile == null) {
            isValid = false;
            return;
        }
        if (!profile.getName().toLowerCase().equals(skinName)) {
            Messaging.debug("GameProfile name (" + profile.getName() + ") and " + "skin name (" + skinName
                    + ") do not match. Has the user renamed recently?");
        }
        skinId = profile.getId();
        skinData = SkinProperty.fromMojangProfile(profile);

        List<SkinnableEntity> entities = new ArrayList<>(pending.keySet());
        for (SkinnableEntity entity : entities) {
            applyAndRespawn(entity);
        }
        pending.clear();
    }

    /**
     * Clear all cached skins.
     */
    public static void clearCache() {
        synchronized (CACHE) {
            for (Skin skin : CACHE.values()) {
                skin.pending.clear();
                if (skin.retryTask != null) {
                    skin.retryTask.cancel();
                }
            }
            CACHE.clear();
        }
    }

    /**
     * Get a skin for a skinnable entity.
     *
     * <p>
     * If a Skin instance does not exist, a new one is created and the skin data is automatically fetched.
     * </p>
     *
     * @param entity
     *            The skinnable entity.
     */
    public static Skin get(SkinnableEntity entity) {
        return get(entity, false);
    }

    /**
     * Get a skin for a skinnable entity.
     *
     * <p>
     * If a Skin instance does not exist, a new one is created and the skin data is automatically fetched.
     * </p>
     *
     * @param entity
     *            The skinnable entity.
     * @param forceUpdate
     *            if the skin should be checked via the cache
     */
    public static Skin get(SkinnableEntity entity, boolean forceUpdate) {
        Preconditions.checkNotNull(entity);

        String skinName = entity.getSkinName().toLowerCase();
        return get(skinName, forceUpdate);
    }

    /**
     * Get a player skin.
     *
     * <p>
     * If a Skin instance does not exist, a new one is created and the skin data is automatically fetched.
     * </p>
     *
     * @param skinName
     *            The name of the skin.
     */
    public static Skin get(String skinName, boolean forceUpdate) {
        Preconditions.checkNotNull(skinName);

        skinName = skinName.toLowerCase();

        Skin skin;
        synchronized (CACHE) {
            skin = CACHE.get(skinName);
        }
        if (skin == null) {
            skin = new Skin(skinName);
        } else if (forceUpdate) {
            skin.fetchForced();
        }
        return skin;
    }

    private static void setNPCSkinData(SkinnableEntity entity, String skinName, UUID skinId,
            SkinProperty skinProperty) {
        NPC npc = entity.getNPC();
        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);

        // cache skins for faster initial skin availability and
        // for use when the latest skin is not required.
        npc.data().setPersistent(CACHED_SKIN_UUID_NAME_METADATA, skinName);
        npc.data().setPersistent(CACHED_SKIN_UUID_METADATA, skinId.toString());
        if (skinProperty.value != null) {
            skinTrait.setTexture(skinProperty.value, skinProperty.signature == null ? "" : skinProperty.signature);
            setNPCTexture(entity, skinProperty);
        } else {
            skinTrait.clearTexture();
        }
    }

    private static void setNPCTexture(SkinnableEntity entity, SkinProperty skinProperty) {
        GameProfile profile = entity.getProfile();

        // don't set property if already set since this sometimes causes
        // packet errors that disconnect the client.
        SkinProperty current = SkinProperty.fromMojangProfile(profile);
        if (current != null && current.value.equals(skinProperty.value) && current.signature != null
                && current.signature.equals(skinProperty.signature))
            return;

        skinProperty.apply(profile);
    }

    private static Map<String, Skin> CACHE = new HashMap<>(20);
    public static String CACHED_SKIN_UUID_METADATA = "cached-skin-uuid";
    public static String CACHED_SKIN_UUID_NAME_METADATA = "cached-skin-uuid-name";
}
