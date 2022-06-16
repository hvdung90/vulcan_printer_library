package net.jsecurity.printbot.prefs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import com.itextpdf.text.pdf.BidiOrder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;
import net.jsecurity.printbot.model.PrintBotInfo;
import org.json.JSONArray;

public class SettingsHelper {
    private static final String BROKEN_ARCHOS_ID = "dead00beef";
    private static final String BROKEN_ID = "9774d56d682e549c";
    private static final String EMPTY_MAC = "00:00:00:00:00:00";
    private static final String HEXES = "0123456789abcdef";

    public static List<PrintBotInfo> getStaticPrinters(Context ctx) {
        List<PrintBotInfo> ret = new ArrayList<>();
        int i = 1;
        while (true) {
            PrintBotInfo info = getPrinter(ctx, i);
            if (info == null) {
                return ret;
            }
            if (!GUIConstants.PROTOCOL_BONJOUR.equals(info.getProtocol())) {
                ret.add(info);
            }
            i++;
        }
    }

    public static int getPrinterCount(Context ctx) {
        int i = 0;
        while (getPrinter(ctx, i + 1) != null) {
            i++;
        }
        return i;
    }

    public static int getPrinterIndex(Context ctx, String networkUrl) {
        int i = 1;
        while (true) {
            PrintBotInfo info = getPrinter(ctx, i);
            if (info == null) {
                return -1;
            }
            if (networkUrl.equals(info.getNetworkUrl())) {
                return i;
            }
            i++;
        }
    }

    public static PrintBotInfo getPrinter(Context ctx, String networkUrl) {
        int i = 1;
        while (true) {
            PrintBotInfo info = getPrinter(ctx, i);
            if (info == null) {
                return null;
            }
            if (info.getNetworkUrl().equals(networkUrl)) {
                Log.i("PrintBot", "Found PrinterInfo " + networkUrl);
                return info;
            }
            i++;
        }
    }

    public static PrintBotInfo getPrinter(Context ctx, int ix) {
        JSONArray array;
        SharedPreferences pref = ctx.getSharedPreferences(GUIConstants.PREFERENCE_KEY_PREFIX + ix, 0);
        if (!(pref.contains(GUIConstants.PROTOCOL) || pref.contains(GUIConstants.DRIVER))) {
            return null;
        }
        PrintBotInfo printer = new PrintBotInfo(ix);
        printer.setProtocol(pref.getString(GUIConstants.PROTOCOL, null));
        printer.setHost(pref.getString(GUIConstants.HOST, null));
        printer.setQueue(pref.getString(GUIConstants.QUEUE, null));
        printer.setUser(pref.getString(GUIConstants.USER, null));
        printer.setPassword(pref.getString(GUIConstants.PASSWORD, null));
        printer.setManufacturer(pref.getString(GUIConstants.MANUFACTURER, null));
        printer.setDriver(pref.getString(GUIConstants.DRIVER, null));
        printer.setResolution(pref.getString(GUIConstants.DEFAULT_RESOLUTION, null));
        printer.setPageSize(pref.getString(GUIConstants.DEFAULT_PAGE_SIZE, printer.getPageSize()));
        printer.setBonjourKey(pref.getString(GUIConstants.BONJOUR_KEY, null));
        List<KeyValuePair> resolutions = new ArrayList<>();
        try {
            String resolutionString = pref.getString(GUIConstants.RESOLUTIONS, null);
            if (!(resolutionString == null || (array = new JSONArray(resolutionString)) == null)) {
                for (int i = 0; i < array.length(); i++) {
                    resolutions.add(new KeyValuePair(array.getString(i), array.getString(i)));
                }
            }
        } catch (Throwable e) {
            Log.w("PrintVulcan", e);
        }
        printer.setResolutions(resolutions);
        return printer;
    }

    public static void deletePrinter(Context ctx, int ix) {
        SharedPreferences pref = ctx.getSharedPreferences(GUIConstants.PREFERENCE_KEY_PREFIX + ix, 0);
        while (true) {
            ix++;
            SharedPreferences nextPref = ctx.getSharedPreferences(GUIConstants.PREFERENCE_KEY_PREFIX + ix, 0);
            if (nextPref.contains(GUIConstants.PROTOCOL) || pref.contains(GUIConstants.DRIVER)) {
                Map<String, ?> allPrefs = nextPref.getAll();
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                for (String key : allPrefs.keySet()) {
                    editor.putString(key, (String) allPrefs.get(key));
                }
                editor.commit();
                pref = nextPref;
            } else {
                SharedPreferences.Editor editor2 = pref.edit();
                editor2.clear();
                editor2.commit();
                return;
            }
        }
    }

