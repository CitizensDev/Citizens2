package net.citizensnpcs.trait;

import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Util;

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
        for (DataKey subkey : key.getRelative("attributes").getSubKeys()) {
            if (Util.getAttribute(subkey.name()) == null) {
                key.removeKey("attributes." + subkey.name());
            }
        }
        attributes.remove(null);
    }

    @Override
    public void onSpawn() {
        if (!(npc.getEntity() instanceof LivingEntity))
            return;
        LivingEntity le = (LivingEntity) npc.getEntity();
        for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
            final Attribute key = entry.getKey();
            final AttributeInstance attributeInstance = le.getAttribute(key);
            if (attributeInstance == null)
                continue;

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