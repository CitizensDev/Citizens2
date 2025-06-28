package net.citizensnpcs.api.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

public class YamlStorage implements Storage {
    private final FileConfiguration config;
    private final File file;
    private final boolean transformLists;

    public YamlStorage(File file) {
        this(file, null);
    }

    public YamlStorage(File file, String header) {
        this(file, header, false);
    }

    public YamlStorage(File file, String header, boolean transformLists) {
        config = new YamlConfiguration();
        tryIncreaseMaxCodepoints(config);
        this.transformLists = transformLists;
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
            Messaging.debug("Creating file: " + file.getName());
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException ex) {
            Messaging.severe("Could not create file: " + file.getName());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        YamlStorage other = (YamlStorage) obj;
        return Objects.equals(file, other.file);
    }

    @Override
    public DataKey getKey(String root) {
        return new MemoryDataKey(config, root);
    }

    @Override
    public int hashCode() {
        return 31 + (file == null ? 0 : file.hashCode());
    }

    @Override
    public boolean load() {
        try {
            config.load(file);
            if (transformLists) {
                transformListsToMapsInConfig(config);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void save() {
        try {
            Files.createParentDirs(file);
            File temporaryFile = File.createTempFile(file.getName(), null, file.getParentFile());
            temporaryFile.deleteOnExit();
            FileConfiguration save = config;
            if (transformLists) {
                save = new YamlConfiguration();
                for (String key : config.getKeys(false)) {
                    save.set(key, config.get(key));
                }
                transformMapsToListsInConfig(save);
            }
            save.save(temporaryFile);
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

    private void transformListsToMapsInConfig(ConfigurationSection root) {
        List<ConfigurationSection> list = Lists.newArrayList(root);
        while (list.size() > 0) {
            ConfigurationSection section = list.remove(list.size() - 1);
            for (String key : section.getKeys(false)) {
                Object value = section.get(key);
                if (value instanceof Collection) {
                    ConfigurationSection listified = section.createSection(key);
                    int i = 0;
                    for (Iterator<?> itr = ((Collection) value).iterator(); itr.hasNext();) {
                        listified.set(Integer.toString(i++), itr.next());
                    }
                } else if (value instanceof ConfigurationSection) {
                    list.add((ConfigurationSection) value);
                }
            }
        }
    }

    private void transformMapsToListsInConfig(ConfigurationSection root) {
        List<ConfigurationSection> list = Lists.newArrayList(root);
        List<Tuple> convert = Lists.newArrayList();
        while (list.size() > 0) {
            ConfigurationSection parent = list.remove(list.size() - 1);

            for (String key : parent.getKeys(false)) {
                Object value = parent.get(key);
                if (value instanceof ConfigurationSection) {
                    list.add((ConfigurationSection) value);
                    convert.add(new Tuple(parent, key));
                }
            }
        }
        outer: for (Tuple t : convert) {
            List<Integer> ints = t.parent.getConfigurationSection(t.key).getKeys(false).stream()
                    .map(i -> Ints.tryParse(i)).collect(Collectors.toList());
            if (ints.size() == 0)
                continue;

            int sum = 0;
            for (Integer i : ints) {
                if (i == null || i < 0)
                    continue outer;

                sum += i;
            }
            if (sum == ints.size() * (ints.get(0) + ints.get(ints.size() - 1) / 2) && ints.get(0) == 0) {
                t.parent.set(t.key,
                        ints.stream().map(i -> t.parent.getConfigurationSection(t.key).get(Integer.toString(i)))
                                .collect(Collectors.toList()));
            }
        }
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

    private static class Tuple {
        String key;
        ConfigurationSection parent;

        public Tuple(ConfigurationSection parent2, String key2) {
            parent = parent2;
            key = key2;
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