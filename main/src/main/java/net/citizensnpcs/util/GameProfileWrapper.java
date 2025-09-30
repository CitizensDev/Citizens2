package net.citizensnpcs.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;

public class GameProfileWrapper {
    public final String name;
    public Multimap<String, SkinProperty> properties;
    public final UUID uuid;

    public GameProfileWrapper(String name, UUID uuid, Multimap<String, SkinProperty> properties) {
        this.name = name;
        this.uuid = uuid;
        this.properties = properties;
    }

    public void applyProperties(GameProfile profile) {
        try {
            Multimap<String, Object> mojang = (Multimap<String, Object>) PROPERTIES_METHOD.invoke(profile);
            for (String key : properties.keySet()) {
                mojang.putAll(key, Collections2.transform(properties.get(key), p -> {
                    try {
                        return PROPERTY_CONSTRUCTOR.newInstance(p.name, p.value, p.signature);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return null;
                }));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static GameProfileWrapper fromMojangProfile(GameProfile profile) {
        if (profile == null)
            return null;
        return new GameProfileWrapper(getName(profile), getId(profile), getProperties(profile));
    }

    private static UUID getId(GameProfile profile) {
        try {
            return (UUID) GET_ID_METHOD.invoke(profile);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getName(GameProfile profile) {
        try {
            return (String) GET_NAME_METHOD.invoke(profile);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Multimap<String, SkinProperty> getProperties(GameProfile profile) {
        try {
            Multimap<String, Object> mojang = (Multimap<String, Object>) PROPERTIES_METHOD.invoke(profile);
            Multimap<String, SkinProperty> converted = HashMultimap.create();
            for (String key : mojang.keySet()) {
                converted.putAll(key,
                        mojang.get(key).stream().map(o -> SkinProperty.fromMojang(o)).collect(Collectors.toList()));
            }
            return converted;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MethodHandle GET_ID_METHOD = null;
    private static MethodHandle GET_NAME_METHOD = null;
    private static MethodHandle PROPERTIES_METHOD = null;
    private static Constructor<?> PROPERTY_CONSTRUCTOR;
    static {
        try {
            PROPERTY_CONSTRUCTOR = Class.forName("com.mojang.authlib.properties.Property").getConstructor(String.class,
                    String.class, String.class);
            PROPERTY_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        PROPERTIES_METHOD = NMS.getMethodHandle(GameProfile.class, "getProperties", false);
        if (PROPERTIES_METHOD == null) {
            PROPERTIES_METHOD = NMS.getMethodHandle(GameProfile.class, "properties", false);
        }
        GET_NAME_METHOD = NMS.getMethodHandle(GameProfile.class, "getName", false);
        if (GET_NAME_METHOD == null) {
            GET_NAME_METHOD = NMS.getMethodHandle(GameProfile.class, "name", false);
        }
        GET_ID_METHOD = NMS.getMethodHandle(GameProfile.class, "getId", false);
        if (GET_ID_METHOD == null) {
            GET_ID_METHOD = NMS.getMethodHandle(GameProfile.class, "id", false);
        }
    }
}
