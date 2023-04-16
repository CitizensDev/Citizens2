package net.citizensnpcs.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class Translator {
    private final Locale defaultLocale;
    private final Map<String, MessageFormat> messageFormatCache = Maps.newHashMap();
    private ResourceBundle preferredBundle;
    private final File resourceFile;

    private Translator(File resourceFile, Locale locale) {
        this.resourceFile = resourceFile;
        this.defaultLocale = locale;
        try {
            preferredBundle = ResourceBundle.getBundle(PREFIX, defaultLocale,
                    new FileClassLoader(Translator.class.getClassLoader(), resourceFile));
        } catch (MissingResourceException e) {
            preferredBundle = getDefaultBundle();
            Messaging.log("No translation for system language (" + defaultLocale + "): defaulting to English");
        }
    }

    private String format(String key, Locale locale, Object... msg) {
        String unreplaced = translate(key, locale);
        MessageFormat formatter = getFormatter(unreplaced);
        return formatter.format(msg);
    }

    private ResourceBundle getBundle(Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(PREFIX, locale,
                    new FileClassLoader(Translator.class.getClassLoader(), resourceFile));
            return bundle == null ? preferredBundle : bundle;
        } catch (MissingResourceException e) {
            return preferredBundle;
        }
    }

    private ResourceBundle getDefaultBundle() {
        return getDefaultResourceBundle(resourceFile, PREFIX + "_en.properties");
    }

    private MessageFormat getFormatter(String unreplaced) {
        MessageFormat formatter = messageFormatCache.get(unreplaced);
        if (formatter == null) {
            messageFormatCache.put(unreplaced, formatter = new MessageFormat(unreplaced));
        }
        return formatter;
    }

    private String translate(String key, Locale locale) {
        ResourceBundle bundle = preferredBundle;
        if (locale != defaultLocale) {
            bundle = getBundle(locale);
        }
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
            } else {
                string = string.replaceFirst("/", "");
                URL test = Translator.class.getResource('/' + string);
                if (test != null)
                    return test;
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
            } else {
                string = string.replaceFirst("/", "");
                InputStream stream = Translator.class.getResourceAsStream('/' + string);
                if (stream != null) {
                    new Thread(new SaveResource(folder, string)).start();
                    return stream;
                }
            }
            return super.getResourceAsStream(string);
        }
    }

    private static class SaveResource implements Runnable {
        private final String fileName;
        private final File rootFolder;

        private SaveResource(File rootFolder, String fileName) {
            this.rootFolder = rootFolder;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            File file = new File(rootFolder, fileName);
            if (file.exists())
                return;
            try {
                rootFolder.mkdirs();
                File to = File.createTempFile(fileName, null, rootFolder);
                to.deleteOnExit();
                Resources.asByteSource(Resources.getResource(Translator.class, '/' + fileName))
                        .copyTo(Files.asByteSink(to));
                if (!file.exists()) {
                    to.renameTo(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static interface TranslationProvider {
        InputStream createInputStream();

        String getName();
    }

    public static String format(String msg, Object... objects) {
        MessageFormat formatter = instance.getFormatter(msg);
        return formatter.format(objects);
    }

    private static Charset getCharset(String fileName) {
        Charset charset = JAPANESE_PATTERN.matcher(fileName).find() && Charset.isSupported("Shift-JIS")
                ? Charset.forName("Shift-JIS")
                : StandardCharsets.UTF_8;
        return charset;
    }

    private static Properties getDefaultBundleProperties() {
        Properties defaults = new Properties();
        try (Reader in = new InputStreamReader(Translator.class.getResourceAsStream("/" + PREFIX + "_en.properties"),
                StandardCharsets.UTF_8)) {
            defaults.load(in);
        } catch (IOException e) {
        }
        return defaults;
    }

    private static ResourceBundle getDefaultResourceBundle(File resourceDirectory, String fileName) {
        if (Translator.defaultBundle != null)
            return Translator.defaultBundle;
        resourceDirectory.mkdirs();

        File bundleFile = new File(resourceDirectory, fileName);
        if (!bundleFile.exists()) {
            try {
                bundleFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Translator.populateDefaults(bundleFile);
        try (Reader reader = new InputStreamReader(new FileInputStream(bundleFile), getCharset(fileName))) {
            Translator.defaultBundle = new PropertyResourceBundle(reader);
        } catch (Exception e) {
            e.printStackTrace();
            Translator.defaultBundle = Translator.getFallbackResourceBundle();
        }
        return Translator.defaultBundle;
    }

    private static ResourceBundle getFallbackResourceBundle() {
        return new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[0][0];
            }
        };
    }

    private static void populateDefaults(File bundleFile) {
        Properties properties = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(bundleFile), getCharset(bundleFile.getName()))) {
            properties.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties defaults = getDefaultBundleProperties();
        for (Entry<Object, Object> entry : defaults.entrySet()) {
            if (!properties.containsKey(entry.getKey())) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }

        try (Writer stream = new OutputStreamWriter(new FileOutputStream(bundleFile),
                getCharset(bundleFile.getName()))) {
            properties.store(stream, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setInstance(File dataFolder, Locale preferredLocale) {
        instance = new Translator(dataFolder, preferredLocale);
    }

    public static String translate(String key, Locale preferredLocale, Object... msg) {
        return msg.length == 0 ? instance.translate(key, preferredLocale) : instance.format(key, preferredLocale, msg);
    }

    public static String translate(String key, Object... msg) {
        return translate(key, instance.defaultLocale, msg);
    }

    private static ResourceBundle defaultBundle;
    private static Translator instance;
    private static final Pattern JAPANESE_PATTERN = Pattern.compile(".*?_ja(_jp)?\\.properties",
            Pattern.CASE_INSENSITIVE);
    private static final String PREFIX = "messages";
}
