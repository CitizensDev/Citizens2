package net.citizensnpcs.api.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;
import com.google.common.primitives.Ints;

public class YamlStorageWithLists implements FileStorage {
    private final FileConfiguration config;
    private final File file;

    public YamlStorageWithLists(File file) {
        this(file, null);
    }

    public YamlStorageWithLists(File file, String header) {
        config = new YamlConfiguration();
        tryIncreaseMaxCodepoints(config);
        this.file = file;
        if (!file.exists()) {
            create();
            if (header != null) {
                config.options().header(header);
            }
            save();
        }
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        YamlStorageWithLists other = (YamlStorageWithLists) obj;
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        return true;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public YamlKey getKey(String root) {
        return new YamlKey(root);
    }

    @Override
    public int hashCode() {
        return 31 + ((file == null) ? 0 : file.hashCode());
    }

    @Override
    public boolean load() {
        try {
            config.load(file);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean pathExists(String key) {
        return config.get(key) != null;
    }

    @Override
    public void save() {
        try {
            Files.createParentDirs(file);
            File temporaryFile = File.createTempFile(file.getName(), null, file.getParentFile());
            temporaryFile.deleteOnExit();
            config.save(temporaryFile);
            file.delete();
            temporaryFile.renameTo(file);
            temporaryFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "YamlStorage {file=" + file + "}";
    }

    private void tryIncreaseMaxCodepoints(FileConfiguration config) {
        if (SET_CODEPOINT_LIMIT == null || LOADER_OPTIONS == null)
            return;
        try {
            SET_CODEPOINT_LIMIT.invoke(LOADER_OPTIONS.get(config), 67108864 /* ~64MB, Paper's limit */);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class YamlKey extends DataKey {
        public YamlKey(String root) {
            super(root);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || getClass() != obj.getClass()) {
                return false;
            }
            YamlKey other = (YamlKey) obj;
            return getOuterType().equals(other.getOuterType());
        }

        private Object get(String path, Object def) {
            int i1 = -1, i2;
            Function<String, Object> next = k -> {
                Object res = config.getConfigurationSection(k);
                return res == null ? config.getList(k) : res;
            };
            Object relative = config;
            while ((i1 = path.indexOf('.', i2 = i1 + 1)) != -1) {
                String curr = path.substring(i2, i1);
                relative = next.apply(curr);
                if (relative == null)
                    return def;
                if (relative instanceof List) {
                    List<Object> list = (List<Object>) relative;
                    next = k -> {
                        if (Ints.tryParse(k) >= list.size())
                            return def;
                        return list.get(Ints.tryParse(k));
                    };
                } else if (relative instanceof ConfigurationSection) {
                    ConfigurationSection section = (ConfigurationSection) relative;
                    next = k -> {
                        Object res = section.getConfigurationSection(k);
                        return res == null ? section.getList(k) : res;
                    };
                } else if (relative instanceof Map) {
                    Map map = (Map) relative;
                    next = k -> {
                        return map.get(k);
                    };
                }
            }

            if (relative == null)
                return def;

            if (relative instanceof ConfigurationSection) {
                return ((ConfigurationSection) relative).get(path.substring(i2));
            } else if (relative instanceof List) {
                return ((List) relative).get(Ints.tryParse(path.substring(i2)));
            } else if (relative instanceof Map) {
                return ((Map) relative).get(path.substring(i2));
            }

            throw new RuntimeException();
        }

        @Override
        public boolean getBoolean(String key) {
            String path = createRelativeKey(key);
            Object val = get(path, null);
            if (val != null) {
                return Boolean.parseBoolean(val.toString());
            }
            return false;
        }

        @Override
        public boolean getBoolean(String key, boolean def) {
            return (boolean) get(createRelativeKey(key), def);
        }

        @Override
        public double getDouble(String key) {
            return getDouble(key, 0);
        }

        @Override
        public double getDouble(String key, double def) {
            String path = createRelativeKey(key);
            Object val = get(path, def);
            if (val != null) {
                if (val instanceof Number) {
                    return ((Number) val).doubleValue();
                }
                String str = val.toString();
                return str.isEmpty() ? def : Double.parseDouble(str);
            }
            return def;
        }

        @Override
        public DataKey getFromRoot(String path) {
            return new YamlKey(path);
        }

        @Override
        public int getInt(String key) {
            return getInt(key, 0);
        }

        @Override
        public int getInt(String key, int def) {
            String path = createRelativeKey(key);
            Object val = get(path, def);
            if (val != null) {
                if (val instanceof Number) {
                    return ((Number) val).intValue();
                }
                String str = val.toString();
                return str.isEmpty() ? def : Integer.parseInt(str);
            }
            return def;
        }

        @Override
        public Iterable<DataKey> getIntegerSubKeys() {
            return super.getIntegerSubKeys();
        }

        @Override
        public long getLong(String key) {
            return getLong(key, 0L);
        }

        @Override
        public long getLong(String key, long def) {
            String path = createRelativeKey(key);
            Object val = get(path, def);
            if (val != null) {
                if (val instanceof Number) {
                    return ((Number) val).longValue();
                }
                String str = val.toString();
                return str.isEmpty() ? def : Long.parseLong(str);
            }
            return def;
        }

        private YamlStorageWithLists getOuterType() {
            return YamlStorageWithLists.this;
        }

        @Override
        public Object getRaw(String key) {
            return get(createRelativeKey(key), null);
        }

        @Override
        public YamlKey getRelative(String relative) {
            if (relative == null || relative.isEmpty())
                return this;
            return new YamlKey(createRelativeKey(relative));
        }

        public YamlStorageWithLists getStorage() {
            return YamlStorageWithLists.this;
        }

        @Override
        public String getString(String key) {
            String path = createRelativeKey(key);
            Object val = get(path, null);
            if (val != null) {
                return val.toString();
            }
            return "";
        }

        @Override
        public Iterable<DataKey> getSubKeys() {
            Object value = get(path, null);
            if (value == null)
                return Collections.emptyList();
            if (value instanceof List) {
                return IntStream.range(0, ((List) value).size()).mapToObj(i -> getRelative(i))
                        .collect(Collectors.toList());
            } else if (value instanceof Map) {
                return ((Map<String, Object>) value).keySet().stream().map(k -> getRelative(k))
                        .collect(Collectors.toList());
            } else {
                ConfigurationSection section = (ConfigurationSection) value;
                return section.getKeys(false).stream().map(k -> getRelative(k)).collect(Collectors.toList());
            }
        }

        @Override
        public Map<String, Object> getValuesDeep() {
            return sectionToValues(config.getConfigurationSection(path));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = prime * super.hashCode() + getOuterType().hashCode();
            return result;
        }

        @Override
        public boolean keyExists(String key) {
            return get(createRelativeKey(key), null) != null;
        }

        @Override
        public String name() {
            int last = path.lastIndexOf('.');
            return path.substring(last == 0 ? 0 : last + 1);
        }

        @Override
        public void removeKey(String key) {
            config.set(createRelativeKey(key), null);
        }

        private void set(String path, Object value) {
            int i1 = -1, i2;
            Object prev = config;

            while ((i1 = path.indexOf('.', i2 = i1 + 1)) != -1) {
                String curr = path.substring(i2, i1);
                Object next = null;

                if (prev instanceof ConfigurationSection) {
                    next = ((ConfigurationSection) prev).get(curr);
                } else if (prev instanceof Map) {
                    next = ((Map) prev).get(curr);
                } else if (prev instanceof List) {
                    Integer idx = Ints.tryParse(curr);
                    if (idx != null) {
                        List list = (List) prev;
                        while (list.size() <= idx) {
                            list.add(new HashMap());
                        }
                        next = list.get(idx);
                    }
                }

                int nextSegment = path.indexOf('.', i1 + 1);
                if (nextSegment != -1 && Ints.tryParse(path.substring(i1 + 1, nextSegment)) != null) {
                    if (!(next instanceof List)) {
                        next = new ArrayList<Object>();
                        if (prev instanceof ConfigurationSection) {
                            ((ConfigurationSection) prev).set(curr, next);
                        } else if (prev instanceof Map) {
                            ((Map) prev).put(curr, next);
                        }
                    }
                } else if (next == null) {
                    if (prev instanceof ConfigurationSection) {
                        next = ((ConfigurationSection) prev).createSection(curr);
                    } else if (prev instanceof Map) {
                        next = new HashMap();
                        ((Map) prev).put(curr, next);
                    }
                }
                prev = next;
            }

            if (prev == null) {
                throw new RuntimeException();
            }

            if (prev instanceof ConfigurationSection) {
                ((ConfigurationSection) prev).set(path.substring(i2), value);
            } else if (prev instanceof List) {
                ((List) prev).set(Ints.tryParse(path.substring(i2)), value);
            } else if (prev instanceof Map) {
                ((Map) prev).put(path.substring(i2), value);
            }
        }

        @Override
        public void setBoolean(String key, boolean value) {
            set(createRelativeKey(key), value);
        }

        @Override
        public void setDouble(String key, double value) {
            set(createRelativeKey(key), String.valueOf(value));
        }

        @Override
        public void setInt(String key, int value) {
            set(createRelativeKey(key), value);
        }

        @Override
        public void setLong(String key, long value) {
            set(createRelativeKey(key), value);
        }

        @Override
        public void setRaw(String key, Object value) {
            set(createRelativeKey(key), value);
        }

        @Override
        public void setString(String key, String value) {
            set(createRelativeKey(key), value);
        }

        @Override
        public String toString() {
            return "YamlKey [path=" + path + "]";
        }
    }

    private static Field LOADER_OPTIONS;
    private static Method SET_CODEPOINT_LIMIT;
    static {
        try {
            LOADER_OPTIONS = YamlConfiguration.class.getDeclaredField("yamlLoaderOptions");
            LOADER_OPTIONS.setAccessible(true);
            SET_CODEPOINT_LIMIT = Class.forName("org.yaml.snakeyaml.LoaderOptions").getMethod("setCodepointLimit",
                    int.class);
            SET_CODEPOINT_LIMIT.setAccessible(true);
        } catch (Exception e) {
        }
    }
}