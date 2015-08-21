package net.citizensnpcs.npc.skin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.profile.ProfileFetchResult;
import net.citizensnpcs.npc.profile.ProfileFetchSubscriber;
import net.citizensnpcs.npc.profile.ProfileFetchThread;
import net.citizensnpcs.npc.profile.ProfileRequest;

/**
 * Stores data for a single skin.
 */
public class Skin {

    private final String skinName;
    private volatile Property skinData;
    private volatile UUID skinId;
    private volatile boolean isValid = true;
    private final Map<SkinnableEntity, Void> pending = new WeakHashMap<SkinnableEntity, Void>(30);

    /**
     * Get a skin for a human NPC entity.
     *
     * <p>If a Skin instance does not exist, a new one is created and the
     * skin data is automatically fetched.</p>
     *
     * @param entity  The human NPC entity.
     */
    public static Skin get(SkinnableEntity entity) {
        Preconditions.checkNotNull(entity);

        String skinName = entity.getSkinName().toLowerCase();

        Skin skin;
        synchronized (CACHE) {
            skin = CACHE.get(skinName);
        }

        if (skin == null) {
            skin = new Skin(skinName);
        }

        return skin;
    }

    /**
     * Constructor.
     *
     * @param skinName  The name of the player the skin belongs to.
     */
    Skin(String skinName) {

        this.skinName = skinName.toLowerCase();

        synchronized (CACHE) {
            if (CACHE.containsKey(skinName))
                throw new IllegalArgumentException("There is already a skin named " + skinName);

            CACHE.put(skinName, this);
        }

        ProfileFetchThread.get().fetch(skinName, new ProfileFetchSubscriber() {

            @Override
            public void onResult(ProfileRequest request) {

                if (request.getResult() == ProfileFetchResult.NOT_FOUND) {
                    isValid = false;
                    return;
                }

                if (request.getResult() == ProfileFetchResult.SUCCESS) {

                    GameProfile profile = request.getProfile();

                    skinId = profile.getId();
                    skinData = Iterables.getFirst(profile.getProperties().get("textures"), null);
                }
            }
        });
    }

    /**
     * Get the name of the skin.
     */
    public String getSkinName() {
        return skinName;
    }

    /**
     * Get the ID of the player the skin belongs to.
     *
     * @return  The skin ID or null if it has not been retrieved yet or
     * the skin is invalid.
     */
    @Nullable
    public UUID getSkinId() {
        return skinId;
    }

    /**
     * Determine if the skin is valid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Determine if the skin data has been retrieved.
     */
    public boolean hasSkinData() {
        return skinData != null;
    }

    /**
     * Set skin data.
     *
     * @param profile  The profile that contains the skin data. If set to null,
     *                 it's assumed that the skin is not valid.
     *
     * @throws IllegalStateException if not invoked from the main thread.
     * @throws IllegalArgumentException if the profile name does not match the skin data.
     */
    public void setData(@Nullable GameProfile profile) {

        if (profile == null) {
            isValid = false;
            return;
        }

        if (!profile.getName().toLowerCase().equals(skinName)) {
            throw new IllegalArgumentException(
                    "GameProfile name (" + profile.getName() + ") and "
                            + "skin name (" + skinName + ") do not match.");
        }

        skinId = profile.getId();
        skinData = Iterables.getFirst(profile.getProperties().get("textures"), null);

        for (SkinnableEntity entity : pending.keySet()) {
            applyAndRespawn(entity);
        }
    }

    /**
     * Apply the skin data to the specified human NPC entity.
     *
     * <p>If invoked before the skin data is ready, the skin is retrieved
     * and the skin is automatically applied to the entity at a later time.</p>
     *
     * @param entity  The human NPC entity.
     *
     * @return  True if the skin data was available and applied, false if
     * the data is being retrieved.
     *
     * @throws IllegalStateException if not invoked from the main thread.
     */
    public boolean apply(SkinnableEntity entity) {
        Preconditions.checkNotNull(entity);

        NPC npc = entity.getNPC();

        if (!hasSkinData()) {
            pending.put(entity, null);

            // Use npc cached skin if available.
            // If npc requires latest skin, cache is used for faster
            // availability until the latest skin can be loaded.
            String cachedName = npc.data().get(CACHED_SKIN_UUID_NAME_METADATA);
            if (this.skinName.equals(cachedName)) {

                skinData = new Property(this.skinName,
                        npc.data().<String>get(PLAYER_SKIN_TEXTURE_PROPERTIES),
                        npc.data().<String>get(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN));

                skinId = UUID.fromString(npc.data().<String>get(CACHED_SKIN_UUID_METADATA));

                setNPCSkinData(entity, skinName, skinId, skinData);

                // check if NPC prefers to use cached skin over the latest skin.
                if (!entity.getNPC().data().get("update-skin",
                        Settings.Setting.NPC_SKIN_UPDATE.asBoolean())) {
                    // cache preferred
                    return true;
                }

                if (!Settings.Setting.NPC_SKIN_UPDATE.asBoolean()) {
                    // cache preferred
                    return true;
                }
            }

            // get latest skin
            fetchSkinFor(entity);

            return false;
        }

        setNPCSkinData(entity, skinName, skinId, skinData);

        return true;
    }

    /**
     * Apply the skin data to the specified skinnable entity
     * and respawn the NPC.
     *
     * @param entity  The skinnable entity.
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

    private void fetchSkinFor(final SkinnableEntity entity) {

        ProfileFetchThread.get().fetch(skinName, new ProfileFetchSubscriber() {

            @Override
            public void onResult(ProfileRequest request) {

                if (request.getResult() != ProfileFetchResult.SUCCESS)
                    return;

                double viewDistance = Settings.Setting.NPC_SKIN_VIEW_DISTANCE.asDouble();
                entity.getSkinTracker().updateNearbyViewers(viewDistance);
            }
        });
    }

    private static void setNPCSkinData(SkinnableEntity entity,
                                       String skinName, UUID skinId, Property skinProperty) {

        NPC npc = entity.getNPC();

        // cache skins for faster initial skin availability
        npc.data().setPersistent(CACHED_SKIN_UUID_NAME_METADATA, skinName);
        npc.data().setPersistent(CACHED_SKIN_UUID_METADATA, skinId.toString());
        npc.data().setPersistent(PLAYER_SKIN_TEXTURE_PROPERTIES, skinProperty.getValue());
        npc.data().setPersistent(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN, skinProperty.getSignature());

        GameProfile profile = entity.getProfile();
        profile.getProperties().removeAll("textures"); // ensure client does not crash due to duplicate properties.
        profile.getProperties().put("textures", skinProperty);
    }

    public static final String PLAYER_SKIN_TEXTURE_PROPERTIES = "player-skin-textures";
    public static final String PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN = "player-skin-signature";
    public static final String CACHED_SKIN_UUID_METADATA = "cached-skin-uuid";
    public static final String CACHED_SKIN_UUID_NAME_METADATA = "cached-skin-uuid-name";

    private static final Map<String, Skin> CACHE = new HashMap<String, Skin>(20);
}
