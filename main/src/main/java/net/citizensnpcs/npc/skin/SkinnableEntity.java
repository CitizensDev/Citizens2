package net.citizensnpcs.npc.skin;

import java.util.Locale;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.SkinLayers;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.SkinProperty;

/**
 * Interface for player entities that are skinnable.
 */
public interface SkinnableEntity extends NPCHolder {
    void applyTexture(SkinProperty property);

    /**
     * Get entity game profile.
     */
    GameProfile gameProfile();

    /**
     * Get the bukkit entity.
     */
    default LivingEntity getBukkitEntity() {
        return (LivingEntity) getNPC().getEntity();
    }

    /**
     * Get the name of the player whose skin the NPC uses.
     */
    default String getSkinName() {
        String skinName = getNPC().getOrAddTrait(SkinTrait.class).getSkinName();
        if (skinName == null) {
            skinName = getNPC().getName();
        }
        return skinName.toLowerCase(Locale.ROOT);
    }

    /**
     * Get the entities skin packet tracker.
     */
    SkinPacketTracker getSkinTracker();

    /**
     * Set the bit flags that represent the skin layer parts visibility.
     *
     * <p>
     * Setting the skin flags automatically updates the NPC skin.
     * </p>
     *
     * @param flags
     *            The bit flags.
     */
    void setSkinFlags(byte flags);

    default void setSkinFlags(Set<SkinLayers.Layer> flags) {
        setSkinFlags(SkinLayers.Layer.toByte(flags));
    }

    default void setSkinPatch(PlayerSkinModelType type, NamespacedKey body, NamespacedKey cape, NamespacedKey elytra) {
    }

    public static interface ForwardingSkinnableEntity extends SkinnableEntity {
        @Override
        default void applyTexture(SkinProperty property) {
            getUnderlying().applyTexture(property);
        }

        @Override
        default GameProfile gameProfile() {
            return getUnderlying().gameProfile();
        }

        @Override
        default SkinPacketTracker getSkinTracker() {
            return getUnderlying().getSkinTracker();
        }

        SkinnableEntity getUnderlying();

        @Override
        default void setSkinFlags(byte flags) {
            getUnderlying().setSkinFlags(flags);
        }

        @Override
        default void setSkinPatch(PlayerSkinModelType type, NamespacedKey body, NamespacedKey cape,
                NamespacedKey elytra) {
            getUnderlying().setSkinPatch(type, body, cape, elytra);
        }
    }

    public enum PlayerSkinModelType {
        SLIM,
        WIDE;
    }
}
