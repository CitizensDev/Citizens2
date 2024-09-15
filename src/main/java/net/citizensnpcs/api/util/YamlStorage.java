package net.citizensnpcs.api.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

public class YamlStorage implements Storage {
    private final FileConfiguration config;
    private final File file;

    public YamlStorage(File file) {
        this(file, null);
    }

    public YamlStorage(File file, String header) {
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