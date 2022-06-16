package net.jsecurity.printbot.prefs;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.UIUtil;

public class AdvancedActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.advanced);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        UIUtil.addStandardMenu(this, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
