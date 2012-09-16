package net.citizensnpcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ListResourceBundle;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.google.common.io.Closeables;

public enum Messages {
    CITIZENS_IMPLEMENTATION_DISABLED("citizens.changed-implementation",
            "Citizens implementation changed, disabling plugin."),
    FAILED_LOAD_SAVES("citizens.saves.load-failed", "Unable to load saves, disabling...");
    private String defaultTranslation;
    private String key;

    Messages(String key, String defaultTranslation) {
        this.key = key;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return key;
    }

    private static ResourceBundle defaultBundle;

    public static ResourceBundle getDefaultResourceBundle(File resourceDirectory, String fileName) {
        if (defaultBundle == null) {
            resourceDirectory.mkdirs();

            File bundleFile = new File(resourceDirectory, fileName);
            if (!bundleFile.exists())
                try {
                    bundleFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            populateDefaults(bundleFile);
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(bundleFile);
                defaultBundle = new PropertyResourceBundle(stream);
            } catch (Exception e) {
                e.printStackTrace();
                defaultBundle = getFallbackResourceBundle();
            } finally {
                Closeables.closeQuietly(stream);
            }
        }
        return defaultBundle;
    }

    private static ResourceBundle getFallbackResourceBundle() {
        return new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                Messages[] values = values();
                Object[][] contents = new Object[values.length][2];
                for (int i = 0; i < values.length; i++) {
                    Messages message = values[i];
                    contents[i] = new Object[] { message.key, message.defaultTranslation };
                }
                return contents;
            }
        };
    }

    private static void populateDefaults(File bundleFile) {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(bundleFile);
            properties.load(in);
        } catch (IOException e) {
        } finally {
            Closeables.closeQuietly(in);
        }
        for (Messages message : values()) {
            if (!properties.containsKey(message.key))
                properties.put(message.key, message.defaultTranslation);
        }
        OutputStream stream = null;
        try {
            properties.store(stream = new FileOutputStream(bundleFile), "");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.closeQuietly(stream);
        }
    }
}
