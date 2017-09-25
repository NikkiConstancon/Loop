package com.zetta.android;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.UUID;

public class ZettaDeviceId implements Serializable {

    @NonNull private final UUID uuid;

    public ZettaDeviceId(@NonNull UUID uuid) {
        this.uuid = uuid;
    }

    @NonNull
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ZettaDeviceId)) {
            return false;
        }

        ZettaDeviceId deviceId = (ZettaDeviceId) o;

        return uuid.equals(deviceId.uuid);

    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
