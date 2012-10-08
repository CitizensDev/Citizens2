package net.citizensnpcs.api.persistence;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.citizensnpcs.api.util.DataKey;

import com.google.common.collect.Lists;

public class PersistenceLoader {
    private static class PersistField {
        private final Persister delegate;
        private final Field field;
        private final Object instance;
        private final String key;
        private final Persist persistAnnotation;
        private Object value;

        private PersistField(Field field, Object instance) {
            this.field = field;
            this.persistAnnotation = field.getAnnotation(Persist.class);
            this.key = persistAnnotation.value().isEmpty() ? field.getName().toLowerCase()
                    : persistAnnotation.value();
            this.delegate = getDelegate(field);
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

        public Class<?> getType() {
            return field.getType();
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

    private static Map<Class<?>, Field[]> fieldCache = new WeakHashMap<Class<?>, Field[]>();
    private static Map<Class<? extends Persister>, Persister> loadedDelegates = new WeakHashMap<Class<? extends Persister>, Persister>();
    private static final Map<Class<?>, Class<? extends Persister>> persistRedirects = new WeakHashMap<Class<?>, Class<? extends Persister>>();

    private static void deserialise(PersistField field, DataKey root) {
        Object value;
        if (List.class.isAssignableFrom(field.getType())) {
            List<Object> list = Lists.newArrayList();
            for (DataKey subKey : root.getRelative(field.key).getSubKeys())
                list.add(field.delegate == null ? subKey.getRaw("") : field.delegate.create(root));
            value = list;
        } else
            value = field.delegate == null ? root.getRaw(field.key) : field.delegate.create(root);
        if (value == null || !field.getType().isAssignableFrom(value.getClass()))
            return;
        field.set(value);
    }

    private static void ensureDelegateLoaded(Class<? extends Persister> delegateClass) {
        if (loadedDelegates.containsKey(delegateClass))
            return;
        try {
            loadedDelegates.put(delegateClass, delegateClass.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            loadedDelegates.put(delegateClass, null);
        }
    }

    private static Persister getDelegate(Field field) {
        DelegatePersistence delegate = field.getAnnotation(DelegatePersistence.class);
        if (delegate == null)
            return null;
        Persister test = loadedDelegates.get(delegate.value());
        if (test == null)
            test = loadedDelegates.get(persistRedirects.get(field.getType()));
        return test;
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
            Persist persistAnnotation = field.getAnnotation(Persist.class);
            if (persistAnnotation == null) {
                itr.remove();
                continue;
            }
            DelegatePersistence delegate = field.getAnnotation(DelegatePersistence.class);
            if (delegate == null)
                continue;
            Class<? extends Persister> delegateClass = delegate.value();
            ensureDelegateLoaded(delegateClass);
            Persister in = loadedDelegates.get(delegateClass);
            if (in == null) {
                // class couldn't be loaded earlier, we can't deserialise.
                itr.remove();
                continue;
            }
        }
        return toFilter.toArray(new Field[toFilter.size()]);
    }

    public static <T> T load(Class<? extends T> clazz, DataKey root) {
        try {
            return load(clazz.newInstance(), root);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T load(T instance, DataKey root) {
        Class<?> clazz = instance.getClass();
        Field[] fields = getFields(clazz);
        for (Field field : fields)
            deserialise(new PersistField(field, instance), root);
        return instance;
    }

    public static void registerPersistDelegate(Class<?> clazz, Class<? extends Persister> delegateClass) {
        persistRedirects.put(clazz, delegateClass);
        ensureDelegateLoaded(delegateClass);
    }

    public static void save(Object instance, DataKey root) {
        Class<?> clazz = instance.getClass();
        Field[] fields = getFields(clazz);
        for (Field field : fields)
            serialise(new PersistField(field, instance), root);
    }

    private static void serialise(PersistField field, DataKey root) {
        if (List.class.isAssignableFrom(field.getType())) {
            List<?> list = field.get();
            for (int i = 0; i < list.size(); i++) {
                String key = field.key + '.' + i;
                if (field.delegate != null)
                    field.delegate.save(list.get(i), root.getRelative(key));
                else
                    root.setRaw(field.key + '.' + i, list.get(i));
            }
        } else {
            if (field.delegate != null)
                field.delegate.save(field.get(), root.getRelative(field.key));
            else
                root.setRaw(field.key, field.get());
        }
    }
}
