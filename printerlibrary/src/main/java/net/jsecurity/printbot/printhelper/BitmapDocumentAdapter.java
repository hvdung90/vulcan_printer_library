package net.jsecurity.printbot.printhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;

import com.itextpdf.text.pdf.ColumnText;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class BitmapDocumentAdapter extends PrintDocumentAdapter {
    private static final int MAX_PRINT_SIZE = 3500;
    private final PrintHelperKitkat.OnPrintFinishCallback callback;
    private final int fittingMode;
    private final Uri imageFile;
    private final String jobName;
    private PrintAttributes mAttributes;
    private Bitmap mBitmap = null;
    private Context mContext;
    private BitmapFactory.Options mDecodeOptions = null;
    private AsyncTask<Uri, Boolean, Bitmap> mLoadBitmap;
    private final Object mLock = new Object();

    BitmapDocumentAdapter(Context context, String jobName2, PrintHelperKitkat.OnPrintFinishCallback callback2, int fittingMode2, Uri imageFile2) {
        this.mContext = context;
        this.jobName = jobName2;
        this.callback = callback2;
        this.fittingMode = fittingMode2;
        this.imageFile = imageFile2;
    }

    @SuppressLint("StaticFieldLeak")
    public void onLayout(final PrintAttributes oldPrintAttributes, final PrintAttributes newPrintAttributes, final CancellationSignal cancellationSignal, final LayoutResultCallback layoutResultCallback, Bundle bundle) {
        boolean changed = true;
        this.mAttributes = newPrintAttributes;
        if (cancellationSignal.isCanceled()) {
            layoutResultCallback.onLayoutCancelled();
        } else if (this.mBitmap != null) {
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(this.jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO).setPageCount(1).build();
            if (newPrintAttributes.equals(oldPrintAttributes)) {
                changed = false;
            }
            layoutResultCallback.onLayoutFinished(info, changed);
        } else {
            this.mLoadBitmap = new AsyncTask<Uri, Boolean, Bitmap>() {

                public void onPreExecute() {
                    cancellationSignal.setOnCancelListener(() -> {
                        BitmapDocumentAdapter.this.cancelLoad();
                    });
                }

                public Bitmap doInBackground(Uri... uris) {
                    try {
                        return BitmapDocumentAdapter.this.loadConstrainedBitmap(BitmapDocumentAdapter.this.imageFile, BitmapDocumentAdapter.MAX_PRINT_SIZE);
                    } catch (FileNotFoundException e) {
                        return null;
                    }
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Bitmap bitmap) {
                    boolean changed = true;
                    super.onPostExecute((Bitmap) bitmap);
                    BitmapDocumentAdapter.this.mBitmap = bitmap;
                    if (bitmap != null) {
                        PrintDocumentInfo info = new PrintDocumentInfo.Builder(BitmapDocumentAdapter.this.jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO).setPageCount(1).build();
                        if (newPrintAttributes.equals(oldPrintAttributes)) {
                            changed = false;
                        }
                        layoutResultCallback.onLayoutFinished(info, changed);
                    } else {
                        layoutResultCallback.onLayoutFailed(null);
                    }
                    BitmapDocumentAdapter.this.mLoadBitmap = null;
                }

                /* access modifiers changed from: protected */
                public void onCancelled(Bitmap result) {
                    layoutResultCallback.onLayoutCancelled();
                    BitmapDocumentAdapter.this.mLoadBitmap = null;
                }
            }.execute(new Uri[0]);
        }
    }

    private void cancelLoad() {
        synchronized (this.mLock) {
            if (this.mDecodeOptions != null) {
                this.mDecodeOptions.requestCancelDecode();
                this.mDecodeOptions = null;
            }
        }
    }

    public void onFinish() {
        super.onFinish();
        cancelLoad();
        if (this.mLoadBitmap != null) {
            this.mLoadBitmap.cancel(true);
        }
        if (this.callback != null) {
            this.callback.onFinish();
        }
        if (this.mBitmap != null) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
    }

    public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
        PrintedPdfDocument pdfDocument = new PrintedPdfDocument(this.mContext, this.mAttributes);
        Bitmap maybeGrayscale = convertBitmapForColorMode(this.mBitmap, this.mAttributes.getColorMode());
        try {
            PdfDocument.Page page = pdfDocument.startPage(1);
            page.getCanvas().drawBitmap(maybeGrayscale, getMatrix(this.mBitmap.getWidth(), this.mBitmap.getHeight(), new RectF(page.getInfo().getContentRect()), this.fittingMode), null);
            pdfDocument.finishPage(page);
            try {
                pdfDocument.writeTo(new FileOutputStream(fileDescriptor.getFileDescriptor()));
                writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            } catch (IOException ioe) {
                Log.e("PrintVulcan", "Error writing printed content", ioe);
                writeResultCallback.onWriteFailed(null);
            }
        } finally {
            if (pdfDocument != null) {
                pdfDocument.close();
            }
            if (fileDescriptor != null) {
                try {
                    fileDescriptor.close();
                } catch (IOException e) {
                }
            }
            if (maybeGrayscale != this.mBitmap) {
                maybeGrayscale.recycle();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap loadConstrainedBitmap(Uri uri, int maxSideLength) throws FileNotFoundException {
        BitmapFactory.Options decodeOptions;
        Bitmap bitmap = null;
        if (maxSideLength <= 0 || uri == null || this.mContext == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        loadBitmap(uri, opt);
        int w = opt.outWidth;
        int h = opt.outHeight;
        if (w > 0 && h > 0) {
            int imageSide = Math.max(w, h);
            int sampleSize = 1;
            while (imageSide > maxSideLength) {
                imageSide >>>= 1;
                sampleSize <<= 1;
            }
            if (sampleSize > 0 && Math.min(w, h) / sampleSize > 0) {
                synchronized (this.mLock) {
                    this.mDecodeOptions = new BitmapFactory.Options();
                    this.mDecodeOptions.inMutable = true;
                    this.mDecodeOptions.inSampleSize = sampleSize;
                    decodeOptions = this.mDecodeOptions;
                }
                try {
                    bitmap = loadBitmap(uri, decodeOptions);
                    synchronized (this.mLock) {
                        this.mDecodeOptions = null;
                    }
                } catch (Throwable th) {
                    synchronized (this.mLock) {
                        this.mDecodeOptions = null;
                        throw th;
                    }
                }
            }
        }
        return bitmap;
    }

    private Bitmap loadBitmap(Uri uri, BitmapFactory.Options o) throws FileNotFoundException {
        if (uri == null || this.mContext == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        InputStream is = null;
        try {
            is = this.mContext.getContentResolver().openInputStream(uri);
            Bitmap decodeStream = BitmapFactory.decodeStream(is, null, o);
            if (is != null) {

            }
            return decodeStream;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException t2) {
                    Log.w("PrintVulcan", "close fail ", t2);
                }
            }
        }
    }

    private Bitmap convertBitmapForColorMode(Bitmap original, int colorMode) {
        if (colorMode != 1) {
            return original;
        }
        Bitmap grayscale = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(grayscale);
        Paint p = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(ColumnText.GLOBAL_SPACE_CHAR_RATIO);
        p.setColorFilter(new ColorMatrixColorFilter(cm));
        c.drawBitmap(original, ColumnText.GLOBAL_SPACE_CHAR_RATIO, ColumnText.GLOBAL_SPACE_CHAR_RATIO, p);
        c.setBitmap(null);
        return grayscale;
    }

    private Matrix getMatrix(int imageWidth, int imageHeight, RectF content, int fittingMode2) {
        float scale;
        Matrix matrix = new Matrix();
        float scale2 = content.width() / ((float) imageWidth);
        if (fittingMode2 == 2) {
            scale = Math.max(scale2, content.height() / ((float) imageHeight));
        } else {
            scale = Math.min(scale2, content.height() / ((float) imageHeight));
        }
        matrix.postScale(scale, scale);
        matrix.postTranslate((content.width() - (((float) imageWidth) * scale)) / 2.0f, (content.height() - (((float) imageHeight) * scale)) / 2.0f);
        return matrix;
    }
}
