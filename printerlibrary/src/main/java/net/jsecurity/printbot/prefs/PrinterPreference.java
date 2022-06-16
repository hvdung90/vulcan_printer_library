package net.jsecurity.printbot.prefs;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.engine.RemoteQueryListener;
import net.jsecurity.printbot.engine.RemoteQueryTask;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;
import net.jsecurity.printbot.model.PrintBotInfo;
import org.json.JSONArray;

public class PrinterPreference extends DialogPreference implements RemoteQueryListener {
    private String currentDriver;
    private String currentManufacturer;
    private Spinner manufacturerSpinner;
    private Spinner printerSpinner;
    private SettingsActivity settings;

    public PrinterPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrinterPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* access modifiers changed from: protected */
    public View onCreateDialogView() {
        View view = super.onCreateDialogView();
        this.settings = (SettingsActivity) getContext();
        int currentPrinterIndex = this.settings.getCurrentPrinterIndex();
        this.currentManufacturer = null;
        PrintBotInfo info = SettingsHelper.getPrinter(getContext(), currentPrinterIndex);
        if (info != null) {
            this.currentManufacturer = info.getManufacturer();
            this.currentDriver = info.getDriver();
        }
        this.manufacturerSpinner = (Spinner) view.findViewById(R.id.ManufacturerSpinner);
        this.printerSpinner = (Spinner) view.findViewById(R.id.PrinterSpinner);
        new RemoteQueryTask(getContext(), this).getManufacturers();
        this.manufacturerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /* class net.jsecurity.printbot.prefs.PrinterPreference.AnonymousClass1 */

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View v, int arg2, long arg3) {
                new RemoteQueryTask(v.getContext(), PrinterPreference.this).getPrintersForManufacturer(((KeyValuePair) PrinterPreference.this.manufacturerSpinner.getSelectedItem()).getKey());
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return view;
    }

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean positiveResult) {
        KeyValuePair driverItem;
        if (positiveResult && (driverItem = (KeyValuePair) this.printerSpinner.getSelectedItem()) != null) {
            new RemoteQueryTask(getContext(), this).getResolutionsForPrinter(driverItem.getKey());
        }
    }

    @Override // net.jsecurity.printbot.engine.RemoteQueryListener
    public void onReturnFromRemoteQuery(RemoteQueryTask.QueryMode mode, List<String> list) {
        if (list == null) {
            UIUtil.showErrorDialog(this.settings, R.string.ErrorInfo);
            return;
        }
        Log.i("PrintVulcan", "Returning from Remote task with mode " + mode);
        if (mode == RemoteQueryTask.QueryMode.MANUFACTURER) {
            List<KeyValuePair> manufacturers = new ArrayList<>(list.size());
            for (String key : list) {
                manufacturers.add(new KeyValuePair(key, UIUtil.formatManufacturer(key)));
            }
            UIUtil.setDropDownValues(this.manufacturerSpinner, manufacturers, this.currentManufacturer);
        } else if (mode == RemoteQueryTask.QueryMode.DRIVER) {
            List<KeyValuePair> drivers = new ArrayList<>(list.size());
            for (String key2 : list) {
                drivers.add(new KeyValuePair(key2, UIUtil.formatModel(key2)));
            }
            UIUtil.setDropDownValues(this.printerSpinner, drivers, this.currentDriver);
        } else if (mode == RemoteQueryTask.QueryMode.RESOLUTION) {
            List<KeyValuePair> resolutions = new ArrayList<>(list.size());
            for (String key3 : list) {
                resolutions.add(new KeyValuePair(key3, key3));
            }
            String resolutionString = null;
            String defaultResolution = null;
            if (resolutions.size() > 0) {
                JSONArray array = new JSONArray();
                Iterator<KeyValuePair> it = resolutions.iterator();
                while (it.hasNext()) {
                    array.put(PdfObject.NOTHING + it.next());
                }
                resolutionString = array.toString();
                defaultResolution = resolutions.get(0).getKey();
            }
            Log.i("PrintVulcan", "Setting resolutions to " + resolutionString);
            this.settings.saveFields(new KeyValuePair[]{new KeyValuePair(GUIConstants.MANUFACTURER, ((KeyValuePair) this.manufacturerSpinner.getSelectedItem()).getKey()), new KeyValuePair(GUIConstants.DRIVER, ((KeyValuePair) this.printerSpinner.getSelectedItem()).getKey()), new KeyValuePair(GUIConstants.DEFAULT_RESOLUTION, defaultResolution), new KeyValuePair(GUIConstants.RESOLUTIONS, resolutionString)});
            this.settings.updateResolutions();
            this.settings.updateSummaries();
        }
    }
}
