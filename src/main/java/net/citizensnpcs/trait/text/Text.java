package net.citizensnpcs.trait.text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.trait.Toggleable;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Paginator;

public class Text extends Attachment implements Runnable, Toggleable, ConversationAbandonedListener {
    private final Map<String, Calendar> cooldowns = new HashMap<String, Calendar>();
    private int currentIndex;
    private final NPC npc;
    private final Plugin plugin;
    private boolean randomTalker = Setting.DEFAULT_RANDOM_TALKER.asBoolean();
    private boolean talkClose = Setting.DEFAULT_TALK_CLOSE.asBoolean();
    private final List<String> text = new ArrayList<String>();

    public Text(NPC npc) {
        this.npc = npc;
        this.plugin = Bukkit.getPluginManager().getPlugin("Citizens");
    }

    public void add(String string) {
        text.add(string);
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent event) {
        Bukkit.dispatchCommand((Player) event.getContext().getForWhom(), "npc text");
    }

    public void edit(int index, String newText) {
        text.set(index, newText);
    }

    public Editor getEditor(final Player player) {
        final Conversation conversation = new ConversationFactory(plugin).addConversationAbandonedListener(this)
                .withLocalEcho(false).withEscapeSequence("/npc text").withModality(false)
                .withFirstPrompt(new StartPrompt(this)).buildConversation(player);
        return new Editor() {

            @Override
            public void begin() {
                Messaging.send(player, "<b>Entered the text editor!");

                conversation.begin();
            }

            @Override
            public void end() {
                Messaging.send(player, "<a>Exited the text editor.");
            }
        };
    }

    public boolean hasIndex(int index) {
        return text.size() > index;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        for (DataKey sub : key.getIntegerSubKeys())
            text.add(sub.getString(""));
        if (text.isEmpty())
            populateDefaultText();

        if (key.keyExists("talk-close"))
            talkClose = key.getBoolean("talk-close");
        if (key.keyExists("random-talker"))
            randomTalker = key.getBoolean("random-talker");
    }

    @Override
    public void onSpawn() {
        if (text.isEmpty())
            populateDefaultText();
    }

    private void populateDefaultText() {
        text.addAll(Setting.DEFAULT_TEXT.asList());
    }

    public void remove(int index) {
        text.remove(index);
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        EntityHuman search = null;
        EntityLiving handle = ((CitizensNPC) npc).getHandle();
        if ((search = handle.world.findNearbyPlayer(handle, 5)) != null && talkClose) {
            Player player = (Player) search.getBukkitEntity();
            // If the cooldown is not expired, do not send text
            if (cooldowns.get(player.getName()) != null) {
                if (!Calendar.getInstance().after(cooldowns.get(player.getName())))
                    return;
                cooldowns.remove(player.getName());
            }
            if (sendText(player)) {
                // Add a cooldown if the text was successfully sent
                Calendar wait = Calendar.getInstance();
                wait.add(
                        Calendar.SECOND,
                        (new Random().nextInt(Setting.TALK_CLOSE_MAXIMUM_COOLDOWN.asInt()) + Setting.TALK_CLOSE_MINIMUM_COOLDOWN
                                .asInt()));
                cooldowns.put(player.getName(), wait);
            }
        }
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("talk-close", talkClose);
        key.setBoolean("random-talker", randomTalker);
        for (int i = 0; i < text.size(); i++)
            key.setString(String.valueOf(i), text.get(i));
    }

    public boolean sendPage(Player player, int page) {
        Paginator paginator = new Paginator().header(npc.getName() + "'s Text Entries");
        for (int i = 0; i < text.size(); i++)
            paginator.addLine("<a>" + i + " <7>- <e>" + text.get(i));

        return paginator.sendPage(player, page);
    }

    public boolean sendText(Player player) {
        if (!player.hasPermission("citizens.admin") && !player.hasPermission("citizens.npc.talk"))
            return false;
        if (text.size() == 0)
            return false;

        int index = 0;
        if (randomTalker)
            index = new Random().nextInt(text.size());
        else {
            if (currentIndex > text.size() - 1)
                currentIndex = 0;
            index = currentIndex++;
        }
        npc.chat(player, text.get(index));
        return true;
    }

    public boolean shouldTalkClose() {
        return talkClose;
    }

    @Override
    public boolean toggle() {
        talkClose = !talkClose;
        return talkClose;
    }

    public boolean toggleRandomTalker() {
        randomTalker = !randomTalker;
        return randomTalker;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Text{talk-close=" + talkClose + ",text=");
        for (String line : text)
            builder.append(line + ",");
        builder.append("}");
        return builder.toString();
    }
}