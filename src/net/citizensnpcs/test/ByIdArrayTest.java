package net.citizensnpcs.test;

import java.util.Random;

import net.citizensnpcs.util.ByIdArray;

import org.junit.Test;

public class ByIdArrayTest {
    @Test
    public void testInsert() {
        ByIdArray<String> test = new ByIdArray<String>();
        test.put(0, "one");
        assert (test.get(0).equals("one"));
        assert (test.contains(0));
        assert (test.size() == 1);
        test.remove(1000); // try illegal remove
        test.clear();
        assert (test.size() == 0);
    }

    @Test
    public void testIteration() {
        int iterations = 1000;
        ByIdArray<String> test = new ByIdArray<String>();
        String[] values = new String[iterations];
        for (int i = 0; i < values.length; ++i)
            values[i] = Integer.toString(i);
        Random random = new Random(100);
        int index = 0;
        for (String value : values)
            test.put((index += random.nextInt(10) + 1), value);
        index = 0;
        for (String value : test)
            assert (value.equals(values[index++]));
    }
}