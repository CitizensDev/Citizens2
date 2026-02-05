package net.citizensnpcs.trait;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.npc.EntityController;
import net.citizensnpcs.npc.EntityControllers;

@TraitName("disguise")
public class DisguiseTrait extends Trait {
    private EntityController synthetic;
    @Persist
    private EntityType type;

    public DisguiseTrait() {
        super("disguise");
    }

    public void disguiseAsType(EntityType type) {
        this.type = type;
        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation());
        }
    }

    public Entity getCosmeticEntity() {
        if (type == null || synthetic == null)
            return null;
        return synthetic.getBukkitEntity();
    }

    public EntityType getDisguiseType() {
        return type;
    }

    @Override
    public void onDespawn() {
        synthetic = null;
    }

    @Override
    public void onSpawn() {
        if (type == null)
            return;
        this.synthetic = EntityControllers.createForType(type);
        this.synthetic.create(npc.getEntity().getLocation(), npc);
    }
}