package net.citizensnpcs.api.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    public void testCanAccessPrivateMembers() {
        root.setInt("test", 5);
        assertTrue(PersistenceLoader.load(PrivateTest.class, root).test == 5);
    }

    @Test
    public void testIllegalCollectionClass() {
        assertNull(PersistenceLoader.load(IllegalCollectionClassTest.class, root));
    }

    @Test
    public void testList() {
        for (int i = 0; i < 6; i++) {
            root.setInt("list." + i, i);
        }
        CollectionTest test = PersistenceLoader.load(CollectionTest.class, root);
        for (int i = 0; i < 6; i++) {
            assertEquals(test.list.get(i).intValue(), i);
        }
    }

    @Test
    public void testRequired() {
        assertTrue(PersistenceLoader.load(PrivateTest.class, root) == null);
    }

    @Test
    public void testSpecificCollectionClass() {
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

    public static class PrivateTest {
        @Persist
        private int test;
    }

    public static class RequiredTest {
        @Persist(required = true)
        private int test;
    }

    public static class SpecificCollectionClassTest {
        @Persist(collectionType = LinkedList.class)
        private List<Integer> list;

        @Persist(collectionType = LinkedHashSet.class)
        private Set<Integer> set;
    }
}
