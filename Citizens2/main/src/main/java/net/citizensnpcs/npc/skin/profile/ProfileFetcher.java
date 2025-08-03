package net.citizensnpcs.npc.skin.profile;

import java.util.Objects;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import net.citizensnpcs.api.CitizensAPI;

/**
 * Fetches game profiles that include skin data from Mojang servers.
 *
 * @see ProfileFetchThread
 */
public class ProfileFetcher {
    ProfileFetcher() {
    }

    /**
     * Fetch a profile.
     *
     * @param name
     *            The name of the player the profile belongs to.
     * @param handler
     *            Optional handler to handle the result. Handler always invoked from the main thread.
     */
    public static void fetch(String name, Consumer<ProfileRequest> handler) {
        Objects.requireNonNull(name);

        if (PROFILE_THREAD == null) {
            initThread();
        }
        PROFILE_THREAD.fetch(name, handler);
    }

    public static void fetchForced(String name, Consumer<ProfileRequest> handler) {
        Objects.requireNonNull(name);

        if (PROFILE_THREAD == null) {
            initThread();
        }
        PROFILE_THREAD.fetchForced(name, handler);
    }

    private static void initThread() {
        if (THREAD_TASK != null) {
            THREAD_TASK.cancel();
        }
        PROFILE_THREAD = new ProfileFetchThread();
        THREAD_TASK = Bukkit.getScheduler().runTaskTimerAsynchronously(CitizensAPI.getPlugin(), PROFILE_THREAD, 21, 20);
    }

    /**
     * Clear all queued and cached requests.
     */
    public static void reset() {
        initThread();
    }

    public static void shutdown() {
        if (THREAD_TASK != null) {
            THREAD_TASK.cancel();
            THREAD_TASK = null;
        }
    }

    private static ProfileFetchThread PROFILE_THREAD;
    private static BukkitTask THREAD_TASK;
}
