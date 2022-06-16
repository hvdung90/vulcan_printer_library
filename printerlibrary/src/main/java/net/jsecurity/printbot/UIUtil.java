package net.jsecurity.printbot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.ListPreference;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.itextpdf.text.pdf.PdfObject;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;

public class UIUtil {
    @SuppressLint("ResourceType")
    public static void setDropDownValues(Spinner spinner, List<KeyValuePair> values, String defaultValue) {
        if (values == null || values.size() == 0) {
            values = new ArrayList<>(1);
            values.add(new KeyValuePair(null, "-"));
            spinner.setEnabled(false);
        } else {
            spinner.setEnabled(true);
        }
        @SuppressLint("ResourceType") ArrayAdapter<CharSequence> adapter = new ArrayAdapter(spinner.getContext(), 17_367_048, values);
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter((SpinnerAdapter) adapter);
        for (int i = 0; i < adapter.getCount(); i++) {
            KeyValuePair item = (KeyValuePair) adapter.getItem(i);
            if ((item.getKey() == null && defaultValue == null) || (defaultValue != null && defaultValue.equals(item.getKey()))) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    public static void setDropDownValues(ListPreference pref, List<KeyValuePair> items, String defaultEntry) {
        if (items == null || items.size() == 0) {
            pref.setEntries(new String[]{"-"});
            pref.setEntryValues(new String[]{PdfObject.NOTHING});
            pref.setEnabled(false);
            return;
        }
        String[] entries = new String[items.size()];
        String[] entryValues = new String[items.size()];
        int counter = 0;
        boolean containsDefault = false;
        for (KeyValuePair item : items) {
            entries[counter] = item.toString();
            entryValues[counter] = item.getKey();
            if (defaultEntry != null && defaultEntry.equals(item.getKey())) {
                containsDefault = true;
            }
            counter++;
        }
        pref.setEntries(entries);
        pref.setEntryValues(entryValues);
        if (containsDefault) {
            pref.setDefaultValue(defaultEntry);
        } else {
            pref.setDefaultValue(entryValues[0]);
        }
        pref.setEnabled(true);
    }

    public static String formatModel(String driver) {
        if (driver == null) {
            return "-";
        }
        return driver.split("-", 2)[1].replace('-', ' ').replace('_', '-');
    }

    public static String formatPageSize(String ps) {
        if (ps == null || ps.length() == 0) {
            return "-";
        }
        return Character.toUpperCase(ps.charAt(0)) + ps.substring(1);
    }

    public static String formatManufacturer(String manufacturer) {
        if (manufacturer == null) {
            return "-";
        }
        return manufacturer.replace('_', '-');
    }

    public static void showNagMessage(Context context, int messageId) {
//        Intent nagIntent = new Intent(context, NagnagIntentDialog.class);
//        nagIntent.putExtra("messageId", messageId)nagIntent;
//        if (context instanceof Activity) {nagIntent
//            ((Activity) context).startActivityForRnagIntentesult(nagIntent, 0);
//            return;nagIntent
//        }nagIntent
//        nagIntent.addFlags(268435456);nagIntent
//        context.startActivity(nagIntent);nagIntent
    }

    public static void showErrorDialog(Activity activity, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(messageId).setTitle(R.string.ErrorTitle).setCancelable(true);
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            /* class net.jsecurity.printbot.UIUtil.AnonymousClass1 */

            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOwnerActivity(activity);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            Log.e("PrintVulcan", "Unexpected BadTokenException", e);
        }
    }

    public static void debug(String message, Throwable e) {
        try {
            File debugDir = new File(Environment.getExternalStorageDirectory(), "/Android/data/net.jsecurity.printbot/");
            debugDir.mkdirs();
            PrintWriter stream = new PrintWriter(new FileWriter(new File(debugDir, "PrintBot.log"), true));
            if (message != null) {
                try {
                    stream.write(message + "\n");
                } catch (Throwable th) {
                    stream.close();
                    throw th;
                }
            }
            if (e != null) {
                e.printStackTrace(stream);
            }
            stream.close();
        } catch (Throwable e1) {
            Log.w("PrintVulcan", e1);
        }
    }

    public static void addStandardMenu(Context ctx, Menu menu) {
        Class<?> aboutClass = null;
        try {
            aboutClass = Class.forName("net.jsecurity.printbot.AboutDialog");
        } catch (ClassNotFoundException e) {
            Log.w("PrintVulcan", "AboutDialog not found");
        }
        if (aboutClass != null) {
            menu.add(R.string.About).setIntent(new Intent(ctx, aboutClass));
            menu.add(R.string.Help).setIntent(new Intent("android.intent.action.VIEW", Uri.parse(GUIConstants.HELP_URL)));
        }
    }
}
