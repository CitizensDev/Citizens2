package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.abstraction.Server;
import net.citizensnpcs.api.attachment.AttachmentFactory;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.npc.CitizensAttachmentFactory;
import net.citizensnpcs.npc.NPCSelector;

public class Citizens implements CitizensPlugin {
    private final CitizensAttachmentFactory attachmentFactory = new CitizensAttachmentFactory();
    private final File dataFolder;
    private final Server server;
    private final NPCSelector selector = new NPCSelector();
    private final NPCRegistry registry;

    public Citizens(Server server, NPCRegistry registry, File dataFolder) {
        this.server = server;
        this.registry = registry;
        this.dataFolder = dataFolder;
    }

    public NPCSelector getNPCSelector() {
        return selector;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public NPCRegistry getNPCRegistry() {
        return registry;
    }

    @Override
    public File getScriptFolder() {
        return new File(getDataFolder(), "scripts");
    }

    @Override
    public AttachmentFactory getAttachmentFactory() {
        return attachmentFactory;
    }

    @Override
    public Server getServer() {
        return server;
    }
}
