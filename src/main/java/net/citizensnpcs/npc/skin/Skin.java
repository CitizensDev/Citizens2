package net.citizensnpcs.npc.skin;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.util.UUIDTypeAdapter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Stores data for a single skin
class Skin {

    private final String skinName;
    private final MinecraftSessionService repo;
    private Property skinProperty;
    private UUID skinUUID;

    Skin(String skinName, MinecraftSessionService repo) {
        this.skinName = skinName.toLowerCase();
        this.repo = repo;

        synchronized (CACHE) {
            if (CACHE.containsKey(skinName))
                throw new IllegalArgumentException("There is already a skin named " + skinName);

            CACHE.put(skinName, this);
        }
    }

    public String getSkinName() {
        return skinName;
    }

    public boolean hasSkinData() {
        return skinProperty != null;
    }

    public boolean fillProfile(final NPC npc, GameProfile profile) {

        if (skinProperty == null)
            return false;

        setNPCMeta(npc);

        profile.getProperties().put("textures", getSkinProperty());

        return true;
    }

    public void fetchAndRespawn(final NPC npc, final SkinThread thread,
                                @Nullable final SkinFetchCallback callback) {

        if (skinProperty != null) {

            respawnNPC(npc);
            callbackResult(callback, FetchResult.SUCCESS);
            return;
        }

        new UUIDFetcher(skinName, npc).fetchUUID(new UUIDFetcher.UUIDFetcherCallback() {

            @Override
            public void onFetch(@Nullable String skinUUIDString) {

                if (skinUUIDString == null) {
                    callbackResult(callback, FetchResult.INVALID_SKIN);
                    return;
                }

                GameProfile skinProfile;
                skinUUID = UUID.fromString(skinUUIDString);

                try {

                    skinProfile = fillProfileProperties(
                            ((YggdrasilMinecraftSessionService) repo).getAuthenticationService(),
                            new GameProfile(skinUUID, ""), true);

                } catch (Exception e) {
                    e.printStackTrace();

                    if (isTooManyRequests(e)) {
                        thread.delay();
                        callbackResult(callback, FetchResult.TOO_MANY_REQUESTS);
                    } else {
                        callbackResult(callback, FetchResult.FAILED);
                    }

                    return;
                }

                if (skinProfile == null || !skinProfile.getProperties().containsKey("textures")) {
                    callbackResult(callback, FetchResult.FAILED);
                    return;
                }

                skinProperty = Iterables.getFirst(skinProfile.getProperties().get("textures"), null);
                if (skinProperty == null || skinProperty.getValue() == null || skinProperty.getSignature() == null) {
                    callbackResult(callback, FetchResult.FAILED);
                    return;
                }

                if (npc.data().has(NPCSkin.PLAYER_SKIN_TEXTURE_PROPERTIES)
                        && npc.data().get(NPCSkin.PLAYER_SKIN_TEXTURE_PROPERTIES).equals("cache")) {

                    respawnNPC(npc);
                }

                if (Messaging.isDebugging()) {

                    Messaging.debug("Fetched skin texture for UUID " + skinUUIDString +
                            " for NPC " + npc.getName() +
                            " UUID " + npc.getUniqueId());
                }

                callbackResult(callback, FetchResult.SUCCESS);
            }
        });
    }

    private Property getSkinProperty() {
        return new Property(skinProperty.getName(), skinProperty.getValue(), skinProperty.getSignature());
    }

    private void scheduleRespawnNPC(final NPC npc) {

        if (!CitizensAPI.getPlugin().isEnabled())
            return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {

                respawnNPC(npc);
            }
        });
    }

    private void respawnNPC(NPC npc) {

        if (!Bukkit.isPrimaryThread()) {
            scheduleRespawnNPC(npc);
            return;
        }

        setNPCMeta(npc);

        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation());
        }
    }

    private void setNPCMeta(NPC npc) {
        npc.data().setPersistent(NPCSkin.CACHED_SKIN_UUID_NAME_METADATA, skinName);
        npc.data().setPersistent(NPCSkin.CACHED_SKIN_UUID_METADATA, skinUUID.toString());
        npc.data().setPersistent(NPCSkin.PLAYER_SKIN_TEXTURE_PROPERTIES, skinProperty.getValue());
        npc.data().setPersistent(NPCSkin.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN, skinProperty.getSignature());
    }

    /*
     * Yggdrasil's default implementation of this method silently fails instead of throwing an Exception like it should.
     */
    private GameProfile fillProfileProperties(YggdrasilAuthenticationService auth, GameProfile profile,
                                              boolean requireSecure) throws Exception {

        URL url = HttpAuthenticationService.constantURL(
                "https://sessionserver.mojang.com/session/minecraft/profile/" +
                        UUIDTypeAdapter.fromUUID(profile.getId()));

        url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);

        MinecraftProfilePropertiesResponse response = null;

        try {
            response = (MinecraftProfilePropertiesResponse)
                    MAKE_REQUEST.invoke(auth, url, null, MinecraftProfilePropertiesResponse.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (response == null)
            return profile;

        GameProfile result = new GameProfile(response.getId(), response.getName());
        result.getProperties().putAll(response.getProperties());
        profile.getProperties().putAll(response.getProperties());

        return result;
    }

    private static void callbackResult(@Nullable SkinFetchCallback callback, FetchResult result) {
        if (callback != null) {
            callback.onFetch(result);
        }
    }

    private static boolean isTooManyRequests(Exception e) {

        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;

        return (message != null && message.contains("too many requests"))
                || (cause != null && cause.contains("too many requests"));
    }

    static Skin getFromCache(String skinName) {
        skinName = skinName.toLowerCase();

        synchronized (CACHE) {
            return CACHE.get(skinName);
        }
    }

    public interface SkinFetchCallback {
        void onFetch(FetchResult result);
    }

    public enum FetchResult {
        SUCCESS,
        FAILED,
        TOO_MANY_REQUESTS,
        INVALID_SKIN
    }

    private static final Map<String, Skin> CACHE = new HashMap<String, Skin>(20);
    private static Method MAKE_REQUEST;

    static {
        try {
            MAKE_REQUEST = YggdrasilAuthenticationService.class.getDeclaredMethod("makeRequest", URL.class,
                    Object.class, Class.class);
            MAKE_REQUEST.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
