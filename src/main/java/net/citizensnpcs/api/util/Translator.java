package net.citizensnpcs.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

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
            Messaging.severe("Missing preferred location bundle.");
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
        return getDefaultResourceBundle(resourceFile, PREFIX + "_ko.properties");
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
            final InputStream stream = Translator.class.getResourceAsStream('/' + fileName);
            if (stream == null)
                return;
            InputSupplier<InputStream> in = new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    return stream;
                }
            };
            try {
                rootFolder.mkdirs();
                File to = File.createTempFile(fileName, null, rootFolder);
                to.deleteOnExit();
                Files.copy(in, to);
                if (!file.exists())
                    to.renameTo(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static interface TranslationProvider {
        InputStream createInputStream();

        String getName();
    }

    private static ResourceBundle defaultBundle;
    private static Translator instance;
    public static final String PREFIX = "messages";

    private static void addTranslation(TranslationProvider from, File to) {
        Properties props = new Properties();
        InputStream in = from.createInputStream();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closeables.closeQuietly(in);
        }
        if (to.exists()) {
            try {
                props.load(in = new FileInputStream(to));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Closeables.closeQuietly(in);
            }
        }
        OutputStream out = null;
        try {
            props.store(out = new FileOutputStream(to), "");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closeables.closeQuietly(out);
        }
    }

    public static void addTranslations(Collection<TranslationProvider> providers) {
        for (TranslationProvider provider : providers) {
            addTranslation(provider, new File(instance.resourceFile, provider.getName()));
        }
        defaultBundle = null;
        setInstance(instance.resourceFile, instance.preferredBundle.getLocale());
    }

    public static void addTranslations(TranslationProvider... providers) {
        addTranslations(Arrays.asList(providers));
    }

    private static Properties getDefaultBundleProperties() {
        Properties defaults = new Properties();
        InputStream in = null;
        try {
            in = Translator.class.getResourceAsStream("/" + PREFIX + "_ko.properties");
            defaults.load(in);
        } catch (IOException e) {
        } finally {
            Closeables.closeQuietly(in);
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
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(bundleFile);
            Translator.defaultBundle = new PropertyResourceBundle(stream);
        } catch (Exception e) {
            e.printStackTrace();
            Translator.defaultBundle = Translator.getFallbackResourceBundle();
        } finally {
            Closeables.closeQuietly(stream);
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
        InputStream in = null;
        try {
            in = new FileInputStream(bundleFile);
            properties.load(in);
        } catch (IOException e) {
        } finally {
            Closeables.closeQuietly(in);
        }
        Properties defaults = getDefaultBundleProperties();
        for (Entry<Object, Object> entry : defaults.entrySet()) {
            if (!properties.containsKey(entry.getKey()))
                properties.put(entry.getKey(), entry.getValue());
        }
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(bundleFile);
            properties.store(stream, "");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.closeQuietly(stream);
        }
    }

    public static void setInstance(File dataFolder, Locale preferredLocale) {
        instance = new Translator(dataFolder, preferredLocale);
    }

    public static String translate(String key, Locale preferredLocale, Object... msg) {
        return Colorizer.parseColors(msg.length == 0 ? instance.translate(key, preferredLocale) : instance.format(key,
                preferredLocale, msg));
    }

    public static String translate(String key, Object... msg) {
        return translate(key, instance.defaultLocale, msg);
    }
}
