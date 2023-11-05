package net.citizensnpcs.npc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;
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
                boolean overwrite = memoryKey.keyExists(fullKey) || override;
                if (!overwrite || fullKey.equals("uuid")) {
                    continue;
                }
                memoryKey.setRaw(fullKey, entry.getValue());
            }
        }
        npc.load(memoryKey);
        if (wasSpawned && npc.getStoredLocation() != null) {
            npc.spawn(npc.getStoredLocation(), SpawnReason.RESPAWN);
        }
    }

    public void delete() {
        new File(getDirectory(), name + ".yml").delete();
    }

    public String getName() {
        return name;
    }

    public void save() throws IOException {
        File file = new File(getDirectory(), name + ".yml");
        if (!file.getParentFile().equals(getDirectory()))
            throw new IOException();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw e;
        }
        YamlStorage storage = new YamlStorage(file);
        DataKey root = storage.getKey("");
        root.setBoolean("override", override);
        root.setRaw("replacements", replacements);
        storage.save();
    }

    public static class Builder {
        private final String name;
        private boolean override = true;
        private final Map<String, Object> replacements = Maps.newHashMap();

        private Builder(String name) {
            this.name = name;
        }

        public Template buildAndSave() throws IOException {
            Template tpl = new Template(name, replacements, override);
            tpl.save();
            return tpl;
        }

        public Builder from(NPC npc) {
            replacements.clear();
            MemoryDataKey key = new MemoryDataKey();
            ((CitizensNPC) npc).save(key);
            replacements.putAll(key.getValuesDeep());
            return this;
        }

        public Builder override(boolean override) {
            this.override = override;
            return this;
        }

        public static Builder create(String name) {
            return new Builder(name);
        }
    }

    private static class Node {
        String headKey;
        Map<String, Object> map;

        private Node(String headKey, Map<String, Object> map) {
            this.headKey = headKey;
            this.map = map;
        }
    }

    public static Template byName(String name) {
        if (TEMPLATES.containsKey(name))
            return TEMPLATES.get(name);
        File file = new File(getDirectory(), name + ".yml");
        if (!file.exists() || !file.getParentFile().equals(getDirectory()))
            return null;
        YamlStorage storage = new YamlStorage(file);
        storage.load();
        YamlKey key = storage.getKey("");
        boolean override = key.getBoolean("override", true);
        Map<String, Object> replacements = key.getRelative("replacements").getValuesDeep();
        Template res = new Template(name, replacements, override);
        TEMPLATES.put(name, res);
        return res;
    }

    private static File getDirectory() {
        return new File(CitizensAPI.getDataFolder(), "templates");
    }

    public static Iterable<Template> getTemplates() {
        return Arrays.asList(getDirectory().list()).stream().map(f -> new File(getDirectory(), f))
                .filter(f -> !f.isDirectory() && Files.getFileExtension(f.getName()).equals(".yml"))
                .map(f -> byName(Files.getNameWithoutExtension(f.getName()))).collect(Collectors.toList());
    }

    public static void migrate() {
        File folder = getDirectory();
        if (!folder.exists()) {
            folder.mkdir();
        }
        File from = new File(CitizensAPI.getDataFolder(), "templates.yml");
        if (from.exists()) {
            YamlStorage storage = new YamlStorage(from);
            storage.load();
            for (DataKey key : storage.getKey("").getSubKeys()) {
                String name = key.name();
                Map<String, Object> replacements = key.getRelative("replacements").getValuesDeep();
                boolean override = key.getBoolean("override", true);
                try {
                    new Template(name, replacements, override).save();
                } catch (IOException e) {
                    Messaging.severe("Unable to migrate template", name, "due to invalid filename");
                }
            }
            try {
                Files.move(from, new File(CitizensAPI.getDataFolder(), "templates_migrated.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void shutdown() {
        TEMPLATES.clear();
    }

    private static Map<String, Template> TEMPLATES = Maps.newHashMap();
}
