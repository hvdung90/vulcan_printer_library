package net.jsecurity.printbot.prefs;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.List;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.PrintBotInfo;

public class PrinterListActivity extends ListActivity {
    private MenuItem deleteItem;
    private MenuItem editItem;
    private List<PrintBotInfo> staticPrinters;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reload();
        if (this.staticPrinters.size() == 0) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 1);
        } else {
            registerForContextMenu(getListView());
        }
    }

    /* access modifiers changed from: protected */
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(GUIConstants.PRINTER_INDEX, this.staticPrinters.get(position).getIndex());
        startActivityForResult(intent, 1);
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("PrintVulcan", "Returning from settings activity, reloading");
        reload();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.NewPrinter).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            /* class net.jsecurity.printbot.prefs.PrinterListActivity.AnonymousClass1 */

            public boolean onMenuItemClick(MenuItem item) {
                PrinterListActivity.this.startActivityForResult(new Intent(PrinterListActivity.this, SettingsActivity.class), 1);
                return true;
            }
        });
        UIUtil.addStandardMenu(this, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.ListMenuTitle);
        this.editItem = menu.add(R.string.EditPrinter);
        this.deleteItem = menu.add(R.string.DelPrinter);
    }

    public boolean onContextItemSelected(MenuItem item) {
        int ix = this.staticPrinters.get(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position).getIndex();
        if (item.equals(this.editItem)) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(GUIConstants.PRINTER_INDEX, ix);
            startActivityForResult(intent, 1);
        } else if (item.equals(this.deleteItem)) {
            SettingsHelper.deletePrinter(this, ix);
            reload();
        }
        return true;
    }

    @SuppressLint("ResourceType")
    private void reload() {
        this.staticPrinters = SettingsHelper.getStaticPrinters(this);
        String[] printerArray = new String[this.staticPrinters.size()];
        int i = 0;
        for (PrintBotInfo info : this.staticPrinters) {
            printerArray[i] = info.getNetworkName();
            i++;
        }
        setListAdapter(new ArrayAdapter(this, 17367043, printerArray));
    }
}
