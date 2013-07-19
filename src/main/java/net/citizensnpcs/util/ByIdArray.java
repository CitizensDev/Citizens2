package net.citizensnpcs.util;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ByIdArray<T> implements Iterable<T> {
    private final int capacity;
    private Object[] elementData;
    private int highest = Integer.MIN_VALUE;
    private int lowest = Integer.MAX_VALUE;
    private int modCount;
    private int size;

    public ByIdArray() {
        this(100);
    }

    public ByIdArray(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("Capacity cannot be less than 0.");
        this.capacity = capacity;
        elementData = new Object[capacity];
    }

    public int add(T t) {
        int index = 0;
        while (elementData[index++] != null) {
            if (index >= elementData.length) {
                ensureCapacity(elementData.length + 1);
                index = elementData.length - 1;
            }
        }
        put(index, t);
        return index;
    }

    public void clear() {
        modCount = highest = size = lowest = 0;
        elementData = new Object[capacity];
    }

    public boolean contains(int index) {
        return elementData.length > index && elementData[index] != null;
    }

    public void ensureCapacity(int minCapacity) { // from ArrayList
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    private void fastRemove(int index) {
        ++modCount;
        if (index == highest)
            recalcHighest();
        if (index == lowest)
            recalcLowest();
        elementData[index] = null;
        --size;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index > elementData.length)
            return null;
        return (T) elementData[index];
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    public void put(int index, T t) {
        if (t == null)
            throw new IllegalArgumentException("can't insert a null object");
        ++modCount;
        if (index > highest)
            highest = index;
        if (index < lowest)
            lowest = index;

        ensureCapacity(index + 2);

        elementData[index] = t;
        ++size;
    }

    private void recalcHighest() {
        highest = elementData.length - 1;
        while (highest != 0 && elementData[highest--] == null)
            ;
    }

    private void recalcLowest() {
        lowest = 0;
        while (elementData.length > lowest && elementData[lowest++] == null)
            ;
    }

    public T remove(int index) {
        if (index > elementData.length || elementData[index] == null)
            return null;
        ++modCount;
        if (index == highest)
            recalcHighest();
        if (index == lowest)
            recalcLowest();
        @SuppressWarnings("unchecked")
        T prev = (T) elementData[index];
        elementData[index] = null;
        if (prev != null)
            --size;
        return prev;
    }

    public int size() {
        return size;
    }

    public void trimToSize() {
        if (elementData.length > highest)
            elementData = Arrays.copyOf(elementData, highest + 1);
    }

    private class Itr implements Iterator<T> {
        private int expected = ByIdArray.this.modCount;
        private int idx;
        {
            if (size > 0) {
                if (highest == Integer.MIN_VALUE || elementData[highest] == null)
                    recalcHighest();
                if (lowest == Integer.MAX_VALUE || elementData[lowest] == null)
                    recalcLowest();
                idx = lowest - 1;
            }
        }

        @Override
        public boolean hasNext() {
            if (modCount != expected) {
                throw new ConcurrentModificationException();
            }
            return size > 0 && highest > idx;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (modCount != expected)
                throw new ConcurrentModificationException();
            if (idx > highest)
                throw new NoSuchElementException();
            do
                idx++;
            while (idx != highest + 1 && elementData[idx] == null);
            T next = (T) elementData[idx];
            if (next == null)
                throw new NoSuchElementException();
            return next;
        }

        @Override
        public void remove() {
            if (modCount != expected)
                throw new ConcurrentModificationException();
            fastRemove(idx);
            expected = modCount;
        }
    }

    public static <T> ByIdArray<T> create() {
        return new ByIdArray<T>();
    }
}