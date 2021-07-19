package net.citizensnpcs.trait.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Maps;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.Toggleable;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

/**
 * Persists text metadata, i.e. text that will be said by an NPC on certain triggers.
 */
@TraitName("text")
public class Text extends Trait implements Runnable, Toggleable, Listener, ConversationAbandonedListener {
    private int bubbleTicks;
    private final Map<UUID, Long> cooldowns = Maps.newHashMap();
    private int currentIndex;
    private int delay = -1;
    private String itemInHandPattern = "default";
    private final Plugin plugin;
    private boolean randomTalker = Setting.DEFAULT_RANDOM_TALKER.asBoolean();
    private double range = Setting.DEFAULT_TALK_CLOSE_RANGE.asDouble();
    private boolean realisticLooker = Setting.DEFAULT_REALISTIC_LOOKING.asBoolean();
    private boolean speechBubbles;
    private int speechIndex = -1;
    private boolean talkClose = Setting.DEFAULT_TALK_CLOSE.asBoolean();
    private final List<String> text = new ArrayList<String>();

    public Text() {
        super("text");
        this.plugin = CitizensAPI.getPlugin();
    }

    /**
     * Adds a piece of text that will be said by the NPC.
     *
     * @param string
     *            the text to say
     */
    public void add(String string) {
        text.add(string);
    }

    private void clearBubble() {
        if (speechIndex < npc.getOrAddTrait(HologramTrait.class).getLines().size()) {
            npc.getOrAddTrait(HologramTrait.class).removeLine(speechIndex);
            speechIndex = -1;
        }
        bubbleTicks = 0;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent event) {
    }

    /**
     * Edit the text at a given index to a new text.
     *
     * @param index
     *            the text's index
     * @param newText
     *            the new text to use
     */
    public void edit(int index, String newText) {
        text.set(index, newText);
    }

    /**
     * Builds a text editor in game for the supplied {@link Player}.
     */
    public Editor getEditor(final Player player) {
        final Conversation conversation = new ConversationFactory(plugin).addConversationAbandonedListener(this)
                .withLocalEcho(false).withEscapeSequence("/npc text").withEscapeSequence("exit").withModality(false)
                .withFirstPrompt(new TextBasePrompt(this)).buildConversation(player);
        return new Editor() {
            @Override
            public void begin() {
                Messaging.sendTr(player, Messages.TEXT_EDITOR_BEGIN);
                conversation.begin();
            }

            @Override
            public void end() {
                Messaging.sendTr(player, Messages.TEXT_EDITOR_END);
                conversation.abandon();
            }
        };
    }

    String getPageText(int page) {
        Paginator paginator = new Paginator().header("Current Texts");
        for (int i = 0; i < text.size(); i++)
            paginator.addLine("<a>" + i + " <7>- <e>" + text.get(i));

        return paginator.getPageText(page);
    }

    /**
     * @return The list of all texts
     */
    public List<String> getTexts() {
        return text;
    }

    /**
     * @return whether there is text at a certain index
     */
    public boolean hasIndex(int index) {
        return index >= 0 && text.size() > index;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        text.clear();
        for (DataKey sub : key.getRelative("text").getIntegerSubKeys()) {
            text.add(sub.getString(""));
        }
        if (text.isEmpty()) {
            populateDefaultText();
        }

        talkClose = key.getBoolean("talk-close", talkClose);
        realisticLooker = key.getBoolean("realistic-looking", realisticLooker);
        randomTalker = key.getBoolean("random-talker", randomTalker);
        range = key.getDouble("range", range);
        delay = key.getInt("delay", delay);
        itemInHandPattern = key.getString("talkitem", itemInHandPattern);
    }

