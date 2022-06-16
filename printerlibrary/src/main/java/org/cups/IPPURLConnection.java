package org.cups;

import java.net.URL;
import java.net.URLConnection;

public class IPPURLConnection extends URLConnection {
    public IPPURLConnection(URL url) {
        super(url);
    }

    public boolean usingProxy() {
        return false;
    }

    @Override // java.net.URLConnection
    public void connect() {
    }

    public void disconnect() {
    }
}
