package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

/**
 * Marker interface for waypoint trigger prompts.
 *
 * Prompts are expected to return to the prompt specified under the {@link #RETURN_PROMPT_KEY} key in the
 * {@link ConversationContext} and to set the specified trigger under {@link #CREATED_TRIGGER_KEY} prior to returning.
 *
 * If the returned trigger is <code>null</code> then the prompt is assumed to have failed and an error message will be
 * displayed.
 */
public interface WaypointTriggerPrompt extends Prompt {
    public WaypointTrigger createFromShortInput(ConversationContext context, String input);

    static String CREATED_TRIGGER_KEY = "created-trigger";
    static String RETURN_PROMPT_KEY = "return-to";
}
