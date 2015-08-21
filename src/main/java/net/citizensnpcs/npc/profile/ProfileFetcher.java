package net.citizensnpcs.npc.profile;

import com.google.common.base.Preconditions;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.NMS;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Fetches game profiles that include skin data from Mojang servers.
 *
 * @see ProfileFetchThread
 */
class ProfileFetcher {

    /**
     * Fetch one or more profiles.
     *
     * @param requests  The profile requests.
     */
    public void fetch(final Collection<ProfileRequest> requests) {
        Preconditions.checkNotNull(requests);

        final GameProfileRepository repo = NMS.getGameProfileRepository();

        String[] playerNames = new String[requests.size()];

        int i=0;
        for (ProfileRequest request : requests) {
            playerNames[i] = request.getPlayerName();
            i++;
        }

        repo.findProfilesByNames(playerNames, Agent.MINECRAFT,
                new ProfileLookupCallback() {

                    @Override
                    public void onProfileLookupFailed(GameProfile profile, Exception e) {

                        if (Messaging.isDebugging()) {
                            Messaging.debug("Profile lookup for skin '" +
                                    profile.getName() + "' failed: " + getExceptionMsg(e));
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
                            Messaging.debug("Fetched profile " + profile.getId()
                                    + " for player " + profile.getName());
                        }

                        ProfileRequest request = findRequest(profile.getName(), requests);
                        if (request == null)
                            return;

                        try {
                            request.setResult(NMS.fillProfileProperties(profile, true), ProfileFetchResult.SUCCESS);
                        } catch (Exception e) {

                            if (Messaging.isDebugging()) {
                                Messaging.debug("Profile lookup for skin '" +
                                        profile.getName() + "' failed: " + getExceptionMsg(e));
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

    @Nullable
    private static ProfileRequest findRequest(String name, Collection<ProfileRequest> requests) {

        name = name.toLowerCase();

        for (ProfileRequest request : requests) {
            if (request.getPlayerName().equals(name))
                return request;
        }
        return null;
    }

    private static boolean isProfileNotFound(Exception e) {
        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;

        return (message != null && message.contains("did not find"))
                || (cause != null && cause.contains("did not find"));
    }

    private static String getExceptionMsg(Exception e) {
        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;
        return cause != null ? cause : message;
    }

    private static boolean isTooManyRequests(Exception e) {

        String message = e.getMessage();
        String cause = e.getCause() != null ? e.getCause().getMessage() : null;

        return (message != null && message.contains("too many requests"))
                || (cause != null && cause.contains("too many requests"));
    }
}
