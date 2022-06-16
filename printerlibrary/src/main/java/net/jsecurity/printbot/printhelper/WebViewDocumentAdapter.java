package net.jsecurity.printbot.printhelper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.webkit.WebView;
import net.jsecurity.printbot.printhelper.PrintHelperKitkat;

public final class WebViewDocumentAdapter extends PrintDocumentAdapter {
    private final PrintHelperKitkat.OnPrintFinishCallback callback;
    private WebView mWebView;
    private final PrintDocumentAdapter mWrappedInstance;

    WebViewDocumentAdapter(WebView mWebView2, PrintHelperKitkat.OnPrintFinishCallback callback2) {
        this.mWebView = mWebView2;
        this.callback = callback2;
        this.mWrappedInstance = mWebView2.createPrintDocumentAdapter();
    }

    public void onStart() {
        this.mWrappedInstance.onStart();
    }

    @SuppressLint({"WrongCall"})
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback2, Bundle extras) {
        this.mWrappedInstance.onLayout(oldAttributes, newAttributes, cancellationSignal, callback2, extras);
    }

    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback2) {
        this.mWrappedInstance.onWrite(pages, destination, cancellationSignal, callback2);
    }

    public void onFinish() {
        this.mWrappedInstance.onFinish();
        this.mWebView.destroy();
        this.mWebView = null;
        if (this.callback != null) {
            this.callback.onFinish();
        }
    }
}
