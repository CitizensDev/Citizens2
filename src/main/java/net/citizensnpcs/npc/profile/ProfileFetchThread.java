package net.citizensnpcs.npc.profile;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.Bukkit;

/**
 * Thread used to fetch profiles from the Mojang servers.
 *
 * <p>Maintains a cache of profiles so that no profile is ever requested more than once
 * during a single server session.</p>
 */
public class ProfileFetchThread implements Runnable {

    private final ProfileFetcher profileFetcher = new ProfileFetcher();
    private final Deque<ProfileRequest> queue = new LinkedList<ProfileRequest>();
    private final Map<String, ProfileRequest> requested = new HashMap<String, ProfileRequest>(35);
    private final Object sync = new Object();

    /**
     * Get the singleton instance.
     */
    public static ProfileFetchThread get() {
        if (PROFILE_THREAD == null) {
            PROFILE_THREAD = new ProfileFetchThread();
            Bukkit.getScheduler().runTaskTimerAsynchronously(CitizensAPI.getPlugin(), PROFILE_THREAD,
                    11, 20);
        }
        return PROFILE_THREAD;
    }

    ProfileFetchThread() {}

    /**
     * Fetch a profile.
     *
     * @param name        The name of the player the profile belongs to.
     * @param subscriber  Optional subscriber to be notified when a result is available.
     *                    Subscriber always invoked from the main thread.
     */
    public void fetch(String name, @Nullable ProfileFetchSubscriber subscriber) {
        Preconditions.checkNotNull(name);

        ProfileRequest request = requested.get(name);

        if (request != null) {

            if (subscriber != null) {

                if (request.getResult() == ProfileFetchResult.PENDING) {
                    request.addSubscriber(subscriber);
                }
                else {
                    subscriber.onResult(request);
                }
            }

            return;
        }

        request = new ProfileRequest(name, subscriber);

        synchronized (sync) {
            queue.add(request);
        }

        requested.put(name, request);

    }

    @Override
    public void run() {

        List<ProfileRequest> requests;

        synchronized (sync) {

            if (queue.isEmpty())
                return;

            requests = new ArrayList<ProfileRequest>(queue);
            queue.clear();
        }

        profileFetcher.fetch(requests);
    }

    private static ProfileFetchThread PROFILE_THREAD;
}