    public static List<KeyValuePair> getProtocols(Context ctx) {
        List<KeyValuePair> ret = new ArrayList<>(3);
        Resources resources = ctx.getResources();
        ret.add(new KeyValuePair(GUIConstants.PROTOCOL_RAW, resources.getString(R.string.ProtocolRAW)));
        ret.add(new KeyValuePair(GUIConstants.PROTOCOL_LPD, resources.getString(R.string.ProtocolLPD)));
        ret.add(new KeyValuePair(GUIConstants.PROTOCOL_IPP, resources.getString(R.string.ProtocolIPP)));
        ret.add(new KeyValuePair(GUIConstants.PROTOCOL_FRITZ, resources.getString(R.string.ProtocolFritz)));
        return ret;
    }

    public static List<KeyValuePair> getPageSizes() {
        List<KeyValuePair> ret = new ArrayList<>(GUIConstants.PAGE_SIZES.length);
        String[] strArr = GUIConstants.PAGE_SIZES;
        for (String ps : strArr) {
            ret.add(new KeyValuePair(ps, UIUtil.formatPageSize(ps)));
        }
        return ret;
    }

    public static Intent getProIntent(Context ctx) {
        Intent pro = new Intent("android.intent.action.RUN");
        pro.setComponent(new ComponentName(GUIConstants.PRO_PACKAGE, "net.jsecurity.printbot.pro.PrintBotPro"));
        if (ctx.getPackageManager().queryIntentActivities(pro, 0).size() == 1) {
            return pro;
        }
        return null;
    }

    public static int getProVersion(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(GUIConstants.PRO_PACKAGE, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static boolean hasVersionChanged(Context ctx) {
        int lastVersion = ctx.getSharedPreferences(GUIConstants.PREFERENCE_KEY, 0).getInt("version", -1);
        int currentVersion = getCurrentVersion(ctx);
        Log.d("PrintVulcan", "Version " + currentVersion + " " + lastVersion);
        if (lastVersion != currentVersion) {
            return true;
        }
        return false;
    }

    private static int getCurrentVersion(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("PrintVulcan", "Package info not found", e);
            return 0;
        }
    }

    public static void setInstalledVersion(Context ctx) {
        int version = getCurrentVersion(ctx);
        SharedPreferences.Editor editor = ctx.getSharedPreferences(GUIConstants.PREFERENCE_KEY, 0).edit();
        editor.putInt("version", version);
        editor.commit();
    }

    public static String getDeviceId(Context ctx) {
        WifiInfo info;
        String wifiMac;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            int source = 0;
            String androidId = Settings.System.getString(ctx.getContentResolver(), "android_id");
            if (androidId != null && !BROKEN_ID.equals(androidId) && !BROKEN_ARCHOS_ID.equals(androidId)) {
                source = 0 | 2;
                digest.update(androidId.getBytes());
            }
            if (source == 0 && (info = ((WifiManager) ctx.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo()) != null && (wifiMac = info.getMacAddress()) != null && wifiMac.length() > 0 && !EMPTY_MAC.equals(wifiMac)) {
                source |= 4;
                digest.update(wifiMac.getBytes());
            }
            return source + getHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e("PrintVulcan", "NoSuchAlgorithmException", e);
            throw new RuntimeException(e);
        }
    }

    private static String getHex(byte[] raw) {
        StringBuilder hex = new StringBuilder(raw.length * 2);
        for (byte b : raw) {
            hex.append(HEXES.charAt((b & 240) >> 4)).append(HEXES.charAt(b & BidiOrder.B));
        }
        return hex.toString();
    }

    public static String getBonjourName(String bonjourKey) {
        int ix = bonjourKey.indexOf(58);
        if (ix == -1) {
            return bonjourKey + " (LPR)";
        }
        String type = bonjourKey.substring(0, ix);
        String bonjourName = bonjourKey.substring(ix + 1);
        if (type.equals(GUIConstants.MDNS_JETDIRECT)) {
            return bonjourName + " (RAW)";
        }
        if (type.equals(GUIConstants.MDNS_IPP)) {
            return bonjourName + " (IPP)";
        }
        if (type.equals(GUIConstants.MDNS_LPR)) {
            return bonjourName + " (LPR)";
        }
        return bonjourName;
    }

    public static void persistReceipt(Context ctx, String userId, String receiptId) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences(GUIConstants.PREFERENCE_KEY, 0).edit();
        editor.putString("AU", userId);
        if (receiptId != null) {
            editor.putString("AR", receiptId);
        }
        editor.putInt("PV", 43);
        editor.commit();
    }
}
