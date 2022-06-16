package net.jsecurity.printbot.engine;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Locale;
import net.jsecurity.printbot.R;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.prefs.SettingsHelper;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpProtocolParams;

public class BobsHttpClient extends DefaultHttpClient {
    private static final char[] STOR_PWD = "zus7aMAg".toCharArray();
    private static SSLSocketFactory sslSocketFactory;
    private final Context context;

    public BobsHttpClient(Context context2) {
        this.context = context2;
        HttpProtocolParams.setUserAgent(getParams(), getUserAgent());
        setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
    }

    public ClientConnectionManager createClientConnectionManager() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(GUIConstants.PROTOCOL_IPP, PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", getSslSocketFactory(), 443));
        return new SingleClientConnManager(getParams(), registry);
    }

    private SSLSocketFactory getSslSocketFactory() {
        if (sslSocketFactory == null) {
            synchronized (getClass()) {
                if (sslSocketFactory == null) {
                    try {
                        KeyStore trusted = KeyStore.getInstance("BKS");
                        InputStream in = this.context.getResources().openRawResource(R.raw.keystore);
                        try {
                            trusted.load(in, STOR_PWD);
                            in.close();
                            sslSocketFactory = new SSLSocketFactory(trusted);
                        } catch (Throwable th) {
                            in.close();
                            throw th;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return sslSocketFactory;
    }

    private String getUserAgent() {
        String version = "unknown";
        try {
            version = this.context.getPackageManager().getPackageInfo("net.jsecurity.printbot", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        String deviceId = SettingsHelper.getDeviceId(this.context);
        int pv = SettingsHelper.getProVersion(this.context);
        String proVersion = "-";
        if (pv > 0) {
            proVersion = Integer.toString(pv);
        }
        return "PrintBot/" + version + " (" + deviceId + "; " + Build.MODEL + "; " + proVersion + "; " + Locale.getDefault().getLanguage() + ")";
    }
}
