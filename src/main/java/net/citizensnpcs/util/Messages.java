package net.citizensnpcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ListResourceBundle;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import net.citizensnpcs.api.CitizensAPI;

import com.google.common.io.Closeables;

public enum Messages {
    ;
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

    public static ResourceBundle getDefaultResourceBundle() {
        if (defaultBundle == null) {
            File dir = new File(CitizensAPI.getDataFolder(), "i18n");
            dir.mkdirs();

            File bundleFile = new File(dir, Translator.PREFIX + "_en.properties");
            if (!bundleFile.exists())
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
        for (Messages message : values()) {
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
