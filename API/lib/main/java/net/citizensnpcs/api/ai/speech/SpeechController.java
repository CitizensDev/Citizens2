package net.citizensnpcs.api.ai.speech;

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents the NPCs speech abilities. Uses {@link SpeechContext}s which contain messages and recipients.
 *
 */
public interface SpeechController {
    /**
     * Sends the speechController's {@link NPC} and {@link SpeechContext}.
     *
     * @param message
     *            The message to speak
     */
    public void speak(SpeechContext message);
}
