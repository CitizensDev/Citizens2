package net.citizensnpcs.api.ai.speech;

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents the NPCs speech abilities using VocalChords registered with the {@link SpeechFactory}. Uses
 * {@link SpeechContext}s which contain messages and recipients.
 *
 */
public interface SpeechController {
    /**
     * Sends the speechController's {@link NPC} and {@link SpeechContext} to the current default {@link VocalChord} for
     * the NPC. If none, the default {@link VocalChord} is used.
     *
     * @param message
     *            The message to speak
     */
    public void speak(SpeechContext message);
}
