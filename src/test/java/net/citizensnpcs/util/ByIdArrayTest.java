package net.citizensnpcs.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Test;

import com.google.common.collect.Iterables;

public class ByIdArrayTest {
    private void assertSize(ByIdArray<?> array, int size) {
        assertThat(array.size(), is(size));
        assertThat(Iterables.size(array), is(size));
    }

    @Test
    public void testBeyondCapacity() {
        ByIdArray<Integer> array = ByIdArray.create();
        array.put(1000, 1);
        assertThat(array.contains(1000), is(true));
        assertSize(array, 1);
    }

    @Test
    public void testClear() {
        ByIdArray<Integer> array = ByIdArray.create();
        array.put(0, 1);
        array.put(1, 2);
        array.clear();
        assertSize(array, 0);
    }

    @Test
    public void testContains() {
        ByIdArray<Integer> array = ByIdArray.create();
        array.put(1, 1);
        assertThat(array.contains(1), is(true));
    }

    @Test
    public void testInsertion() {
        ByIdArray<Integer> array = ByIdArray.create();
        array.add(1);
        array.add(2);
        assertSize(array, 2);
    }

    @Test
    public void testIteratorRemove() {
        ByIdArray<Integer> array = ByIdArray.create();
        array.put(10, 1);
        array.put(20, 2);
        array.put(30, 3);
        Iterator<Integer> itr = array.iterator();
        itr.next();
        itr.remove();
        itr.next();
        assertSize(array, 2);
        assertThat(array.contains(10), is(false));
        assertThat(array.get(20), is(2));
        assertThat(array.get(30), is(3));

        itr = array.iterator();
        while (itr.hasNext()) {
            itr.next();
            itr.remove();
        }
        assertSize(array, 0);
    }

    @Test
    public void testPut() {
        ByIdArray<Integer> array = ByIdArray.create();
        array.put(50, 1);
        array.put(20, 2);
        assertSize(array, 2);
        assertThat(array.get(20), is(2));
        assertThat(array.get(50), is(1));
    }

    @Test
    public void testRemoval() {
        ByIdArray<Integer> array = ByIdArray.create();
        array.put(0, 1);
        array.put(1, 2);
        array.remove(1);
        assertSize(array, 1);
    }
}
