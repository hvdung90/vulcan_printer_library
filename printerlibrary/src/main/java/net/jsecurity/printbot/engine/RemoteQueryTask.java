package net.jsecurity.printbot.engine;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.Util;
import net.jsecurity.printbot.model.GUIConstants;
import net.jsecurity.printbot.model.I18nException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class RemoteQueryTask extends AsyncTask<String, Void, List<String>> {
    private Context ctx;
    private RemoteQueryListener listener;
    private QueryMode mode;
    private String baseUrl;
    private HttpClient httpClient;

    public enum QueryMode {
        MANUFACTURER,
        DRIVER,
        RESOLUTION,
        PROPOSE_DRIVER
    }

    public RemoteQueryTask(Context ctx2, RemoteQueryListener listener2) {
        this.listener = listener2;
        this.ctx = ctx2;
        this.httpClient = new BobsHttpClientCus(ctx2);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        this.baseUrl = GUIConstants.REMOTE_URL;
    }

    public void onPostExecute(List<String> list) {
        this.listener.onReturnFromRemoteQuery(this.mode, list);
    }

    public void getManufacturers() {
        this.mode = QueryMode.MANUFACTURER;
        execute("listManufacturers");
    }

    public void getPrintersForManufacturer(String manufacturer) {
        this.mode = QueryMode.DRIVER;
        execute("listDrivers", GUIConstants.MANUFACTURER, manufacturer);
    }

    public void getResolutionsForPrinter(String driver) {
        this.mode = QueryMode.RESOLUTION;
        execute("listResolutions", GUIConstants.DRIVER, driver);
    }

    public void getDriverProposal(String printerType) {
        this.mode = QueryMode.PROPOSE_DRIVER;
        execute("proposeDriver", "printerType", printerType);
    }


    public List<String> doInBackground(String... r15) {

        try {
            String query = r15[0];
            String key = null;
            String value = null;
            if (r15.length > 1)
                key = r15[1];
            if (r15.length > 2)
                value = r15[2];
            return query(query, key, value);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<String> query1(String query, String key, String value) throws IOException {
        String queryString = this.baseUrl + GUIConstants.INFO_SERVLET + "?query=" + query;
        if (!(key == null || value == null)) {
            queryString = queryString + "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
        }
        Log.d("PrintVulcan", "Query string " + queryString);
        HttpResponse response = this.httpClient.execute(new HttpGet(queryString));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                JSONArray array = new JSONArray((String) responseHandler.handleResponse(response));
                List<String> ret = new ArrayList(array.length());
                for (int i = 0; i < array.length(); i++) {
                    ret.add(array.getString(i));
                }
                return ret;
            } catch (JSONException e) {
                throw new I18nException(R.string.ErrorConnecting);
            }
        } else {
            throw new I18nException(R.string.ErrorConnecting);
        }
    }

    private List<String> query(String query, String key, String value) throws IOException {
        String queryString = GUIConstants.INFO_SERVLET + "?query=" + query;
        if (!(key == null || value == null)) {
            queryString = queryString + "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
        }
        Log.d("PrintVulcan", "Query string " + queryString);
        HttpURLConnection http = Util.getHttpURLConnection(ctx, queryString);
        http.connect();
        if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                JSONArray array = new JSONArray(sb.toString());
                List<String> ret = new ArrayList(array.length());
                for (int i = 0; i < array.length(); i++) {
                    ret.add(array.getString(i));
                }
                return ret;
            } catch (JSONException e) {
                throw new I18nException(R.string.ErrorConnecting);
            }
        } else {
            throw new I18nException(R.string.ErrorConnecting);
        }
    }
}
