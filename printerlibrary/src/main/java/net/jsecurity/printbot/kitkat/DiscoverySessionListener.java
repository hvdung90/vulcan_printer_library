package net.jsecurity.printbot.kitkat;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/* compiled from: DiscoverySession */
class DiscoverySessionListener implements NsdManager.DiscoveryListener {
    private DiscoverySession session;
    private String type;

    public DiscoverySessionListener(DiscoverySession session2, String type2) {
        this.session = session2;
        this.type = type2;
    }

    public void onDiscoveryStarted(String serviceType) {
        Log.i("PrintVulcan", "Discovery started");
    }

    public void onDiscoveryStopped(String serviceType) {
        Log.i("PrintVulcan", "Discovery stopped");
    }

    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Log.i("PrintVulcan", "Found printer " + serviceInfo);
        this.session.addPrinter(serviceInfo.getServiceName(), this.type,serviceInfo.getHost());
    }

    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.w("PrintVulcan", "Service lost " + serviceInfo);
    }

    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.w("PrintVulcan", "Discovery start failed");
    }

    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.w("PrintVulcan", "Discovery stop failed");
    }
}
