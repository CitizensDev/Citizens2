package net.citizensnpcs.api.npc.templates;

import java.io.File;

import net.citizensnpcs.api.CitizensAPI;

public class TemplateWorkspace {
    private final File citizensFolder;
    private final File namespaceFolder;
    private final File templatesFolder;

    public TemplateWorkspace(File namespaceFolder) {
        this.namespaceFolder = namespaceFolder;
        this.citizensFolder = CitizensAPI.getDataFolder();
        this.templatesFolder = new File(citizensFolder, "templates");
    }

    public File getFile(String fileName) {
        File test = new File(namespaceFolder, fileName);
        return test.exists() && test.getParentFile().equals(namespaceFolder) ? test : null;
    }
}
