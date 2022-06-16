package net.jsecurity.printbot.engine;

import android.content.Context;
import android.os.Handler;

import net.jsecurity.printbot.model.PrintBotInfo;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class JetDirect extends PrintEngine {
    private static final int RAW_PORT = 9100;
    private PrintBotInfo printerInfo;

    protected JetDirect(PrintBotInfo printerInfo2) {
        this.printerInfo = printerInfo2;
    }

    @Override
    public void checkConnection(Context ctx) throws IOException {
        checkConnection(this.printerInfo.getHost(), RAW_PORT);
    }

    @Override
    public void print(File file, String filename) throws IOException {
        Socket printer = connect(this.printerInfo.getHost(), RAW_PORT);
        DataOutputStream out = new DataOutputStream(printer.getOutputStream());
        copyFileToStream(file, out);
        out.flush();
        close(printer, null, out);
    }
}
