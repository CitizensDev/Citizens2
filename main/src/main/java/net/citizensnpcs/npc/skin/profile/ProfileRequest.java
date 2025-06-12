package net.citizensnpcs.npc.skin.profile;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.api.CitizensAPI;

/**
 * Stores basic information about a single profile used to request profiles from the Mojang servers.
 *
 * <p>
 * Also stores the result of the request.
 * </p>
 */
public class ProfileRequest {
    private Deque<Consumer<ProfileRequest>> handlers;
    private final String playerName;
    private GameProfile profile;
    private volatile ProfileFetchResult result = ProfileFetchResult.PENDING;

    /**
     * Constructor.
     *
     * @param playerName
     *            The name of the player whose profile is being requested.
     * @param handler
     *            Optional handler to handle the result for the profile. Handler always invoked from the main thread.
     */

    public ProfileRequest(String playerName, Consumer<ProfileRequest> handler) {
        Objects.requireNonNull(playerName);

        this.playerName = playerName;

        if (handler != null) {
            addHandler(handler);
        }
    }

    /**
     * Add one time result handler.
     *
     * <p>
     * Handler is always invoked from the main thread.
     * </p>
     *
     * @param handler
     *            The result handler.
     */
    public void addHandler(Consumer<ProfileRequest> handler) {
        Objects.requireNonNull(handler);

        if (result != ProfileFetchResult.PENDING) {
            handler.accept(this);
            return;
        }
        if (handlers == null) {
            handlers = new ArrayDeque<>();
        }
        handlers.addLast(handler);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProfileRequest other = (ProfileRequest) obj;
        if (playerName == null) {
            if (other.playerName != null) {
                return false;
            }
        } else if (!playerName.equals(other.playerName)) {
            return false;
        }
        return true;
    }

    /**
     * Get the name of the player the requested profile belongs to.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Get the game profile that was requested.
     *
     * @return The game profile or null if the profile has not been retrieved yet or there was an error while retrieving
     *         the profile.
     */
    @Nullable
    public GameProfile getProfile() {
        return profile;
    }

    /**
     * Get the result of the profile fetch.
     */
    public ProfileFetchResult getResult() {
        return result;
    }

    @Override
    public int hashCode() {
        return 31 + ((playerName == null) ? 0 : playerName.hashCode());
    }

    /**
     * Invoked to set the profile result.
     *
     * <p>
     * Can be invoked from any thread, always executes on the main thread.
     * </p>
     *
     * @param profile
     *            The profile. Null if there was an error.
     * @param result
     *            The result of the request.
     */
    void setResult(@Nullable GameProfile profile, ProfileFetchResult result) {
        if (!CitizensAPI.hasImplementation())
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            ProfileRequest.this.profile = profile;
            ProfileRequest.this.result = result;

            if (handlers == null)
                return;

            while (!handlers.isEmpty()) {
                handlers.removeFirst().accept(ProfileRequest.this);
            }
            handlers = null;
        });
    }
}
