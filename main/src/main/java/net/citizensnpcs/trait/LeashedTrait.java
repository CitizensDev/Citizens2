package net.citizensnpcs.trait;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("leashedtrait")
public class LeashedTrait extends Trait {
    @Persist
    private Location leashedLocation;
    @Persist
    private UUID leashedTo;

    public LeashedTrait() {
        super("leashedtrait");
    }

    @Override
    public void onSpawn() {
        if (!npc.getEntity().getType().isAlive())
            return;
        LivingEntity le = (LivingEntity) npc.getEntity();

        Entity entity;
        if (leashedTo != null && (entity = Bukkit.getEntity(leashedTo)) != null) {
            le.setLeashHolder(entity);
        }
        if (leashedLocation != null) {
            LeashHitch hitch = npc.getEntity().getWorld().spawn(leashedLocation, LeashHitch.class);
            if (hitch != null) {
                le.setLeashHolder(hitch);
            }
        }
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !npc.getEntity().getType().isAlive()
                || npc.data().get(NPC.Metadata.LEASH_PROTECTED, npc.isProtected()))
            return;
        LivingEntity le = (LivingEntity) npc.getEntity();
        if (!le.isLeashed())
            return;

        Entity holder = le.getLeashHolder();
        if (holder instanceof LeashHitch) {
            if (leashedLocation == null) {
                leashedLocation = holder.getLocation();
            } else if (leashedLocation != null && (leashedLocation.getWorld() != holder.getLocation().getWorld()
                    || holder.getLocation().distanceSquared(leashedLocation) > 1)) {
                leashedLocation = holder.getLocation();
            }
            leashedTo = null;
        } else {
            leashedLocation = null;
            leashedTo = holder.getUniqueId();
        }
    }
}
