package net.jsecurity.printbot;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Random;

import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.prefs.SettingsHelper;

public class Util {
    private static final Random RAND = new Random();

    public static void streamCopy(InputStream in, OutputStream out) {
        byte[] b = new byte[1024];
        while (true) {
            try {
                int read = in.read(b);
                if (read < 1) {
                    break;
                } else {
                    out.write(b, 0, read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            out.close();
            in.close();
        }
    }

    public static File createTempFile(Context ctx, String postfix) throws IOException {
        File cacheDir;
        if (!"mounted".equals(Environment.getExternalStorageState()) || !new File(ctx.getExternalCacheDir(), "PrintBotWriteDummy").canWrite()) {
            cacheDir = ctx.getCacheDir();
        } else {
            cacheDir = ctx.getExternalCacheDir();
        }
        File tempFile = new File(cacheDir, "PrintVulcan" + RAND.nextInt(1000000) + postfix);
        if (tempFile.exists()) {
            return createTempFile(ctx, postfix);
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamCopy(in, out);
        return out.toByteArray();
    }

    public static HttpURLConnection getHttpURLConnection(Context ctx, String servletString) throws IOException, MalformedURLException {
        HttpURLConnection con = (HttpURLConnection) new URL(GUIConstants.REMOTE_URL + servletString).openConnection();
        String version = "";
        try {
            PackageInfo pc = ctx.getPackageManager().getPackageInfo(GUIConstants.PREFERENCE_KEY, 0);
            version = pc.versionName != null ? pc.versionName : "Dev";
        } catch (PackageManager.NameNotFoundException e) {
        }
        con.setRequestProperty("User-Agent", "PrintBot/" + version + " (" + SettingsHelper.getDeviceId(ctx) + "; " + Build.MODEL + "; " + Locale.getDefault().getLanguage() + ")");
        con.setUseCaches(false);
        con.setConnectTimeout(GUIConstants.CONNECT_TIMEOUT);
        return con;
    }
}
