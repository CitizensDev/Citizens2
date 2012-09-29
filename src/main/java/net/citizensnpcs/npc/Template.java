package net.citizensnpcs.npc;

import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.api.util.YamlStorage.YamlKey;

import com.google.common.collect.Maps;

public class Template {
    private final String name;
    private final boolean override;
    private final Map<String, Object> replacements;

    private Template(String name, Map<String, Object> replacements, boolean override) {
        this.replacements = replacements;
        this.override = override;
        this.name = name;
    }

    public void apply(NPC npc) {
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
            replacements.putAll(key.getRawTree());
            return this;
        }

        public TemplateBuilder override(boolean override) {
            this.override = override;
            return this;
        }

        public void save() {
            DataKey root = templates.getKey(name);
            root.setBoolean("override", override);
            root.setRaw("replacements", replacements);
            templates.save();
        }

        public static TemplateBuilder create(String name) {
            return new TemplateBuilder(name);
        }
    }

    private static YamlStorage templates = new YamlStorage(CitizensAPI.getDataFolder(), "templates.yml");
    public static Template byName(String name) {
        if (!templates.getKey("").keyExists(name))
            return null;
        YamlKey key = templates.getKey(name);
        boolean override = key.getBoolean("override", false);
        Map<String, Object> replacements = key.getRelative("replacements").getValuesDeep();
        return new Template(name, replacements, override);
    }

    static {
        templates.load();
    }
}
