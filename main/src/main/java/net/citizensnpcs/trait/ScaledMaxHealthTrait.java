package net.citizensnpcs.trait;

import java.math.BigDecimal;

import net.citizensnpcs.util.Util;
import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitEventHandler;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("scaledhealthtrait")
public class ScaledMaxHealthTrait extends Trait {
    @Persist
    private Double maxHealth;

    public ScaledMaxHealthTrait() {
        super("scaledhealthtrait");
    }

    public Double getMaxHealth() {
        return maxHealth;
    }

    @TraitEventHandler(@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true))
    public void onDamage(NPCDamageEvent event) {
        if (maxHealth == null || (npc.getEntity() instanceof LivingEntity))
            return;
        event.setCancelled(true);
        LivingEntity entity = (LivingEntity) npc.getEntity();
        entity.setHealth(new BigDecimal(entity.getHealth() - (Math.min(maxHealth, MAX_VALUE) / event.getDamage()))
                .setScale(0, java.math.RoundingMode.HALF_DOWN).doubleValue());
    }

    @Override
    public void onSpawn() {
        if (maxHealth != null && npc.getEntity() instanceof LivingEntity) {
            if (SUPPORTS_ATTRIBUTES) {
                ((LivingEntity) npc.getEntity()).getAttribute(Util.getRegistryValue(Registry.ATTRIBUTE, "generic.max_health", "max_health"))
                        .setBaseValue(Math.min(MAX_VALUE, maxHealth));
            } else {
                ((LivingEntity) npc.getEntity()).setMaxHealth(maxHealth);
            }
        }
    }

    public void setMaxHealth(Double maxHealth) {
        this.maxHealth = maxHealth;
    }

    private static int MAX_VALUE = 2048;
    private static boolean SUPPORTS_ATTRIBUTES = true;
    static {
        try {
            Class.forName("org.bukkit.attribute.Attribute");
        } catch (ClassNotFoundException e) {
            SUPPORTS_ATTRIBUTES = false;
        }
    }
}
