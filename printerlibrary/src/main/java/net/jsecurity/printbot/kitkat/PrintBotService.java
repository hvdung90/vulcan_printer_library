package net.jsecurity.printbot.kitkat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.print.PrintAttributes;
import android.print.PrintJobId;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.engine.PrintTask;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.I18nException;
import net.jsecurity.printbot.model.PrintBotInfo;
import net.jsecurity.printbot.prefs.SettingsHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PrintBotService extends PrintService implements ServiceConnection {
    private static final Map<PrintJobId, PrintTask> tasks = new HashMap();

    public void onServiceDisconnected(ComponentName name) {
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
    }

    public void onCreate() {
        Log.i("PrintVulcan", "Creating PrintBotService");
 //       String installerPackageName = getPackageManager().getInstallerPackageName(getPackageName());
 //       int pv = getSharedPreferences(GUIConstants.PREFERENCE_KEY, 0).getInt("PV", 0);
//        if (SettingsHelper.getProVersion(this) > pv) {
//            Intent serviceConnector = new Intent(this, ServiceConnector.class);
//            serviceConnector.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(serviceConnector);
//        } else if (pv == 0) {
//            if (installerPackageName == null || !installerPackageName.startsWith("com.amazon")) {
//                Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
//                intent.setPackage("com.android.vending");
//                bindService(intent, this, Context.BIND_AUTO_CREATE);
//            }
//        }
        tasks.clear();
    }

    public void onDestroy() {
        Log.i("PrintVulcan", "Destroying PrintBotService");
        unbindService(this);
    }

    public PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        Log.i("PrintVulcan", "Starting PrintBotService");
        return new DiscoverySession(this);
    }

    public void onPrintJobQueued(PrintJob job) {
        PrintBotInfo info = SettingsHelper.getPrinter(this, job.getInfo().getPrinterId().getLocalId());
        if (info == null) {
            job.fail(getResources().getString(R.string.ErrorPrinterOffline));
        } else  {
            PrintAttributes.MediaSize mediaSize = job.getInfo().getAttributes().getMediaSize();
            info.setPageSize(getGSMediaSize(mediaSize));
            info.setPortrait(mediaSize.isPortrait());
            try {
                PrintTask task = new PrintTask(getBaseContext(), job, info);
                tasks.put(job.getInfo().getId(), task);
                job.start();
                task.execute((Void) null);
            } catch (I18nException e2) {
                job.fail(e2.getLocalizedMessage(this));
            } catch (IOException e3) {
                job.fail(getResources().getString(R.string.ErrorReading, e3.getMessage()));
            }
        }
    }

    public void onRequestCancelPrintJob(PrintJob job) {
        PrintTask task = tasks.get(job.getInfo().getId());
        if (task != null) {
            task.cancel(true);
        }
        job.cancel();
    }

    private String getGSMediaSize(PrintAttributes.MediaSize mediaSize) {
        Log.d("PrintVulcan", "Kitkat media size: " + mediaSize.getId());
        if (PrintAttributes.MediaSize.ISO_A5.equals(mediaSize)) {
            return GUIConstants.PAGE_SIZE_A5;
        }
        if (PrintAttributes.MediaSize.NA_LEGAL.equals(mediaSize)) {
            return GUIConstants.PAGE_SIZE_LEGAL;
        }
        if (PrintAttributes.MediaSize.NA_LEDGER.equals(mediaSize)) {
            return GUIConstants.PAGE_SIZE_LEDGER;
        }
        if (PrintAttributes.MediaSize.NA_LETTER.equals(mediaSize)) {
            return GUIConstants.PAGE_SIZE_LETTER;
        }
        if (PrintAttributes.MediaSize.ISO_A3.equals(mediaSize)) {
            return GUIConstants.PAGE_SIZE_A3;
        }
        return GUIConstants.PAGE_SIZE_A4;
    }
}
