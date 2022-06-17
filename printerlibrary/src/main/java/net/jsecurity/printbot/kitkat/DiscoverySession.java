package net.jsecurity.printbot.kitkat;

import static net.jsecurity.printbot.model.GUIConstants.JOB_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;
import net.jsecurity.printbot.model.PrintBotInfo;
import net.jsecurity.printbot.model.Printer;
import net.jsecurity.printbot.prefs.SettingsHelper;

public class DiscoverySession extends PrinterDiscoverySession {
    private static final PrintAttributes.Margins DEFAULT_MARGINS = new PrintAttributes.Margins(10, 10, 10, 10);
    private NsdManager dns;
    private DiscoverySessionListener ippListener;
    private DiscoverySessionListener jetdirectListener;
    private DiscoverySessionListener lprListener;
    private Handler mainThread;
    private PrintBotService printService;

    public DiscoverySession(PrintBotService printService2) {
        this.printService = printService2;
        this.mainThread = new Handler(printService2.getMainLooper());
        Log.i("PrintVulcan", "Starting DiscoverySession.");
    }

    public void onDestroy() {
    }

    @Override
    public void onStartPrinterDiscovery(List<PrinterId> list) {
        Log.i("PrintVulcan", "Starting printService discovery.");
        addStaticPrinters();
        scanPrinters();
    }

    private void scanPrinters() {
        this.dns = (NsdManager) this.printService.getSystemService(Context.NSD_SERVICE);
        this.jetdirectListener = new DiscoverySessionListener(this, GUIConstants.MDNS_JETDIRECT);
        this.dns.discoverServices("_pdl-datastream._tcp", 1, this.jetdirectListener);
        this.lprListener = new DiscoverySessionListener(this, GUIConstants.MDNS_LPR);
        this.dns.discoverServices("_printer._tcp", 1, this.lprListener);
        this.ippListener = new DiscoverySessionListener(this, GUIConstants.MDNS_IPP);
        this.dns.discoverServices("_ipp._tcp", 1, this.ippListener);
    }

    private void addStaticPrinters() {
        List<PrinterInfo> printers = new ArrayList<>();
        for (PrintBotInfo info : SettingsHelper.getStaticPrinters(this.printService)) {
            PrinterId id = this.printService.generatePrinterId(info.getNetworkUrl());
            printers.add(new PrinterInfo.Builder(id, info.getNetworkName(), PrinterInfo.STATUS_IDLE).setCapabilities(getCapabilities(id, info)).build());
        }
        addPrinters(printers);
    }

    public void onStopPrinterDiscovery() {
        Log.i("PrintVulcan", "Stopping printService discovery");
        if (this.dns != null) {
            try {
                this.dns.stopServiceDiscovery(this.jetdirectListener);
                this.dns.stopServiceDiscovery(this.lprListener);
                this.dns.stopServiceDiscovery(this.ippListener);
            } catch (IllegalArgumentException e) {
                Log.w("PrintVulcan", "Error stopping service discovery", e);
            }
        }
    }

    public void onStartPrinterStateTracking(PrinterId id) {
        Log.i("PrintVulcan", "Start tracking printer " + id.getLocalId());
        String networkUrl = id.getLocalId();
        PrintBotInfo info = SettingsHelper.getPrinter(this.printService, networkUrl);
        if (info != null) {
            addPrinterWithCapabilities(id, info);
        } else if (networkUrl.startsWith(GUIConstants.PROTOCOL_BONJOUR)) {
            new ProposalTask(this.printService, this, id, networkUrl.substring(GUIConstants.PROTOCOL_BONJOUR.length() + 1)).execute(new Void[0]);
        }
        String json = this.printService.getSharedPreferences(GUIConstants.PREFERENCE_KEY, Context.MODE_PRIVATE).getString("printers", "");
        List<Printer> data = new Gson().fromJson(json, new TypeToken<List<Printer>>() {
        }.getType());
        if (data == null)
            data = new ArrayList<>();
        for (Printer item : data) {
            if (item.getName().equals(info.getNetworkName())) ;
            return;
        }

        Printer print = new Printer(info.getIndex() + "", info.getNetworkName(), info.getHost());
        data.add(print);
        this.printService.getSharedPreferences(GUIConstants.PREFERENCE_KEY, Context.MODE_PRIVATE).edit().putString(JOB_NAME, new Gson().toJson(data));

    }

    public void addPrinterWithCapabilities(PrinterId id, PrintBotInfo info) {
        PrinterInfo printer = new PrinterInfo.Builder(id, info.getNetworkName(), PrinterInfo.STATUS_IDLE).setCapabilities(getCapabilities(id, info)).build();
        Log.i("PrintVulcan", "Adding printer with capabilities " + id + " on " + info.getNetworkName());
        addPrinters(Arrays.asList(printer));
    }

