package net.citizensnpcs.api.npc.templates;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.DataKey;

public class Template {
    private final List<Consumer<NPC>> actions = Lists.newArrayList();
    private final String name;
    private final String namespace;

    private Template(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public static Template load(TemplateWorkspace workspace, String namespace, DataKey key) {
        Template template = new Template(namespace, key.name());
        if (key.keyExists("yaml_replace")) {
            template.addAction(PersistenceLoader.load(YamlReplacementAction.class, key.getRelative("yaml_replace")));
        }
        return template;
    }
}
