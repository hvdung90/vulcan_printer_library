package net.jsecurity.printbot.model;

import android.content.Context;
import android.content.res.Resources;
import java.io.IOException;

public class I18nException extends IOException {
    private static final long serialVersionUID = -1979897775210831495L;
    private String detail;
    private int resId;

    public I18nException(int resId2) {
        super("Error: " + Integer.toHexString(resId2));
        this.resId = resId2;
    }

    public I18nException(int resId2, String detail2) {
        super("Error: " + Integer.toHexString(resId2) + " " + detail2);
        this.resId = resId2;
        this.detail = detail2;
    }

    public int getResId() {
        return this.resId;
    }

    public String getDetail() {
        return this.detail;
    }

    public String getLocalizedMessage(Context ctx) {
        Resources resources = ctx.getResources();
        if (this.detail == null) {
            return resources.getString(this.resId);
        }
        return resources.getString(this.resId, this.detail);
    }
}
