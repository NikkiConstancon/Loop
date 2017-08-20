package com.zetta.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zetta.android.R.layout.login_activity;

public class ServerComms extends AsyncTask<String, Void, Boolean> {
    private String result = null;
    private ProgressDialog progressDialog;
    private boolean pass = false;
    private Context context;

    public boolean getPass() { return pass; }
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
            //TODO
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HttpURLConnection urlConnection;


        String uri = "http://192.168.1.103:8080/login"; // 192.168.1.103
        JSONObject obj = new JSONObject();
        for (int i = 0; i < params.length-1; i=i+2) {
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
//            urlConnection.setRequestProperty("Content-Type", "application/json");
//            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            urlConnection.setRequestMethod("POST");


            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(obj.toString());
            writer.close();
            outputStream.close();

            urlConnection.connect();

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