package net.citizensnpcs.api.ai.speech;

import org.bukkit.entity.LivingEntity;

/**
 * SpeechFactory keeps track of and creates new VocalChord instances,
 * necessary for NPC Speech.
 *
 */
public interface SpeechFactory {

	/**
	 * Creates a new instance of a VocalChord
	 * 
	 * @param name
	 * 			The class of the desired VocalChord
	 * @return a new instance of this VocalChord
	 * 
	 */
	public VocalChord getVocalChord(Class<? extends VocalChord> clazz);

	/**
	 * Creates a new instance of a {@link VocalChord}
	 * 
	 * @param name
	 * 			The name of the desired VocalChord
	 * @return 
	 * 			a new instance of this VocalChord, ornull if
	 * 			a VocalChord is not registered with this name
	 * 
	 */
	public VocalChord getVocalChord(String name);
	
	/**
	 * Returns the registered name of a {@link VocalChord} class
	 * 
	 * @param clazz
	 * 			The VocalChord class
	 * @return
	 * 			the registered name, null if not registered
	 * 
	 */
	public String getVocalChordName(Class<? extends VocalChord> clazz);

	/**
	 * Checks whether the supplied {@link VocalChord} name is registered.
	 * 
	 * @param name
	 * 			The name of the VocalChord to check
	 * @return 
	 * 			true if the VocalChord name is registered
	 * 
	 */
	public boolean isRegistered(String name);
	
	/**
	 * Registers a {@link VocalChord} class with the SpeechController, making it
	 * available for use within.  Requires a 'name', which should generally
	 * describe the intent of the VocalChord.
	 * 
	 * @param clazz
	 * 			The VocalChord class
	 * @param name
	 * 			The name of the VocalChord
	 * 
	 */
	public void register(Class<? extends VocalChord> clazz, String name);

	/**
	 * Creates a new Talkable entity and returns it
	 * 
	 * @param livingEntity
	 * 		the livingEntity to use
	 * 
	 * @return a Talkable entity
	 */
	public Talkable newTalkableEntity(LivingEntity entity);
}
