package net.citizensnpcs.api.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.bukkit.entity.Cat;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

public class OldEnumCompat {
    public static class CatTypeEnum extends ReflectiveOldEnum<Cat.Type> {
        public CatTypeEnum(Cat.Type instance) {
            super(instance, METHODS);
        }

        public static CatTypeEnum valueOf(String name) {
            return reflectValueOf(name, METHODS, t -> new CatTypeEnum((Cat.Type) t));
        }

        public static CatTypeEnum[] values() {
            return reflectValues(METHODS, t -> new CatTypeEnum((Cat.Type) t));
        }

        private static final Methods METHODS = new Methods(Cat.Type.class);
    }

    public static class FrogVariantEnum extends ReflectiveOldEnum<Frog.Variant> {
        public FrogVariantEnum(Frog.Variant instance) {
            super(instance, METHODS);
        }

        public static FrogVariantEnum valueOf(String name) {
            return reflectValueOf(name, METHODS, t -> new FrogVariantEnum((Frog.Variant) t));
        }

        public static FrogVariantEnum[] values() {
            return reflectValues(METHODS, t -> new FrogVariantEnum((Frog.Variant) t));
        }

        private static final Methods METHODS = new Methods(Frog.Variant.class);
    }

    private static class Methods {
        private final Class<?> clazz;
        private final MethodHandle name;
        private final MethodHandle valueOf;
        private final MethodHandle values;

        public Methods(Class<?> clazz) {
            this.clazz = clazz;
            valueOf = getMethod(clazz, "valueOf", String.class);
            name = getMethod(clazz, "name");
            values = getMethod(clazz, "values");
        }
    }

    private static abstract class ReflectiveOldEnum<T> {
        private final T instance;
        private final Methods methods;

        private ReflectiveOldEnum(T t, Methods staticMethods) {
            instance = t;
            methods = staticMethods;
        }

        @Override
        public boolean equals(Object other) {
            try {
                return (boolean) OBJECT_EQUALS.invoke(other);
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }
        }

        public T getInstance() {
            return instance;
        }

        @Override
        public int hashCode() {
            try {
                return (int) OBJECT_HASHCODE.invoke(instance);
            } catch (Throwable e) {
                e.printStackTrace();
                return 0;
            }
        }

        public String name() {
            try {
                return (String) methods.name.invoke(instance);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            try {
                return (String) OBJECT_TOSTRING.invoke(instance);
            } catch (Throwable e) {
                e.printStackTrace();
                return "";
            }
        }

        protected static <T> T reflectValueOf(String name, Methods METHODS, Function<Object, T> constructor) {
            try {
                return constructor.apply(METHODS.valueOf.invoke(name));
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        protected static <T> T[] reflectValues(Methods METHODS, Function<Object, T> constructor) {
            try {
                Object[] values = (Object[]) METHODS.values.invoke();
                T[] result = (T[]) Array.newInstance(constructor.apply(values[0]).getClass(), values.length);
                for (int i = 0; i < values.length; i++) {
                    result[i] = constructor.apply(values[i]);
                }
                return result;
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class VillagerProfessionEnum extends ReflectiveOldEnum<Villager.Profession> {
        public VillagerProfessionEnum(Villager.Profession instance) {
            super(instance, METHODS);
        }

        public static VillagerProfessionEnum valueOf(String name) {
            return reflectValueOf(name, METHODS, t -> new VillagerProfessionEnum((Profession) t));
        }

        public static VillagerProfessionEnum[] values() {
            return reflectValues(METHODS, t -> new VillagerProfessionEnum((Profession) t));
        }

        private static final Methods METHODS = new Methods(Villager.Profession.class);
    }

    public static class VillagerTypeEnum extends ReflectiveOldEnum<Villager.Type> {
        public VillagerTypeEnum(Villager.Type instance) {
            super(instance, METHODS);
        }

        public static VillagerTypeEnum valueOf(String name) {
            return reflectValueOf(name, METHODS, t -> new VillagerTypeEnum((Villager.Type) t));
        }

        public static VillagerTypeEnum[] values() {
            return reflectValues(METHODS, t -> new VillagerTypeEnum((Villager.Type) t));
        }

        private static final Methods METHODS = new Methods(Villager.Type.class);
    }

    private static MethodHandle getMethod(Class<?> clazz, String method, Class<?>... params) {
        try {
            Method f = null;
            try {
                f = clazz.getMethod(method, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return MethodHandles.publicLookup().unreflect(f);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final MethodHandle OBJECT_EQUALS = getMethod(Object.class, "equals", Object.class);
    private static final MethodHandle OBJECT_HASHCODE = getMethod(Object.class, "hashCode");
    private static final MethodHandle OBJECT_TOSTRING = getMethod(Object.class, "toString");
}
