package net.citizensnpcs.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class Translator {
    private final Map<String, String> baseTranslations = getBaseTranslations();
    private final Locale defaultLocale;
    private final Map<String, MessageFormat> messageFormatCache = Maps.newHashMap();
    private final Map<String, String> translations;

    private Translator(File resourceDir, Locale locale) {
        this.defaultLocale = locale;
        this.translations = getTranslations(resourceDir, locale);
    }

    private String format(String key, Locale locale, Object... msg) {
        String unreplaced = translate(key, locale);
        MessageFormat formatter = getFormatter(unreplaced);
        return formatter.format(msg);
    }

    private MessageFormat getFormatter(String unreplaced) {
        MessageFormat formatter = messageFormatCache.get(unreplaced);
        if (formatter == null) {
            messageFormatCache.put(unreplaced, formatter = new MessageFormat(unreplaced));
        }
        return formatter;
    }

    private String translate(String key) {
        String res = translations.computeIfAbsent(key, k -> baseTranslations.get(key));
        return res == null ? "?" + key + "?" : res;
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

    public static String format(String msg, Object... objects) {
        MessageFormat formatter = INSTANCE.getFormatter(msg);
        return formatter.format(objects);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getBaseTranslations() {
        JSONParser jp = new JSONParser();
        try (Reader in = new InputStreamReader(Translator.class.getResourceAsStream("/en.json"),
                StandardCharsets.UTF_8)) {
            return (JSONObject) jp.parse(in);
        } catch (ParseException | IOException e) {
            return null;
        }
    }

    private static Charset getCharset(String fileName) {
        Charset charset = JAPANESE_PATTERN.matcher(fileName).find() && Charset.isSupported("Shift-JIS")
                ? Charset.forName("Shift-JIS")
                : StandardCharsets.UTF_8;
        return charset;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getTranslations(File resourceDir, Locale locale) {
        JSONParser jp = new JSONParser();
        InputStream is = null;
        Charset charset = getCharset(locale.toString() + ".json");
        if (new File(resourceDir, locale.toString() + ".json").exists()) {
            try {
                is = new FileInputStream(new File(resourceDir, locale.toString() + ".json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (new File(resourceDir, locale.getLanguage() + ".json").exists()) {
            try {
                is = new FileInputStream(new File(resourceDir, locale.getLanguage() + ".json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            is = Translator.class.getResourceAsStream("/" + locale.getLanguage() + ".json");
            if (is != null) {
                if (!locale.getLanguage().equals("en")) {
                    new Thread(new SaveResource(resourceDir, locale.getLanguage() + ".json")).start();
                }
            } else {
                is = Translator.class.getResourceAsStream("/" + locale.toString() + ".json");
                if (is != null && !locale.getLanguage().equals("en")) {
                    new Thread(new SaveResource(resourceDir, locale.toString() + ".json")).start();
                }
            }
        }
        try (Reader in = new InputStreamReader(is, charset)) {
            return (JSONObject) jp.parse(in);
        } catch (NullPointerException | ParseException | IOException e) {
            return Maps.newHashMap();
        }
    }

    public static void setInstance(File dataFolder, Locale preferredLocale) {
        INSTANCE = new Translator(dataFolder, preferredLocale);
    }

    private static String translate(String key, Locale preferredLocale, Object... msg) {
        return msg.length == 0 ? INSTANCE.translate(key) : INSTANCE.format(key, preferredLocale, msg);
    }

    public static String translate(String key, Object... msg) {
        return translate(key, INSTANCE.defaultLocale, msg);
    }

    private static Translator INSTANCE;
    private static final Pattern JAPANESE_PATTERN = Pattern.compile(".*?ja(_jp)?\\.json", Pattern.CASE_INSENSITIVE);
}
