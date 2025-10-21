package net.citizensnpcs.trait;

import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.npc.skin.SkinnableEntity.PlayerSkinModelType;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.SkinProperty;

@TraitName("skintrait")
public class SkinTrait extends Trait {
    @Persist
    private NamespacedKey body;
    @Persist
    private NamespacedKey cape;
    @Persist
    private NamespacedKey elytra;
    @Persist
    private boolean fetchDefaultSkin = Setting.NPC_SKIN_FETCH_DEFAULT.asBoolean();
    private String filledPlaceholder;
    @Persist
    private PlayerSkinModelType modelType = PlayerSkinModelType.WIDE;
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

    public void applyTextureInternal(String signature, String value) {
        textureRaw = value;
        this.signature = signature;
    }

    private boolean checkPlaceholder() {
        if (body != null || skinName == null)
            return false;
        String filled = ChatColor.stripColor(Placeholders.replace(skinName, null, npc).toLowerCase());
        if (!filled.equalsIgnoreCase(skinName) && !filled.equalsIgnoreCase(filledPlaceholder)) {
            filledPlaceholder = filled;
            Messaging.debug("Filled skin placeholder", filled, "from", skinName);
            return true;
        }
        return false;
    }

    /**
     * Clears skin texture and name.
     */
    public void clearTexture() {
        textureRaw = null;
        signature = null;
        skinName = null;
        body = null;
        cape = null;
        elytra = null;
        modelType = PlayerSkinModelType.WIDE;
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

    private void onSkinChange(boolean forceUpdate) {
        if (!(npc.getEntity() instanceof SkinnableEntity))
            return;
        ((SkinnableEntity) npc.getEntity()).getSkinTracker().notifySkinChange(forceUpdate);
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof SkinnableEntity && body != null) {
            ((SkinnableEntity) npc.getEntity()).setSkinPatch(modelType, body, cape, elytra);
        }
    }

    @Override
    public void run() {
        if (timer-- > 0)
            return;
        timer = Setting.PLACEHOLDER_SKIN_UPDATE_FREQUENCY.asTicks();
        if (checkPlaceholder()) {
            onSkinChange(true);
        }
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
        Objects.requireNonNull(name);
        setSkinNameInternal(name);
        onSkinChange(forceUpdate);
    }

    private void setSkinNameInternal(String name) {
        skinName = ChatColor.stripColor(name);
    }

    /**
     * Sets the skin texture patch. Only guaranteed to work on Mannequin NPCs.
     *
     * @param patch
     */
    public void setSkinPatch(PlayerSkinModelType modelType, NamespacedKey body, NamespacedKey cape,
            NamespacedKey elytra) {
        this.body = body;
        this.cape = cape;
        this.elytra = elytra;
        this.modelType = modelType;
        if (body != null && npc.getEntity() instanceof SkinnableEntity) {
            ((SkinnableEntity) npc.getEntity()).setSkinPatch(modelType, body, cape, elytra);
        }
    }

    /**
     * Set skin data copying from a {@link Player}. Not subject to rate limiting from Mojang.
     *
     * @param player
     *            The player to copy
     */
    public void setSkinPersistent(Player player) {
        SkinProperty sp = SkinProperty.fromMojangProfile(NMS.getProfile(player));
        setSkinPersistent(sp.name, sp.signature, sp.value);
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
        Objects.requireNonNull(skinName);
        Objects.requireNonNull(signature);
        Objects.requireNonNull(data);

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

    /**
     * @return Whether the skin should be updated from Mojang periodically
     */
    public boolean shouldUpdateSkins() {
        return updateSkins;
    }
}
