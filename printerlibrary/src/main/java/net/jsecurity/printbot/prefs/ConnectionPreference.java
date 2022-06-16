package net.jsecurity.printbot.prefs;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.itextpdf.text.pdf.PdfObject;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.KeyValuePair;
import net.jsecurity.printbot.model.PrintBotInfo;

public class ConnectionPreference extends DialogPreference {
    private TextView hostLabel;
    private EditText hostText;
    private TextView passwordLabel;
    private EditText passwordText;
    private Spinner protocolSpinner;
    private TextView queueLabel;
    private EditText queueText;
    private TextView userLabel;
    private EditText userText;

    public ConnectionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConnectionPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public View onCreateDialogView() {
        Log.d("PrintVulcan", "Start onCreateDialogView");
        View view = super.onCreateDialogView();
        int currentPrinterIndex = ((SettingsActivity) getContext()).getCurrentPrinterIndex();
        this.protocolSpinner = (Spinner) view.findViewById(R.id.ProtocolSpinner);
        this.hostText = (EditText) view.findViewById(R.id.HostText);
        this.queueText = (EditText) view.findViewById(R.id.QueueText);
        this.userText = (EditText) view.findViewById(R.id.UserText);
        this.passwordText = (EditText) view.findViewById(R.id.PasswordText);
        this.hostLabel = (TextView) view.findViewById(R.id.HostLabel);
        this.queueLabel = (TextView) view.findViewById(R.id.QueueLabel);
        this.userLabel = (TextView) view.findViewById(R.id.UserLabel);
        this.passwordLabel = (TextView) view.findViewById(R.id.PasswordLabel);
        String currentProtocol = GUIConstants.PROTOCOL_RAW;
        PrintBotInfo info = SettingsHelper.getPrinter(getContext(), currentPrinterIndex);
        if (info != null) {
            currentProtocol = info.getProtocol();
            this.hostText.setText(info.getHost());
            this.queueText.setText(info.getQueue());
            this.userText.setText(info.getUser());
            this.passwordText.setText(info.getPassword());
        }
        final boolean enableAuth = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enableAuth", false);
        UIUtil.setDropDownValues(this.protocolSpinner, SettingsHelper.getProtocols(getContext()), currentProtocol);
        this.protocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                Log.d("PrintVulcan", "protocolSpinner.onItemSelected " + ConnectionPreference.this.protocolSpinner);
                String protocol = ((KeyValuePair) ConnectionPreference.this.protocolSpinner.getSelectedItem()).getKey();
                if (GUIConstants.PROTOCOL_RAW.equals(protocol) || GUIConstants.PROTOCOL_FRITZ.equals(protocol)) {
                    ConnectionPreference.this.hostText.setVisibility(View.VISIBLE);
                    ConnectionPreference.this.hostLabel.setVisibility(View.VISIBLE);
                    if (GUIConstants.PROTOCOL_FRITZ.equals(protocol)) {
                        ConnectionPreference.this.hostText.setText("fritz.box");
                    }
                    ConnectionPreference.this.queueText.setVisibility(View.GONE);
                    ConnectionPreference.this.queueLabel.setVisibility(View.GONE);
                    ConnectionPreference.this.queueText.setText(PdfObject.NOTHING);
                    ConnectionPreference.this.userText.setVisibility(View.GONE);
                    ConnectionPreference.this.userLabel.setVisibility(View.GONE);
                    ConnectionPreference.this.userText.setText(PdfObject.NOTHING);
                    ConnectionPreference.this.passwordText.setVisibility(View.GONE);
                    ConnectionPreference.this.passwordLabel.setVisibility(View.GONE);
                    ConnectionPreference.this.passwordText.setText(PdfObject.NOTHING);
                } else if (GUIConstants.PROTOCOL_IPP.equals(protocol)) {
                    ConnectionPreference.this.queueText.setVisibility(View.VISIBLE);
                    ConnectionPreference.this.queueLabel.setVisibility(View.VISIBLE);
                    ConnectionPreference.this.hostText.setVisibility(View.VISIBLE);
                    ConnectionPreference.this.hostLabel.setVisibility(View.VISIBLE);
                    if (enableAuth) {
                        ConnectionPreference.this.userText.setVisibility(View.VISIBLE);
                        ConnectionPreference.this.userLabel.setVisibility(View.VISIBLE);
                        ConnectionPreference.this.passwordText.setVisibility(View.VISIBLE);
                        ConnectionPreference.this.passwordLabel.setVisibility(View.VISIBLE);
                        return;
                    }
                    ConnectionPreference.this.userText.setVisibility(View.GONE);
                    ConnectionPreference.this.userLabel.setVisibility(View.GONE);
                    ConnectionPreference.this.passwordText.setVisibility(View.GONE);
                    ConnectionPreference.this.passwordLabel.setVisibility(View.GONE);
                } else {
                    ConnectionPreference.this.queueText.setVisibility(View.VISIBLE);
                    ConnectionPreference.this.queueLabel.setVisibility(View.VISIBLE);
                    ConnectionPreference.this.hostText.setVisibility(View.VISIBLE);
                    ConnectionPreference.this.hostLabel.setVisibility(View.VISIBLE);
                    if (enableAuth) {
                        ConnectionPreference.this.userText.setVisibility(View.VISIBLE);
                        ConnectionPreference.this.userLabel.setVisibility(View.VISIBLE);
                    } else {
                        ConnectionPreference.this.userText.setVisibility(View.GONE);
                        ConnectionPreference.this.userLabel.setVisibility(View.GONE);
                    }
                    ConnectionPreference.this.passwordText.setVisibility(View.GONE);
                    ConnectionPreference.this.passwordLabel.setVisibility(View.GONE);
                    ConnectionPreference.this.passwordText.setText(PdfObject.NOTHING);
                }
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return view;
    }

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean positiveResult) {
        Log.d("PrintVulcan", "onDialogClosed " + positiveResult);
        if (positiveResult) {
            SettingsActivity settings = (SettingsActivity) getContext();
            String protocol = ((KeyValuePair) this.protocolSpinner.getSelectedItem()).getKey();
            if (GUIConstants.PROTOCOL_FRITZ.equals(protocol)) {
                protocol = GUIConstants.PROTOCOL_RAW;
            }
            settings.saveFields(new KeyValuePair[]{new KeyValuePair(GUIConstants.PROTOCOL, protocol), new KeyValuePair(GUIConstants.HOST, this.hostText.getText().toString().trim()), new KeyValuePair(GUIConstants.QUEUE, this.queueText.getText().toString().trim()), new KeyValuePair(GUIConstants.USER, this.userText.getText().toString().trim()), new KeyValuePair(GUIConstants.PASSWORD, this.passwordText.getText().toString().trim())});
            settings.updateSummaries();
        }
    }
}
