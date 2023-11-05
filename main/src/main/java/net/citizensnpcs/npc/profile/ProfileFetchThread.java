package net.citizensnpcs.npc.profile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.NMS;

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
    private final Deque<ProfileRequest> queue = new ArrayDeque<>();
    private final Map<String, ProfileRequest> requested = new HashMap<>(40);
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

    /**
     * Fetch one or more profiles.
     *
     * @param requests
     *            The profile requests.
     */
    private void fetchRequests(Collection<ProfileRequest> requests) {
        Preconditions.checkNotNull(requests);

        String[] playerNames = new String[requests.size()];

        int i = 0;
        for (ProfileRequest request : requests) {
            playerNames[i++] = request.getPlayerName();
        }
        NMS.findProfilesByNames(playerNames, new ProfileLookupCallback() {
            @SuppressWarnings("unused")
            public void onProfileLookupFailed(GameProfile profile, Exception e) {
                onProfileLookupFailed(profile.getName(), e);
            }

            @Override
            public void onProfileLookupFailed(String profileName, Exception e) {
                if (Messaging.isDebugging()) {
                    Messaging.debug("Profile lookup for player '" + profileName + "' failed: " + getExceptionMsg(e));
                    Messaging.debug(Throwables.getStackTraceAsString(e));
                }
                ProfileRequest request = findRequest(profileName, requests);
                if (request == null)
                    return;

                if (isProfileNotFound(e)) {
                    request.setResult(null, ProfileFetchResult.NOT_FOUND);
                } else if (isTooManyRequests(e)) {
                    request.setResult(null, ProfileFetchResult.TOO_MANY_REQUESTS);
                } else {
                    request.setResult(null, ProfileFetchResult.FAILED);
                }
            }

            @Override
            public void onProfileLookupSucceeded(GameProfile profile) {
                Messaging.idebug(() -> "Fetched profile " + profile.getId() + " for player " + profile.getName());

                ProfileRequest request = findRequest(profile.getName(), requests);
                if (request == null)
                    return;

                try {
                    request.setResult(NMS.fillProfileProperties(profile, true), ProfileFetchResult.SUCCESS);
                } catch (Throwable e) {
                    if (Messaging.isDebugging()) {
                        Messaging.debug("Filling profile lookup for player '" + profile.getName() + "' failed: "
                                + getExceptionMsg(e) + " " + isTooManyRequests(e));
                        Messaging.debug(Throwables.getStackTraceAsString(e));
                    }
                    if (isTooManyRequests(e)) {
                        request.setResult(null, ProfileFetchResult.TOO_MANY_REQUESTS);
                    } else {
                        request.setResult(null, ProfileFetchResult.FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void run() {
        List<ProfileRequest> requests;

        synchronized (sync) {
            if (queue.isEmpty())
                return;

            requests = new ArrayList<>(queue);
            queue.clear();
        }
        try {
            fetchRequests(requests);
        } catch (Exception ex) {
            Messaging.severe("Error fetching skins: " + ex.getMessage());
            for (ProfileRequest req : requests) {
                req.setResult(null, ProfileFetchResult.FAILED);
            }
        }
    }

    private static void addHandler(ProfileRequest request, ProfileFetchHandler handler) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> request.addHandler(handler), 1);
    }

    @Nullable
    private static ProfileRequest findRequest(String name, Collection<ProfileRequest> requests) {
        name = name.toLowerCase();

        for (ProfileRequest request : requests) {
            if (request.getPlayerName().equals(name))
                return request;
        }
        return null;
    }

    private static String getExceptionMsg(Throwable e) {
        return Throwables.getRootCause(e).getMessage();
    }

    private static boolean isProfileNotFound(Exception e) {
        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;

        return message != null && message.contains("did not find") || cause != null && cause.contains("did not find");
    }

    private static boolean isTooManyRequests(Throwable e) {
        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;

        return message != null && message.contains("too many requests")
                || cause != null && cause.contains("too many requests");
    }

    private static void sendResult(ProfileFetchHandler handler, ProfileRequest request) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> handler.onResult(request), 1);
    }
}
