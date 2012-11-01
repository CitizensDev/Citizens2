package net.citizensnpcs.api.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;

import org.junit.Before;
import org.junit.Test;

public class PersistenceLoaderTest {
    private DataKey root;

    @Before
    public void setUp() {
        root = new MemoryDataKey();
    }

    @Test
    public void canAccessPrivateMembers() {
        root.setInt("integer", 5);
        assertTrue(PersistenceLoader.load(SaveLoadTest.class, root).integer == 5);
    }

    @Test
    public void illegalCollectionClass() {
        assertNull(PersistenceLoader.load(IllegalCollectionClassTest.class, root));
    }

    @Test
    public void loadsList() {
        for (int i = 0; i < 6; i++) {
            root.setInt("list." + i, i);
        }
        CollectionTest test = PersistenceLoader.load(CollectionTest.class, root);
        for (int i = 0; i < 6; i++) {
            assertEquals(test.list.get(i).intValue(), i);
        }
    }

    @Test
    public void processesRequiredCorrectly() {
        assertTrue(PersistenceLoader.load(RequiredTest.class, root) == null);
    }

    @Test
    public void saveLoadCycle() throws Exception {
        SaveLoadTest test = new SaveLoadTest();
        PersistenceLoader.save(test, root);
        PersistenceLoader.load(test, root);
        SaveLoadTest newInstance = new SaveLoadTest();
        for (Field field : test.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            assertEquals(field.get(test), field.get(newInstance));
        }
    }

    public static class SaveLoadTest implements Cloneable {
        @Persist
        public int integer = 2;

        @Persist
        public Integer integerWrapped = 2;

        @Persist
        public double d = 0.5;

        @Persist
        public float f = 0.6F;

        @Persist("namedtest")
        public int named = 4;
    }

    public static class RequiredTest {
        @Persist(required = true)
        private int requiredInteger;
    }

    @Test
    public void usesSpecificCollectionClass() {
        root.setInt("list.0", 5);
        root.setInt("set.0", 5);
        SpecificCollectionClassTest instance = PersistenceLoader
                .load(SpecificCollectionClassTest.class, root);
        assertEquals(instance.list.getClass(), LinkedList.class);
        assertEquals(instance.set.getClass(), LinkedHashSet.class);
    }

    public static class CollectionTest {
        @Persist
        private List<Integer> list;
    }

    public static class IllegalCollectionClassTest {
        @Persist(collectionType = Integer.class)
        private List<Integer> list;
    }

    public static class SpecificCollectionClassTest {
        @Persist(collectionType = LinkedList.class)
        private List<Integer> list;

        @Persist(collectionType = LinkedHashSet.class)
        private Set<Integer> set;
    }
}
