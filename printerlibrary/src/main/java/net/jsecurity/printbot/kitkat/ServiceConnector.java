package net.jsecurity.printbot.kitkat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.prefs.SettingsHelper;

public class ServiceConnector extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_connector);
        Intent proIntent = SettingsHelper.getProIntent(this);
        if (proIntent != null) {
            startActivityForResult(proIntent, 0);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String pl;
        if (requestCode == 0 && data != null && (pl = data.getStringExtra("PL")) != null) {
            SharedPreferences.Editor editor = getSharedPreferences(GUIConstants.PREFERENCE_KEY, 0).edit();
            editor.putString("PL", pl);
            editor.putInt("PV", SettingsHelper.getProVersion(this));
            editor.apply();
            finish();
        }
    }
}
