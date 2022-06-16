package net.jsecurity.printbot.chooser;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.xml.xmp.PdfSchema;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.ServiceDialog;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;
import net.jsecurity.printbot.prefs.PrinterListActivity;
import net.jsecurity.printbot.prefs.SettingsHelper;
import net.jsecurity.printbot.printhelper.PrintHelperKitkat;

import org.apache.james.mime4j.field.ContentTypeField;

public class FileChooser extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private static final Map<String, String> MIME_TYPE_MAP = new HashMap();
    private static final String[][] MIME_TYPE_MAP_FILL = {new String[]{"jpg", "image/jpeg"}, new String[]{"jpeg", "image/jpeg"}, new String[]{"png", "image/png"}, new String[]{"gif", "image/gif"}, new String[]{"bmp", "image/bmp"}, new String[]{"txt", ContentTypeField.TYPE_TEXT_PLAIN}, new String[]{"html", "text/html"}, new String[]{"htm", "text/html"}, new String[]{PdfSchema.DEFAULT_XPATH_ID, GUIConstants.APPLICATION_PDF}};
    public static final String TYPE_FOLDER = "folder";
    private FileArrayAdapter adapter;
    private File currentDir;
    private File rootDir;

    static {
        for (int i = 0; i < MIME_TYPE_MAP_FILL.length; i++) {
            MIME_TYPE_MAP.put(MIME_TYPE_MAP_FILL[i][0], MIME_TYPE_MAP_FILL[i][1]);
            MIME_TYPE_MAP.put(MIME_TYPE_MAP_FILL[i][0].toUpperCase(), MIME_TYPE_MAP_FILL[i][1]);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
            startup();
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        startup();
    }

    private void startup() {
        if (SettingsHelper.hasVersionChanged(this)) {
            startActivityForResult(new Intent(this, ServiceDialog.class), 1);
        }
        setContentView(R.layout.file_chooser);
        List<KeyValuePair> fileSystems = StorageUtils.getStorageList();
        Spinner fileSystemSpinner = (Spinner) findViewById(R.id.FileSystemSpinner);
        fileSystemSpinner.setOnItemSelectedListener(this);
        UIUtil.setDropDownValues(fileSystemSpinner, fileSystems, (String) null);
        this.currentDir = Environment.getExternalStorageDirectory();
        rootDir = currentDir;
        fill(this.currentDir);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            Log.i("PrintVulcan", "Returning from service settings, setting version");
            SettingsHelper.setInstalledVersion(this);
        }
    }

    private void fill(File f) {
        Log.d("PrintVulcan", "Filling " + f);
        ((TextView) findViewById(R.id.FileSystemPath)).setText(f.getAbsolutePath().substring(this.rootDir.getAbsolutePath().length()));
        File[] dirs = f.listFiles();
        List<Option> dir = new ArrayList<>();
        List<Option> fls = new ArrayList<>();
        try {
            for (File ff : dirs) {
                if (ff.getName().charAt(0) != '.') {
                    if (ff.isDirectory()) {
                        dir.add(new Option(ff.getName(), PdfObject.NOTHING, ff.getAbsolutePath(), TYPE_FOLDER));
                    } else {
                        fls.add(new Option(ff.getName(), PdfObject.NOTHING + ff.length(), ff.getAbsolutePath(), getMimeTypeFromExtension(ff)));
                    }
                }
            }
        } catch (Exception e) {
            Log.w("PrintVulcan", "Error reading file system, probably not permitted.");
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.equals(this.rootDir)) {
            dir.add(0, new Option("..", PdfObject.NOTHING, f.getParent(), TYPE_FOLDER));
        }
        this.adapter = new FileArrayAdapter(this, R.layout.file_view, dir);
        ListView fileSystemList = (ListView) findViewById(R.id.FileSystemList);
        fileSystemList.setAdapter((ListAdapter) this.adapter);
        fileSystemList.setOnItemClickListener(this);
    }

    private String getMimeTypeFromExtension(File file) {
        String filename = file.getName();
        int ix = filename.lastIndexOf(46);
        if (ix < 0) {
            return null;
        }
        return MIME_TYPE_MAP.get(filename.substring(ix + 1));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        Option o = this.adapter.getItem(position);
        if (o.getType() == null) {
            Toast.makeText(this, getResources().getString(R.string.WarnFileTypeNotSupported, o.getName()), Toast.LENGTH_LONG).show();
        } else if (TYPE_FOLDER.equals(o.getType())) {
            this.currentDir = new File(o.getPath());
            fill(this.currentDir);
        } else {
            new PrintHelperKitkat(this).print(o.getType(), o.getName(), Uri.fromFile(new File(o.getPath())), null);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapter2, View view, int position, long id) {
        KeyValuePair item = (KeyValuePair) adapter2.getItemAtPosition(position);
        if (item != null && item.getKey() != null) {
            this.rootDir = new File(item.getKey());
            this.currentDir = this.rootDir;
            fill(this.currentDir);
        }
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            menu.add(R.string.Settings).setIntent(new Intent(Settings.class.getField("ACTION_PRINT_SETTINGS").get(null).toString()));
        } catch (Exception e) {
        }
        menu.add(R.string.ListMenuTitle).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            /* class net.jsecurity.printbot.chooser.FileChooser.AnonymousClass1 */

            public boolean onMenuItemClick(MenuItem item) {
                FileChooser.this.startActivity(new Intent(FileChooser.this, PrinterListActivity.class));
                return true;
            }
        });
        menu.add(R.string.PrintClipboard).setOnMenuItemClickListener(item -> {
            FileChooser.this.printClipboard();
            return true;
        });
        UIUtil.addStandardMenu(this, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onBackPressed() {
        if (this.currentDir == null || this.currentDir.equals(this.rootDir)) {
            super.onBackPressed();
            return;
        }
        this.currentDir = this.currentDir.getParentFile();
        fill(this.currentDir);
    }

    private void printClipboard() {
        print();
    }


    void print() {
        Thread thread = new Thread(() -> {
//            try {
//                Socket socket = new Socket("192.168.100.80", Integer.parseInt("9100"));
//                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
//
//
//                pw.println("\n\n\n\f");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//                pw.print("THIS IS YOUR AWESOME TEXT FOR PRINT");
//
////                pw.write(0x0C); //here you release the paper
////                pw.write(0x40); //finish printing
//                pw.flush();
//                pw.close();
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            sendTPrinter();
        });
        thread.start();

    }

    void sendTPrinter() {
        DataOutputStream outToServer = null;
        Socket clientSocket = null;
        String result;
        InputStream fileInputStream = null;
        try {

            clientSocket = new Socket("192.168.100.80", Integer.parseInt("9100"));
            InputStream inputStream = clientSocket.getInputStream();
            outToServer = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

            char ESC = 0x1b;// "0x1b";
            String UEL = ESC + "%-12345X";
            String ESC_SEQ = ESC + "%-12345";
            outToServer.write(convertTOUtf(UEL + "@PJL COMMENT *Start Job*"));
            outToServer.write(convertTOUtf("@PJL \r\n"));
            outToServer.write(convertTOUtf("@PJL JOB NAME = $myjob"));
            outToServer.write(convertTOUtf("@PJL SET RET = "));
            outToServer.writeChars(("@PJL SET PAPER=" + "A4"));
            outToServer.write((convertTOUtf("@PJL SET COPIES= 1")));
            outToServer.write(convertTOUtf("@PJL ENTER LANGUAGE = PCL"));
            outToServer.write(convertTOUtf(ESC + "E"));
            outToServer.write(convertTOUtf(ESC + "EHello World"));

            outToServer.write(convertTOUtf(ESC_SEQ));
            outToServer.write(convertTOUtf("@PJL \r\n"));
            outToServer.write(convertTOUtf("@PJL RESET \r\n"));
            outToServer.write(convertTOUtf("@PJL EOJ NAME = $myjob"));
            outToServer.write(convertTOUtf(UEL));

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            int count = bufferedInputStream.available();
        } catch (ConnectException connectException) {
            Log.e("TAG", connectException.toString());
            result = connectException.toString();
        } catch (UnknownHostException unknownHostException) {
            Log.e("TAG", unknownHostException.toString());
            result = unknownHostException.toString();
        } catch (IOException ioException) {
            Log.e("TAG", ioException.toString());
            result = ioException.toString();
        } finally {
            try {

                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException ioException) {
                result = ioException.toString();
            }
        }
    }

    byte[] convertTOUtf(String text) {
        String normal = "";
        try {
            normal = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return normal.getBytes();
    }
}
