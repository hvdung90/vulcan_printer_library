package net.jsecurity.printbot.engine;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.I18nException;
import net.jsecurity.printbot.model.PrintBotInfo;
import org.cups.Cups;
import org.cups.CupsJob;

public class IPP extends PrintEngine {
    private String password;
    private URL printerURL;
    private String queue;
    private String user;

    protected IPP(PrintBotInfo printerInfo) {
        try {
            this.printerURL = new URL(printerInfo.getNetworkUrl());
            this.queue = printerInfo.getQueue();
            int ix = this.queue.lastIndexOf(47);
            if (ix >= 0) {
                this.queue = this.queue.substring(ix + 1);
            }
            this.user = printerInfo.getUser();
            this.password = printerInfo.getPassword();
            Log.i("PrintVulcan", "IPP url " + this.printerURL.toExternalForm());
            Log.i("PrintVulcan", "IPP queue " + this.queue);
        } catch (MalformedURLException e) {
            Log.e("PrintVulcan", "Error in IPP url", e);
        }
    }

    @Override
    public void checkConnection(Context ctx) throws IOException {
        if (this.printerURL == null) {
            throw new IOException("Error in IPP url");
        }
        int port = this.printerURL.getPort();
        if (port <= 0) {
            port = 631;
        }
        checkConnection(this.printerURL.getHost(), port);
    }

    @Override
    public void print(File file, String filename) throws IOException {
        try {
            CupsJob job = getCups().cupsPrintFile(file.getAbsolutePath(), null);
            if (job == null) {
                throw new IOException("No print job");
            }
            Log.d("PrintVulcan", "Job status: " + job.jobStatusText());
        } catch (SocketTimeoutException e) {
            Log.w("PrintVulcan", "SocketTimeoutException sending print command to printer", e);
        } catch (Exception e2) {
            Log.w("PrintVulcan", "Error sending print command to printer", e2);
            throw new I18nException(R.string.ErrorPrinting, e2.getMessage() == null ? e2.toString() : e2.getMessage());
        }
    }

    private Cups getCups() {
        Cups cups = new Cups(this.printerURL);
        if (this.printerURL.getPort() <= 0) {
            Log.d("PrintVulcan", "Setting cups port to 631");
            cups.setPort(631);
            cups.setProtocol(GUIConstants.PROTOCOL_IPP);
        }
        if (this.user != null && this.user.length() > 0) {
            Log.d("PrintVulcan", "Setting cups user to " + this.user);
            cups.setUser(this.user);
        }
        if (this.password != null && this.password.length() > 0) {
            Log.d("PrintVulcan", "Setting cups password for " + this.user);
            cups.setPasswd(this.password);
        }
        return cups;
    }
}
