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

    @NonNull
    public static SdkProperties newInstance(@NonNull Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String apiUrlKey = context.getString(R.string.key_api_url_with_history);
        String mockResponsesKey = context.getString(R.string.key_mock_responses);
        return new SdkProperties(sharedPreferences, apiUrlKey, mockResponsesKey);
    }

    SdkProperties(@NonNull SharedPreferences sharedPreferences,
                  @NonNull String apiUrlKey,
                  @NonNull String mockResponsesKey) {
        this.sharedPreferences = sharedPreferences;
        this.apiUrlKey = apiUrlKey;
        this.mockResponsesKey = mockResponsesKey;
    }

    public boolean hasUrl() {
        return !"".equals(getUrl().trim());
    }

    @NonNull
    public String getUrl() {

        return "http://192.168.1.102:1337";
    }

    public boolean useMockResponses() {
        return sharedPreferences.getBoolean(mockResponsesKey, false);
    }

}
