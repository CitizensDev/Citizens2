package net.citizensnpcs.api.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;

import org.junit.Before;
import org.junit.Test;

public class PersistenceLoaderTest {
    private DataKey root;

    @Test
    public void canAccessPrivateMembers() {
        root.setInt("integer", 5);
        assertThat(PersistenceLoader.load(SaveLoadTest.class, root).integer, is(5));
    }

    @Test
    public void illegalCollectionClass() {
        assertThat(PersistenceLoader.load(IllegalCollectionClassTest.class, root), is(nullValue()));
    }

    @Test
    public void loadsCollections() {
        for (int i = 0; i < 6; i++) {
            root.setInt("list." + i, i);
            root.setInt("set." + i, i);
            root.setInt("map." + i, i);
        }
        CollectionTest test = PersistenceLoader.load(CollectionTest.class, root);
        for (int i = 0; i < 6; i++) {
            assertThat(test.list.get(i).intValue(), is(i));
            assertThat(test.set.contains(i), is(true));
            assertThat(test.map.containsKey(Integer.toString(i)), is(true));
            assertThat(test.map.get(Integer.toString(i)).intValue(), is(i));
        }
    }

    @Test
    public void processesRequiredCorrectly() {
        assertThat(PersistenceLoader.load(RequiredTest.class, root), is(nullValue()));
    }

    @Test
    public void saveLoadCycle() throws Exception {
        SaveLoadTest test = new SaveLoadTest();
        PersistenceLoader.save(test, root);
        PersistenceLoader.load(test, root);
        SaveLoadTest newInstance = new SaveLoadTest();
        for (Field field : test.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            assertThat(field.get(test), is(field.get(newInstance)));
        }
    }

    @Before
    public void setUp() {
        root = new MemoryDataKey();
    }

    @Test
    public void testTypeInference() {
        root.setString("map.1", "1");
        InferenceTest test = PersistenceLoader.load(InferenceTest.class, root);
        assertThat(test.map, is(ConcurrentHashMap.class));
    }

    @Test
    public void usesSpecificCollectionClass() {
        root.setInt("list.0", 5);
        root.setInt("set.0", 5);
        SpecificCollectionClassTest instance = PersistenceLoader.load(SpecificCollectionClassTest.class, root);
        assertEquals(instance.list.getClass(), LinkedList.class);
        assertEquals(instance.set.getClass(), LinkedHashSet.class);
    }

    public static class CollectionTest {
        @Persist
        private List<Integer> list;
        @Persist
        private Map<String, Integer> map;
        @Persist
        private Set<Integer> set;
    }

    public static class IllegalCollectionClassTest {
        @Persist(collectionType = Integer.class)
        private List<Integer> list;
    }

    public static class InferenceTest {
        @Persist
        public Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();
    }

    public static class RequiredTest {
        @Persist(required = true)
        private int requiredInteger;
    }

    public static class SaveLoadTest implements Cloneable {
        @Persist
        public double d = 0.5;

        @Persist
        public float f = 0.6F;

        @Persist
        public int integer = 2;

        @Persist
        public Integer integerWrapped = 2;

        @Persist("namedtest")
        public int named = 4;
    }

    public static class SpecificCollectionClassTest {
        @Persist(collectionType = LinkedList.class)
        private List<Integer> list;

        @Persist(collectionType = LinkedHashSet.class)
        private Set<Integer> set;
    }
}
