package net.citizensnpcs.api.npc.templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.bukkit.NamespacedKey;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;

public class TemplateRegistry {
    private final Path baseFolder;
    private final Map<NamespacedKey, Template> fullyQualifiedTemplates = Maps.newHashMap();
    private final Multimap<String, Template> templatesByName = HashMultimap.create();

    public TemplateRegistry(Path folder) {
        this.baseFolder = folder;
        if (!folder.toFile().exists()) {
            folder.toFile().mkdir();
        }
        loadTemplates(baseFolder);
    }

    public void generateTemplateFromNPC(NamespacedKey key, NPC npc) {
        String namespace = key.getNamespace();
        String file = "templates.yml";
        File namespaceFolder = new File(baseFolder.toFile(), namespace);
        namespaceFolder.mkdirs();
        File generatedFile = new File(namespaceFolder, file);
        Storage templateStorage = new YamlStorage(generatedFile);
        if (!templateStorage.load())
            throw new IllegalStateException();
        DataKey root = templateStorage.getKey(key.getKey());
        npc.save(root.getRelative("yaml_replace.replacements"));
        root.setBoolean("yaml_replace.override", true);
        root.removeKey("yaml_replace.replacements.uuid");
        templateStorage.save();
        try {
            fullyQualifiedTemplates.remove(key);
            loadTemplatesFromYamlFile(namespace, generatedFile);
        } catch (TemplateLoadException e) {
            e.printStackTrace();
        }
    }

    public Collection<Template> getAllTemplates() {
        return fullyQualifiedTemplates.values();
    }

    public Template getTemplateByKey(NamespacedKey key) {
        return fullyQualifiedTemplates.get(key);
    }

    public Collection<Template> getTemplates(String name) {
        return templatesByName.get(name);
    }

    public boolean hasNamespace(String namespace) {
        return fullyQualifiedTemplates.keySet().stream().anyMatch(k -> k.getNamespace().equals(namespace));
    }

    private void loadTemplate(File folder, String namespace, DataKey key) throws TemplateLoadException {
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key.name().toLowerCase(Locale.ROOT));
        if (fullyQualifiedTemplates.containsKey(namespacedKey))
            throw new TemplateLoadException("Duplicate template key " + namespacedKey);

        Template template = Template.load(new TemplateWorkspace(folder), namespacedKey, key);
        fullyQualifiedTemplates.put(namespacedKey, template);
        templatesByName.put(namespacedKey.getKey(), template);
    }

    private void loadTemplates(Path folder) {
        try {
            // migrate first
            Files.walk(folder, 1).forEach(path -> {
                File namespaceFile = path.toFile();
                if (namespaceFile.isFile() && namespaceFile.getName().endsWith(".yml")) {
                    try {
                        migrateOldTemplate(folder.toFile(), namespaceFile);
                    } catch (TemplateLoadException e) {
                        Messaging.severe("Error migrating " + namespaceFile.getName() + ": " + e.getMessage());
                    }
                }
            });

            // load templates
            Files.walk(folder, 1).forEach(namespacePath -> {
                File namespaceFile = namespacePath.toFile();
                if (namespaceFile.isDirectory() && !namespaceFile.getName().contains(":")) {
                    try {
                        Files.walk(namespacePath, 1).forEach(templatePath -> {
                            File templateFile = templatePath.toFile();
                            if (templateFile.isFile() && templateFile.getName().endsWith(".yml")) {
                                try {
                                    loadTemplatesFromYamlFile(namespaceFile.getName(), templateFile);
                                } catch (TemplateLoadException e) {
                                    Messaging.severe("Error loading " + templateFile.getName() + ": " + e.getMessage());
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            Messaging.log("Loaded", fullyQualifiedTemplates.size(), "templates.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTemplatesFromYamlFile(String namespace, File file) throws TemplateLoadException {
        YamlStorage storage = new YamlStorage(file);
        if (!storage.load())
            throw new TemplateLoadException("Unable to load " + file.getName());

        DataKey root = storage.getKey("");
        for (DataKey templateKey : root.getSubKeys()) {
            loadTemplate(file.getParentFile(), namespace, templateKey);
        }
    }

    private void migrateOldTemplate(File folder, File template) throws TemplateLoadException {
        Messaging.log("Migrating template", template.getName());
        Storage storage = new YamlStorage(template);
        if (!storage.load())
            throw new TemplateLoadException("Unable to migrate " + template.getName());

        File namespaceFolder = new File(folder, "migrated");
        if (!namespaceFolder.exists() && !namespaceFolder.mkdir())
            throw new TemplateLoadException(
                    "Unable to create destination folder while migrating " + template.getName());

        String templateName = template.getName().replace(".yml", "");
        Storage destination = new YamlStorage(new File(namespaceFolder, template.getName()));

        DataKey from = storage.getKey("");
        DataKey dest = destination.getKey(templateName + ".yaml_replace");
        dest.setBoolean("override", from.getBoolean("override"));
        dest.setRaw("replacements", from.getRelative("replacements").getValuesDeep());
        destination.save();

        template.renameTo(new File(folder, template.getName() + ".migrated"));
    }

    @SuppressWarnings("serial")
    private static class TemplateLoadException extends Exception {
        public TemplateLoadException(String string) {
            super(string);
        }
    }
}
