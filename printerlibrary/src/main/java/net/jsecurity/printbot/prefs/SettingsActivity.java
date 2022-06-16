package net.jsecurity.printbot.prefs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.print.PrintJobInfo;
import android.util.Log;
import android.view.Menu;

import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfObject;
import java.util.ArrayList;
import java.util.List;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.engine.PrintTask;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;
import net.jsecurity.printbot.model.PrintBotInfo;

public class SettingsActivity extends PreferenceActivity {
    private static final String CONNECTION_PREFERENCE = "connection";
    private static final String PRINTER_PREFERENCE = "printer";
    private static final String RESOLUTION_PREFERENCE = "resolution";
    private static final String TEST_BUTTON = "testButton";
    private Preference connectionPref;
    private int currentPrinterIndex;
    private Preference printerPref;
    private ListPreference resolutionPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("PrintVulcan", "Starting settings activity");
        this.currentPrinterIndex = getIntent().getIntExtra(GUIConstants.PRINTER_INDEX, -1);
        PrintJobInfo oldJobInfo = (PrintJobInfo) getIntent().getParcelableExtra("android.intent.extra.print.PRINT_JOB_INFO");
        if (!(oldJobInfo == null || oldJobInfo.getPrinterId() == null || oldJobInfo.getPrinterId().getLocalId() == null)) {
            String networkName = oldJobInfo.getPrinterId().getLocalId();
            this.currentPrinterIndex = SettingsHelper.getPrinterIndex(this, networkName);
            if (this.currentPrinterIndex == -1) {
                Log.e("PrintVulcan", "Printer " + networkName + " not found");
                setResult(0, new Intent());
                finish();
            }
        }
        if (this.currentPrinterIndex == -1) {
            this.currentPrinterIndex = SettingsHelper.getPrinterCount(this) + 1;
        }
        addPreferencesFromResource(R.layout.preferences);
        this.resolutionPref = (ListPreference) findPreference(RESOLUTION_PREFERENCE);
        this.connectionPref = findPreference(CONNECTION_PREFERENCE);
        this.printerPref = findPreference("printer");
        updateResolutions();
        this.resolutionPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            /* class net.jsecurity.printbot.prefs.SettingsActivity.AnonymousClass1 */

            public boolean onPreferenceChange(Preference pref, Object newValue) {
                SettingsActivity.this.saveField(GUIConstants.DEFAULT_RESOLUTION, PdfObject.NOTHING + newValue);
                SettingsActivity.this.updateSummaries();
                return true;
            }
        });
        findPreference(TEST_BUTTON).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            /* class net.jsecurity.printbot.prefs.SettingsActivity.AnonymousClass2 */

            public boolean onPreferenceClick(Preference pref) {
                Activity activity = SettingsActivity.this;
                PrintBotInfo info = SettingsHelper.getPrinter(activity, SettingsActivity.this.currentPrinterIndex);
                if (info != null) {
                    ProgressDialog progressDialog = ProgressDialog.show(activity, PdfObject.NOTHING, activity.getResources().getString(R.string.PrintTestPage), true, true);
                    progressDialog.setOwnerActivity(activity);
                    PrintTask task = new PrintTask(activity, info);
                    task.setProgressDialog(progressDialog);
                    task.execute((Void) null);
                }
                return false;
            }
        });
        updateSummaries();
    }

    /* access modifiers changed from: protected */
    public void updateResolutions() {
        PrintBotInfo info = SettingsHelper.getPrinter(this, this.currentPrinterIndex);
        List<KeyValuePair> resolutions = new ArrayList<>();
        String resolution = null;
        if (info != null) {
            resolutions = info.getResolutions();
            resolution = info.getResolution();
        }
        UIUtil.setDropDownValues(this.resolutionPref, resolutions, resolution);
    }

    /* access modifiers changed from: protected */
    public void updateSummaries() {
        PrintBotInfo info = SettingsHelper.getPrinter(this, this.currentPrinterIndex);
        if (info != null) {
            this.connectionPref.setSummary(info.getNetworkUrl());
            if (GUIConstants.PROTOCOL_BONJOUR.equals(info.getProtocol())) {
                this.connectionPref.setEnabled(false);
            }
            this.printerPref.setSummary(UIUtil.formatManufacturer(info.getManufacturer()) + " " + UIUtil.formatModel(info.getDriver()));
            this.resolutionPref.setSummary(info.getResolution());
        }
    }

    public void saveField(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(GUIConstants.PREFERENCE_KEY_PREFIX + this.currentPrinterIndex, 0).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void saveFields(KeyValuePair[] items) {
        SharedPreferences.Editor editor = getSharedPreferences(GUIConstants.PREFERENCE_KEY_PREFIX + this.currentPrinterIndex, 0).edit();
        for (KeyValuePair i : items) {
            editor.putString(i.getKey(), i.toString());
        }
        editor.commit();
    }

    public int getCurrentPrinterIndex() {
        return this.currentPrinterIndex;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        UIUtil.addStandardMenu(this, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
