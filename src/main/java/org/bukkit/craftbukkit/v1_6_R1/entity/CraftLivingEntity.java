package org.bukkit.craftbukkit.v1_6_R1.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_6_R1.EntityLiving;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class CraftLivingEntity extends CraftEntity implements LivingEntity {
    @Deprecated
    public void _INVALID_damage(int amount) {
        damage(amount);
    }

    @Deprecated
    public void _INVALID_damage(int amount, Entity source) {
        damage(amount, source);
    }

    @Deprecated
    public int _INVALID_getHealth() {
        return NumberConversions.ceil(getHealth());
    }

    @Deprecated
    public int _INVALID_getLastDamage() {
        return NumberConversions.ceil(getLastDamage());
    }

    @Deprecated
    public int _INVALID_getMaxHealth() {
        return NumberConversions.ceil(getMaxHealth());
    }

    @Deprecated
    public void _INVALID_setHealth(int health) {
        setHealth(health);
    }

    @Deprecated
    public void _INVALID_setLastDamage(int damage) {
        setLastDamage(damage);
    }

    @Deprecated
    public void _INVALID_setMaxHealth(int health) {
        setMaxHealth(health);
    }

    
    public boolean addPotionEffect(PotionEffect effect) {
        return false;
    }

    
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        return false;
    }

    
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        return false;
    }

    
    public void damage(double amount) {
    }

    
    public void damage(double amount, Entity source) {
    }

    
    @Deprecated
    public void damage(int arg0) {
    }

    
    @Deprecated
    public void damage(int arg0, Entity arg1) {
    }

    
    public boolean eject() {
        return false;
    }

    
    public Collection<PotionEffect> getActivePotionEffects() {
        return null;
    }

    
    public boolean getCanPickupItems() {
        return false;
    }

    
    public String getCustomName() {
        return null;
    }

    
    public int getEntityId() {
        return 0;
    }

    
    public EntityEquipment getEquipment() {
        return null;
    }

    
    public double getEyeHeight() {
        return 0;
    }

    
    public double getEyeHeight(boolean ignoreSneaking) {
        return 0;
    }

    
    public Location getEyeLocation() {
        return null;
    }

    
    public float getFallDistance() {
        return 0;
    }

    
    public int getFireTicks() {
        return 0;
    }

    
    public EntityLiving getHandle() {
        return null;
    }

    
    public double getHealth() {
        return 0;
    }

    
    public Player getKiller() {
        return null;
    }

    
    public double getLastDamage() {
        return 0;
    }

    
    public EntityDamageEvent getLastDamageCause() {
        return null;
    }

    
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
        return null;
    }

    
    public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance) {
        return null;
    }

    
    public Location getLocation() {
        return null;
    }

    
    public Location getLocation(Location loc) {
        return null;
    }

    
    public int getMaxFireTicks() {
        return 0;
    }

    
    public double getMaxHealth() {
        return 0;
    }

    
    public int getMaximumAir() {
        return 0;
    }

    
    public int getMaximumNoDamageTicks() {
        return 0;
    }

    
    public List<MetadataValue> getMetadata(String metadataKey) {
        return null;
    }

    
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        return null;
    }

    
    public int getNoDamageTicks() {
        return 0;
    }

    
    public Entity getPassenger() {
        return null;
    }

    
    public int getRemainingAir() {
        return 0;
    }

    
    public boolean getRemoveWhenFarAway() {
        return false;
    }

    
    public Server getServer() {
        return null;
    }

    
    public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
        return null;
    }

    
    public int getTicksLived() {
        return 0;
    }

    
    public EntityType getType() {
        return null;
    }

    
    public UUID getUniqueId() {
        return null;
    }

    
    public Entity getVehicle() {
        return null;
    }

    
    public Vector getVelocity() {
        return null;
    }

    
    public World getWorld() {
        return null;
    }

    
    public boolean hasLineOfSight(Entity other) {
        return false;
    }

    
    public boolean hasMetadata(String metadataKey) {
        return false;
    }

    
    public boolean hasPotionEffect(PotionEffectType type) {
        return false;
    }

    
    public boolean isCustomNameVisible() {
        return false;
    }

    
    public boolean isDead() {
        return false;
    }

    
    public boolean isEmpty() {
        return false;
    }

    
    public boolean isInsideVehicle() {
        return false;
    }

    
    public boolean isOnGround() {
        return false;
    }

    
    public boolean isValid() {
        return false;
    }

    
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        return null;
    }

    
    public boolean leaveVehicle() {
        return false;
    }

    
    public void playEffect(EntityEffect type) {
    }

    
    public void remove() {
    }

    
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
    }

    
    public void removePotionEffect(PotionEffectType type) {
    }

    
    public void resetMaxHealth() {
    }

    
    public void setCanPickupItems(boolean pickup) {
    }

    
    public void setCustomName(String name) {
    }

    
    public void setCustomNameVisible(boolean flag) {
    }

    
    public void setFallDistance(float distance) {
    }

    
    public void setFireTicks(int ticks) {
    }

    
    public void setHealth(double health) {
    }

    
    @Deprecated
    public void setHealth(int arg0) {
    }

    
    public void setLastDamage(double damage) {
    }

    
    @Deprecated
    public void setLastDamage(int arg0) {
    }

    
    public void setLastDamageCause(EntityDamageEvent event) {
    }

    
    public void setMaxHealth(double health) {
    }

    
    @Deprecated
    public void setMaxHealth(int arg0) {
    }

    
    public void setMaximumAir(int ticks) {
    }

    
    public void setMaximumNoDamageTicks(int ticks) {
    }

    
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
    }

    
    public void setNoDamageTicks(int ticks) {
    }

    
    public boolean setPassenger(Entity passenger) {
        return false;
    }

    
    public void setRemainingAir(int ticks) {
    }

    
    public void setRemoveWhenFarAway(boolean remove) {
    }

    
    public void setTicksLived(int value) {
    }

    
    public void setVelocity(Vector velocity) {
    }

    
    @Deprecated
    public Arrow shootArrow() {
        return null;
    }

    
    public boolean teleport(Entity destination) {
        return false;
    }

    
    public boolean teleport(Entity destination, TeleportCause cause) {
        return false;
    }

    
    public boolean teleport(Location location) {
        return false;
    }

    
    public boolean teleport(Location location, TeleportCause cause) {
        return false;
    }

    
    @Deprecated
    public Egg throwEgg() {
        return null;
    }

    
    @Deprecated
    public Snowball throwSnowball() {
        return null;
    }
}
