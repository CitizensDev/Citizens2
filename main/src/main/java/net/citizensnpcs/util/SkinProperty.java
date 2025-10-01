package net.citizensnpcs.util;

import java.lang.invoke.MethodHandle;
import java.util.Collection;

import com.mojang.authlib.GameProfile;

public class SkinProperty {
    public final String name;
    public final String signature;
    public final String value;

    public SkinProperty(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public GameProfile applyProperties(Object profile) {
        GameProfileWrapper gpw = GameProfileWrapper.fromMojangProfile(profile);
        gpw.properties.removeAll("textures"); // ensure client does not crash due to duplicate properties.
        gpw.properties.put("textures", this);
        return gpw.applyProperties(profile);
    }

    public static SkinProperty fromMojang(Object prop) {
        if (prop == null)
            return null;
        try {
            return new SkinProperty((String) GET_NAME_METHOD.invoke(prop), (String) GET_VALUE_METHOD.invoke(prop),
                    (String) GET_SIGNATURE_METHOD.invoke(prop));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SkinProperty fromMojangProfile(Object profile) {
        if (profile == null)
            return null;
        GameProfileWrapper gpw = GameProfileWrapper.fromMojangProfile(profile);
        Collection<SkinProperty> textures = gpw.properties.get("textures");
        if (textures.size() == 0)
            return null;
        return gpw.properties.get("textures").iterator().next();
    }

    private static MethodHandle GET_NAME_METHOD = null;
    private static MethodHandle GET_SIGNATURE_METHOD = null;
    private static MethodHandle GET_VALUE_METHOD = null;
    private static MethodHandle PROPERTIES_METHOD = null;
    static {
        PROPERTIES_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "getProperties", false);
        if (PROPERTIES_METHOD == null) {
            PROPERTIES_METHOD = NMS.getMethodHandle(com.mojang.authlib.GameProfile.class, "properties", false);
        }
        GET_NAME_METHOD = NMS.getMethodHandle(com.mojang.authlib.properties.Property.class, "getName", false);
        if (GET_NAME_METHOD == null) {
            GET_NAME_METHOD = NMS.getMethodHandle(com.mojang.authlib.properties.Property.class, "name", false);
        }
        GET_SIGNATURE_METHOD = NMS.getMethodHandle(com.mojang.authlib.properties.Property.class, "getSignature", false);
        if (GET_SIGNATURE_METHOD == null) {
            GET_SIGNATURE_METHOD = NMS.getMethodHandle(com.mojang.authlib.properties.Property.class, "signature",
                    false);
        }
        GET_VALUE_METHOD = NMS.getMethodHandle(com.mojang.authlib.properties.Property.class, "getValue", false);
        if (GET_VALUE_METHOD == null) {
            GET_VALUE_METHOD = NMS.getMethodHandle(com.mojang.authlib.properties.Property.class, "value", false);
        }
    }
}
