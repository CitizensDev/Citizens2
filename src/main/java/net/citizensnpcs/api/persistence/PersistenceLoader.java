package net.citizensnpcs.api.persistence;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.citizensnpcs.api.util.DataKey;

import com.google.common.collect.Lists;

public class PersistenceLoader {
    public static <T> T load(T instance, DataKey root) {
        Class<?> clazz = instance.getClass();
        Field[] fields = getFields(clazz);
        for (Field field : fields)
            deserialise(instance, field, root);
        return instance;
    }

    private static void deserialise(Object instance, Field field, DataKey root) {
        if (List.class.isAssignableFrom(field.getType())) {
            deserialiseList(instance, field, root);
            return;
        }
        Persist persistAnnotation = field.getAnnotation(Persist.class);
        String key = persistAnnotation.value().isEmpty() ? field.getName().toLowerCase() : persistAnnotation
                .value();
        PersistDelegate delegate = getDelegate(field);
        Object value = delegate == null ? root.getRaw(key) : delegate.create(root);
        if (value == null)
            return;
        try {
            field.set(instance, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PersistDelegate getDelegate(Field field) {
        DelegatePersistence delegate = field.getAnnotation(DelegatePersistence.class);
        if (delegate == null)
            return null;
        return loadedDelegates.get(delegate);
    }

    private static void deserialiseList(Object instance, Field field, DataKey root) {
        List<Object> list = Lists.newArrayList();
        Persist persistAnnotation = field.getAnnotation(Persist.class);
        String key = persistAnnotation.value().isEmpty() ? field.getName().toLowerCase() : persistAnnotation
                .value();
        PersistDelegate delegate = getDelegate(field);
        for (DataKey subKey : root.getRelative(key).getSubKeys()) {
            Object value = delegate == null ? subKey.getRaw("") : delegate.create(subKey);
            if (value == null)
                continue;
            list.add(value);
        }
        try {
            field.set(instance, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            Class<? extends PersistDelegate> delegateClass = delegate.value();
            if (!loadedDelegates.containsKey(delegateClass)) {
                try {
                    loadedDelegates.put(delegateClass, delegateClass.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                    loadedDelegates.put(delegateClass, null);
                }
            }
            PersistDelegate in = loadedDelegates.get(delegateClass);
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

    public static void save(Object save, DataKey root) {
    }

    private static Map<Class<?>, Field[]> fieldCache = new WeakHashMap<Class<?>, Field[]>();
    private static Map<Class<? extends PersistDelegate>, PersistDelegate> loadedDelegates = new WeakHashMap<Class<? extends PersistDelegate>, PersistDelegate>();
}
