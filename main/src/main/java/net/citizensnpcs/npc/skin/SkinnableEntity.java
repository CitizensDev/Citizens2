package net.citizensnpcs.npc.skin;

import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.SkinTrait;

/**
 * Interface for player entities that are skinnable.
 */
public interface SkinnableEntity extends NPCHolder {

    /**
     * Get the bukkit entity.
     */
    Player getBukkitEntity();

    /**
     * Get entity game profile.
     */
    GameProfile getProfile();

    /**
     * Get the name of the player whose skin the NPC uses.
     */
    String getSkinName();

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

    /**
     * Set the name of the player whose skin the NPC uses.
     *
     * <p>
     * Setting the skin name automatically updates and respawn the NPC.
     * </p>
     *
     * @see SkinTrait#setSkinName(String)
     *
     * @param name
     *            The skin name.
     */
    @Deprecated
    void setSkinName(String name);

    /**
     * @see SkinTrait#setSkinName(String,boolean)
     */
    @Deprecated
    void setSkinName(String skinName, boolean forceUpdate);

    /**
     * @see SkinTrait#setSkinPersistent(String,String,String)
     */
    @Deprecated
    void setSkinPersistent(String skinName, String signature, String data);
}
