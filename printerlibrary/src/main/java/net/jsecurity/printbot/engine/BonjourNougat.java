package net.jsecurity.printbot.engine;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.itextpdf.text.xml.xmp.XmpWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.I18nException;
import net.jsecurity.printbot.model.PrintBotInfo;

class BonjourNougat extends Bonjour {
    protected BonjourNougat(PrintBotInfo printerInfo) {
        super(printerInfo);
    }

    @Override
    public BonjourResult resolveService(Context ctx, WifiManager wm, String name, String type) throws IOException {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType("_" + type + "._tcp.");
        final List<NsdServiceInfo> result = new ArrayList<>();
        ((NsdManager) ctx.getSystemService(Context.NSD_SERVICE)).resolveService(serviceInfo, new NsdManager.ResolveListener() {

            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i("PrintVulcan", "Service resolved " + serviceInfo);
                synchronized (result) {
                    result.add(serviceInfo);
                    result.notify();
                }
            }

            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.w("PrintVulcan", "Resolve failed " + errorCode);
                synchronized (result) {
                    result.notify();
                }
            }
        });
        synchronized (result) {
            try {
                result.wait();
            } catch (InterruptedException e) {
                Log.w("PrintVulcan", "wait() failed ", e);
            }
        }
        if (result.size() == 0) {
            throw new I18nException(R.string.ErrorResolving, "Printer offline");
        }
        NsdServiceInfo firstResult = result.get(0);
        try {
            Map<String, byte[]> attributes = (Map) NsdServiceInfo.class.getMethod("getAttributes", new Class[0]).invoke(firstResult, new Object[0]);
            return new BonjourResult(firstResult.getHost(), firstResult.getPort(), getAttribute(attributes, GUIConstants.MDNS_QUEUE), getAttribute(attributes, GUIConstants.MDNS_TYPE));
        } catch (Exception e2) {
            throw new IOException("No method for getting Bonjour attributes");
        }
    }

    private String getAttribute(Map<String, byte[]> attributes, String key) {
        try {
            byte[] bs = attributes.get(key);
            if (bs == null) {
                return null;
            }
            return new String(bs, XmpWriter.UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
