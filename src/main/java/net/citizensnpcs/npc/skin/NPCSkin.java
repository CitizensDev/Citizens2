package net.citizensnpcs.npc.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;

// used to set NPC skin
public class NPCSkin {

    private final NPC npc;

    public NPCSkin(NPC npc) {
        this.npc = npc;

        if (SKIN_THREAD == null) {
            SKIN_THREAD = new SkinThread();
            Bukkit.getScheduler().runTaskTimerAsynchronously(CitizensAPI.getPlugin(), SKIN_THREAD,
                    11, 10);
        }
    }

    public NPC getNPC() {
        return npc;
    }

    public boolean setSkinFromCache(GameProfile profile) {

        String skinName = getSkinName(npc);

        // check if skin is globally cached
        Skin skin = Skin.getFromCache(skinName);
        if (skin != null && skin.hasSkinData()) {

            skin.fillProfile(npc, profile);
            return true;
        }
        else {

            // check if skin is cached in NPC meta
            String cachedSkinName = npc.data().get(CACHED_SKIN_UUID_NAME_METADATA);
            if (skinName.equalsIgnoreCase(cachedSkinName)
                    && npc.data().has(PLAYER_SKIN_TEXTURE_PROPERTIES)) {

                if (!npc.data().get(PLAYER_SKIN_TEXTURE_PROPERTIES).equals("cache")) {

                    setSkinFromMeta(profile, npc.data());
                    return true;
                }
            }
        }
        return false;
    }

    public void setSkin(final WorldServer nmsWorld, GameProfile profile,
                        @Nullable final Runnable onFinish) {

        if (setSkinFromCache(profile)) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        SKIN_THREAD.retrieveSkin(getSkinName(npc), this, nmsWorld.getMinecraftServer().aD(),
                (onFinish == null)
                        ? null
                        : new SkinThread.SkinRetrieved() {

                    @Override
                    public void onRetrieve(Skin.FetchResult result) {
                        onFinish.run();
                    }
                });
    }

    private static void setSkinFromMeta(GameProfile profile, MetadataStore meta) {

        Property property = new Property("textures",
                meta.<String> get(PLAYER_SKIN_TEXTURE_PROPERTIES),
                meta.<String> get(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN));

        profile.getProperties().put("textures", property);
    }

    private static String getSkinName(NPC npc) {
        String skinName = npc.data().get(NPC.PLAYER_SKIN_UUID_METADATA);
        if (skinName == null) {
            skinName = ChatColor.stripColor(npc.getName());
        }
        return skinName;
    }

    static final String PLAYER_SKIN_TEXTURE_PROPERTIES = "player-skin-textures";
    static final String PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN = "player-skin-signature";
    static final String CACHED_SKIN_UUID_METADATA = "cached-skin-uuid";
    static final String CACHED_SKIN_UUID_NAME_METADATA = "cached-skin-uuid-name";
    static final String PLAYER_SKIN_INVALID = "player-skin-invalid";

    private static SkinThread SKIN_THREAD;
}
