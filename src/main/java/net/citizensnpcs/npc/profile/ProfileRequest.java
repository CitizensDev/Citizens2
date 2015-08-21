package net.citizensnpcs.npc.profile;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Stores basic information about a single profile used to request
 * profiles from the Mojang servers.
 *
 * <p>Also stores the result of the request.</p>
 */
public class ProfileRequest {

    private final String playerName;
    private Deque<ProfileFetchSubscriber> subscribers;
    private GameProfile profile;
    private ProfileFetchResult result = ProfileFetchResult.PENDING;

    /**
     * Constructor.
     *
     * @param playerName  The name of the player whose profile is being requested.
     * @param subscriber  Optional subscriber to be notified when a result is available
     *                    for the profile. Subscriber always invoked from the main thread.
     */
    ProfileRequest(String playerName, @Nullable ProfileFetchSubscriber subscriber) {
        Preconditions.checkNotNull(playerName);

        this.playerName = playerName;

        if (subscriber != null)
            addSubscriber(subscriber);
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
     * @return  The game profile or null if the profile has not been retrieved
     * yet or there was an error while retrieving the profile.
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

    /**
     * Add a result subscriber to be notified when a result is available.
     *
     * <p>Subscriber is always invoked from the main thread.</p>
     *
     * @param subscriber  The subscriber.
     */
    public void addSubscriber(ProfileFetchSubscriber subscriber) {
        Preconditions.checkNotNull(subscriber);

        if (subscribers == null)
            subscribers = new ArrayDeque<ProfileFetchSubscriber>();

        subscribers.addLast(subscriber);
    }

    /**
     * Invoked to set the profile result.
     *
     * <p>Can be invoked from any thread, always executes on the main thread.</p>
     *
     * @param profile  The profile. Null if there was an error.
     * @param result   The result of the request.
     */
    void setResult(final @Nullable GameProfile profile, final ProfileFetchResult result) {

        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {

                ProfileRequest.this.profile = profile;
                ProfileRequest.this.result = result;

                if (subscribers == null)
                    return;

                while (!subscribers.isEmpty()) {
                    subscribers.removeFirst().onResult(ProfileRequest.this);
                }

                subscribers = null;
            }
        });
    }
}
