package net.citizensnpcs.trait.waypoint.triggers;

import java.util.Map;

import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.conversations.Prompt;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

public class WaypointTriggerRegistry implements Persister<WaypointTrigger> {
    @Override
    public WaypointTrigger create(DataKey root) {
        String type = root.getString("type");
        Class<? extends WaypointTrigger> clazz = triggers.get(type);
        return clazz == null ? null : PersistenceLoader.load(clazz, root);
    }

    @Override
    public void save(WaypointTrigger instance, DataKey root) {
        PersistenceLoader.save(instance, root);
    }

    public static void addTrigger(String name, Class<? extends WaypointTrigger> triggerClass,
            Class<? extends WaypointTriggerPrompt> promptClass) {
        triggers.put(name, triggerClass);
        triggerPrompts.put(name, promptClass);
    }

    public static String describeValidTriggerNames() {
        return Joiner.on(", ").join(triggerPrompts.keySet());
    }

    public static Prompt getTriggerPromptFrom(String input) {
        Class<? extends Prompt> promptClass = triggerPrompts.get(input);
        if (promptClass == null)
            return null;
        try {
            return promptClass.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static final Map<String, Class<? extends Prompt>> triggerPrompts = Maps.newHashMap();
    private static final Map<String, Class<? extends WaypointTrigger>> triggers = Maps.newHashMap();

    static {
        addTrigger("animation", AnimationTrigger.class, AnimationTriggerPrompt.class);
        addTrigger("chat", ChatTrigger.class, ChatTriggerPrompt.class);
        addTrigger("delay", DelayTrigger.class, DelayTriggerPrompt.class);
        addTrigger("teleport", TeleportTrigger.class, TeleportTriggerPrompt.class);
        addTrigger("speed", SpeedTrigger.class, SpeedTriggerPrompt.class);
        // addTrigger("pose", PoseTrigger.class, PoseTriggerPrompt.class);
    }
}
