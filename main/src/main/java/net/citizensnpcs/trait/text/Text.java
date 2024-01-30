package net.citizensnpcs.trait.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
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
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

/**
 * Persists text metadata, i.e. text that will be said by an NPC on certain triggers.
 */
@TraitName("text")
public class Text extends Trait implements Runnable, Listener {
    private final Map<UUID, Long> cooldowns = Maps.newHashMap();
    private int currentIndex;
    @Persist
    private int delay = -1;
    @Persist(value = "talkitem")
    private String itemInHandPattern = "默认";
    private final Plugin plugin;
    @Persist(value = "random-talker")
    private boolean randomTalker = Setting.DEFAULT_RANDOM_TALKER.asBoolean();
    private double range = Setting.DEFAULT_TALK_CLOSE_RANGE.asDouble();
    @Persist(value = "realistic-looking")
    private boolean realisticLooker = Setting.DEFAULT_REALISTIC_LOOKING.asBoolean();
    @Persist(value = "speech-bubbles")
    private boolean speechBubbles;
    @Persist(value = "talk-close")
    private boolean talkClose = Setting.DEFAULT_TALK_CLOSE.asBoolean();
    private final List<String> text = new ArrayList<>();

    public Text() {
        super("text");
        plugin = CitizensAPI.getPlugin();
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
    public Editor getEditor(Player player) {
        Conversation conversation = new ConversationFactory(plugin).withLocalEcho(false).withEscapeSequence("/npc text")
                .withEscapeSequence("exit").withModality(false).withFirstPrompt(new TextBasePrompt(this))
                .buildConversation(player);
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
        for (int i = 0; i < text.size(); i++) {
            paginator.addLine("<green>" + i + " <gray>- <yellow>" + text.get(i));
        }
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

    boolean hasPage(int page) {
        return new Paginator(text.size()).hasPage(page);
    }

    public boolean isRandomTalker() {
        return randomTalker;
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
        range = key.getDouble("range");
    }

    @EventHandler
    private void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(npc) || text.size() == 0)
            return;
        String localPattern = itemInHandPattern.equals("default") ? Setting.TALK_ITEM.asString() : itemInHandPattern;
        if (Util.matchesItemInHand(event.getClicker(), localPattern) && !shouldTalkClose()) {
            talk(event.getClicker());
            event.setDelayedCancellation(true);
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
        if (!npc.isSpawned() || !talkClose || text.size() == 0)
            return;

        for (Player player : CitizensAPI.getLocationLookup().getNearbyPlayers(npc.getEntity().getLocation(), range)) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            talk(player);
        }
    }

    @Override
    public void save(DataKey key) {
        key.setDouble("range", range);
        key.removeKey("text");
        for (int i = 0; i < text.size(); i++) {
            key.setString("text." + String.valueOf(i), text.get(i));
        }
    }

    boolean sendPage(CommandSender player, int page) {
        Paginator paginator = new Paginator().header("Current Texts").enablePageSwitcher("/npc text page $page");
        for (int i = 0; i < text.size(); i++) {
            paginator.addLine(text.get(i) + " <green>(<click:suggest_command:edit " + i
                    + " ><yellow>编辑</click>) (<hover:show_text:删除此文本><click:run_command:/npc text remove "
                    + i + "><red>-</click></hover>)");
        }
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
            trait.addTemporaryLine(Placeholders.replace(text.get(index), player, npc),
                    Setting.DEFAULT_TEXT_SPEECH_BUBBLE_DURATION.asTicks());
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

    private void talk(Player player) {
        Long cooldown = cooldowns.get(player.getUniqueId());
        if (cooldown != null) {
            if (System.currentTimeMillis() < cooldown)
                return;

            cooldowns.remove(player.getUniqueId());
        }
        sendText(player);

        int delay = this.delay == -1
                ? Setting.DEFAULT_TEXT_DELAY_MIN.asTicks() + Util.getFastRandom()
                        .nextInt(Setting.DEFAULT_TEXT_DELAY_MAX.asTicks() - Setting.DEFAULT_TEXT_DELAY_MIN.asTicks())
                : this.delay;
        if (delay <= 0)
            return;
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + delay * 50);
    }

    /**
     * Toggles talking at random intervals.
     */
    public boolean toggleRandomTalker() {
        return randomTalker = !randomTalker;
    }

    /**
     * Toggles requiring line of sight before talking.
     */
    public boolean toggleRealisticLooking() {
        return realisticLooker = !realisticLooker;
    }

    /**
     * Toggles using speech bubbles instead of messages.
     */
    public boolean toggleSpeechBubbles() {
        return speechBubbles = !speechBubbles;
    }

    /**
     * Toggles talking to nearby Players.
     */
    public boolean toggleTalkClose() {
        return talkClose = !talkClose;
    }

    public boolean useRealisticLooking() {
        return realisticLooker;
    }

    public boolean useSpeechBubbles() {
        return speechBubbles;
    }

    private static Random RANDOM = Util.getFastRandom();
}
