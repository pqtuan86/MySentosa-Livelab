package sg.edu.smu.livelabs.integration;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Le Gia Hai  on 8/6/2015.
 */
class Net {

    interface HttpCallback {
        void onSuccess(String respone);
        void onFailed(Throwable t);
    }

    private Net() {}

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String getMACAddress(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();
        return address;
    }

    public static void post(final String urlStr, final Map<String, String> postParams, final HttpCallback callback) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private Throwable t;
            private String text;

            @Override
            protected Void doInBackground(Void... params) {
                String data = encodeHttpParams(postParams);
                BufferedReader reader=null;
                try {
                    // Defined URL  where to send data
                    URL url = new URL(urlStr);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write( data );
                    wr.flush();

                    // Get the server response
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        // Append server response in string
                        sb.append(line + "\n");
                    }
                    text = sb.toString();
                } catch(Throwable ex) {
                    t = ex;
                } finally {
                    try
                    {
                        reader.close();
                    }
                    catch(Exception ex) {}
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (t != null) {
                    try {
                        callback.onFailed(t);
                    } catch (Throwable t2) {}
                } else {
                    try {
                        callback.onSuccess(text);
                    } catch (Throwable t2) {}
                }
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private static String encodeHttpParams(Map<String, String> params) {
        List<String> items = new ArrayList<>();
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) {
                value = "";
            }
            try {
                items.add(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return TextUtils.join("&", items);
    }
}
