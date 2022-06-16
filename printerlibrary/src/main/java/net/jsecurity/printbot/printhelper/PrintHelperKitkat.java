package net.jsecurity.printbot.printhelper;

import android.content.Context;
import android.net.Uri;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.jsecurity.printbot.model.GUIConstants;

public class PrintHelperKitkat {
    public static final int COLOR_MODE_COLOR = 2;
    public static final int COLOR_MODE_MONOCHROME = 1;
    public static final int SCALE_MODE_FILL = 2;
    public static final int SCALE_MODE_FIT = 1;
    private int mColorMode = PrintAttributes.COLOR_MODE_COLOR;
    private final Context mContext;

    public interface OnPrintFinishCallback {
        void onFinish();
    }

    public PrintHelperKitkat(Context context) {
        this.mContext = context;
    }

    public void setColorMode(int colorMode) {
        this.mColorMode = colorMode;
    }

    public int getColorMode() {
        return this.mColorMode;
    }

    public void print(String mimeType, String title, Uri uri, OnPrintFinishCallback callback) {
        if (title == null) {
            title = "-";
        }
        if (mimeType.startsWith("text/")) {
            printText(title, uri, callback);
        } else if (mimeType.equals(GUIConstants.APPLICATION_PDF)) {
            printPDF(title, uri, callback);
        } else {
            printBitmap(title, uri, callback);
        }
    }

    private void printBitmap(String jobName, Uri imageFile, OnPrintFinishCallback callback) {
        PrintDocumentAdapter printDocumentAdapter = new BitmapDocumentAdapter(this.mContext, jobName, callback, 1, imageFile);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(this.mColorMode);
        builder.setMediaSize(PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE);
        ((PrintManager) this.mContext.getSystemService(Context.PRINT_SERVICE)).print(jobName, printDocumentAdapter, builder.build());
    }

    private void printPDF(String jobName, Uri pdf, OnPrintFinishCallback callback) {
        PrintDocumentAdapter printDocumentAdapter = new PDFDocumentAdapter(this.mContext, callback, pdf, jobName);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(this.mColorMode);
        builder.setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT);
        ((PrintManager) this.mContext.getSystemService(Context.PRINT_SERVICE)).print(jobName, printDocumentAdapter, builder.build());
    }

    private void printText(final String jobName, Uri text, OnPrintFinishCallback callback) {
        final PrintManager printManager = (PrintManager) this.mContext.getSystemService(Context.PRINT_SERVICE);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(this.mColorMode);
        builder.setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT);
        final PrintAttributes attr = builder.build();
        WebView mWebView = new WebView(this.mContext);
        final PrintDocumentAdapter adapter = new WebViewDocumentAdapter(mWebView, callback);
        mWebView.setWebViewClient(new WebViewClient() {
            /* class net.jsecurity.printbot.printhelper.PrintHelperKitkat.AnonymousClass1 */

            public void onPageFinished(WebView view, String url) {
                printManager.print(jobName, adapter, attr);
            }
        });
        mWebView.loadUrl(text.toString());
    }
}
