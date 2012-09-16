package net.citizensnpcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.common.collect.Maps;

public class Translator {
    private ResourceBundle bundle;
    private final Map<String, MessageFormat> messageFormatCache = Maps.newHashMap();
    private final File resourceFile;

    private Translator(File resourceFile, Locale locale) {
        this.resourceFile = resourceFile;
        try {
            bundle = ResourceBundle.getBundle(PREFIX, locale,
                    new FileClassLoader(Translator.class.getClassLoader(), resourceFile));
        } catch (MissingResourceException e) {
            bundle = getDefaultBundle();
        }
    }

    public String format(String key, Object... msg) {
        String unreplaced = translate(key);
        MessageFormat formatter = getFormatter(unreplaced);
        return formatter.format(msg);
    }

    private ResourceBundle getDefaultBundle() {
        return Messages.getDefaultResourceBundle(resourceFile, PREFIX + "_en.properties");
    }

    private MessageFormat getFormatter(String unreplaced) {
        MessageFormat formatter = messageFormatCache.get(unreplaced);
        if (formatter == null)
            messageFormatCache.put(unreplaced, formatter = new MessageFormat(unreplaced));
        return formatter;
    }

    public String translate(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            try {
                return getDefaultBundle().getString(key);
            } catch (MissingResourceException ex) {
                return "?" + key + "?";
            }
        }
    }

    private static class FileClassLoader extends ClassLoader {
        private final File folder;

        public FileClassLoader(ClassLoader classLoader, File folder) {
            super(classLoader);
            this.folder = folder;
        }

        @Override
        public URL getResource(String string) {
            File file = new File(folder, string);
            if (file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException ex) {
                }
            }
            return super.getResource(string);
        }

        @Override
        public InputStream getResourceAsStream(String string) {
            File file = new File(folder, string);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                }
            }
            return super.getResourceAsStream(string);
        }
    }

    private static Translator instance;

    public static final String PREFIX = "messages";
    public static void setInstance(File resourceFile, Locale locale) {
        instance = new Translator(resourceFile, locale);
    }

    public static String tr(Messages key) {
        return tr(key.getKey());
    }

    public static String tr(Messages key, Object... msg) {
        return tr(key.getKey(), msg);
    }

    public static String tr(String key, Object... msg) {
        return msg.length == 0 ? instance.translate(key) : instance.format(key, msg);
    }
}
