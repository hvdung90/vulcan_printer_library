package net.jsecurity.printbot.engine;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.printservice.PrintJob;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfObject;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.Util;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.I18nException;
import net.jsecurity.printbot.model.PrintBotInfo;
import net.jsecurity.printbot.model.PrintMode;

import org.apache.http.Header;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.cups.IPPHttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PrintTask extends AsyncTask<Void, Void, I18nException> {
    private Context ctx;
    private String filename;
    private InputStream input;
    private PrintJob job;
    private PrintBotInfo printer;
    private Dialog progressDialog;
    private File temp;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    public PrintTask(Context ctx2, PrintJob job2, PrintBotInfo printer2) throws IOException {
        this.ctx = ctx2;
        this.printer = printer2;
        this.filename = job2.getDocument().getInfo().getName();
        setInputStream(new FileInputStream(job2.getDocument().getData().getFileDescriptor()));
        this.job = job2;
    }

    public PrintTask(Context ctx2, PrintBotInfo printer2) {
        this.ctx = ctx2;
        this.printer = printer2;
        this.filename = "PrintVulcan Test Page";
    }

    public void setProgressDialog(Dialog progressDialog2) {
        this.progressDialog = progressDialog2;
    }

    public void setInputStream(InputStream input2) throws IOException {
        this.temp = Util.createTempFile(this.ctx, PdfObject.NOTHING);
        Util.streamCopy(input2, new FileOutputStream(this.temp));
        this.input = new FileInputStream(this.temp);
    }

    public Context getContext() {
        return this.ctx;
    }

    public void onCancelled() {
        cleanUp();
    }

    @SuppressLint("InvalidWakeLockTag")
    public void onPreExecute() {
        this.wakeLock = ((PowerManager) this.ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PrintVulcan");
        this.wakeLock.acquire();
    }

    public void onPostExecute(I18nException result) {
        String value;
        if (this.job == null) {
            if (result == null) {
                value = this.ctx.getResources().getString(R.string.InfoSuccess, this.filename);
            } else {
                value = this.ctx.getResources().getString(result.getResId(), result.getDetail());
            }
            Toast.makeText(this.ctx, value, Toast.LENGTH_LONG).show();
        } else if (result == null) {
            this.job.complete();
        } else {
            this.job.fail(result.getLocalizedMessage(getContext()));
        }
        cleanUp();
    }

    public void cancelWithError(int resId) {
        this.job.fail(getContext().getString(resId));
        cleanUp();
    }

    public void cleanUp() {
        if (this.wifiLock != null && this.wifiLock.isHeld()) {
            this.wifiLock.release();
        }
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            try {
                this.progressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                Log.e("PrintVulcan", "Unexpected IllegalArgumentException", e);
            }
        }
        if (this.wakeLock != null && this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
        if (this.temp != null && this.temp.exists()) {
            this.temp.delete();
        }
    }

    public I18nException doInBackground(Void... arg0) {
        I18nException i18nException = null;
        String message;
        String message2;
        File renderFile = null;
        try {
            if (this.printer.getDriver() == null || this.printer.getDriver().length() == 0) {
                Log.w("PrintVulcan", "No printer driver selected");
                i18nException = new I18nException(R.string.ErrorNoDriver);
                if (0 != 0) {
                    try {
                        renderFile.delete();
                    } catch (Throwable e) {
                        Log.w("PrintVulcan", e);
                    }
                }
            } else {
                boolean disableWifiCheck = PreferenceManager.getDefaultSharedPreferences(this.ctx).getBoolean("disableWifiCheck", false);
                Log.d("PrintVulcan", "disableWifiCheck " + disableWifiCheck);
                if (!disableWifiCheck) {
                    WifiManager wm = (WifiManager) this.ctx.getSystemService(Context.WIFI_SERVICE);
                    Log.i("PrintVulcan", "Checking WIFI connection");
                    WifiInfo info = wm.getConnectionInfo();
                    if (info == null || SupplicantState.COMPLETED != info.getSupplicantState()) {
                        Log.w("PrintVulcan", "No connection to WIFI network " + info.getSupplicantState());
                        i18nException = new I18nException(R.string.ErrorNoWIFI);
                        if (0 != 0) {
                            try {
                                renderFile.delete();
                            } catch (Throwable e2) {
                                Log.w("PrintVulcan", e2);
                            }
                        }
                    } else {
                        Log.d("PrintVulcan", "Acquiring WIFI lock");
                        this.wifiLock = wm.createWifiLock(1, "PrintVulcan");
                        this.wifiLock.acquire();
                    }
                }
                try {
                    PrintEngine printEngine = PrintEngine.getPrintEngine(this.printer);
                    printEngine.checkConnection(this.ctx);
                    if (isCancelled()) {
                        i18nException = null;
                        if (0 != 0) {
                            try {
                                renderFile.delete();
                            } catch (Throwable e3) {
                                Log.w("PrintVulcan", e3);
                            }
                        }
                    } else {
                        String printerType = null;
                        try {
                            if (printEngine instanceof Bonjour) {
                                printerType = ((Bonjour) printEngine).getPrinterType();
                            }
                            Pair<File, PrintMode> result = render(this.input, this.filename, this.printer, printerType);
                            File renderFile2 = result.first;
//                            File renderFile2 = new File("/storage/emulated/0/Download/Converted_PDF.pdf");//new RemoteEngine(this.ctx).render(this.input, this.filename, this.printer, printerType);
                            if (isCancelled()) {
                                i18nException = null;
                                if (renderFile2 != null) {
                                    try {
                                        renderFile2.delete();
                                    } catch (Throwable e4) {
                                        Log.w("PrintVulcan", e4);
                                    }
                                }
                            } else {
                                try {
                                    printEngine.print(renderFile2, this.filename);
                                    i18nException = null;
                                    if (renderFile2 != null) {
                                        try {
                                            renderFile2.delete();
                                        } catch (Throwable e5) {
                                            Log.w("PrintVulcan", e5);
                                        }
                                    }
                                } catch (Throwable e6) {
                                    Log.w("PrintVulcan", e6);
                                }
                            }
                        } catch (Throwable e7) {
                            Log.w("PrintVulcan", e7);
                        }
                    }
                } catch (IOException e8) {
                    Log.w("PrintVulcan", e8);
                    if (e8 instanceof I18nException) {
                        i18nException = (I18nException) e8;
                        if (0 != 0) {
                            renderFile.delete();
                        }
                    } else {
                        if (e8.getMessage() == null) {
                            message = e8.toString();
                        } else {
                            message = e8.getMessage();
                        }
                        i18nException = new I18nException(R.string.ErrorPrintServer, message);
                        if (0 != 0) {
                            renderFile.delete();
                        }
                    }
                } catch (Throwable e9) {
                    Log.w("PrintVulcan", e9);
                }
            }
            return i18nException;
        } catch (Throwable e10) {
            Log.w("PrintVulcan", e10);
        }
        return i18nException;
    }

    public Pair<File, PrintMode> render(InputStream input, String filename, PrintBotInfo printer, String printerType) throws IOException {
        MultipartEntity reqEntity = new MultipartEntity();
        if (printer.getResolution() != null) {
            reqEntity.addPart("resolution", new StringBody(printer.getResolution()));
        }
        if (printer.getDriver() != null) {
            reqEntity.addPart(GUIConstants.DRIVER, new StringBody(printer.getDriver()));
        }
        if (printer.getPageSize() != null) {
            reqEntity.addPart("pageSize", new StringBody(printer.getPageSize()));
        }
        if (!printer.isPortrait()) {
            reqEntity.addPart("orientation", new StringBody("landscape"));
        }
        if (printerType != null) {
            reqEntity.addPart(GUIConstants.MDNS_TYPE, new StringBody(printerType));
        }
//        reqEntity.addPart("pv", new StringBody("43"));
        if (input != null) {
            reqEntity.addPart("bin", new InputStreamBody(input, GUIConstants.APPLICATION_PDF, filename));
        }

        Header header = reqEntity.getContentType();
        HttpURLConnection httpURLConnection = Util.getHttpURLConnection(ctx, GUIConstants.RENDER_SERVLET);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.addRequestProperty("Content-length", reqEntity.getContentLength() + "");
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.addRequestProperty(header.getName(), header.getValue());
        OutputStream os = httpURLConnection.getOutputStream();
        reqEntity.writeTo(httpURLConnection.getOutputStream());
        os.close();
        httpURLConnection.connect();
        int statusCode = httpURLConnection.getResponseCode();
        Log.d("PrintVulcan", "Rendering ready with status code " + statusCode);
        switch (statusCode) {
            case IPPHttp.HTTP_OK:
                File ret = Util.createTempFile(this.ctx, ".ras");
                InputStream inputStream = httpURLConnection.getInputStream();
                FileOutputStream out = new FileOutputStream(ret);
                String hMode = httpURLConnection.getHeaderField("X-Print-Mode");

                Util.streamCopy(inputStream, out);
                out.close();
                return new Pair(ret, PrintMode.PRO);
            case IPPHttp.HTTP_BAD_REQUEST /*{ENCODED_INT: 400}*/:
                throw new I18nException(R.string.ErrorDeviceNotSupported);
            case IPPHttp.HTTP_PAYMENT_REQUIRED /*{ENCODED_INT: 402}*/:
            case IPPHttp.HTTP_FORBIDDEN /*{ENCODED_INT: 403}*/:
                throw new I18nException(R.string.ErrorTooManyPrints);
            case IPPHttp.HTTP_NOT_ACCEPTABLE /*{ENCODED_INT: 406}*/:
                throw new I18nException(R.string.ErrorDriverNameChanged);
            case IPPHttp.HTTP_GONE /*{ENCODED_INT: 410}*/:
                throw new I18nException(R.string.ErrorWrongVersion);
            case IPPHttp.HTTP_PRECONDITION /*{ENCODED_INT: 412}*/:
                throw new I18nException(R.string.ErrorWrongProVersion);
            case IPPHttp.HTTP_REQUEST_TOO_LARGE /*{ENCODED_INT: 413}*/:
                throw new I18nException(R.string.ErrorRendering, "File too large");
            case IPPHttp.HTTP_UNSUPPORTED_MEDIATYPE /*{ENCODED_INT: 415}*/:
                throw new I18nException(R.string.ErrorUnknownType);
            default:
                throw new IOException(httpURLConnection.getResponseMessage());
        }
    }
}
