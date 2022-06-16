package net.jsecurity.printbot.printhelper;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import net.jsecurity.printbot.R;

import java.io.FileOutputStream;
import java.io.IOException;

/* access modifiers changed from: package-private */
public final class PDFDocumentAdapter extends PrintDocumentAdapter {
    private final PrintHelperKitkat.OnPrintFinishCallback callback;
    private final String jobName;
    private Context mContext;
    private PdfReader mReader;
    private final Uri pdf;

    PDFDocumentAdapter(Context context, PrintHelperKitkat.OnPrintFinishCallback callback2, Uri pdf2, String jobName2) {
        this.callback = callback2;
        this.pdf = pdf2;
        this.jobName = jobName2;
        this.mContext = context;
    }

    public void onLayout(PrintAttributes oldPrintAttributes, PrintAttributes newPrintAttributes, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
        boolean changed = false;
        if (cancellationSignal.isCanceled()) {
            layoutResultCallback.onLayoutCancelled();
            return;
        }
        try {
            this.mReader = new PdfReader(this.mContext.getContentResolver().openInputStream(this.pdf));
        } catch (Throwable th) {
        }
        if (this.mReader != null) {
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(this.jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(this.mReader.getNumberOfPages()).build();
            if (!newPrintAttributes.equals(oldPrintAttributes)) {
                changed = true;
            }
            layoutResultCallback.onLayoutFinished(info, changed);
            return;
        }
        layoutResultCallback.onLayoutFailed(this.mContext.getResources().getString(R.string.ErrorConvertPDF));
    }

    public void onFinish() {
        super.onFinish();
        if (this.mReader != null) {
            this.mReader.close();
        }
        if (this.callback != null) {
            this.callback.onFinish();
        }
    }

    public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
        Document pdfDocument = new Document();
        try {
            PdfCopy copy = new PdfCopy(pdfDocument, new FileOutputStream(fileDescriptor.getFileDescriptor()));
            pdfDocument.open();
            for (PageRange range : pageRanges) {
                for (int p = range.getStart(); p <= range.getEnd(); p++) {
                    Log.d("PrintVulcan", "Writing page " + p);
                    copy.addPage(copy.getImportedPage(this.mReader, p + 1));
                }
            }
            pdfDocument.close();
            writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        } catch (Throwable th) {
            if (pdfDocument != null && pdfDocument.isOpen()) {
                try {
                    pdfDocument.close();
                } catch (Throwable th2) {
                }
            }
            if (fileDescriptor != null) {
                try {
                    fileDescriptor.close();
                } catch (IOException e) {
                }
            }
            try {
                throw th;
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (pdfDocument != null && pdfDocument.isOpen()) {
            try {
                pdfDocument.close();
            } catch (Throwable th3) {
            }
        }
        if (fileDescriptor != null) {
            try {
                fileDescriptor.close();
            } catch (IOException e2) {
            }
        }
    }
}
