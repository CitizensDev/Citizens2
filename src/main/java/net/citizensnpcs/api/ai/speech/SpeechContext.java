package net.citizensnpcs.api.ai.speech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * SpeechContext contains information about a {@link NPCSpeechEvent}, including the {@link Talkable} talker, recipients,
 * and message.
 * 
 */
public class SpeechContext implements Iterable<Talkable> {

    private String message;
    private List<Talkable> recipients = Collections.emptyList();
    private Talkable talker = null;

    public SpeechContext() {
        // Must set talker/message (and recipients, if any)
        // with supplied methods.
    }

    public SpeechContext(NPC talker, String message) {
        if (talker != null)
            setTalker(talker.getEntity());
        this.message = message;
    }

    public SpeechContext(NPC talker, String message, LivingEntity recipient) {
        this(talker, message);
        if (recipient != null) {
            addRecipient(recipient);
        }
    }

    public SpeechContext(String message) {
        this.message = message;
    }

    public SpeechContext(String message, LivingEntity recipient) {
        this.message = message;
        if (recipient != null)
            addRecipient(recipient);
    }

    /**
     * Adds a direct {@link Talkable} recipient. The {@link VocalChord} should use this information to correctly direct
     * the message. Note: depending on the VocalChord, this list may not be inclusive as to who gets the message.
     * 
     * @param talkable
     *            Talkable entity
     * @return the speech context
     * 
     */
    public SpeechContext addRecipient(Entity entity) {
        if (recipients.isEmpty())
            recipients = new ArrayList<Talkable>();
        recipients.add(CitizensAPI.getSpeechFactory().newTalkableEntity(entity));
        return this;
    }

    @Deprecated
    public SpeechContext addRecipient(LivingEntity entity) {
        return addRecipient((Entity) entity);
    }

    /**
     * Adds a list of {@link Talkable} recipients. The {@link VocalChord} should use this information to correctly
     * direct the message. Note: depending on the VocalChord, this list may not be inclusive as to who gets the message.
     * 
     * @param talkable
     *            Talkable entity
     * @return the Tongue
     * 
     */
    public SpeechContext addRecipients(List<Talkable> talkables) {
        if (recipients.isEmpty())
            recipients = new ArrayList<Talkable>();
        recipients.addAll(talkables);
        return this;
    }

    /**
     * Gets the text message sent.
     * 
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the talker.
     * 
     * @return NPC doing the talking
     * 
     */
    public Talkable getTalker() {
        return talker;
    }

    /**
     * Checks if there are any recipients. If none, this {@link SpeechContext} is not targeted.
     * 
     * @return true if recipients are specified.
     */
    public boolean hasRecipients() {
        return (recipients.isEmpty()) ? false : true;
    }

    /**
     * Gets direct recipients, if any.
     * 
     * @return recipients Iterator
     * 
     */
    @Override
    public Iterator<Talkable> iterator() {
        final Iterator<Talkable> itr = recipients.iterator();
        return itr;
    }

    /**
     * Sets the text message sent. Overrides text set with the constructor.
     * 
     * @param message
     *            The text to send.
     * 
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the talker.
     * 
     * @param entity
     *            NPC doing the talking
     * 
     */
    public void setTalker(Entity entity) {
        this.talker = CitizensAPI.getSpeechFactory().newTalkableEntity(entity);
    }

    @Deprecated
    public void setTalker(LivingEntity entity) {
        setTalker((Entity) entity);
    }

    /**
     * @return number of recipients.
     */
    public int size() {
        return recipients.size();
    }

}