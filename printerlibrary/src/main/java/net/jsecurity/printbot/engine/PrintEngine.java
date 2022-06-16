package net.jsecurity.printbot.engine;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.PrintBotInfo;

public abstract class PrintEngine {
    protected static final int PRINT_TIMEOUT = 60000;

    public abstract void checkConnection(Context context) throws IOException;

    public abstract void print(File file, String str) throws IOException;

    public static PrintEngine getPrintEngine(PrintBotInfo printerInfo) throws IOException {
        if (GUIConstants.PROTOCOL_LPD.equals(printerInfo.getProtocol())) {
            return new LPR(printerInfo);
        }
        if (GUIConstants.PROTOCOL_RAW.equals(printerInfo.getProtocol())) {
            return new JetDirect(printerInfo);
        }
        if (GUIConstants.PROTOCOL_IPP.equals(printerInfo.getProtocol())) {
            return new IPP(printerInfo);
        }
        if (GUIConstants.PROTOCOL_BONJOUR.equals(printerInfo.getProtocol())) {
            return getBonjourEngine(printerInfo);
        }
        throw new IOException("Unknown protocol " + printerInfo.getProtocol());
    }

    public static Bonjour getBonjourEngine(PrintBotInfo printerInfo) {
        return new BonjourNougat(printerInfo);
    }

    public void checkConnection(String hostnameString, int defaultPort) throws IOException {
        getSocket(hostnameString, defaultPort).close();
    }

    public Socket connect(String hostnameString, int defaultPort) throws IOException {
        Socket socket = getSocket(hostnameString, defaultPort);
        socket.setSoTimeout(PRINT_TIMEOUT);
        return socket;
    }

    private Socket getSocket(String hostnameString, int defaultPort) throws IOException {
        int port;
        String hostname;
        boolean isIp6 = hostnameString.startsWith("[");
        if ((!isIp6 || !hostnameString.contains("]:")) && (isIp6 || !hostnameString.contains(":"))) {
            hostname = hostnameString;
            port = defaultPort;
        } else {
            try {
                int ix = hostnameString.lastIndexOf(58);
                hostname = hostnameString.substring(0, ix);
                port = Integer.valueOf(hostnameString.substring(ix + 1)).intValue();
                Log.d("PrintVulcan", "Parsing hostname: " + hostname + ":" + port);
            } catch (Exception e) {
                Log.w("PrintVulcan", "Illegal host:port notation", e);
                throw new UnknownHostException("Illegal host:port notation");
            }
        }
        Log.d("PrintVulcan", "Connecting to " + hostname + ":" + port);
        return new Socket(InetAddress.getByName(hostname), port);
    }

    public void close(Socket socket, DataInputStream in, DataOutputStream out) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e2) {
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e3) {
            }
        }
    }

    public void copyFileToStream(File file, DataOutputStream out) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        byte[] buf = new byte[1024];
        while (true) {
            int count = stream.read(buf);
            if (count > 0) {
                out.write(buf, 0, count);
            } else {
                return;
            }
        }
    }
}
