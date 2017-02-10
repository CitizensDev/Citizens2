package net.citizensnpcs.npc;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.api.util.YamlStorage.YamlKey;

public class Template {
    private final String name;
    private final boolean override;
    private final Map<String, Object> replacements;

    private Template(String name, Map<String, Object> replacements, boolean override) {
        this.replacements = replacements;
        this.override = override;
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public void apply(NPC npc) {
        MemoryDataKey memoryKey = new MemoryDataKey();
        boolean wasSpawned = npc.isSpawned();
        if (wasSpawned) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
        }
        npc.save(memoryKey);
        List<Node> queue = Lists.newArrayList(new Node("", replacements));
        for (int i = 0; i < queue.size(); i++) {
            Node node = queue.get(i);
            for (Entry<String, Object> entry : node.map.entrySet()) {
                String fullKey = node.headKey.isEmpty() ? entry.getKey() : node.headKey + '.' + entry.getKey();
                if (entry.getValue() instanceof Map<?, ?>) {
                    queue.add(new Node(fullKey, (Map<String, Object>) entry.getValue()));
                    continue;
                }
                boolean overwrite = memoryKey.keyExists(fullKey) | override;
                if (!overwrite || fullKey.equals("uuid"))
                    continue;
                memoryKey.setRaw(fullKey, entry.getValue());
            }
        }
        npc.load(memoryKey);
        if (wasSpawned && npc.getStoredLocation() != null) {
            npc.spawn(npc.getStoredLocation());
        }
    }

    public void delete() {
        templates.load();
        templates.getKey("").removeKey(name);
        templates.save();
    }

    public String getName() {
        return name;
    }

    private static class Node {
        String headKey;
        Map<String, Object> map;

        private Node(String headKey, Map<String, Object> map) {
            this.headKey = headKey;
            this.map = map;
        }
    }

    public static class TemplateBuilder {
        private final String name;
        private boolean override;
        private final Map<String, Object> replacements = Maps.newHashMap();

        private TemplateBuilder(String name) {
            this.name = name;
        }

        public Template buildAndSave() {
            save();
            return new Template(name, replacements, override);
        }

        public TemplateBuilder from(NPC npc) {
            replacements.clear();
            MemoryDataKey key = new MemoryDataKey();
            ((CitizensNPC) npc).save(key);
            replacements.putAll(key.getValuesDeep());
            return this;
        }

        public TemplateBuilder override(boolean override) {
            this.override = override;
            return this;
        }

        public void save() {
            templates.load();
            DataKey root = templates.getKey(name);
            root.setBoolean("override", override);
            root.setRaw("replacements", replacements);
            templates.save();
        }

        public static TemplateBuilder create(String name) {
            return new TemplateBuilder(name);
        }
    }

    public static Iterable<Template> allTemplates() {
        templates.load();
        return Iterables.transform(templates.getKey("").getSubKeys(), new Function<DataKey, Template>() {
            @Override
            public Template apply(DataKey arg0) {
                return Template.byName(arg0.name());
            }
        });
    }

    public static Template byName(String name) {
        templates.load();
        if (!templates.getKey("").keyExists(name))
            return null;
        YamlKey key = templates.getKey(name);
        boolean override = key.getBoolean("override", false);
        Map<String, Object> replacements = key.getRelative("replacements").getValuesDeep();
        return new Template(name, replacements, override);
    }

    private static YamlStorage templates = new YamlStorage(new File(CitizensAPI.getDataFolder(), "templates.yml"));

    static {
        templates.load();
    }
}
