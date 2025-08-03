package net.citizensnpcs.api.persistence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Horse;
import org.junit.Before;
import org.junit.Test;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;

public class PersistenceLoaderTest {
    private DataKey root;
    private DataKey yamlRoot;
    private Storage yamlStorage;

    @Test
    public void canAccessPrivateMembers() {
        root.setInt("integer", 5);
        assertThat(PersistenceLoader.load(SaveLoadTest.class, root).integer, is(5));
    }

    @Test
    public void defaultsRemainUntouched() {
        assertThat(PersistenceLoader.load(HorseColorTest.class, root).color, is(Horse.Color.CREAMY));
    }

    @Test
    public void floatArrays() {
        root.setDouble("array.0", -10.0);
        root.setDouble("array.1", 0.0);
        root.setDouble("array.2", 360.0);
        assertThat(PersistenceLoader.load(SaveLoadTest.class, root).array, is(new float[] { -10.0F, 0.0F, 360.0F }));
    }

    @Test
    public void getRelativeEmpty() {
        root.setString("blah.basr", "PLAYER");
        assertThat(root.getRelative("blah.basr").getString(""), is("PLAYER"));
    }

    @Test
    public void getRoot() {
        root.setString("blah.basr", "test");
        assertThat(root.getRelative("blah").getFromRoot("").getString("blah.basr"), is("test"));
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
            assertThat(test.list.get(i), is(i));
            assertThat(test.set.contains(i), is(true));
            assertThat(test.map.containsKey(Integer.toString(i)), is(true));
            assertThat(test.map.get(Integer.toString(i)), is(i));
        }
    }

    @Test
    public void loadsNullSets() {
        SpecificCollectionClassTest test = PersistenceLoader.load(SpecificCollectionClassTest.class, root);
        PersistenceLoader.save(test, root);
        PersistenceLoader.load(test, root);
        assertEquals(test.list, null);
        assertEquals(test.set, null);
    }

    @Test
    public void longLoadSaveTest() {
        LongLoadSaveTest load = new LongLoadSaveTest();
        load.term = 23423423333333L;
        PersistenceLoader.save(load, root);
        PersistenceLoader.load(load, root);
        assertEquals(load.term, 23423423333333L);
    }

    @Test
    public void longLoadSaveTestYaml() {
        LongLoadSaveTest load = new LongLoadSaveTest();
        load.term = 3;
        PersistenceLoader.save(load, yamlRoot);
        yamlStorage.save();
        load = new LongLoadSaveTest();
        yamlStorage.load();
        PersistenceLoader.load(load, yamlRoot);
        assertEquals(load.term, 3);
    }

    @Test
    public void mapReify() {
        root.setInt("enabled.test.integer", 5);
        assertThat(PersistenceLoader.load(TestMapReify.class, root).enabled.get("test").key, is("test"));
        assertThat(PersistenceLoader.load(TestMapReify.class, root).enabled.get("test").integer, is(5));
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

    @Test
    public void savesNulls() throws Exception {
        NullUUIDTest test = new NullUUIDTest();
        test.uuid = UUID.randomUUID();
        PersistenceLoader.save(test, root);
        test.uuid = null;
        PersistenceLoader.save(test, root);
        assertThat(root.keyExists("uuid"), is(false));
    }

    @Before
    public void setUp() {
        root = new MemoryDataKey();
        try {
            yamlStorage = new YamlStorage(File.createTempFile("citizens_test", null));
            yamlRoot = yamlStorage.getKey("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void staticPersistence() {
        StaticPersistenceTest.abc = "a";
        StaticPersistenceTest.cba = "b";
        StaticPersistenceTest.MAP.put("test", "value");
        PersistenceLoader.save(new StaticPersistenceTest(), root);
        assertThat(root.getString("test2"), equalTo("b"));
        assertThat(root.getString("map.test"), equalTo("value"));
        assertThat(root.getString("global.test.abc"), equalTo("a"));
        root.setString("global.test.abc", "test");
        root.setString("test2", "test");
        root.setString("map.test", "value2");
        PersistenceLoader.load(StaticPersistenceTest.class, root);
        assertThat(StaticPersistenceTest.MAP.get("test"), equalTo("value2"));
        PersistenceLoader.load(StaticPersistenceTest.class, root);
        assertThat(StaticPersistenceTest.cba, equalTo("test"));
        PersistenceLoader.load(StaticPersistenceTest.class, root);
        assertThat(StaticPersistenceTest.abc, equalTo("test"));
    }

    @Test
    public void testCustomConstructorPersister() {
        assertThat(PersistenceLoader.load(CustomConstructor.class, root), notNullValue());
    }

    @Test
    public void testLists() {
        ListTest load = new ListTest();
        yamlRoot.setRaw("test", Arrays.asList("one", "two", "three"));
        PersistenceLoader.load(load, yamlRoot);
        assertThat(load.test, equalTo(Arrays.asList("one", "two", "three")));
    }

    @Test
    public void testMapReload() {
        root.setBoolean("enabled.trig1", true);
        TestMap stored = PersistenceLoader.load(TestMap.class, root);
        PersistenceLoader.save(stored, root);
        assertThat(PersistenceLoader.load(TestMap.class, root).enabled.get("trig1"), equalTo(true));
    }

    @Test
    public void testReifiedLists() {
        ReifiedListTest load = new ReifiedListTest();
        load.test = Arrays.asList(new ReifiedListValue(0), new ReifiedListValue(2));
        PersistenceLoader.save(load, root);
        load = PersistenceLoader.load(load, root);
        assertEquals(load.test.get(0).index, 0);
        assertEquals(load.test.get(1).index, 2);
    }

    @Test
    public void testSuperClass() {
        root.setInt("integer", 5);
        root.setString("superclass", "test");
        SuperclassTest test = PersistenceLoader.load(SuperclassTest.class, root);
        assertEquals(test.superclass, "test");
        assertEquals(test.integer, 5);
        root = new MemoryDataKey();
        PersistenceLoader.save(test, root);
        assertEquals(root.getString("superclass"), "test");
        assertEquals(root.getInt("integer"), 5);
    }

    @Test
    public void testTypeInference() {
        root.setString("map.1", "1");
        InferenceTest test = PersistenceLoader.load(InferenceTest.class, root);
        assertEquals(test.map.getClass(), ConcurrentHashMap.class);
    }

    @Test
    public void testYamlLists() throws IOException {
        YamlStorage ys = new YamlStorage(File.createTempFile("citizens_test2", null), null, true);
        DataKey r = ys.getKey("");
        r.setBoolean("list2.a", true);
        r.setBoolean("list.0.v", true);
        r.setBoolean("list.0.v1", true);
        r.setBoolean("list.1.v", true);
        r.setBoolean("list.2.v", true);
        r.setBoolean("list.2.v3.p.0", true);
        ys.save();
        ys.load();
        r = ys.getKey("");
        assertThat(r.getBoolean("list.2.v3.p.0"), is(true));
    }

    @Test
    public void usesSpecificCollectionClass() {
        root.setInt("list.0", 5);
        root.setInt("set.0", 5);
        SpecificCollectionClassTest instance = PersistenceLoader.load(SpecificCollectionClassTest.class, root);
        assertEquals(instance.list.getClass(), LinkedList.class);
        assertEquals(instance.set.getClass(), LinkedHashSet.class);
    }

    @Test
    public void valuesDeep() {
        root.setString("blah.basr", "test");
        Map<String, Object> values = root.getRelative("blah").getValuesDeep();
        assertThat(values.get("basr"), is("test"));
    }

    public static class CollectionTest {
        @Persist
        private List<Integer> list;
        @Persist
        private Map<String, Integer> map;
        @Persist
        private Set<Integer> set;
    }

    public static class CustomConstructor {
        @DelegatePersistence(CustomConstructorPersister.class)
        public CustomConstructor(String name) {
        }

        public static class CustomConstructorPersister implements Persister<CustomConstructor> {
            @Override
            public CustomConstructor create(DataKey root) {
                return new CustomConstructor("name");
            }

            @Override
            public void save(CustomConstructor instance, DataKey root) {
            }
        }
    }

    public static class HorseColorTest {
        @Persist
        private final Horse.Color color = Horse.Color.CREAMY;
    }

    public static class IllegalCollectionClassTest {
        @Persist(collectionType = Integer.class)
        private List<Integer> list;
    }

    public static class InferenceTest {
        @Persist
        public Map<String, Integer> map = new ConcurrentHashMap<>();
    }

    public static class ListTest {
        @Persist
        private List<Map> mapTest;
        @Persist
        private List<String> test;
    }

    public static class LongLoadSaveTest {
        @Persist("root2")
        public long term = 0;
    }

    public static class NullUUIDTest {
        @Persist
        private UUID uuid;
    }

    public static class ReifiedListTest {
        @Persist(reify = true, valueType = ReifiedListValue.class)
        private List<ReifiedListValue> test;
    }

    public static class ReifiedListValue {
        @Persist(value = "$index")
        private int index;

        public ReifiedListValue() {
        }

        public ReifiedListValue(int i) {
            this.index = i;
        }
    }

    public static class RequiredTest {
        @Persist(required = true)
        private int requiredInteger;
    }

    public static class SaveLoadTest implements Cloneable {
        @Persist
        public float[] array = { -10, 0, 360 };

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

    private static class StaticPersistenceTest {
        @Persist(namespace = "test")
        private static String abc;

        @Persist(value = "test2")
        private static String cba;

        @Persist(value = "map")
        private static Map<String, String> MAP = new HashMap<>();
    }

    public static class Superclass {
        @Persist
        public String superclass;
    }

    public static class SuperclassTest extends Superclass {
        @Persist
        public int integer;
        @Persist("")
        public String key;
    }

    public static class TestMap {
        @Persist(value = "enabled", collectionType = ConcurrentHashMap.class)
        private final Map<String, Boolean> enabled = new ConcurrentHashMap<>();
    }

    public static class TestMapReify {
        @Persist(reify = true, valueType = SuperclassTest.class)
        private final Map<String, SuperclassTest> enabled = new HashMap<>();
    }
}
