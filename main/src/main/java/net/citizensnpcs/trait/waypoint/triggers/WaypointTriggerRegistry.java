package net.citizensnpcs.trait.waypoint.triggers;

import java.util.Map;

import org.bukkit.conversations.Prompt;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.util.DataKey;

/**
 * Registers valid {@link WaypointTrigger} classes and their chat configuration prompts. WaypointTriggers are persisted
 * using {@link PersistenceLoader}.
 *
 */
public class WaypointTriggerRegistry implements Persister<WaypointTrigger> {
    @Override
    public WaypointTrigger create(DataKey root) {
        String type = root.getString("type");
        Class<? extends WaypointTrigger> clazz = TRIGGERS.get(type);
        return clazz == null ? null : PersistenceLoader.load(clazz, root);
    }

    @Override
    public void save(WaypointTrigger instance, DataKey root) {
        PersistenceLoader.save(instance, root);
        for (Map.Entry<String, Class<? extends WaypointTrigger>> entry : TRIGGERS.entrySet()) {
            if (entry.getValue() == instance.getClass()) {
                root.setString("type", entry.getKey());
                break;
            }
        }
    }

    public static void addTrigger(String name, Class<? extends WaypointTrigger> triggerClass,
            Class<? extends WaypointTriggerPrompt> promptClass) {
        TRIGGERS.put(name, triggerClass);
        TRIGGER_PROMPTS.put(name, promptClass);
    }

    public static String describeValidTriggerNames() {
        return Joiner.on(", ").join(TRIGGER_PROMPTS.keySet());
    }

    public static Prompt getTriggerPromptFrom(String input) {
        Class<? extends Prompt> promptClass = TRIGGER_PROMPTS.get(input);
        if (promptClass == null)
            return null;
        try {
            return promptClass.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static final Map<String, Class<? extends Prompt>> TRIGGER_PROMPTS = Maps.newHashMap();
    private static final Map<String, Class<? extends WaypointTrigger>> TRIGGERS = Maps.newHashMap();

    static {
        addTrigger("animation", AnimationTrigger.class, AnimationTriggerPrompt.class);
        addTrigger("command", CommandTrigger.class, CommandTriggerPrompt.class);
        addTrigger("chat", ChatTrigger.class, ChatTriggerPrompt.class);
        addTrigger("delay", DelayTrigger.class, DelayTriggerPrompt.class);
        addTrigger("teleport", TeleportTrigger.class, TeleportTriggerPrompt.class);
        addTrigger("speed", SpeedTrigger.class, SpeedTriggerPrompt.class);
    }
}
