package net.citizensnpcs.api.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Location;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

public class PersistenceLoader {
    private static class PersistField {
        private final Persister<?> delegate;
        private final Field field;
        private final Object instance;
        private final String key;
        private final Persist persistAnnotation;
        private Object value;

        private PersistField(Field field, Object instance) {
            this.field = field;
            this.persistAnnotation = field.getAnnotation(Persist.class);
            this.key = persistAnnotation.value().equals("UNINITIALISED") ? field.getName() : persistAnnotation.value();
            Class<?> fallback = field.getType();
            if (field.getGenericType() instanceof ParameterizedType) {
                fallback = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            }
            this.delegate = getDelegate(field, fallback);
            this.instance = instance;
        }

        @SuppressWarnings("unchecked")
        public <T> T get() {
            if (value == null)
                try {
                    value = field.get(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                    value = NULL;
                }
            if (value == NULL)
                return null;
            return (T) value;
        }

        public Class<?> getCollectionType() {
            return persistAnnotation.collectionType();
        }

        public Class<?> getType() {
            return field.getType();
        }

        public boolean isRequired() {
            return persistAnnotation.required();
        }

        public void set(Object value) {
            try {
                field.set(instance, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static final Object NULL = new Object();
    }

    private static String createRelativeKey(String key, int ext) {
        return createRelativeKey(key, Integer.toString(ext));
    }

    private static String createRelativeKey(String parent, String ext) {
        if (ext.isEmpty())
            return parent;
        if (ext.charAt(0) == '.')
            return parent.isEmpty() ? ext.substring(1, ext.length()) : parent + ext;
        return parent.isEmpty() ? ext : parent + '.' + ext;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void deserialise(PersistField field, DataKey root) throws Exception {
        Object value;
        Class<?> type = field.getType();
        Class<?> collectionType = field.getCollectionType();
        // TODO: this is pretty ugly.
        if (!Collection.class.isAssignableFrom(collectionType) && !Map.class.isAssignableFrom(collectionType))
            throw loadException;
        if (List.class.isAssignableFrom(type)) {
            List<Object> list = (List<Object>) (!List.class.isAssignableFrom(collectionType) ? Lists.newArrayList()
                    : collectionType.newInstance());
            Object raw = root.getRaw(field.key);
            if (raw instanceof List && collectionType.isAssignableFrom(raw.getClass()))
                list = (List<Object>) raw;
            else
                deserialiseCollection(list, root, field);
            value = list;
        } else if (Set.class.isAssignableFrom(type)) {
            Set<Object> set;
            if (Set.class.isAssignableFrom(collectionType)) {
                set = (Set<Object>) collectionType.newInstance();
            } else {
                if (field.getType().isEnum()) {
                    set = EnumSet.noneOf((Class<? extends Enum>) field.getType());
                } else {
                    set = (Set<Object>) (field.get() != null && Set.class.isAssignableFrom(field.get().getClass()) ? field
                            .get().getClass().newInstance()
                            : Sets.newHashSet());
                }
            }
            Object raw = root.getRaw(field.key);
            if (raw instanceof Set && collectionType.isAssignableFrom(raw.getClass())) {
                set = (Set<Object>) raw;
            } else
                deserialiseCollection(set, root, field);
            value = set;
        } else if (Map.class.isAssignableFrom(type)) {
            Map<String, Object> map;
            if (Map.class.isAssignableFrom(collectionType)) {
                map = (Map<String, Object>) collectionType.newInstance();
            } else {
                map = (Map<String, Object>) (field.get() != null && Map.class.isAssignableFrom(field.get().getClass())
                        && !field.get().getClass().isInterface() ? field.get() : Maps.newHashMap());
            }
            deserialiseMap(map, root, field);
            value = map;
        } else
            value = deserialiseValue(field, root.getRelative(field.key));
        if (value == null && field.isRequired())
            throw loadException;
        if (type.isPrimitive()) {
            if (value == null)
                return;
            type = Primitives.wrap(type);
        }
        if (value != null && !type.isAssignableFrom(value.getClass()))
            return;
        field.set(value);
    }

    private static void deserialiseCollection(Collection<Object> collection, DataKey root, PersistField field) {
        for (DataKey subKey : root.getRelative(field.key).getSubKeys()) {
            Object loaded = deserialiseValue(field, subKey);
            if (loaded == null)
                continue;
            collection.add(loaded);
        }
    }

    private static void deserialiseMap(Map<String, Object> map, DataKey root, PersistField field) {
        for (DataKey subKey : root.getRelative(field.key).getSubKeys()) {
            Object loaded = deserialiseValue(field, subKey);
            if (loaded == null)
                continue;
            map.put(subKey.name(), loaded);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object deserialiseValue(PersistField field, DataKey root) {
        Class<?> type = field.field.getType().isEnum() ? field.field.getType() : getGenericType(field.field);
        if (field.delegate == null && type.isEnum()) {
            Class<? extends Enum> clazz = (Class<? extends Enum>) type;
            Object obj = root.getRaw("");
            if (obj instanceof String) {
                try {
                    return Enum.valueOf(clazz, obj.toString());
                } catch (IllegalArgumentException e) {
                    // fallback to default
                }
            }
        }
        return field.delegate == null ? root.getRaw("") : field.delegate.create(root);

    }

    private static void ensureDelegateLoaded(Class<? extends Persister<?>> delegateClass) {
        if (loadedDelegates.containsKey(delegateClass))
            return;
        try {
            loadedDelegates.put(delegateClass, delegateClass.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            loadedDelegates.put(delegateClass, null);
        }
    }

    private static Persister<?> getDelegate(Field field, Class<?> fallback) {
        DelegatePersistence delegate = field.getAnnotation(DelegatePersistence.class);
        Persister<?> persister;
        if (delegate == null) {
            persister = loadedDelegates.get(persistRedirects.get(fallback));
            if (persister == null)
                return null;
        } else
            persister = loadedDelegates.get(delegate.value());
        if (persister == null)
            persister = loadedDelegates.get(persistRedirects.get(fallback));
        return persister;
    }

    private static Field[] getFields(Class<?> clazz) {
        Field[] fields = fieldCache.get(clazz);
        if (fields == null)
            fieldCache.put(clazz, fields = getFieldsFromClass(clazz));
        return fields;
    }

    private static Field[] getFieldsFromClass(Class<?> clazz) {
        List<Field> toFilter = Lists.newArrayList(clazz.getDeclaredFields());
        Iterator<Field> itr = toFilter.iterator();
        while (itr.hasNext()) {
            Field field = itr.next();
            field.setAccessible(true);
            Persist persistAnnotation = field.getAnnotation(Persist.class);
            if (persistAnnotation == null) {
                itr.remove();
                continue;
            }
            DelegatePersistence delegate = field.getAnnotation(DelegatePersistence.class);
            if (delegate == null)
                continue;
            Class<? extends Persister<?>> delegateClass = delegate.value();
            ensureDelegateLoaded(delegateClass);
            Persister<?> in = loadedDelegates.get(delegateClass);
            if (in == null) {
                // class couldn't be loaded earlier, we can't deserialise.
                itr.remove();
                continue;
            }
        }
        return toFilter.toArray(new Field[toFilter.size()]);
    }

    private static Class<?> getGenericType(Field field) {
        if (field.getGenericType() == null || !(field.getGenericType() instanceof ParameterizedType))
            return field.getType();
        Type[] args = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        return args.length > 0 && args[0] instanceof Class ? (Class<?>) args[0] : field.getType();
    }

    /**
     * Creates an instance of the given class using the default constructor and
     * loads it using {@link #load(Object, DataKey)}. Will return null if an
     * exception occurs.
     * 
     * @see #load(Object, DataKey)
     * @param clazz
     *            The class to create an instance from
     * @param root
     *            The root key to load from
     * @return The loaded instance
     */
    public static <T> T load(Class<? extends T> clazz, DataKey root) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        if (instance == null)
            return null;
        return load(instance, root);
    }

    /**
     * Analyses the class for {@link Field}s with the {@link Persist} annotation
     * and loads data into them using the given {@link DataKey}. If a
     * {@link DelegatePersistence} annotation is provided the referenced
     * {@link Persister} will be used to create the instance. This annotation
     * can be omitted if the Persister has been registered using
     * {@link #registerPersistDelegate(Class, Class)}
     * 
     * @param instance
     *            The instance to load data into
     * @param root
     *            The key to load data from
     * @return The instance, with persisted fields loaded
     */
    public static <T> T load(T instance, DataKey root) {
        Class<?> clazz = instance.getClass();
        Field[] fields = getFields(clazz);
        for (Field field : fields)
            try {
                deserialise(new PersistField(field, instance), root);
            } catch (Exception e) {
                if (e != loadException)
                    e.printStackTrace();
                return null;
            }
        return instance;
    }

    /**
     * Registers a {@link Persister} redirect. Fields with the {@link Persist}
     * annotation with a type that has been registered using this method will
     * use the Persister by default to load and save data. The
     * {@link DelegatePersistence} annotation will be preferred if present.
     * 
     * @param clazz
     *            The class to redirect
     * @param delegateClass
     *            The Persister class to use when loading and saving
     */
    public static void registerPersistDelegate(Class<?> clazz, Class<? extends Persister<?>> delegateClass) {
        persistRedirects.put(clazz, delegateClass);
        ensureDelegateLoaded(delegateClass);
    }

    /**
     * Scans the object for fields annotated with {@link Persist} and saves them
     * to the given {@link DataKey}.
     * 
     * @param instance
     *            The instance to save
     * @param root
     *            The key to save into
     */
    public static void save(Object instance, DataKey root) {
        Class<?> clazz = instance.getClass();
        Field[] fields = getFields(clazz);
        for (Field field : fields)
            serialise(new PersistField(field, instance), root);
    }

    private static void serialise(PersistField field, DataKey root) {
        if (field.get() == null)
            return;
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<?> collection = field.get();
            root.removeKey(field.key);
            int i = 0;
            for (Object object : collection) {
                String key = createRelativeKey(field.key, i);
                serialiseValue(field, root.getRelative(key), object);
                i++;
            }
        } else if (Map.class.isAssignableFrom(field.getType())) {
            Map<String, Object> map = field.get();
            root.removeKey(field.key);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = createRelativeKey(field.key, entry.getKey());
                serialiseValue(field, root.getRelative(key), entry.getValue());
            }
        } else {
            serialiseValue(field, root.getRelative(field.key), field.get());
        }
    }

    @SuppressWarnings("unchecked")
    private static void serialiseValue(PersistField field, DataKey root, Object value) {
        if (field.delegate != null) {
            ((Persister<Object>) field.delegate).save(value, root);
        } else if (value instanceof Enum) {
            root.setRaw("", ((Enum<?>) value).name());
        } else
            root.setRaw("", value);
    }

    private static final Map<Class<?>, Field[]> fieldCache = new WeakHashMap<Class<?>, Field[]>();
    private static final Map<Class<? extends Persister<?>>, Persister<?>> loadedDelegates = new WeakHashMap<Class<? extends Persister<?>>, Persister<?>>();
    private static final Exception loadException = new Exception() {
        @SuppressWarnings("unused")
        public void fillInStackTrace(StackTraceElement[] elements) {
        }

        private static final long serialVersionUID = -4245839150826112365L;
    };
    private static final Map<Class<?>, Class<? extends Persister<?>>> persistRedirects = new WeakHashMap<Class<?>, Class<? extends Persister<?>>>();

    static {
        registerPersistDelegate(Location.class, LocationPersister.class);
    }
}
