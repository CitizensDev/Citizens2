package net.citizensnpcs.trait;

import org.bukkit.ChatColor;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;

@TraitName("skintrait")
public class SkinTrait extends Trait {
    @Persist
    private boolean fetchDefaultSkin = Setting.NPC_SKIN_FETCH_DEFAULT.asBoolean();
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
        if (!filled.equalsIgnoreCase(skinName) && !filled.equalsIgnoreCase(filledPlaceholder)) {
            filledPlaceholder = filled;
            Messaging.debug("Filled skin placeholder", filled, "from", skinName);
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
        return filledPlaceholder != null && skinName != null ? filledPlaceholder
                : skinName == null ? skinName : skinName.toLowerCase();
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

    private void onSkinChange(boolean forceUpdate) {
        if (npc.isSpawned() && npc.getEntity() instanceof SkinnableEntity) {
            ((SkinnableEntity) npc.getEntity()).getSkinTracker().notifySkinChange(forceUpdate);
        }
    }

    @Override
    public void run() {
        if (timer-- > 0)
            return;
        timer = Setting.PLACEHOLDER_SKIN_UPDATE_FREQUENCY.asTicks();
        checkPlaceholder(true);
    }

    /**
     * @see #fetchDefaultSkin
     */
    public void setFetchDefaultSkin(boolean fetch) {
        fetchDefaultSkin = fetch;
    }

    /**
     * @see #shouldUpdateSkins()
     */
    public void setShouldUpdateSkins(boolean update) {
        updateSkins = update;
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
        skinName = ChatColor.stripColor(name);
        checkPlaceholder(false);
    }

    /**
     * Sets the skin data directly, respawning the NPC if spawned.
     *
     * @param skinName
     *            Skin name or cache key
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
        if (!json.contains("textures"))
            throw new IllegalArgumentException("Invalid texture data");

        this.signature = signature;
        textureRaw = data;
        updateSkins = false;
        npc.data().setPersistent(Skin.CACHED_SKIN_UUID_NAME_METADATA, skinName.toLowerCase());
        onSkinChange(false);
    }

    public void setTexture(String value, String signature) {
        textureRaw = value;
        this.signature = signature;
    }

    /**
     * @return Whether the skin should be updated from Mojang periodically
     */
    public boolean shouldUpdateSkins() {
        return updateSkins;
    }
}
