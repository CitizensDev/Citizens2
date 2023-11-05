package net.citizensnpcs.npc.profile;

/**
 * The result status of a profile fetch.
 */
public enum ProfileFetchResult {
    /**
     * The profile request failed for unknown reasons.
     */
    FAILED,
    /**
     * The profile request failed because the profile was not found.
     */
    NOT_FOUND,
    /**
     * The profile has not been fetched yet.
     */
    PENDING,
    /**
     * The profile was successfully fetched.
     */
    SUCCESS,
    /**
     * The profile request failed because too many requests were sent.
     */
    TOO_MANY_REQUESTS
}
