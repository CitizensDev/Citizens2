package net.citizensnpcs.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MojangSkinGenerator {
    public static JSONObject generateFromPNG(final byte[] png) throws InterruptedException, ExecutionException {
        return EXECUTOR.submit(() -> {
            DataOutputStream out = null;
            BufferedReader reader = null;
            try {
                URL target = new URL("https://api.mineskin.org/generate/upload");
                HttpURLConnection con = (HttpURLConnection) target.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("Cache-Control", "no-cache");
                con.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
                con.setConnectTimeout(1000);
                con.setReadTimeout(30000);
                out = new DataOutputStream(con.getOutputStream());
                out.writeBytes("--*****\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"skin.png\";filename=\"skin.png\"\r\n\r\n");
                out.write(png);
                out.writeBytes("\r\n");
                out.writeBytes("--*****--\r\n");
                out.flush();
                out.close();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JSONObject output = (JSONObject) new JSONParser().parse(reader);
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

    public static JSONObject generateFromURL(final String url) throws InterruptedException, ExecutionException {
        return EXECUTOR.submit(() -> {
            DataOutputStream out = null;
            BufferedReader reader = null;
            try {
                URL target = new URL("https://api.mineskin.org/generate/url");
                HttpURLConnection con = (HttpURLConnection) target.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setConnectTimeout(1000);
                con.setReadTimeout(30000);
                out = new DataOutputStream(con.getOutputStream());
                out.writeBytes("url=" + URLEncoder.encode(url, "UTF-8"));
                out.close();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JSONObject output = (JSONObject) new JSONParser().parse(reader);
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

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
}
