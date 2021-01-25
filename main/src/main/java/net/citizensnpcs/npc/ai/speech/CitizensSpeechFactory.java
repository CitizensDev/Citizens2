package net.citizensnpcs.npc.ai.speech;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.google.common.base.Preconditions;

import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;

public class CitizensSpeechFactory implements SpeechFactory {
    Map<String, Class<? extends VocalChord>> registered = new HashMap<String, Class<? extends VocalChord>>();

    @Override
    public VocalChord getVocalChord(Class<? extends VocalChord> clazz) {
        Preconditions.checkNotNull(clazz, "class cannot be null");
        // Return a new instance of the VocalChord specified
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public VocalChord getVocalChord(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        // Check if VocalChord name is a registered type
        if (isRegistered(name)) {
            // Return a new instance of the VocalChord specified
            try {
                return registered.get(name.toLowerCase()).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getVocalChordName(Class<? extends VocalChord> clazz) {
        // Get the name of a VocalChord class that has been registered
        for (Entry<String, Class<? extends VocalChord>> vocalChord : registered.entrySet())
            if (vocalChord.getValue() == clazz)
                return vocalChord.getKey();

        return null;
    }

    @Override
    public boolean isRegistered(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        return registered.containsKey(name.toLowerCase());
    }

    @Override
    public Talkable newTalkableEntity(Entity entity) {
        if (entity == null)
            return null;
        return new TalkableEntity(entity);
    }

    @Override
    public Talkable newTalkableEntity(LivingEntity entity) {
        return newTalkableEntity((Entity) entity);
    }

    @Override
    public void register(Class<? extends VocalChord> clazz, String name) {
        Preconditions.checkNotNull(name, "info cannot be null");
        Preconditions.checkNotNull(clazz, "vocalchord cannot be null");
        if (registered.containsKey(name.toLowerCase()))
            throw new IllegalArgumentException("vocalchord name already registered");
        registered.put(name.toLowerCase(), clazz);
    }

}
