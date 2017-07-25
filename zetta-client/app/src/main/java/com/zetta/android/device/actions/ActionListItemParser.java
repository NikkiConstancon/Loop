package com.zetta.android.device.actions;

import android.support.annotation.NonNull;

import com.apigee.zettakit.ZIKTransition;
import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionListItemParser {

    @NonNull
    public ListItem parseActionListItem(@NonNull ZettaDeviceId deviceId,
                                        @NonNull ZettaStyle style,
                                        @NonNull ZIKTransition transition) {
        List<Map<String, Object>> eventFields = transition.getFields();
        if (eventFields.size() == 1) {
            String action = transition.getName();
            return new ActionToggleListItem(
                deviceId,
                action,
                action,
                style
            );
        } else if (eventFields.size() == 2) {
            Map<String, Object> eventField = eventFields.get(0);
            String label = String.valueOf(eventField.get("name"));
            String action = transition.getName();
            return new ActionSingleInputListItem(
                deviceId,
                label,
                action,
                style
            );
        } else {
            List<String> labels = new ArrayList<>();
            for (Map<String, Object> eventField : eventFields) {
                String label = String.valueOf(eventField.get("name"));
                labels.add(label);
            }
            String action = transition.getName();
            return new ActionMultipleInputListItem(
                deviceId,
                labels,
                action,
                style
            );
        }
    }

}
