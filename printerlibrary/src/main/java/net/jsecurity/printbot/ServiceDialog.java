package net.jsecurity.printbot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

public class ServiceDialog extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service);
        ((TextView) findViewById(R.id.ServiceButton)).setOnClickListener(v -> {
            try {
                ServiceDialog.this.startActivityForResult(new Intent(Settings.class.getField("ACTION_PRINT_SETTINGS").get(null).toString()), 0);
                ServiceDialog.this.finish();
            } catch (Exception e) {
            }
        });
    }
}
