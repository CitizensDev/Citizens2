package net.citizensnpcs.api.npc.templates;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;

public class YamlReplacementAction implements Consumer<NPC> {
    private final boolean override;
    private final Map<String, Object> replacements;

    @DelegatePersistence(YRAPersister.class)
    private YamlReplacementAction(boolean override, Map<String, Object> replacements) {
        this.replacements = replacements;
        this.override = override;
    }

    @Override
    public void accept(NPC npc) {
        MemoryDataKey memoryKey = new MemoryDataKey();
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
                boolean overwrite = memoryKey.keyExists(fullKey) || override;
                if (!overwrite || fullKey.equals("uuid"))
                    continue;

                memoryKey.setRaw(fullKey, entry.getValue());
            }
        }
        npc.load(memoryKey);
    }

    private static class Node {
        String headKey;
        Map<String, Object> map;

        private Node(String headKey, Map<String, Object> map) {
            this.headKey = headKey;
            this.map = map;
        }
    }

    private static class YRAPersister implements Persister<YamlReplacementAction> {
        public YRAPersister() {
        }

        @Override
        public YamlReplacementAction create(DataKey root) {
            return new YamlReplacementAction(root.getBoolean("override"),
                    root.getRelative("replacements").getValuesDeep());
        }

        @Override
        public void save(YamlReplacementAction instance, DataKey root) {
            root.setBoolean("override", instance.override);
            root.setRaw("replacements", instance.replacements);
        }
    }
}
