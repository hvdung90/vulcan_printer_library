package net.jsecurity.printbot.chooser;

import android.os.Environment;

import net.jsecurity.printbot.model.KeyValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {
    private static final String PROC_MOUNTS = "/proc/mounts";


    public static List<KeyValuePair> getStorageList() {
        List<KeyValuePair> r14 = new ArrayList();
//        File r19 = Environment.getExternalStorageDirectory();
//        java.lang.String r6 = r19.getPath();
//        boolean r9 = android.os.Environment.isExternalStorageRemovable();
//        java.lang.String r10 = android.os.Environment.getExternalStorageState();
        return r14;

    }

    private static String getDisplayName(boolean readonly, boolean removable, int number) {
        StringBuilder res = new StringBuilder();
        if (!removable) {
            res.append("Internal SD card");
        } else if (number > 1) {
            res.append("SD card " + number);
        } else {
            res.append("SD card");
        }
        if (readonly) {
            res.append(" (Read only)");
        }
        return res.toString();
    }

}
