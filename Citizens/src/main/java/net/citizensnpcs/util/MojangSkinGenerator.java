package net.citizensnpcs.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.io.CharStreams;
import com.mojang.authlib.GameProfile;

import net.citizensnpcs.api.util.Messaging;

public class MojangSkinGenerator {
    public static JSONObject generateFromPNG(final byte[] png, boolean slim)
            throws InterruptedException, ExecutionException {
        return EXECUTOR.submit(() -> {
            DataOutputStream out = null;
            InputStreamReader reader = null;
            try {
                URL target = new URI("https://api.mineskin.org/generate/upload" + (slim ? "?model=slim" : "")).toURL();
                HttpURLConnection con = (HttpURLConnection) target.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("User-Agent", "Citizens/2.0");
                con.setRequestProperty("Cache-Control", "no-cache");
                con.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
                con.setConnectTimeout(2000);
                con.setReadTimeout(30000);
                out = new DataOutputStream(con.getOutputStream());
                out.writeBytes("--*****\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"\r\n");
                out.writeBytes("Content-Type: image/png\r\n\r\n");
                out.write(png);
                out.writeBytes("\r\n");
                out.writeBytes("--*****\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"name\";\r\n\r\n\r\n");
                if (slim) {
                    out.writeBytes("--*****\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"variant\";\r\n\r\n");
                    out.writeBytes("slim\r\n");
                }
                out.writeBytes("--*****--\r\n");
                out.flush();
                out.close();
                reader = new InputStreamReader(con.getInputStream());
                String str = CharStreams.toString(reader);
                if (Messaging.isDebugging()) {
                    Messaging.debug(str);
                }
                if (con.getResponseCode() != 200)
                    return null;

                JSONObject output = (JSONObject) new JSONParser().parse(str);
                JSONObject data = (JSONObject) output.get("data");
                con.disconnect();
                return data;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).get();
    }

    public static JSONObject generateFromURL(final String url, boolean slim)
            throws InterruptedException, ExecutionException {
        return EXECUTOR.submit(() -> {
            DataOutputStream out = null;
            InputStreamReader reader = null;
            try {
                URL target = new URI("https://api.mineskin.org/generate/url").toURL();
                HttpURLConnection con = (HttpURLConnection) target.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("User-Agent", "Citizens/2.0");
                con.setRequestProperty("Cache-Control", "no-cache");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Content-Type", "application/json");
                con.setConnectTimeout(2000);
                con.setReadTimeout(30000);
                out = new DataOutputStream(con.getOutputStream());
                JSONObject req = new JSONObject();
                req.put("url", url);
                req.put("name", "");
                if (slim) {
                    req.put("variant", "slim");
                }
                out.writeBytes(req.toJSONString().replace("\\", ""));
                out.close();
                reader = new InputStreamReader(con.getInputStream());
                String str = CharStreams.toString(reader);
                if (Messaging.isDebugging()) {
                    Messaging.debug(str);
                }
                if (con.getResponseCode() != 200)
                    return null;

                JSONObject output = (JSONObject) new JSONParser().parse(str);
                JSONObject data = (JSONObject) output.get("data");
                con.disconnect();
                return data;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).get();
    }

    public static GameProfile getFilledGameProfileByXUID(String name, long xuid)
            throws InterruptedException, ExecutionException {
        return EXECUTOR.submit(() -> {
            InputStreamReader reader = null;
            try {
                URL target = new URI("https://api.geysermc.org/v2/skin/" + xuid).toURL();
                HttpURLConnection con = (HttpURLConnection) target.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Citizens/2.0");
                con.setRequestProperty("Accept", "application/json");
                con.setConnectTimeout(2000);
                con.setReadTimeout(20000);
                reader = new InputStreamReader(con.getInputStream());
                String str = CharStreams.toString(reader);
                if (Messaging.isDebugging()) {
                    Messaging.debug(str);
                }
                if (con.getResponseCode() != 200)
                    return null;

                JSONObject output = (JSONObject) new JSONParser().parse(str);
                con.disconnect();
                String hex = Long.toHexString(xuid);
                GameProfile profile = new GameProfile(
                        UUID.fromString("00000000-0000-0000-" + hex.substring(0, 4) + "-" + hex.substring(4)), name);
                new SkinProperty((String) output.get("texture_id"), (String) output.get("value"),
                        (String) output.get("signature")).apply(profile);
                return profile;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).get();
    }

    public static Long getXUIDFromName(String name) throws InterruptedException, ExecutionException {
        return EXECUTOR.submit(() -> {
            InputStreamReader reader = null;
            try {
                URL target = new URI("https://api.geysermc.org/v2/xbox/xuid/" + name).toURL();
                HttpURLConnection con = (HttpURLConnection) target.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Citizens/2.0");
                con.setRequestProperty("Accept", "application/json");
                con.setConnectTimeout(2000);
                con.setReadTimeout(10000);
                reader = new InputStreamReader(con.getInputStream());
                String str = CharStreams.toString(reader);
                if (Messaging.isDebugging()) {
                    Messaging.debug(str);
                }
                if (con.getResponseCode() != 200)
                    return null;

                JSONObject output = (JSONObject) new JSONParser().parse(str);
                con.disconnect();
                if (!output.containsKey("xuid"))
                    return null;

                return ((Number) output.get("xuid")).longValue();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).get();
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
}
