package net.jsecurity.printbot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import net.jsecurity.printbot.printhelper.PrintHelperKitkat;

public class PrintActivity extends Activity implements PrintHelperKitkat.OnPrintFinishCallback {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri == null) {
            Object o = intent.getExtras().get("android.intent.extra.STREAM");
            if (o instanceof Uri) {
                uri = (Uri) o;
            }
        }
        PrintHelperKitkat printHelper = new PrintHelperKitkat(this);
        String mimeType = intent.getType();
        if (uri == null || mimeType == null) {
            Log.e("PrintVulcan", "No URI or mime type given for printing.");
            UIUtil.showErrorDialog(this, R.string.ErrorUnknown);
            finish();
            return;
        }
        printHelper.print(mimeType, uri.getLastPathSegment(), uri, this);
    }

    @Override
    public void onFinish() {
        finish();
    }
}
