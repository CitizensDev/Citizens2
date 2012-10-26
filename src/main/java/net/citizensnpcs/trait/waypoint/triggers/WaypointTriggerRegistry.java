package net.citizensnpcs.trait.waypoint.triggers;

import java.util.Map;

import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.conversations.Prompt;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

public class WaypointTriggerRegistry implements Persister {
    @Override
    public Object create(DataKey root) {
        String type = root.getString("type");
        Class<? extends WaypointTrigger> clazz = triggers.get(type);
        return clazz == null ? null : PersistenceLoader.load(clazz, root);
    }

    @Override
    public void save(Object instance, DataKey root) {
        PersistenceLoader.save(instance, root);
    }

    private static final Map<String, Class<? extends Prompt>> triggerPrompts = Maps.newHashMap();
    private static final Map<String, Class<? extends WaypointTrigger>> triggers = Maps.newHashMap();

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

    static {
        addTrigger("teleport", TeleportTrigger.class, TeleportTriggerPrompt.class);
        addTrigger("delay", DelayTrigger.class, DelayTriggerPrompt.class);
    }
}
