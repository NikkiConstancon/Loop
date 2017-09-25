package com.zetta.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.zetta.android.R;

public class SdkProperties {

    @NonNull private final SharedPreferences sharedPreferences;
    @NonNull private final String apiUrlKey;
    @NonNull private final String mockResponsesKey;
    private String serverURL;

    @NonNull
    public static SdkProperties newInstance(@NonNull Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String apiUrlKey = context.getString(R.string.key_api_url_with_history);
        String serverURL = context.getString(R.string.serverURL);
        String mockResponsesKey = context.getString(R.string.key_mock_responses);
        return new SdkProperties(sharedPreferences, apiUrlKey, mockResponsesKey, serverURL);
    }

    SdkProperties(@NonNull SharedPreferences sharedPreferences,
                  @NonNull String apiUrlKey,
                  @NonNull String mockResponsesKey, @NonNull String serverURL) {
        this.sharedPreferences = sharedPreferences;
        this.apiUrlKey = apiUrlKey;
        this.serverURL = serverURL;
        this.mockResponsesKey = mockResponsesKey;
    }

    public boolean hasUrl() {
        return !"".equals(getUrl().trim());
    }

    @NonNull
    public String getUrl() {
        return "http://" + serverURL + "";
    } // THE SERVER: http://iomt.dedicated.co.za:3009

    public void setUrl(String URL) {
        serverURL = URL;
    }

    public boolean useMockResponses() {
        return sharedPreferences.getBoolean(mockResponsesKey, false);
    }

}
