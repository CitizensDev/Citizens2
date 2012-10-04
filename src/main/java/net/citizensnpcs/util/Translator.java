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
    private final Locale defaultLocale;
    private final Map<String, MessageFormat> messageFormatCache = Maps.newHashMap();
    private ResourceBundle preferredBundle;
    private final File resourceFile;

    private Translator(File resourceFile, Locale locale) {
        this.resourceFile = resourceFile;
        this.defaultLocale = locale;
        try {
            preferredBundle = ResourceBundle.getBundle(PREFIX, defaultLocale, new FileClassLoader(
                    Translator.class.getClassLoader(), resourceFile));
        } catch (MissingResourceException e) {
            preferredBundle = getDefaultBundle();
        }
    }

    private String format(String key, Locale locale, Object... msg) {
        String unreplaced = translate(key, locale);
        MessageFormat formatter = getFormatter(unreplaced);
        return formatter.format(msg);
    }

    private ResourceBundle getBundle(Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(PREFIX, locale, new FileClassLoader(
                    Translator.class.getClassLoader(), resourceFile));
            return bundle == null ? preferredBundle : bundle;
        } catch (MissingResourceException e) {
            return preferredBundle;
        }
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

    private String translate(String key, Locale locale) {
        ResourceBundle bundle = preferredBundle;
        if (locale != defaultLocale)
            bundle = getBundle(locale);
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

    static String tr(String key, Locale preferredLocale, Object... msg) {
        return StringHelper.parseColors(msg.length == 0 ? instance.translate(key, preferredLocale) : instance
                .format(key, preferredLocale, msg));
    }

    static String tr(String key, Object... msg) {
        return tr(key, instance.defaultLocale, msg);
    }
}
