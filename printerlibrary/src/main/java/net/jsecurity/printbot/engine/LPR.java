package net.jsecurity.printbot.engine;

import android.content.Context;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import net.jsecurity.printbot.model.PrintBotInfo;

public class LPR extends PrintEngine {
    private static final Object JOB_ID_LOCK = new Object();
    private static final int LPR_PORT = 515;
    private static int nextjobid = 0;
    private PrintBotInfo printerInfo;

    protected LPR(PrintBotInfo printerInfo2) {
        this.printerInfo = printerInfo2;
    }

    @Override
    public void checkConnection(Context ctx) throws IOException {
        checkConnection(this.printerInfo.getHost(), 515);
    }

    @Override
    public void print(File file, String filename) throws IOException {
        Socket printer = connect(this.printerInfo.getHost(), 515);
        String jobid = getNewJobId();
        String cfA = makecfA(filename, jobid, this.printerInfo.getUser());
        DataInputStream in = new DataInputStream(printer.getInputStream());
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(printer.getOutputStream(), 1024));
        out.write(2);
        out.writeBytes(this.printerInfo.getQueue() + "\n");
        out.flush();
        if (in.read() != 0) {
            throw new IOException("Error while start printing on queue " + this.printerInfo.getQueue());
        }
        out.write(2);
        out.writeBytes(String.valueOf(cfA.length()));
        out.writeBytes(" cfA" + jobid + "PrintBot" + "\n");
        out.flush();
        if (in.read() != 0) {
            throw new IOException("Error while start sending control file");
        }
        out.writeBytes(cfA);
        out.writeByte(0);
        out.flush();
        if (in.read() != 0) {
            throw new IOException("Error while sending control file");
        }
        out.write(3);
        out.writeBytes(String.valueOf(file.length()));
        out.writeBytes(" dfA" + jobid + "PrintBot" + "\n");
        out.flush();
        if (in.read() != 0) {
            throw new IOException("Error while start sending data file");
        }
        copyFileToStream(file, out);
        out.writeByte(0);
        out.flush();
        if (in.read() != 0) {
            throw new IOException("Error while sending data file");
        }
        close(printer, in, out);
    }

    private String makecfA(String document, String jobid, String user) {
        if (document.length() > 32) {
            document = document.substring(0, 32);
        }
        if (user == null || user.length() == 0) {
            user = "PrintBot";
        } else if (user.length() > 32) {
            user = user.substring(0, 32);
        }
        StringBuilder cfA = new StringBuilder();
        cfA.append("HPrintBot\n");
        cfA.append("P" + user + "\n");
        cfA.append("J" + document + "\n");
        cfA.append("ldfA" + jobid + "PrintBot" + "\n");
        cfA.append("UdfA" + jobid + "PrintBot" + "\n");
        cfA.append("N" + document + "\n");
        return cfA.toString();
    }

    private static String getNewJobId() {
        String substring;
        synchronized (JOB_ID_LOCK) {
            nextjobid = (nextjobid + 1) % 1000;
            substring = ("000" + nextjobid).substring(0, 3);
        }
        return substring;
    }
}
