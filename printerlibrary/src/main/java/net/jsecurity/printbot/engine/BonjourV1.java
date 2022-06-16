package net.jsecurity.printbot.engine;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.itextpdf.text.pdf.PdfObject;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.I18nException;
import net.jsecurity.printbot.model.PrintBotInfo;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.SocketTimeoutException;

public class BonjourV1 extends PrintEngine {
    private PrintEngine engine;
    private WifiManager.MulticastLock mLock;
    private PrintBotInfo printerInfo;
    private String printerType;

    protected BonjourV1(PrintBotInfo printerInfo2) {
        this.printerInfo = printerInfo2;
    }

    @Override
    public void checkConnection(Context ctx) throws IOException {
        String type;
        String name;
        String hostIp;
        String bonjourKey = this.printerInfo.getBonjourKey();
        int ix = bonjourKey.indexOf(58);
        if (ix == -1) {
            type = GUIConstants.MDNS_LPR;
            name = bonjourKey;
        } else {
            type = bonjourKey.substring(0, ix);
            name = bonjourKey.substring(ix + 1);
        }
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        if (wifiInfo == null || SupplicantState.COMPLETED != wifiInfo.getSupplicantState()) {
            throw new IOException("Not in WiFi network");
        }
        BonjourResult result = resolveService(ctx, wm, name, type);
        this.printerType = result.getType();
        PrintBotInfo wrappedInfo = new PrintBotInfo();
        wrappedInfo.setDriver(this.printerInfo.getDriver());
        wrappedInfo.setPageSize(this.printerInfo.getPageSize());
        wrappedInfo.setResolution(this.printerInfo.getResolution());
        if (result.getSource() instanceof Inet6Address) {
            hostIp = "[" + result.getSource().getHostAddress() + "]";
        } else {
            hostIp = result.getSource().getHostAddress();
        }
        String host = hostIp + ":" + result.getPort();
        Log.i("PrintVulcan", "Found host " + host);
        wrappedInfo.setHost(host);
        if (GUIConstants.MDNS_LPR.equalsIgnoreCase(type)) {
            wrappedInfo.setProtocol(GUIConstants.PROTOCOL_LPD);
            String rp = result.getRp();
            if (rp == null) {
                Log.i("PrintVulcan", "Printer queue null");
                rp = PdfObject.NOTHING;
            }
            wrappedInfo.setQueue(rp);
        } else if (GUIConstants.MDNS_JETDIRECT.equalsIgnoreCase(type)) {
            wrappedInfo.setProtocol(GUIConstants.PROTOCOL_RAW);
        } else if (GUIConstants.MDNS_IPP.equalsIgnoreCase(type)) {
            wrappedInfo.setProtocol(GUIConstants.PROTOCOL_IPP);
            String rp2 = result.getRp();
            if (rp2 == null) {
                Log.i("PrintVulcan", "Printer queue null");
                rp2 = PdfObject.NOTHING;
            }
            wrappedInfo.setQueue("/" + rp2);
        } else {
            throw new IOException("Unknown bonjour protocol");
        }
        this.engine = PrintEngine.getPrintEngine(wrappedInfo);
    }

    public BonjourResult resolveService(Context ctx, WifiManager wm, String name, String type) throws IOException {
        String service = name + "._" + type + "._tcp.local";
        this.mLock = wm.createMulticastLock("PrintVulcan");
        this.mLock.acquire();
        if (!this.mLock.isHeld()) {
            throw new IOException("Could not obtain multicast lock");
        }
        Log.d("PrintVulcan", "Service " + service);
        try {
            MDNSDiscover.Result result = MDNSDiscover.resolve(service, GUIConstants.RESOLVE_TIME);
            BonjourResult bonjourResult = new BonjourResult(result.source, result.srv.port, result.txt.dict.get(GUIConstants.MDNS_QUEUE), result.txt.dict.get(GUIConstants.MDNS_TYPE));
            this.mLock.release();
            return bonjourResult;
        } catch (SocketTimeoutException e) {
            throw new I18nException(R.string.ErrorResolving, e.getMessage());
        } catch (Throwable th) {
            this.mLock.release();
            throw th;
        }
    }

    @Override
    public void print(File file, String filename) throws IOException {
        if (this.engine == null) {
            throw new IOException("Printer connection not set up");
        }
        this.engine.print(file, filename);
    }

    public String getPrinterType() {
        return this.printerType;
    }
}
