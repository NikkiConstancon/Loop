package com.zetta.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zetta.android.browse.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.zetta.android.R.layout.login_activity;

public class ServerComms extends AsyncTask<String, Void, Boolean> {
    private String result = null;
    private ProgressDialog progressDialog;

    private Context context;
    static final String COOKIES_HEADER = "Set-Cookie";
    static final String COOKIE = "Cookie";

    static CookieManager msCookieManager = new CookieManager();

    public String getResult() {
        return result;
    }

    public ServerComms(Context context) {
        this.context = context.getApplicationContext();
        progressDialog =  new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        try {
            progressDialog = ProgressDialog.show(context, "Signing in...", "This should take a few seconds.", true);
        } catch (final Throwable th) {

        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HttpURLConnection urlConnection;



        String uri = params[0]; // 192.168.1.103
        Log.d("uri", uri);
        JSONObject obj = new JSONObject();
        for (int i = 1; i < params.length-1; i=i+2) {
            try {
                obj.put(params[i], params[i+1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("obj", obj.toString());

        try {
            //Connect
            Log.d("try", "Trying to connect");

            urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                urlConnection.setRequestProperty(COOKIE, TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));

            }

            urlConnection.setRequestMethod("POST");

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(obj.toString());
            writer.close();
            outputStream.close();

            urlConnection.connect();

            Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    Log.d("Cookie", HttpCookie.parse(cookie).get(0).toString());
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            int statusCode = urlConnection.getResponseCode();
            String responseMsg = urlConnection.getResponseMessage();
            if (statusCode == 200) {
                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                result = sb.toString();
                return true;
            } else {
                return false;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }


    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.dismiss();
        if (result) {
            Intent intent =  new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            String message = "Incorrect user credentials";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

    }
}