package net.citizensnpcs.npc.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.profile.ProfileFetchHandler;
import net.citizensnpcs.npc.profile.ProfileFetcher;
import net.citizensnpcs.npc.profile.ProfileRequest;

/**
 * Stores data for a single skin.
 */
public class Skin {
    private boolean fetching;
    private int fetchRetries = -1;
    private boolean hasFetched;
    private volatile boolean isValid = true;
    private final Map<SkinnableEntity, Void> pending = new WeakHashMap<SkinnableEntity, Void>(15);
    private BukkitTask retryTask;
    private volatile Property skinData;
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

        // Use npc cached skin if available.
        // If npc requires latest skin, cache is used for faster
        // availability until the latest skin can be loaded.
        String cachedName = npc.data().get(CACHED_SKIN_UUID_NAME_METADATA);
        String texture = npc.data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, "cache");
        if (this.skinName.equals(cachedName) && !texture.equals("cache")) {
            Property localData = new Property("textures", texture,
                    npc.data().<String> get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA));
            setNPCTexture(entity, localData);

            // check if NPC prefers to use cached skin over the latest skin.
            if (!entity.getNPC().data().get(NPC.PLAYER_SKIN_USE_LATEST, Setting.NPC_SKIN_USE_LATEST.asBoolean())) {
                // cache preferred
                return true;
            }
        }

        if (!hasSkinData()) {
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

        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation());
        }
    }

    private void fetch() {
        final int maxRetries = Setting.MAX_NPC_SKIN_RETRIES.asInt();
        if (maxRetries > -1 && fetchRetries >= maxRetries) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Reached max skin fetch retries for '" + skinName + "'");
            }
            return;
        }
        fetching = true;

        ProfileFetcher.fetch(this.skinName, new ProfileFetchHandler() {
            @Override
            public void onResult(ProfileRequest request) {
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
                        long delay = Setting.NPC_SKIN_RETRY_DELAY.asLong();
                        retryTask = Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                fetch();
                            }
                        }, delay);

                        if (Messaging.isDebugging()) {
                            Messaging.debug("Retrying skin fetch for '" + skinName + "' in " + delay + " ticks.");
                        }
                        break;
                    case SUCCESS:
                        GameProfile profile = request.getProfile();
                        setData(profile);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void fetchForced() {
        final int maxRetries = Setting.MAX_NPC_SKIN_RETRIES.asInt();
        if (maxRetries > -1 && fetchRetries >= maxRetries) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Reached max skin fetch retries for '" + skinName + "'");
            }
            return;
        }
        fetching = true;

        ProfileFetcher.fetchForced(this.skinName, new ProfileFetchHandler() {
            @Override
            public void onResult(ProfileRequest request) {
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
                        long delay = Setting.NPC_SKIN_RETRY_DELAY.asLong();
                        retryTask = Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                fetchForced();
                            }
                        }, delay);

                        if (Messaging.isDebugging()) {
                            Messaging.debug("Retrying skin fetch for '" + skinName + "' in " + delay + " ticks.");
                        }
                        break;
                    case SUCCESS:
                        GameProfile profile = request.getProfile();
                        setData(profile);
                        break;
                    default:
                        break;
                }
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
            throw new IllegalArgumentException(
                    "GameProfile name (" + profile.getName() + ") and " + "skin name (" + skinName + ") do not match.");
        }

        skinId = profile.getId();
        skinData = Iterables.getFirst(profile.getProperties().get("textures"), null);

        List<SkinnableEntity> entities = new ArrayList<SkinnableEntity>(pending.keySet());
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

    private static void setNPCSkinData(SkinnableEntity entity, String skinName, UUID skinId, Property skinProperty) {
        NPC npc = entity.getNPC();

        // cache skins for faster initial skin availability and
        // for use when the latest skin is not required.
        npc.data().setPersistent(CACHED_SKIN_UUID_NAME_METADATA, skinName);
        npc.data().setPersistent(CACHED_SKIN_UUID_METADATA, skinId.toString());
        if (skinProperty.getValue() != null) {
            npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, skinProperty.getValue());
            if (skinProperty.getSignature() == null) {
                npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, "");
            } else {
                npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, skinProperty.getSignature());
            }
            setNPCTexture(entity, skinProperty);
        } else {
            npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
            npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
        }
    }

    private static void setNPCTexture(SkinnableEntity entity, Property skinProperty) {
        GameProfile profile = entity.getProfile();

        // don't set property if already set since this sometimes causes
        // packet errors that disconnect the client.
        Property current = Iterables.getFirst(profile.getProperties().get("textures"), null);
        if (current != null && current.getValue().equals(skinProperty.getValue())
                && (current.getSignature() != null && current.getSignature().equals(skinProperty.getSignature()))) {
            return;
        }

        profile.getProperties().removeAll("textures"); // ensure client does not crash due to duplicate properties.
        profile.getProperties().put("textures", skinProperty);
    }

    private static final Map<String, Skin> CACHE = new HashMap<String, Skin>(20);
    public static final String CACHED_SKIN_UUID_METADATA = "cached-skin-uuid";
    public static final String CACHED_SKIN_UUID_NAME_METADATA = "cached-skin-uuid-name";
}
