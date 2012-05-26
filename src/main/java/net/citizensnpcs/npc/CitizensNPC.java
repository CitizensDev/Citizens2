package net.citizensnpcs.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.LivingEntity;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.attachment.builtin.Spawned;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messaging;

public class CitizensNPC extends AbstractNPC {
    public CitizensNPC(String name) {
        super(CitizensAPI.getNPCRegistry(), name);
    }

    public CitizensNPC(NPCRegistry registry, String name) {
        super(registry, name);
    }

    @Override
    public boolean despawn() {
        if (!isSpawned()) {
            Messaging.debug(String.format("The NPC with the ID '%d' is already despawned.", getId()));
            return false;
        }

        CitizensAPI.getServer().callEvent(new NPCDespawnEvent(this));
        getEntity().remove();
        controller = null;

        return true;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) controller.getEntity();
    }

    @Override
    public boolean isSpawned() {
        return getEntity() != null;
    }

    @Override
    protected Attachment getAttachmentFor(Class<? extends Attachment> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    public void load(DataKey root) {
        // Load traits
        for (DataKey attachmentKey : root.getRelative("traits").getSubKeys()) {
            Attachment trait = attachmentFactory.getTrait(attachmentKey.name(), this);
            if (trait == null) {
                Messaging.severeF("Skipped missing attachment '%s' while loading NPC ID: '%d'. Has the name changed?",
                        attachmentKey.name(), getId());
                continue;
            }
            addAttachment(trait);
            try {
                getAttachment(trait.getClass()).load(attachmentKey);
            } catch (NPCLoadException ex) {
                Messaging.logF("The attachment '%s' failed to load for NPC ID: '%d'.", attachmentKey.name(), getId(),
                        ex.getMessage());
            }
        }

        // Spawn the NPC
        if (getAttachment(Spawned.class).shouldSpawn()) {
            WorldVector spawnLoc = getAttachment(CurrentLocation.class).getLocation();
            if (spawnLoc != null)
                spawn(spawnLoc);
        }
    }

    public void save(DataKey root) {
        root.setString("name", getFullName());
        // Save all existing traits
        for (Attachment trait : attachments.values()) {
            trait.save(root.getRelative("traits." + trait.getName()));
        }
    }

    @Override
    public boolean spawn(WorldVector at) {
        if (at == null)
            throw new IllegalArgumentException("location cannot be null");
        if (isSpawned()) {
            Messaging.debug("NPC (ID: " + getId() + ") is already spawned.");
            return false;
        }
        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, at);
        CitizensAPI.getServer().callEvent(spawnEvent);
        if (spawnEvent.isCancelled())
            return false;

        controller.spawn(at);

        // Set the spawned state
        getAttachment(CurrentLocation.class).setLocation(at);
        getAttachment(Spawned.class).setSpawned(true);

        // Modify NPC using traits after the entity has been created
        for (Attachment attached : attachments.values())
            attached.onSpawn();
        return true;
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