    @EventHandler
    private void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(npc))
            return;
        String localPattern = itemInHandPattern.equals("default") ? Setting.TALK_ITEM.asString() : itemInHandPattern;
        if (Util.matchesItemInHand(event.getClicker(), localPattern) && !shouldTalkClose()) {
            sendText(event.getClicker());
            event.setCancelled(true);
        }
    }

    private void populateDefaultText() {
        text.addAll(Setting.DEFAULT_TEXT.asList());
    }

    /**
     * Remove text at a given index.
     */
    public void remove(int index) {
        text.remove(index);
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;

        if (bubbleTicks > 0 && --bubbleTicks == 0) {
            clearBubble();
        }

        if (!talkClose)
            return;
        List<Entity> nearby = npc.getEntity().getNearbyEntities(range, range, range);
        for (Entity search : nearby) {
            if (!(search instanceof Player) || ((Player) search).getGameMode() == GameMode.SPECTATOR)
                continue;
            Player player = (Player) search;

            Long cooldown = cooldowns.get(player.getUniqueId());
            if (cooldown != null) {
                if (System.currentTimeMillis() < cooldown) {
                    return;
                }
                cooldowns.remove(player.getUniqueId());
            }
            sendText(player);

            int secondsDelta = delay != -1 ? delay
                    : RANDOM.nextInt(Setting.TALK_CLOSE_MAXIMUM_COOLDOWN.asInt())
                            + Setting.TALK_CLOSE_MINIMUM_COOLDOWN.asInt();
            if (secondsDelta <= 0)
                return;
            long millisecondsDelta = TimeUnit.MILLISECONDS.convert(secondsDelta, TimeUnit.SECONDS);
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + millisecondsDelta);
        }
    }

    @Override
    public void save(DataKey key) {
        key.setInt("delay", delay);
        key.setBoolean("talk-close", talkClose);
        key.setBoolean("random-talker", randomTalker);
        key.setBoolean("realistic-looking", realisticLooker);
        key.setDouble("range", range);
        key.setString("talkitem", itemInHandPattern);
        key.removeKey("text");
        for (int i = 0; i < text.size(); i++) {
            key.setString("text." + String.valueOf(i), text.get(i));
        }
    }

    boolean sendPage(Player player, int page) {
        Paginator paginator = new Paginator().header("Current Texts");
        for (int i = 0; i < text.size(); i++)
            paginator.addLine("<a>" + i + " <7>- <e>" + text.get(i));

        return paginator.sendPage(player, page);
    }

    private boolean sendText(Player player) {
        if (text.size() == 0)
            return false;

        int index = 0;
        if (randomTalker) {
            index = RANDOM.nextInt(text.size());
        } else {
            if (currentIndex > text.size() - 1) {
                currentIndex = 0;
            }
            index = currentIndex++;
        }

        if (speechBubbles) {
            HologramTrait trait = npc.getOrAddTrait(HologramTrait.class);
            if (speechIndex == -1) {
                speechIndex = trait.getLines().size();
                trait.addLine(text.get(index));
                bubbleTicks = Setting.DEFAULT_TEXT_SPEECH_BUBBLE_TICKS.asInt();
            } else if (speechIndex < trait.getLines().size()) {
                trait.setLine(speechIndex, text.get(index));
            }
        } else {
            npc.getDefaultSpeechController().speak(new SpeechContext(text.get(index), player));
        }
        return true;
    }

    /**
     * Set the text delay between messages.
     *
     * @param delay
     *            the delay in ticks
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Sets the item in hand pattern required to talk to NPCs, if enabled.
     *
     * @param pattern
     *            The new pattern
     */
    public void setItemInHandPattern(String pattern) {
        itemInHandPattern = pattern;
    }

    /**
     * Set the range in blocks before text will be sent.
     *
     * @param range
     */
    public void setRange(double range) {
        this.range = range;
    }

    /**
     * @return Whether talking close is enabled.
     */
    public boolean shouldTalkClose() {
        return talkClose;
    }

    /**
     * Toggles talking to nearby Players.
     */
    @Override
    public boolean toggle() {
        return (talkClose = !talkClose);
    }

    /**
     * Toggles talking at random intervals.
     */
    public boolean toggleRandomTalker() {
        return (randomTalker = !randomTalker);
    }

    /**
     * Toggles requiring line of sight before talking.
     */
    public boolean toggleRealisticLooking() {
        return (realisticLooker = !realisticLooker);
    }

    /**
     * Toggles using speech bubbles instead of messages.
     */
    public boolean toggleSpeechBubbles() {
        if (speechBubbles) {
            clearBubble();
        }
        return (speechBubbles = !speechBubbles);
    }

    private static Random RANDOM = Util.getFastRandom();
}