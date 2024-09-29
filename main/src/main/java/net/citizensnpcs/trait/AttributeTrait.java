package net.citizensnpcs.trait;

import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("attributetrait")
public class AttributeTrait extends Trait {
    @Persist(keyType = Attribute.class)
    private final Map<Attribute, Double> attributes = Maps.newEnumMap(Attribute.class);

    public AttributeTrait() {
        super("attributetrait");
    }

    public Double getAttributeValue(Attribute attribute) {
        return attributes.get(attribute);
    }

    public boolean hasAttribute(Attribute attribute) {
        return attributes.containsKey(attribute);
    }

    @Override
    public void onSpawn() {
        if (!(npc.getEntity() instanceof LivingEntity))
            return;
        LivingEntity le = (LivingEntity) npc.getEntity();
        for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
            le.getAttribute(entry.getKey()).setBaseValue(entry.getValue());
        }
    }

    public void setAttributeValue(Attribute attribute, double value) {
        attributes.put(attribute, value);
        onSpawn();
    }

    public void setDefaultAttribute(Attribute attribute) {
        attributes.remove(attribute);
        if (!(npc.getEntity() instanceof LivingEntity))
            return;

        LivingEntity le = (LivingEntity) npc.getEntity();
        AttributeInstance instance = le.getAttribute(attribute);
        instance.setBaseValue(instance.getDefaultValue());
    }
}