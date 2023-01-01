package net.citizensnpcs.api.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;

/**
 * Performs reflective persistence of objects into {@link DataKey}s. {@link Persist} annotations are used to mark fields
 * for annotation.
 *
 * @see Persist
 */
public class PersistenceLoader {
    private static class PersistenceLoaderPersister implements Persister<Object> {
        private final Class<?> clazz;

        private PersistenceLoaderPersister(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object create(DataKey root) {
            return PersistenceLoader.load(clazz, root);
        }

        @Override
        public void save(Object instance, DataKey root) {
            PersistenceLoader.save(instance, root);
        }

    }

    private static class PersistField {
        private boolean checkedForDefault;
        private Object defaultValue;
        private final Persister<?> delegate;
        private final Field field;
        private final String key;
        private final Persist persistAnnotation;

        private PersistField(Field field) {
            this.field = field;
            this.persistAnnotation = field.getAnnotation(Persist.class);
            this.key = persistAnnotation.value().equals("UNINITIALISED") ? field.getName() : persistAnnotation.value();
            Class<?> fieldType = field.getType();
            if (field.getGenericType() instanceof ParameterizedType) {
                int index = Map.class.isAssignableFrom(field.getType()) ? 1 : 0;
                fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[index];
            }
            if (persistAnnotation.valueType() != Object.class) {
                fieldType = persistAnnotation.valueType();
            }
            this.delegate = persistAnnotation.reify() ? new PersistenceLoaderPersister(fieldType)
                    : getDelegate(field, fieldType);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(Object instance) {
            try {
                return (T) field.get(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return (T) NULL;
            }
        }

        public Class<?> getCollectionType() {
            return persistAnnotation.collectionType();
        }

        public DataKey getDataKey(DataKey root) {
            if (!persistAnnotation.namespace().isEmpty()) {
                return root.getFromRoot("global." + persistAnnotation.namespace());
            }
            return root;
        }

        public Class<?> getType() {
            return field.getType();
        }

        public boolean isDefault(Object value) {
            return false;
            // return defaultValue != null && defaultValue.equals(value);
        }

        public boolean isRequired() {
            return persistAnnotation.required();
        }

        public void populateDefault(Object instance) {
            if (checkedForDefault)
                return;

            try {
                defaultValue = field.get(instance);
                checkedForDefault = true;
                if (defaultValue != null && !defaultValue.getClass().isPrimitive() && !(defaultValue instanceof Number)
                        && !(defaultValue instanceof Enum) && !(defaultValue instanceof Boolean)
                        && !(defaultValue instanceof String)) {
                    defaultValue = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void set(Object instance, Object value) {
            try {
                field.set(instance, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static final Object NULL = new Object();
    }

    public static <T> PersisterRegistry<T> createRegistry(Class<?> base) {
        PersisterRegistry<T> registry = new PersisterRegistry<T>();
        registries.put(base, registry);
        return registry;
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
    private static void deserialise(PersistField field, Object instance, Object oldValue, DataKey root)
            throws Exception {
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
            if (raw instanceof List && collectionType.isAssignableFrom(raw.getClass())) {
                list = (List<Object>) raw;
            } else {
                deserialiseCollection(list, root, field);
            }
            value = list;
        } else if (Set.class.isAssignableFrom(type)) {
            Set set;
            if (Set.class.isAssignableFrom(collectionType)) {
                set = (Set) collectionType.newInstance();
            } else {
                if (field.getType().isEnum()) {
                    set = EnumSet.noneOf((Class<? extends Enum>) field.getType());
                } else {
                    set = (Set) (oldValue != null && Set.class.isAssignableFrom(oldValue.getClass())
                            ? oldValue.getClass().newInstance()
                            : Sets.newHashSet());
                }
            }
            deserialiseCollection(set, root, field);
            value = set;
        } else if (Map.class.isAssignableFrom(type)) {
            Map<Object, Object> map;
            if (Map.class.isAssignableFrom(collectionType)) {
                map = (Map<Object, Object>) collectionType.newInstance();
            } else {
                boolean hasConcreteType = oldValue != null && Map.class.isAssignableFrom(oldValue.getClass())
                        && !oldValue.getClass().isInterface();
                map = (Map<Object, Object>) (hasConcreteType ? oldValue : Maps.newHashMap());
            }
            deserialiseMap(map, root, field);
            value = map;
        } else if (float[].class.isAssignableFrom(type)) {
            List<Float> floats = Lists.newArrayList();
            for (DataKey sub : root.getRelative(field.key).getIntegerSubKeys()) {
                floats.add((float) sub.getDouble(""));
            }
            value = new float[floats.size()];
            for (int i = 0; i < floats.size(); i++) {
                ((float[]) value)[i] = floats.get(i);
            }
        } else if (double[].class.isAssignableFrom(type)) {
            List<Double> doubles = Lists.newArrayList();
            for (DataKey sub : root.getRelative(field.key).getIntegerSubKeys()) {
                doubles.add(sub.getDouble(""));
            }
            value = new double[doubles.size()];
            for (int i = 0; i < doubles.size(); i++) {
                ((double[]) value)[i] = doubles.get(i);
            }
        } else if (int[].class.isAssignableFrom(type)) {
            List<Integer> ints = Lists.newArrayList();
            for (DataKey sub : root.getRelative(field.key).getIntegerSubKeys()) {
                ints.add(sub.getInt(""));
            }
            value = new int[ints.size()];
            for (int i = 0; i < ints.size(); i++) {
                ((int[]) value)[i] = ints.get(i);
            }
        } else if (field.key.equals("$key")) {
            value = int.class.isAssignableFrom(type) ? Integer.parseInt(root.name()) : root.name();
        } else {
            value = deserialiseValue(field, root.getRelative(field.key));
        }
        if (value == null && field.isRequired())
            throw loadException;
        if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            if (value == null)
                return;
            if (!Primitives.isWrapperType(type)) {
                type = Primitives.wrap(type);
            }
            Class<?> clazz = value.getClass();
            if (clazz == Integer.class && type != Integer.class && type != Double.class && type != Long.class
                    && type != Float.class) {
                return;
            }
            if (clazz == Float.class && type != Double.class && type != Float.class) {
                return;
            }
            if (clazz == Double.class && type != Double.class) {
                if (type == Float.class) {
                    value = ((Double) value).floatValue();
                } else {
                    return;
                }
            }
            if (clazz == Byte.class && type != Short.class && type != Byte.class && type != Integer.class
                    && type != Double.class && type != Long.class && type != Float.class) {
                return;
            }
            if (clazz == Short.class && type != Short.class && type != Integer.class && type != Double.class
                    && type != Long.class && type != Float.class) {
                return;
            }
            if (clazz == Character.class && type != Character.class && type != Short.class && type != Integer.class
                    && type != Double.class && type != Long.class && type != Float.class) {
                return;
            }
            field.set(instance, value);
        } else {
            if (value != null && !type.isAssignableFrom(value.getClass())) {
                if (root.getRelative(field.key).getSubKeys().iterator().hasNext()
                        && field.field.getType() == String.class && field.delegate == null) {
                    field.set(instance, root.getRelative(field.key).name());
                }
                return;
            }
            field.set(instance, value);
        }
    }

    private static void deserialiseCollection(Collection<Object> collection, DataKey root, PersistField field) {
        for (DataKey subKey : root.getRelative(field.key).getSubKeys()) {
            Object loaded = deserialiseCollectionValue(field, subKey, field.persistAnnotation.valueType());
            if (loaded == null)
                continue;
            collection.add(loaded);
        }
    }

    private static Object deserialiseCollectionValue(PersistField field, DataKey subKey, Class<?> type) {
        Object deserialised = deserialiseValue(field, subKey);
        if (deserialised == null || type == Object.class)
            return deserialised;
        Class<?> clazz = deserialised.getClass();
        if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            if (!Primitives.isWrapperType(clazz)) {
                clazz = Primitives.wrap(clazz);
            }
            if (type != clazz) {
                if (type == Long.class) {
                    return ((Number) deserialised).longValue();
                }
                if (type == Byte.class) {
                    return ((Number) deserialised).byteValue();
                }
                if (type == Short.class) {
                    return ((Number) deserialised).shortValue();
                }
                if (type == Float.class) {
                    return ((Number) deserialised).floatValue();
                }
                if (type == Double.class) {
                    return ((Number) deserialised).doubleValue();
                }
                if (type == Integer.class) {
                    return ((Number) deserialised).intValue();
                }
            }
        }
        return deserialised;
    }

    private static void deserialiseMap(Map<Object, Object> map, DataKey root, PersistField field) {
        for (DataKey subKey : root.getRelative(field.key).getSubKeys()) {
            Object loaded = deserialiseCollectionValue(field, subKey, field.persistAnnotation.valueType());
            if (loaded == null)
                continue;
            Object key = subKey.name();
            Class<?> type = field.persistAnnotation.keyType();
            if (type != String.class) {
                if (type.isPrimitive() || Primitives.isWrapperType(type)) {
                    if (type == Long.class) {
                        key = Long.parseLong(String.valueOf(key));
                    }
                    if (type == Byte.class) {
                        key = Byte.parseByte(String.valueOf(key));
                    }
                    if (type == Short.class) {
                        key = Short.parseShort(String.valueOf(key));
                    }
                    if (type == Float.class) {
                        key = Float.parseFloat(String.valueOf(key));
                    }
                    if (type == Double.class) {
                        key = Double.parseDouble(String.valueOf(key));
                    }
                    if (type == Integer.class) {
                        key = Integer.parseInt(String.valueOf(key));
                    }
                } else if (type == UUID.class) {
                    key = UUID.fromString(String.valueOf(key));
                } else {
                    throw new UnsupportedOperationException();
                }
            }
            map.put(key, loaded);
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
        Object deserialised = field.delegate == null ? root.getRaw("") : field.delegate.create(root);
        return deserialised;
    }

    private static void ensureDelegateLoaded(Class<? extends Persister<?>> delegateClass) {
        if (loadedDelegates.containsKey(delegateClass))
            return;
        try {
            Constructor<? extends Persister<?>> constructor = delegateClass.getConstructor();
            constructor.setAccessible(true);
            loadedDelegates.put(delegateClass, constructor.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            loadedDelegates.put(delegateClass, null);
        }
    }

    private static Persister<?> getDelegate(Field field, Class<?> fieldType) {
        DelegatePersistence delegate = field.getAnnotation(DelegatePersistence.class);
        Persister<?> persister;
        if (delegate == null) {
            if (registries.containsKey(fieldType))
                return registries.get(fieldType);
            return loadedDelegates.get(persistRedirects.get(fieldType));
        }
        persister = loadedDelegates.get(delegate.value());
        return persister == null ? loadedDelegates.get(persistRedirects.get(fieldType)) : persister;
    }

    private static PersistField[] getFields(Class<?> clazz) {
        PersistField[] fields = fieldCache.get(clazz);
        if (fields == null) {
            fieldCache.put(clazz, fields = getFieldsFromClass(clazz));
        }
        return fields;
    }

    private static PersistField[] getFieldsFromClass(Class<?> clazz) {
        List<Field> toFilter = Lists.newArrayList(clazz.getDeclaredFields());
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != Object.class && superClass != null) {
            toFilter.addAll(Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();
        }
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
        return Collections2.transform(toFilter, (a) -> new PersistField(a)).toArray(new PersistField[toFilter.size()]);
    }

    private static Class<?> getGenericType(Field field) {
        if (field.getGenericType() == null || !(field.getGenericType() instanceof ParameterizedType))
            return field.getType();
        Type[] args = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        return args.length > 0 && args[0] instanceof Class ? (Class<?>) args[0] : field.getType();
    }

    /**
     * Creates an instance of the given class using the default constructor and loads it using
     * {@link #load(Object, DataKey)}. Will return null if an exception occurs.
     *
     * @see #load(Object, DataKey)
     * @param clazz
     *            The class to create an instance from
     * @param root
     *            The root key to load from
     * @return The loaded instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(Class<? extends T> clazz, DataKey root) {
        T instance = null;
        try {
            Constructor<?> constructor = constructorCache.get(clazz);
            if (constructor == null) {
                for (Constructor<?> cons : clazz.getDeclaredConstructors()) {
                    if (cons.getParameterCount() == 0) {
                        cons.setAccessible(true);
                        constructor = cons;
                        constructorCache.put(clazz, constructor);
                        break;
                    }
                }
            }

            instance = (T) constructor.newInstance();
        } catch (Exception e) {
            Messaging.severe("Error creating instance for " + clazz + " using " + root);
            e.printStackTrace();
        }
        if (instance == null)
            return null;
        return load(instance, root);
    }

    /**
     * Analyses the class for {@link Field}s with the {@link Persist} annotation and loads data into them using the
     * given {@link DataKey}. If a {@link DelegatePersistence} annotation is provided the referenced {@link Persister}
     * will be used to create the instance. This annotation can be omitted if the Persister has been registered using
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
        PersistField[] fields = getFields(clazz);
        for (PersistField field : fields) {
            try {
                field.populateDefault(instance);
                deserialise(field, instance, field.get(instance), field.getDataKey(root));
            } catch (Exception e) {
                if (e != loadException) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        if (instance instanceof Persistable) {
            ((Persistable) instance).load(root);
        }
        return instance;
    }

    /**
     * Registers a {@link Persister} redirect. Fields with the {@link Persist} annotation with a type that has been
     * registered using this method will use the Persister by default to load and save data. The
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
     * Scans the object for fields annotated with {@link Persist} and saves them to the given {@link DataKey}.
     *
     * @param instance
     *            The instance to save
     * @param root
     *            The key to save into
     */
    public static void save(Object instance, DataKey root) {
        Class<?> clazz = instance.getClass();
        PersistField[] fields = getFields(clazz);
        for (PersistField field : fields) {
            serialise(field, field.get(instance), field.getDataKey(root));
        }
        if (instance instanceof Persistable) {
            ((Persistable) instance).save(root);
        }
    }

    private static void serialise(PersistField field, Object fieldValue, DataKey root) {
        if (fieldValue == null)
            return;
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<?> collection = (Collection<?>) fieldValue;
            root.removeKey(field.key);
            int i = 0;
            for (Object object : collection) {
                String key = createRelativeKey(field.key, i);
                serialiseValue(field, root.getRelative(key), object);
                i++;
            }
        } else if (Map.class.isAssignableFrom(field.getType())) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) fieldValue;
            root.removeKey(field.key);
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                String key = createRelativeKey(field.key, String.valueOf(entry.getKey()));
                serialiseValue(field, root.getRelative(key), entry.getValue());
            }
        } else if (float[].class.isAssignableFrom(field.getType())) {
            float[] floats = (float[]) fieldValue;
            root.removeKey(field.key);
            for (int i = 0; i < floats.length; i++) {
                String key = createRelativeKey(field.key, i);
                serialiseValue(field, root.getRelative(key), floats[i]);
            }
        } else if (double[].class.isAssignableFrom(field.getType())) {
            double[] doubles = (double[]) fieldValue;
            root.removeKey(field.key);
            for (int i = 0; i < doubles.length; i++) {
                String key = createRelativeKey(field.key, i);
                serialiseValue(field, root.getRelative(key), doubles[i]);
            }
        } else if (int[].class.isAssignableFrom(field.getType())) {
            int[] ints = (int[]) fieldValue;
            root.removeKey(field.key);
            for (int i = 0; i < ints.length; i++) {
                String key = createRelativeKey(field.key, i);
                serialiseValue(field, root.getRelative(key), ints[i]);
            }
        } else if (field.key.equals("$key")) {
            return;
        } else {
            serialiseValue(field, root.getRelative(field.key), fieldValue);
        }
    }

    @SuppressWarnings("unchecked")
    private static void serialiseValue(PersistField field, DataKey root, Object value) {
        if (field.isDefault(value)) {
            root.removeKey("");
            return;
        }
        if (field.delegate != null) {
            ((Persister<Object>) field.delegate).save(value, root);
        } else if (value instanceof Enum) {
            root.setRaw("", ((Enum<?>) value).name());
        } else {
            if (root.getSubKeys().iterator().hasNext() && !(value instanceof Collection)) {
                return;
            }
            root.setRaw("", value);
        }
    }

    private static Map<Class<?>, Constructor<?>> constructorCache = new WeakHashMap<>();
    private static final Map<Class<?>, PersistField[]> fieldCache = new WeakHashMap<>();
    private static final Map<Class<? extends Persister<?>>, Persister<?>> loadedDelegates = new WeakHashMap<>();
    private static final Exception loadException = new Exception() {
        @SuppressWarnings("unused")
        public void fillInStackTrace(StackTraceElement[] elements) {
        }

        private static final long serialVersionUID = -4245839150826112365L;
    };
    private static final Map<Class<?>, Class<? extends Persister<?>>> persistRedirects = new WeakHashMap<>();
    private static final Map<Class<?>, PersisterRegistry<?>> registries = new WeakHashMap<>();

    static {
        registerPersistDelegate(Location.class, LocationPersister.class);
        registerPersistDelegate(ItemStack.class, ItemStackPersister.class);
        registerPersistDelegate(EulerAngle.class, EulerAnglePersister.class);
        registerPersistDelegate(UUID.class, UUIDPersister.class);
    }
}
