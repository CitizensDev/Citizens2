package net.citizensnpcs.trait;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.md_5.bungee.api.ChatColor;

@TraitName("skintrait")
public class SkinTrait extends Trait {
    @Persist
    private boolean fetchDefaultSkin = true;
    private String filledPlaceholder;
    @Persist
    private String signature;
    @Persist
    private String skinName;
    @Persist
    private String textureRaw;
    private int timer;
    @Persist
    private boolean updateSkins = Setting.NPC_SKIN_USE_LATEST.asBoolean();

    public SkinTrait() {
        super("skintrait");
    }

    private void checkPlaceholder(boolean update) {
        if (skinName == null)
            return;
        String filled = ChatColor.stripColor(Placeholders.replace(skinName, null, npc).toLowerCase());
        if (!filled.equalsIgnoreCase(skinName)) {
            filledPlaceholder = filled;
            if (update) {
                onSkinChange(true);
            }
        }
    }

    /**
     * Clears skin texture and name.
     */
    public void clearTexture() {
        textureRaw = null;
        signature = null;
        skinName = null;
    }

    /**
     * Whether to fetch the Mojang skin using the NPC's name on spawn.
     */
    public boolean fetchDefaultSkin() {
        return fetchDefaultSkin;
    }

    /**
     * @return The texture signature, or null
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return The skin name if set, or null (i.e. using the NPC's name)
     */
    public String getSkinName() {
        return filledPlaceholder != null && skinName != null ? filledPlaceholder : skinName;
    }

    /**
     * @return The encoded texture data, or null
     */
    public String getTexture() {
        return textureRaw;
    }

    @Override
    public void load(DataKey key) {
        checkPlaceholder(false);
    }

    @SuppressWarnings("deprecation")
    private void migrate() {
        boolean update = false;
        if (npc.data().has(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA)) {
            textureRaw = npc.data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
            npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
            update = true;

        }
        if (npc.data().has(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA)) {
            signature = npc.data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
            npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
            update = true;
        }
        if (npc.data().has(NPC.PLAYER_SKIN_UUID_METADATA)) {
            this.skinName = npc.data().get(NPC.PLAYER_SKIN_UUID_METADATA);
            npc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA);
            update = true;
        }
        if (npc.data().has(NPC.PLAYER_SKIN_USE_LATEST)) {
            this.updateSkins = npc.data().get(NPC.PLAYER_SKIN_USE_LATEST);
            npc.data().remove(NPC.PLAYER_SKIN_USE_LATEST);
        }
        if (update) {
            onSkinChange(false);
        }
    }

    private void onSkinChange(boolean forceUpdate) {
        if (npc.isSpawned() && npc.getEntity() instanceof SkinnableEntity) {
            ((SkinnableEntity) npc.getEntity()).getSkinTracker().notifySkinChange(forceUpdate);
        }
    }

    @Override
    public void run() {
        migrate();
        if (timer-- > 0)
            return;
        timer = Setting.PLACEHOLDER_SKIN_UPDATE_FREQUENCY.asInt();
        if (filledPlaceholder == null)
            return;
        checkPlaceholder(true);
    }

    /**
     * @see #fetchDefaultSkin
     */
    public void setFetchDefaultSkin(boolean fetch) {
        this.fetchDefaultSkin = fetch;
    }

    /**
     * @see #shouldUpdateSkins()
     */
    public void setShouldUpdateSkins(boolean update) {
        this.updateSkins = update;
    }

    /**
     * Sets the skin name - will respawn NPC if spawned.
     *
     * @param name
     *            The skin name
     */
    public void setSkinName(String name) {
        setSkinName(name, false);
    }

    /**
     * Sets the skin name - will respawn NPC if spawned.
     *
     * @param name
     *            The skin name
     * @param forceUpdate
     *            Whether to force update if no data has been fetched yet
     * @see net.citizensnpcs.npc.skin.Skin#get(SkinnableEntity, boolean)
     */
    public void setSkinName(String name, boolean forceUpdate) {
        Preconditions.checkNotNull(name);
        setSkinNameInternal(name);
        onSkinChange(forceUpdate);
    }

    private void setSkinNameInternal(String name) {
        skinName = ChatColor.stripColor(name.toLowerCase());
        checkPlaceholder(false);
        String filled = ChatColor.stripColor(Placeholders.replace(skinName, null, npc).toLowerCase());
        if (!filled.equalsIgnoreCase(skinName)) {
            filledPlaceholder = filled;
        } else {
            filledPlaceholder = null;
        }
    }

    /**
     * Sets the skin data directly, respawning the NPC if spawned
     *
     * @param skinName
     *            Skin name, for caching purposes
     * @param signature
     *            {@link #getSignature()}
     * @param data
     *            {@link #getTexture()}
     */
    public void setSkinPersistent(String skinName, String signature, String data) {
        Preconditions.checkNotNull(skinName);
        Preconditions.checkNotNull(signature);
        Preconditions.checkNotNull(data);

        setSkinNameInternal(skinName);
        String json = new String(BaseEncoding.base64().decode(data), Charsets.UTF_8);
        if (!json.contains("textures")) {
            throw new IllegalArgumentException("Invalid texture data");
        }
        this.signature = signature;
        this.textureRaw = data;
        this.updateSkins = false;
        npc.data().setPersistent("cached-skin-uuid-name", skinName.toLowerCase());
        onSkinChange(false);
    }

    public void setTexture(String value, String signature) {
        this.textureRaw = value;
        this.signature = signature;
    }

    /**
     * @return Whether the skin should be updated from Mojang periodically
     */
    public boolean shouldUpdateSkins() {
        return updateSkins;
    }
}
