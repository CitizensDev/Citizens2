package net.citizensnpcs.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messaging;

public class CitizensNPC extends AbstractNPC {
    private boolean spawned;

    public CitizensNPC(NPCRegistry registry, String name) {
        super(registry, name);
    }

    public CitizensNPC(String name) {
        super(CitizensAPI.getNPCRegistry(), name);
    }

    @Override
    protected Attachment getAttachmentFor(Class<? extends Attachment> clazz) {
        return CitizensAPI.getAttachmentFactory().getAttachment(clazz);
    }

    private Attachment getAttachmentFor(String name) {
        return CitizensAPI.getAttachmentFactory().getAttachment(name);
    }

    public void load(DataKey root) {
        // Load traits
        for (DataKey attachmentKey : root.getRelative("traits").getSubKeys()) {
            Attachment trait = getAttachmentFor(attachmentKey.name());
            if (trait == null) {
                Messaging.severeF("Skipped missing attachment '%s' while loading NPC ID: '%d'. Has the name changed?",
                        attachmentKey.name(), getId());
                continue;
            }
            attach(trait);
            try {
                getAttachment(trait.getClass()).load(attachmentKey);
            } catch (NPCLoadException ex) {
                Messaging.logF("The attachment '%s' failed to load for NPC ID: '%d'.", attachmentKey.name(), getId(),
                        ex.getMessage());
            }
        }

        // Spawn the NPC
        spawned = root.getBoolean("spawned");
        if (spawned) {
            WorldVector spawnLoc = getAttachment(CurrentLocation.class).getLocation();
            if (spawnLoc != null)
                spawn(spawnLoc);
        }
    }

    public void save(DataKey root) {
        root.setString("name", name);
        // Save all existing traits
        for (Attachment trait : attachments.values()) {
            trait.save(root.getRelative("traits." + trait.getName()));
        }
    }

    @Override
    public void update() {
        try {
            super.update();
        } catch (Exception ex) {
            Messaging.logF("Exception while updating %d: %s.", getId(), ex.getMessage());
            ex.printStackTrace();
        }
    }
}