package net.citizensnpcs.npc.profile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

import com.google.common.base.Preconditions;

import net.citizensnpcs.api.CitizensAPI;

/**
 * Thread used to fetch profiles from the Mojang servers.
 *
 * <p>
 * Maintains a cache of profiles so that no profile is ever requested more than once during a single server session.
 * </p>
 *
 * @see ProfileFetcher
 */
class ProfileFetchThread implements Runnable {
    private final ProfileFetcher profileFetcher = new ProfileFetcher();
    private final Deque<ProfileRequest> queue = new ArrayDeque<ProfileRequest>();
    private final Map<String, ProfileRequest> requested = new HashMap<String, ProfileRequest>(35);
    private final Object sync = new Object(); // sync for queue & requested fields

    ProfileFetchThread() {
    }

    /**
     * Fetch a profile.
     *
     * @param name
     *            The name of the player the profile belongs to.
     * @param handler
     *            Optional handler to handle result fetch result. Handler always invoked from the main thread.
     *
     * @see ProfileFetcher#fetch
     */
    void fetch(String name, @Nullable ProfileFetchHandler handler) {
        Preconditions.checkNotNull(name);

        name = name.toLowerCase();
        ProfileRequest request;

        synchronized (sync) {
            request = requested.get(name);
            if (request == null) {
                request = new ProfileRequest(name, handler);
                queue.add(request);
                requested.put(name, request);
                return;
            } else if (request.getResult() == ProfileFetchResult.TOO_MANY_REQUESTS) {
                queue.add(request);
            }
        }

        if (handler != null) {
            if (request.getResult() == ProfileFetchResult.PENDING
                    || request.getResult() == ProfileFetchResult.TOO_MANY_REQUESTS) {
                addHandler(request, handler);
            } else {
                sendResult(handler, request);
            }
        }
    }

    public void fetchForced(String name, ProfileFetchHandler handler) {
        Preconditions.checkNotNull(name);

        name = name.toLowerCase();
        ProfileRequest request;

        synchronized (sync) {
            request = requested.get(name);
            if (request != null) {
                if (request.getResult() == ProfileFetchResult.TOO_MANY_REQUESTS) {
                    queue.add(request);
                } else {
                    requested.remove(name);
                    queue.remove(request);
                    request = null;
                }
            }
            if (request == null) {
                request = new ProfileRequest(name, handler);
                queue.add(request);
                requested.put(name, request);
                return;
            }
        }

        if (handler != null) {
            if (request.getResult() == ProfileFetchResult.PENDING
                    || request.getResult() == ProfileFetchResult.TOO_MANY_REQUESTS) {
                addHandler(request, handler);
            } else {
                sendResult(handler, request);
            }
        }
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

        profileFetcher.fetchRequests(requests);
    }

    private static void addHandler(final ProfileRequest request, final ProfileFetchHandler handler) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                request.addHandler(handler);
            }
        }, 1);
    }

    private static void sendResult(final ProfileFetchHandler handler, final ProfileRequest request) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                handler.onResult(request);
            }
        }, 1);
    }
}
