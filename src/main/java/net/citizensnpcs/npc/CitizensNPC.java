package net.citizensnpcs.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messaging;
import net.minecraft.server.EntityLiving;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public abstract class CitizensNPC extends AbstractNPC {
    protected EntityLiving mcEntity;
    private final CitizensNavigator navigator = new CitizensNavigator(this);

    protected CitizensNPC(int id, String name) {
        super(id, name);
    }

    protected abstract EntityLiving createHandle(Location loc);

    @Override
    public boolean despawn() {
        if (!isSpawned()) {
            Messaging.debug(String.format("The NPC with the ID '%d' is already despawned.", getId()));
            return false;
        }

        Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));
        boolean keepSelected = getTrait(Spawned.class).shouldSpawn();
        if (!keepSelected)
            data().remove("selectors");
        getBukkitEntity().remove();
        mcEntity = null;

        return true;
    }

    @Override
    public LivingEntity getBukkitEntity() {
        if (getHandle() == null)
            return null;
        return (LivingEntity) getHandle().getBukkitEntity();
    }

    public EntityLiving getHandle() {
        return mcEntity;
    }

    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public Trait getTraitFor(Class<? extends Trait> clazz) {
        return CitizensAPI.getTraitFactory().getTrait(clazz);
    }

    @Override
    public boolean isSpawned() {
        return getHandle() != null;
    }

    public void load(DataKey root) {
        metadata.loadFrom(root.getRelative("metadata"));
        // Load traits
        for (DataKey traitKey : root.getRelative("traits").getSubKeys()) {
            if (traitKey.keyExists("enabled") && !traitKey.getBoolean("enabled"))
                continue;
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitKey.name());
            Trait trait;
            if (hasTrait(clazz)) {
                trait = getTrait(clazz);
            } else {
                trait = CitizensAPI.getTraitFactory().getTrait(traitKey.name());
                if (trait == null) {
                    Messaging
                            .severeF(
                                    "Skipped broken or missing trait '%s' while loading ID '%d'. Has the name changed?",
                                    traitKey.name(), getId());
                    continue;
                }
                addTrait(trait);
            }
            try {
                trait.load(traitKey);
            } catch (NPCLoadException ex) {
                Messaging.logF("The trait '%s' failed to load for NPC ID: '%d'.", traitKey.name(), getId(),
                        ex.getMessage());
            }
        }

        // Spawn the NPC
        if (getTrait(Spawned.class).shouldSpawn()) {
            Location spawnLoc = getTrait(CurrentLocation.class).getLocation();
            if (spawnLoc != null)
                spawn(spawnLoc);
        }

        navigator.load(root.getRelative("navigator"));
    }

    public void save(DataKey root) {
        root.setString("name", getFullName());

        metadata.saveTo(root.getRelative("metadata"));
        navigator.save(root.getRelative("navigator"));

        // Save all existing traits
        for (Trait trait : traits.values()) {
            trait.save(root.getRelative("traits." + trait.getName()));
        }
    }

    @Override
    public boolean spawn(Location loc) {
        Validate.notNull(loc, "location cannot be null");
        if (isSpawned()) {
            Messaging.debug("NPC (ID: " + getId() + ") is already spawned.");
            return false;
        }
        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled())
            return false;

        mcEntity = createHandle(loc);

        mcEntity.world.addEntity(mcEntity, SpawnReason.CUSTOM);
        mcEntity.world.players.remove(mcEntity);

        // Set the spawned state
        getTrait(CurrentLocation.class).setLocation(loc);
        getTrait(Spawned.class).setSpawned(true);

        navigator.onSpawn();
        // Modify NPC using traits after the entity has been created
        for (Trait trait : traits.values())
            trait.onSpawn();
        return true;
    }

    @Override
    public void update() {
        try {
            super.update();
            if (isSpawned())
                navigator.update();
        } catch (Exception ex) {
            Messaging.logF("Exception while updating %d: %s.", getId(), ex.getMessage());
            ex.printStackTrace();
        }
    }
}