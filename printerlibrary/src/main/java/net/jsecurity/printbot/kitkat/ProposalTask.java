package net.jsecurity.printbot.kitkat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.print.PrinterId;
import android.util.Log;
import com.itextpdf.text.pdf.PdfObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.jsecurity.printbot.engine.Bonjour;
import net.jsecurity.printbot.engine.PrintEngine;
import net.jsecurity.printbot.engine.RemoteQueryListener;
import net.jsecurity.printbot.engine.RemoteQueryTask;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;
import net.jsecurity.printbot.model.PrintBotInfo;
import net.jsecurity.printbot.prefs.SettingsHelper;
import org.json.JSONArray;

public class ProposalTask extends AsyncTask<Void, Void, Void> implements RemoteQueryListener {
    private static final Object LOCK = new Object();
    private String bonjourKey;
    private Context ctx;
    private PrinterId id;
    private String manufacturer;
    private String printerType;
    private String proposedDriver;
    private DiscoverySession session;

    public ProposalTask(Context ctx2, DiscoverySession session2, PrinterId id2, String bonjourKey2) {
        this.ctx = ctx2;
        this.session = session2;
        this.bonjourKey = bonjourKey2;
        this.id = id2;
    }

    public Void doInBackground(Void... params) {
        PrintBotInfo printerInfo = new PrintBotInfo();
        printerInfo.setBonjourKey(this.bonjourKey);
        Bonjour bonjour = PrintEngine.getBonjourEngine(printerInfo);
        try {
            bonjour.checkConnection(this.ctx);
            if (bonjour.getPrinterType() != null) {
                this.printerType = bonjour.getPrinterType();
            }
            Log.i("PrintVulcan", "Querying proposal for " + this.printerType);
            new RemoteQueryTask(this.ctx, this).getDriverProposal(this.printerType);
        } catch (Exception e) {
            Log.w("PrintVulcan", "Remote error ", e);
        }
        return null;
    }

    @Override
    public void onReturnFromRemoteQuery(RemoteQueryTask.QueryMode mode, List<String> list) {
        if (mode == RemoteQueryTask.QueryMode.PROPOSE_DRIVER && list != null && list.size() == 2) {
            KeyValuePair proposal = new KeyValuePair(list.get(0), list.get(1));
            this.manufacturer = proposal.getKey();
            this.proposedDriver = proposal.toString();
            Log.i("PrintVulcan", "Got driver proposal " + this.proposedDriver);
            Log.d("PrintVulcan", "Querying resolutions for " + this.proposedDriver);
            new RemoteQueryTask(this.ctx, this).getResolutionsForPrinter(this.proposedDriver);
        } else if (mode == RemoteQueryTask.QueryMode.RESOLUTION) {
            Log.i("PrintVulcan", "Got resolutions for " + this.proposedDriver);
            List<KeyValuePair> resolutions = new ArrayList<>(list.size());
            for (String key : list) {
                resolutions.add(new KeyValuePair(key, key));
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
            Log.i("PrintVulcan", "Creating printer " + this.manufacturer + " " + this.proposedDriver + " on " + this.bonjourKey);
            this.session.addPrinterWithCapabilities(this.id, createPrinter(new KeyValuePair[]{new KeyValuePair(GUIConstants.PROTOCOL, GUIConstants.PROTOCOL_BONJOUR), new KeyValuePair(GUIConstants.BONJOUR_KEY, this.bonjourKey), new KeyValuePair(GUIConstants.MANUFACTURER, this.manufacturer), new KeyValuePair(GUIConstants.DRIVER, this.proposedDriver), new KeyValuePair(GUIConstants.DEFAULT_RESOLUTION, defaultResolution), new KeyValuePair(GUIConstants.RESOLUTIONS, resolutionString)}));
        }
    }

    private PrintBotInfo createPrinter(KeyValuePair[] items) {
        int nextIx;
        synchronized (LOCK) {
            nextIx = SettingsHelper.getPrinterCount(this.ctx) + 1;
            SharedPreferences.Editor editor = this.ctx.getSharedPreferences(GUIConstants.PREFERENCE_KEY_PREFIX + nextIx, 0).edit();
            for (KeyValuePair i : items) {
                editor.putString(i.getKey(), i.toString());
            }
            editor.commit();
        }
        return SettingsHelper.getPrinter(this.ctx, nextIx);
    }
}
