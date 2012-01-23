package net.citizensnpcs.storage.database;

import java.util.List;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.storage.Storage;

public class DatabaseStorage implements Storage {

    @Override
    public DataKey getKey(String root) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void load() {
        // TODO Auto-generated method stub

    }

    @Override
    public void save() {
        // TODO Auto-generated method stub

    }

    public class DatabaseKey extends DataKey {

        @Override
        public void copy(String to) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean getBoolean(String key) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public double getDouble(String key) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getInt(String key) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public List<DataKey> getIntegerSubKeys() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getLong(String key) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getRaw(String key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DataKey getRelative(String relative) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getString(String key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterable<DataKey> getSubKeys() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean keyExists(String key) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String name() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removeKey(String key) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setBoolean(String key, boolean value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setDouble(String key, double value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setInt(String key, int value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setLong(String key, long value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setRaw(String path, Object value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setString(String key, String value) {
            // TODO Auto-generated method stub

        }
    }
}