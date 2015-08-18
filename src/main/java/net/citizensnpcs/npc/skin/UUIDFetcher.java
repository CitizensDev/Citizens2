package net.citizensnpcs.npc.skin;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import javax.annotation.Nullable;

class UUIDFetcher {

    private final NPC npc;
    private String skinName;

    UUIDFetcher(String skinName, NPC npc) {
        this.skinName = skinName;
        this.npc = npc;
    }

    public void fetchUUID(final UUIDFetcherCallback callback) {

        if (skinName.contains("-")) {
            callback.onFetch(skinName);
            return;
        }

        final GameProfileRepository repo = ((CraftServer) Bukkit.getServer()).getServer()
                .getGameProfileRepository();

        repo.findProfilesByNames(new String[]{ChatColor.stripColor(skinName)}, Agent.MINECRAFT,
                new ProfileLookupCallback() {

                    @Override
                    public void onProfileLookupFailed(GameProfile profile, Exception e) {

                        if (Messaging.isDebugging()) {
                            Messaging.debug("Profile lookup for skin '" +
                                    skinName + "' failed: " + getExceptionMsg(e));
                        }

                        if (isProfileNotFound(e)) {
                            npc.data().set(NPCSkin.PLAYER_SKIN_INVALID, true);
                            callback.onFetch(null);
                        }
                    }

                    @Override
                    public void onProfileLookupSucceeded(final GameProfile profile) {

                        if (Messaging.isDebugging()) {
                            Messaging.debug("Fetched UUID " + profile.getId() + " for NPC " + npc.getName()
                                    + " UUID " + npc.getUniqueId());
                        }

                        npc.data().setPersistent(NPCSkin.CACHED_SKIN_UUID_METADATA, profile.getId().toString());
                        npc.data().setPersistent(NPCSkin.CACHED_SKIN_UUID_NAME_METADATA, profile.getName());

                        callback.onFetch(profile.getId().toString());
                    }
                });
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

    public interface UUIDFetcherCallback {
        void onFetch(@Nullable String skinUUID);
    }
}
