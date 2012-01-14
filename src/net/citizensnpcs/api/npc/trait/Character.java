package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;

/**
 * Represents a Character with a unique name that can be loaded and saved (one
 * Character can be attached to an NPC at one time)
 */
public interface Character extends Trait {

	/**
	 * Called when an NPC is left-clicked
	 * 
	 * @param npc
	 *            NPC that was left-clicked
	 */
	public void onLeftClick(NPC npc, Player by);

	/**
	 * Called when an NPC is right-clicked
	 * 
	 * @param npc
	 *            NPC that was right-clicked
	 */
	public void onRightClick(NPC npc, Player by);
}