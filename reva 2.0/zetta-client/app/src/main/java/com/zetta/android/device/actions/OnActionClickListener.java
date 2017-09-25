package com.zetta.android.device.actions;

import android.support.annotation.NonNull;

import com.zetta.android.ZettaDeviceId;

import java.util.Map;

public interface OnActionClickListener {

    void onActionClick(@NonNull ZettaDeviceId deviceId, @NonNull String action, @NonNull Map<String, Object> inputs);
}
