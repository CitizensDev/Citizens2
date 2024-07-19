package net.citizensnpcs.npc.skin.profile;

/**
 * Interface for a subscriber of the results of a profile fetch.
 */
public interface ProfileFetchHandler {
    /**
     * Invoked when a result for a profile is ready.
     *
     * @param request
     *            The profile request that was handled.
     */
    void onResult(ProfileRequest request);
}
