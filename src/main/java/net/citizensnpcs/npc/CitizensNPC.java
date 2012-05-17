package net.citizensnpcs.npc;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.ai.CitizensAI;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntityLiving;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class CitizensNPC extends AbstractNPC {
    private final CitizensAI ai = new CitizensAI(this);
    protected EntityLiving mcEntity;
    private final CitizensTraitManager traitManager;

    protected CitizensNPC(int id, String name) {
        super(id, name);
        traitManager = (CitizensTraitManager) CitizensAPI.getTraitManager();
        // TODO: remove this dependency
    }

    @Override
    public void chat(Player player, String message) {
        Messaging.sendWithNPC(player, Setting.CHAT_PREFIX.asString() + message, this);
    }

    @Override
    public void chat(String message) {
        for (Player player : Bukkit.getOnlinePlayers())
            chat(player, message);
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
            removeMetadata("selectors", CitizensAPI.getPlugin());
        getBukkitEntity().remove();
        mcEntity = null;

        return true;
    }

    @Override
    public CitizensAI getAI() {
        return ai;
    }

    @Override
    public LivingEntity getBukkitEntity() {
        return (LivingEntity) getHandle().getBukkitEntity();
    }

    public EntityLiving getHandle() {
        return mcEntity;
    }

    @Override
    public org.bukkit.inventory.Inventory getInventory() {
        Inventory inventory = Bukkit.getServer().createInventory(this, 36, StringHelper.parseColors(getFullName()));
        inventory.setContents(getTrait(net.citizensnpcs.api.trait.trait.Inventory.class).getContents());
        return inventory;
    }

    @Override
    public Trait getTraitFor(Class<? extends Trait> clazz) {
        return traitManager.getTrait(clazz, this);
    }

    @Override
    public boolean isSpawned() {
        return getHandle() != null;
    }

    public void load(DataKey root) {
        // Spawn the NPC
        if (getTrait(Spawned.class).shouldSpawn()) {
            Location spawnLoc = getTrait(CurrentLocation.class).getLocation();
            if (spawnLoc != null)
                spawn(spawnLoc);
        }

        Character character = CitizensAPI.getCharacterManager().getCharacter(root.getString("character"));

        // Load the character if it exists
        if (character != null) {
            try {
                character.load(root.getRelative("characters." + character.getName()));
            } catch (NPCLoadException e) {
                Messaging.severe(String.format("Unable to load character '%s': %s.", character.getName(),
                        e.getMessage()));
            }
            setCharacter(character);
        }

        // Load traits
        for (DataKey traitKey : root.getRelative("traits").getSubKeys()) {
            Trait trait = traitManager.getTrait(traitKey.name(), this);
            if (trait == null) {
                Messaging.severe(String.format(
                        "Skipped missing trait '%s' while loading NPC ID: '%d'. Has the name changed?",
                        traitKey.name(), getId()));
                continue;
            }
            addTrait(trait);
            try {
                getTrait(trait.getClass()).load(traitKey);
            } catch (NPCLoadException ex) {
                Messaging.log(
                        String.format("The trait '%s' failed to load for NPC ID: '%d'.", traitKey.name(), getId()),
                        ex.getMessage());
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        CitizensAPI.getNPCRegistry().deregister(this);
    }

    public void save(DataKey root) {
        root.setString("name", getFullName());

        // Save the character if it exists
        if (getCharacter() != null) {
            root.setString("character", getCharacter().getName());
            getCharacter().save(root.getRelative("characters." + getCharacter().getName()));
        }

        // Save all existing traits
        for (Trait trait : traits.values()) {
            trait.save(root.getRelative("traits." + trait.getName()));
        }
    }

    @Override
    public void setName(String name) {
        super.setName(name);
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

        mcEntity.world.addEntity(mcEntity);
        mcEntity.world.players.remove(mcEntity);

        // Set the spawned state
        getTrait(CurrentLocation.class).setLocation(loc);
        getTrait(Spawned.class).setSpawned(true);

        // Modify NPC using traits after the entity has been created
        for (Trait trait : traits.values())
            trait.onNPCSpawn();
        return true;
    }

    @Override
    public void update() {
        try {
            super.update();
            ai.update();
        } catch (Exception ex) {
            Messaging.log("Exception while updating " + getId() + ": " + ex.getMessage() + ".");
            ex.printStackTrace();
        }
    }
}