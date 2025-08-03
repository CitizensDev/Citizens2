package net.citizensnpcs.util;

import java.lang.invoke.MethodHandle;
import java.util.Collection;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class SkinProperty {
    public final String name;
    public final String signature;
    public final String value;

    public SkinProperty(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public void apply(GameProfile profile) {
        profile.getProperties().removeAll("textures"); // ensure client does not crash due to duplicate properties.
        profile.getProperties().put("textures", new com.mojang.authlib.properties.Property(name, value, signature));
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

    public static SkinProperty fromMojangProfile(GameProfile profile) {
        if (profile == null)
            return null;
        Collection<Property> textures = profile.getProperties().get("textures");
        if (textures == null || textures.size() == 0)
            return null;
        return fromMojang(textures.iterator().next());
    }

    private static MethodHandle GET_NAME_METHOD = null;
    private static MethodHandle GET_SIGNATURE_METHOD = null;
    private static MethodHandle GET_VALUE_METHOD = null;
    static {
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
