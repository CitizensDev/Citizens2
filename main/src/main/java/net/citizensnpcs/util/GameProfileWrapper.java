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
import com.mojang.authlib.properties.PropertyMap;

public class GameProfileWrapper {
    public final String name;
    public Multimap<String, SkinProperty> properties;
    public final UUID uuid;

    public GameProfileWrapper(String name, UUID uuid, Multimap<String, SkinProperty> properties) {
        this.name = name;
        this.uuid = uuid;
        this.properties = properties;
    }

    public GameProfile applyProperties(Object profile) {
        try {
            Multimap mojang = HashMultimap.create();
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
            if (GAME_PROFILE_RECORD_CONSTRUCTOR != null) {
                return GAME_PROFILE_RECORD_CONSTRUCTOR.newInstance(getId(profile), getName(profile),
                        new PropertyMap(mojang));
            } else if (GAME_PROFILE_CONSTRUCTOR != null) {
                Object object = GAME_PROFILE_CONSTRUCTOR.newInstance(getId(profile), getName(profile));
                setProperties(object, mojang);
                return GameProfile.class.cast(object);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameProfileWrapper fromMojangProfile(Object profile) {
        if (profile == null)
            return null;
        return new GameProfileWrapper(getName(profile), getId(profile), getProperties(profile));
    }

    private static UUID getId(Object profile) {
        try {
            return (UUID) GET_ID_METHOD.invoke(profile);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getName(Object profile) {
        try {
            return (String) GET_NAME_METHOD.invoke(profile);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Multimap<String, SkinProperty> getProperties(Object profile) {
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

    private static void setProperties(Object profile, Multimap map) {
        try {
            Multimap<String, Object> mojang = (Multimap<String, Object>) PROPERTIES_METHOD.invoke(profile);
            mojang.clear();
            mojang.putAll(map);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static Constructor<GameProfile> GAME_PROFILE_CONSTRUCTOR;
    private static Constructor<GameProfile> GAME_PROFILE_RECORD_CONSTRUCTOR;
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
        try {
            GAME_PROFILE_RECORD_CONSTRUCTOR = com.mojang.authlib.GameProfile.class.getConstructor(UUID.class,
                    String.class, PropertyMap.class);
            GAME_PROFILE_RECORD_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                GAME_PROFILE_CONSTRUCTOR = com.mojang.authlib.GameProfile.class.getConstructor(UUID.class,
                        String.class);
                GAME_PROFILE_CONSTRUCTOR.setAccessible(true);
            } catch (NoSuchMethodException | SecurityException e1) {
                e1.printStackTrace();
            }
        }
        PROPERTIES_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "getProperties", false);
        if (PROPERTIES_METHOD == null) {
            PROPERTIES_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "properties", false);
        }
        GET_NAME_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "getName", false);
        if (GET_NAME_METHOD == null) {
            GET_NAME_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "name", false);
        }
        GET_ID_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "getId", false);
        if (GET_ID_METHOD == null) {
            GET_ID_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "id", false);
        }
    }
}
