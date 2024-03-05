package net.citizensnpcs.api.npc.templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;

public class TemplateRegistry {
    private final Path baseFolder;
    private final Map<String, Template> fullyQualifiedTemplates = Maps.newHashMap();
    private final Multimap<String, Template> templatesByName = HashMultimap.create();

    public TemplateRegistry(Path folder) {
        this.baseFolder = folder;
        if (!folder.toFile().exists()) {
            folder.toFile().mkdir();
        }
        loadTemplates(baseFolder);
    }

    public Collection<Template> getAllTemplates() {
        return fullyQualifiedTemplates.values();
    }

    public Template getTemplateByKey(String namespace, String name) {
        return fullyQualifiedTemplates.get(namespace + ":" + name);
    }

    public Template getTemplateByNamespacedKey(String key) {
        return fullyQualifiedTemplates.get(key);
    }

    public Collection<Template> getTemplates(String name) {
        return templatesByName.get(name);
    }

    public boolean hasNamespace(String namespace) {
        for (String key : fullyQualifiedTemplates.keySet()) {
            if (key.split(":")[0].equalsIgnoreCase(namespace))
                return true;
        }
        return false;
    }

    private void loadTemplate(File folder, String namespace, DataKey key) throws TemplateLoadException {
        if (fullyQualifiedTemplates.containsKey(namespace + ":" + key.name()))
            throw new TemplateLoadException("Duplicate template key " + namespace + ":" + key.name());

        Template template = Template.load(new TemplateWorkspace(folder), namespace, key);
        fullyQualifiedTemplates.put(namespace + ":" + key.name(), template);
        templatesByName.put(key.name(), template);
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
