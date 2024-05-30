package net.citizensnpcs.api.npc.templates;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.NamespacedKey;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.DataKey;

public class Template {
    private final List<Consumer<NPC>> actions = Lists.newArrayList();
    private final NamespacedKey key;

    private Template(NamespacedKey key) {
        this.key = key;
    }

    private void addAction(Consumer<NPC> action) {
        actions.add(action);
    }

    public void apply(NPC npc) {
        boolean respawn = npc.isSpawned();
        if (respawn) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
        }
        for (Consumer<NPC> action : actions) {
            action.accept(npc);
        }
        if (respawn) {
            npc.spawn(npc.getStoredLocation());
        }
    }

    public NamespacedKey getKey() {
        return key;
    }

    public static Template load(TemplateWorkspace workspace, NamespacedKey nkey, DataKey key) {
        Template template = new Template(nkey);
        if (key.keyExists("yaml_replace")) {
            template.addAction(PersistenceLoader.load(YamlReplacementAction.class, key.getRelative("yaml_replace")));
        }
        if (key.keyExists("commands")) {
            loadCommands(template, workspace, key.getRelative("commands"));
        }
        return template;
    }

    @SuppressWarnings("unchecked")
    private static void loadCommands(Template template, TemplateWorkspace workspace, DataKey key) {
        for (DataKey sub : key.getSubKeys()) {
            if (sub.name().equals("on_spawn")) {
                template.addAction(new CommandEventAction(NPCSpawnEvent.class,
                        new CommandListExecutor((List<String>) key.getRaw(""))));
            }
        }
    }
}
