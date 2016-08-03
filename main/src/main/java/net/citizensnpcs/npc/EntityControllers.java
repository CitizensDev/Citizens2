package net.citizensnpcs.npc;

import java.util.Map;

import org.bukkit.entity.EntityType;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class EntityControllers {
    public static boolean controllerExistsForType(EntityType type) {
        return TYPES.containsKey(type);
    }

    public static EntityController createForType(EntityType type) {
        Class<? extends EntityController> controllerClass = TYPES.get(type);
        if (controllerClass == null)
            throw new IllegalArgumentException("Unknown EntityType: " + type);
        try {
            return controllerClass.newInstance();
        } catch (Throwable ex) {
            Throwables.getRootCause(ex).printStackTrace();
            return null;
        }
    }

    public static void setEntityControllerForType(EntityType type, Class<? extends EntityController> controller) {
        TYPES.put(type, controller);
    }

    private static final Map<EntityType, Class<? extends EntityController>> TYPES = Maps.newEnumMap(EntityType.class);
}
