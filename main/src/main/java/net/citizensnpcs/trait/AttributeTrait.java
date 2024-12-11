package net.citizensnpcs.trait;

import java.util.Map;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Util;
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
    private final Map<Attribute, Double> attributes = Maps.newHashMap();

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
    public void load(DataKey key) throws NPCLoadException {
        for (Map.Entry<String, Object> entry : key.getValuesDeep().entrySet()) {
            final String rawAttributeName = entry.getKey();
            final Attribute attribute = Util.getAttribute(rawAttributeName);
            if (attribute != null) {
                final Object rawValue = entry.getValue();
                if (rawValue instanceof Double) {
                    attributes.put(attribute, (Double) rawValue);
                }
            }
        }
    }

    @Override
    public void onSpawn() {
        if (!(npc.getEntity() instanceof LivingEntity))
            return;
        LivingEntity le = (LivingEntity) npc.getEntity();
        for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
            final Attribute key = entry.getKey();
            final AttributeInstance attributeInstance = le.getAttribute(key);
            if (attributeInstance == null) { // not applicable anymore so ignore
                continue;
            }
            attributeInstance.setBaseValue(entry.getValue());
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