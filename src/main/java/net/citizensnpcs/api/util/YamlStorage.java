package net.citizensnpcs.api.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlStorage implements Storage {
    private final FileConfiguration config;
    private final File file;

    public YamlStorage(String fileName, String header) {
        config = new YamlConfiguration();
        file = new File(fileName);
        if (!file.exists()) {
            create();
            config.options().header(header);
            save();
        } else
            load();
    }

    private void create() {
        try {
            Bukkit.getLogger().log(Level.INFO, "Creating file: " + file.getName());
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not create file: " + file.getName());
        }
    }

    @Override
    public DataKey getKey(String root) {
        return new YamlKey(root);
    }

    @Override
    public void load() {
        try {
            config.load(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean pathExists(String key) {
        return config.get(key) != null;
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class YamlKey extends DataKey {
        private final String current;

        public YamlKey(String root) {
            current = root;
        }

        @Override
        public void copy(String to) {
            ConfigurationSection root = config.getConfigurationSection(current);
            if (root == null)
                return;
            config.createSection(to, root.getValues(true));
        }

        @Override
        public boolean getBoolean(String key) {
            String path = getKeyExt(key);
            if (pathExists(path)) {
                if (config.getString(path) == null)
                    return config.getBoolean(path);
                return Boolean.parseBoolean(config.getString(path));
            }
            return false;
        }

        @Override
        public boolean getBoolean(String key, boolean def) {
            return config.getBoolean(getKeyExt(key), def);
        }

        @Override
        public double getDouble(String key) {
            String path = getKeyExt(key);
            if (pathExists(path)) {
                if (config.getString(path) == null) {
                    if (config.get(path) instanceof Integer)
                        return config.getInt(path);
                    return config.getDouble(path);
                }
                return Double.parseDouble(config.getString(path));
            }
            return 0;
        }

        @Override
        public double getDouble(String key, double def) {
            return config.getDouble(getKeyExt(key), def);
        }

        @Override
        public int getInt(String key) {
            String path = getKeyExt(key);
            if (pathExists(path)) {
                if (config.getString(path) == null)
                    return config.getInt(path);
                return Integer.parseInt(config.getString(path));
            }
            return 0;
        }

        @Override
        public int getInt(String key, int def) {
            return config.getInt(getKeyExt(key), def);
        }

        @Override
        public List<DataKey> getIntegerSubKeys() {
            List<DataKey> res = new ArrayList<DataKey>();
            ConfigurationSection section = config.getConfigurationSection(current);
            if (section == null)
                return res;
            List<Integer> keys = new ArrayList<Integer>();
            for (String key : section.getKeys(false)) {
                try {
                    keys.add(Integer.parseInt(key));
                } catch (NumberFormatException ex) {
                }
            }
            Collections.sort(keys);
            for (int key : keys)
                res.add(getRelative(Integer.toString(key)));
            return res;
        }

        private String getKeyExt(String from) {
            if (from.isEmpty())
                return current;
            if (from.charAt(0) == '.')
                return current.isEmpty() ? from.substring(1, from.length()) : current + from;
            return current.isEmpty() ? from : current + "." + from;
        }

        @Override
        public long getLong(String key) {
            String path = getKeyExt(key);
            if (pathExists(path)) {
                if (config.getString(path) == null) {
                    if (config.get(path) instanceof Integer)
                        return config.getInt(path);
                    return config.getLong(path);
                }
                return Long.parseLong(config.getString(path));
            }
            return 0;
        }

        @Override
        public long getLong(String key, long def) {
            return config.getLong(getKeyExt(key), def);
        }

        @Override
        public Object getRaw(String key) {
            return config.get(getKeyExt(key));
        }

        @Override
        public DataKey getRelative(String relative) {
            if (relative == null || relative.isEmpty())
                return this;
            return new YamlKey(getKeyExt(relative));
        }

        @Override
        public String getString(String key) {
            String path = getKeyExt(key);
            if (pathExists(path)) {
                return config.get(path).toString();
            }
            return "";
        }

        @Override
        public Iterable<DataKey> getSubKeys() {
            List<DataKey> res = new ArrayList<DataKey>();
            ConfigurationSection section = config.getConfigurationSection(current);
            if (section == null)
                return res;
            for (String key : section.getKeys(false)) {
                res.add(getRelative(key));
            }
            return res;
        }

        @Override
        public boolean keyExists(String key) {
            return config.get(getKeyExt(key)) != null;
        }

        @Override
        public String name() {
            int last = current.lastIndexOf('.');
            return current.substring(last == 0 ? 0 : last + 1);
        }

        @Override
        public void removeKey(String key) {
            config.set(getKeyExt(key), null);
            save();
        }

        @Override
        public void setBoolean(String key, boolean value) {
            config.set(getKeyExt(key), value);
        }

        @Override
        public void setDouble(String key, double value) {
            config.set(getKeyExt(key), String.valueOf(value));
        }

        @Override
        public void setInt(String key, int value) {
            config.set(getKeyExt(key), value);
        }

        @Override
        public void setLong(String key, long value) {
            config.set(getKeyExt(key), value);
        }

        @Override
        public void setRaw(String key, Object value) {
            config.set(getKeyExt(key), value);
        }

        @Override
        public void setString(String key, String value) {
            config.set(getKeyExt(key), value);
        }
    }
}