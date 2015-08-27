package net.citizensnpcs.npc.skin;

import com.mojang.authlib.GameProfile;
import net.citizensnpcs.npc.ai.NPCHolder;
import org.bukkit.entity.Player;

/**
 * Interface for player entities that are skinnable.
 */
public interface SkinnableEntity extends NPCHolder {

    /**
     * Get the entities skin packet tracker.
     */
    SkinPacketTracker getSkinTracker();

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
     * Set the name of the player whose skin the NPC
     * uses.
     *
     * <p>Setting the skin name automatically updates and
     * respawn the NPC.</p>
     */
    void setSkinName(String name);
}
