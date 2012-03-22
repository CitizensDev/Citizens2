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
        this(1000);
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
        if (index == lowest)
            recalcLowest();
        if (index == highest)
            recalcHighest();
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
        while (highest != 0 && elementData[highest--] == null)
            ;
    }

    private void recalcLowest() {
        while (elementData.length > lowest && highest > lowest && elementData[lowest++] == null)
            ;
    }

    public T remove(int index) {
        if (index > elementData.length || elementData[index] == null)
            return null;
        ++modCount;
        if (index == lowest)
            recalcLowest();
        if (index == highest)
            recalcHighest();
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
        private int idx = lowest;

        @Override
        public boolean hasNext() {
            if (ByIdArray.this.modCount != expected)
                throw new ConcurrentModificationException();
            return highest + 1 > idx && size > 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (ByIdArray.this.modCount != expected)
                throw new ConcurrentModificationException();
            T next = (T) elementData[idx];
            if (next == null || idx > highest)
                throw new NoSuchElementException();
            do
                idx++;
            while (idx != highest + 1 && elementData[idx] == null);
            return next;
        }

        @Override
        public void remove() {
            if (ByIdArray.this.modCount != expected)
                throw new ConcurrentModificationException();
            ByIdArray.this.fastRemove(idx);
            expected = ByIdArray.this.modCount;
        }
    }

    public static <T> ByIdArray<T> create() {
        return new ByIdArray<T>();
    }
}