    public void onStopPrinterStateTracking(PrinterId arg0) {
        Log.i("PrintVulcan", "Stop tracking printer " + arg0.getLocalId());
    }

    @Override
    public void onValidatePrinters(List<PrinterId> list) {
        Log.i("PrintVulcan", "Validating printers");
        addStaticPrinters();
        scanPrinters();
    }

    @SuppressLint("WrongConstant")
    private PrinterCapabilitiesInfo getCapabilities(PrinterId id, PrintBotInfo info) {
        int resX;
        int resY;
        int colorModes = 1;
        int defaultColorMode = 1;
        if (1 != 0) {
            colorModes = 1 | 2;
            defaultColorMode = 2;
        }
        PrinterCapabilitiesInfo.Builder capabilitiesBuilder = new PrinterCapabilitiesInfo.Builder(id);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.NA_LETTER, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A5, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.NA_LEGAL, false);
        capabilitiesBuilder.addMediaSize(PrintAttributes.MediaSize.NA_LEDGER, false);
        if (info.getResolutions() == null || info.getResolutions().size() == 0) {
            capabilitiesBuilder.addResolution(new PrintAttributes.Resolution("default", "Default", 300, 300), true);
        } else {
            String defaultRes = info.getResolution();
            for (KeyValuePair res : info.getResolutions()) {
                String resolution = res.getKey();
                boolean isDefault = resolution.equals(defaultRes);
                if (resolution.endsWith("dpi")) {
                    resolution = resolution.substring(0, resolution.length() - 3);
                }
                int ix = resolution.indexOf(120);
                if (ix == -1) {
                    resY = Integer.valueOf(resolution).intValue();
                    resX = resY;
                } else {
                    resX = Integer.valueOf(resolution.substring(0, ix)).intValue();
                    resY = Integer.valueOf(resolution.substring(ix + 1)).intValue();
                }
                Log.d("PrintVulcan", "Adding resolution " + resX + ":" + resY + " default " + isDefault);
                capabilitiesBuilder.addResolution(new PrintAttributes.Resolution(resolution, resolution, resX, resY), isDefault);
            }
        }
        capabilitiesBuilder.setColorModes(colorModes, defaultColorMode);
        capabilitiesBuilder.setMinMargins(DEFAULT_MARGINS);
        return capabilitiesBuilder.build();
    }

    public void addPrinter(final String serviceName, final String type, InetAddress host) {
        this.mainThread.post(() -> {
            String oldBonjourKey;
            int ix;
            String bonjourKey = type + ":" + serviceName;
            boolean found = false;
            boolean replaced = false;
            for (PrinterInfo existingPrinter : DiscoverySession.this.getPrinters()) {
                String localId = existingPrinter.getId().getLocalId();
                if (localId.startsWith(GUIConstants.PROTOCOL_BONJOUR) && (ix = (oldBonjourKey = localId.substring(GUIConstants.PROTOCOL_BONJOUR.length() + 1)).indexOf(58)) > 0) {
                    String oldType = oldBonjourKey.substring(0, ix);
                    String oldServiceName = oldBonjourKey.substring(ix + 1);
                    if (serviceName.equals(oldServiceName)) {
                        found = true;
                        if (oldType.length() < type.length()) {
                            Log.d("PrintVulcan", "Replacing printer " + oldServiceName + " with type " + oldType + " with new type " + type);
                            DiscoverySession.this.removePrinters(Arrays.asList(existingPrinter.getId()));
                            replaced = true;
                            updatePrinter(serviceName, null, true);
                        }
                    }
                }
            }
            if (!found || replaced) {
                Log.d("PrintVulcan", "Adding new printer " + bonjourKey);
                PrinterInfo printer = new PrinterInfo.Builder(DiscoverySession.this.printService.generatePrinterId("zeroconf:" + bonjourKey), SettingsHelper.getBonjourName(bonjourKey), PrinterInfo.STATUS_IDLE).build();
                DiscoverySession.this.addPrinters(Arrays.asList(printer));
                updatePrinter(serviceName, host == null ? null : host.getHostAddress(), false);
            }
        });
    }

    private void updatePrinter(String name, String ip, boolean remote) {
        String json = this.printService.getSharedPreferences(GUIConstants.PREFERENCE_KEY, Context.MODE_PRIVATE).getString("printers", "");
        List<Printer> data = new Gson().fromJson(json, new TypeToken<List<Printer>>() {
        }.getType());
        if (data == null)
            data = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Printer item = data.get(i);
            if (item.getName().equals(name)) {
                if (remote) {
                    data.remove(i);
                }
                return;
            }
        }
        if (remote) {
            return;
        }
        Printer print = new Printer(new Random().nextInt(100) + "", name, ip);
        data.add(print);
        this.printService.getSharedPreferences(GUIConstants.PREFERENCE_KEY, Context.MODE_PRIVATE).edit().putString(JOB_NAME, new Gson().toJson(data));
    }
}
