package net.citizensnpcs.npc.profile;

import java.util.Collection;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.NMS;

/**
 * Fetches game profiles that include skin data from Mojang servers.
 *
 * @see ProfileFetchThread
 */
public class ProfileFetcher {
    ProfileFetcher() {
    }

    /**
     * Fetch one or more profiles.
     *
     * @param requests
     *            The profile requests.
     */
    void fetchRequests(final Collection<ProfileRequest> requests) {
        Preconditions.checkNotNull(requests);

        final GameProfileRepository repo = NMS.getGameProfileRepository();

        String[] playerNames = new String[requests.size()];

        int i = 0;
        for (ProfileRequest request : requests) {
            playerNames[i++] = request.getPlayerName();
        }

        repo.findProfilesByNames(playerNames, Agent.MINECRAFT, new ProfileLookupCallback() {
            @Override
            public void onProfileLookupFailed(GameProfile profile, Exception e) {
                if (Messaging.isDebugging()) {
                    Messaging.debug(
                            "Profile lookup for player '" + profile.getName() + "' failed2: " + getExceptionMsg(e));
                    Messaging.debug(Throwables.getStackTraceAsString(e));
                }

                ProfileRequest request = findRequest(profile.getName(), requests);
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
            public void onProfileLookupSucceeded(final GameProfile profile) {
                if (Messaging.isDebugging()) {
                    Messaging.debug("Fetched profile " + profile.getId() + " for player " + profile.getName());
                }

                ProfileRequest request = findRequest(profile.getName(), requests);
                if (request == null)
                    return;

                try {
                    request.setResult(NMS.fillProfileProperties(profile, true), ProfileFetchResult.SUCCESS);
                } catch (Exception e) {
                    if (Messaging.isDebugging()) {
                        Messaging.debug("Profile lookup for player '" + profile.getName() + "' failed: "
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

    /**
     * Fetch a profile.
     *
     * @param name
     *            The name of the player the profile belongs to.
     * @param handler
     *            Optional handler to handle the result. Handler always invoked from the main thread.
     */
    public static void fetch(String name, @Nullable ProfileFetchHandler handler) {
        Preconditions.checkNotNull(name);

        if (PROFILE_THREAD == null) {
            initThread();
        }
        PROFILE_THREAD.fetch(name, handler);
    }

    public static void fetchForced(String name, ProfileFetchHandler handler) {
        Preconditions.checkNotNull(name);

        if (PROFILE_THREAD == null) {
            initThread();
        }
        PROFILE_THREAD.fetchForced(name, handler);
    }

    @Nullable
    private static ProfileRequest findRequest(String name, Collection<ProfileRequest> requests) {
        name = name.toLowerCase();

        for (ProfileRequest request : requests) {
            if (request.getPlayerName().equals(name)) {
                return request;
            }
        }
        return null;
    }

    private static String getExceptionMsg(Exception e) {
        return Throwables.getRootCause(e).getMessage();
    }

    private static void initThread() {
        if (THREAD_TASK != null) {
            THREAD_TASK.cancel();
        }

        PROFILE_THREAD = new ProfileFetchThread();
        THREAD_TASK = Bukkit.getScheduler().runTaskTimerAsynchronously(CitizensAPI.getPlugin(), PROFILE_THREAD, 21, 20);
    }

    private static boolean isProfileNotFound(Exception e) {
        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;

        return (message != null && message.contains("did not find"))
                || (cause != null && cause.contains("did not find"));
    }

    private static boolean isTooManyRequests(Exception e) {

        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;

        return (message != null && message.contains("too many requests"))
                || (cause != null && cause.contains("too many requests"));
    }

    /**
     * Clear all queued and cached requests.
     */
    public static void reset() {
        initThread();
    }

    private static ProfileFetchThread PROFILE_THREAD;
    private static BukkitTask THREAD_TASK;
}
