package com.zetta.android.device.actions;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zetta.android.R;

import java.util.Collections;
import java.util.Map;

public class ActionToggleViewHolder extends RecyclerView.ViewHolder {

    @NonNull private final TextView actionLabelWidget;
    @NonNull private final AppCompatButton actionToggleButton;

    public ActionToggleViewHolder(@NonNull View itemView) {
        super(itemView);
        actionLabelWidget = (TextView) itemView.findViewById(R.id.list_item_action_label);
        actionToggleButton = (AppCompatButton) itemView.findViewById(R.id.list_item_action_toggle);
    }

    public void bind(@NonNull final ActionToggleListItem item,
                     @NonNull final OnActionClickListener onActionClickListener) {
        actionLabelWidget.setText(item.getLabel());
        actionLabelWidget.setTextColor(item.getActionInputTextColor());
        actionToggleButton.setText(item.getAction());
        actionToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = item.getLabel();
                String input = item.getAction();
                Map<String, Object> labelledInput = Collections.<String, Object>singletonMap(label, input);
                onActionClickListener.onActionClick(item.getDeviceId(), item.getAction(), labelledInput);
            }
        });
        actionToggleButton.setTextColor(item.getActionTextColor());
        actionToggleButton.setBackground(item.createActionBackground());
        itemView.setBackground(item.createBackground());
    }
}
